/*
 IDEA PsiViewer Plugin
 Copyright (C) 2002 Andrew J. Armstrong

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Author:
 Andrew J. Armstrong <andrew_armstrong@bigpond.com>
*/
package idea.plugin.psiviewer.view;

import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import consulo.psiviewer.icon.PsiViewerIconGroup;
import idea.plugin.psiviewer.PsiViewerConstants;
import idea.plugin.psiviewer.controller.project.PsiViewerProjectComponent;
import idea.plugin.psiviewer.model.PsiViewerTreeModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.LinkedList;

/**
 * A JPanel that holds a toolbar, a tree view, and a property sheet.
 */
// TODO should be a project component. Move from PsiViewerProjectcomponent the initialization to here

public class PsiViewerPanel extends JPanel implements Runnable, PsiViewerConstants
{
	private static final Logger LOG = Logger.getInstance(PsiViewerPanel.class);

	private String _actionTitle;
	private PsiViewerTree myTree;
	private PsiViewerTreeModel _model;
	private PsiElement _rootElement; // The root element of the tree
	private PsiElement _selectedElement; // The currently selected element in the tree
	private PropertySheetPanel myPropertyPanel;
	private final Project _project;
	private OnePixelSplitter mySplitPane;
	private final ViewerTreeSelectionListener _treeSelectionListener;
	private final EditorCaretMover _caretMover;
	private final EditorPsiElementHighlighter _highlighter;
	private final PsiViewerProjectComponent _projectComponent;
	private final PropertySheetHeaderRenderer _propertyHeaderRenderer = new PropertySheetHeaderRenderer(PsiViewerIconGroup.psi(), SwingConstants.LEFT, BorderFactory
			.createEtchedBorder());
	private final PropertySheetHeaderRenderer _valueHeaderRenderer = new PropertySheetHeaderRenderer(PsiViewerIconGroup.psi(), SwingConstants.LEFT, BorderFactory
			.createEtchedBorder());

	public PsiViewerPanel(PsiViewerProjectComponent projectComponent)
	{
		_projectComponent = projectComponent;
		_project = projectComponent.getProject();
		_caretMover = new EditorCaretMover(_projectComponent.getProject());
		_highlighter = new EditorPsiElementHighlighter(_project);
		_model = new PsiViewerTreeModel(_projectComponent);
		_treeSelectionListener = new ViewerTreeSelectionListener();

		buildGUI();
	}

	public void selectRootElement(PsiElement element, String actionTitle)
	{
		_actionTitle = actionTitle;
		setRootElement(element);
	}

	public void refreshRootElement()
	{
		selectRootElement(getRootElement(), _actionTitle);
	}

	private void showRootElement()
	{
		getToolWindow().setTitle(_actionTitle + " " + getRootElement());
		resetTree();
	}

	private void resetTree()
	{
		myTree.getSelectionModel().removeTreeSelectionListener(_treeSelectionListener);

		Enumeration expandedDescendants = null;
		TreePath path = null;
		if(_model.getRoot() != null)
		{
			expandedDescendants = myTree.getExpandedDescendants(new TreePath(_model.getRoot()));
			path = myTree.getSelectionModel().getSelectionPath();
		}

		_model = new PsiViewerTreeModel(_projectComponent);
		_model.setRoot(getRootElement());
		myTree.setModel(_model);
		if(expandedDescendants != null)
		{
			while(expandedDescendants.hasMoreElements())
			{
				TreePath treePath = (TreePath) expandedDescendants.nextElement();
				myTree.expandPath(treePath);
			}
		}
		myTree.setSelectionPath(path);
		myTree.scrollPathToVisible(path);

		myTree.getSelectionModel().addTreeSelectionListener(_treeSelectionListener);
	}

	public void showProperties(boolean showProperties)
	{
		mySplitPane.getSecondComponent().setVisible(showProperties);
		updatePropertySheet();
	}

	@Nonnull
	private ToolWindow getToolWindow()
	{
		return ToolWindowManager.getInstance(_project).getToolWindow(ID_TOOL_WINDOW);
	}

	private void buildGUI()
	{
		setLayout(new BorderLayout());

		myTree = new PsiViewerTree(_model);
		myTree.setBorder(null);
		myTree.getSelectionModel().addTreeSelectionListener(_treeSelectionListener);

		ActionMap actionMap = myTree.getActionMap();
		actionMap.put("EditSource", new AbstractAction("EditSource")
		{
			public void actionPerformed(ActionEvent e)
			{
				debug("key typed " + e);
				if(getSelectedElement() == null)
				{
					return;
				}
				Editor editor = _caretMover.openInEditor(getSelectedElement());
				selectElementAtCaret(editor, TREE_SELECTION_CHANGED);
				editor.getContentComponent().requestFocus();
			}
		});
		InputMap inputMap = myTree.getInputMap();
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, true), "EditSource");

		myPropertyPanel = new PropertySheetPanel();
		myPropertyPanel.setVisible(false);

		mySplitPane = new OnePixelSplitter(true);
		mySplitPane.setFirstComponent(ScrollPaneFactory.createScrollPane(myTree, true));
		mySplitPane.setSecondComponent(myPropertyPanel);
		mySplitPane.setAndLoadSplitterProportionKey("PsiViewerPanel.mySplitPane");

		add(mySplitPane);
	}

	public void run()
	{
	}

	private class ViewerTreeSelectionListener implements TreeSelectionListener
	{
		public void valueChanged(TreeSelectionEvent e)
		{
			setSelectedElement((PsiElement) myTree.getLastSelectedPathComponent(), PsiViewerPanel.TREE_SELECTION_CHANGED);
		}
	}

	public void applyWhitespaceFilter()
	{
		showRootElement();
	}

	public void applyHighlighting()
	{
		_highlighter.highlightElement(getSelectedElement());
	}

	public void removeHighlighting()
	{
		_highlighter.removeHighlight();
	}

	private PsiElement getSelectedElement()
	{
		return _selectedElement;
	}

	private static final String CARET_MOVED = "caret moved";
	private static final String TREE_SELECTION_CHANGED = "tree selection changed";

	private boolean inSetSelectedElement = false;

	private void setSelectedElement(PsiElement element, String reason)
	{
		if(inSetSelectedElement)
		{
			return;
		}

		try
		{
			debug("selection changed to " + element + " due to " + reason);
			inSetSelectedElement = true;
			_selectedElement = element;
			updatePropertySheet();
			if(reason != TREE_SELECTION_CHANGED)
			{
				changeTreeSelection();
			}
			applyHighlighting();
			if(reason != CARET_MOVED && element != null)
			{
				moveEditorCaret();
			}
		}
		finally
		{
			inSetSelectedElement = false;
		}
	}

	private void updatePropertySheet()
	{
		if(!_projectComponent.isShowProperties())
		{
			return;
		}
		myPropertyPanel.setTarget(_selectedElement);
		myPropertyPanel.getTable().getTableHeader().setReorderingAllowed(false);

		_propertyHeaderRenderer.setIconForElement(_selectedElement);
		myPropertyPanel.getTable().getColumnModel().getColumn(0).setHeaderRenderer(_propertyHeaderRenderer);
		myPropertyPanel.getTable().getColumnModel().getColumn(1).setHeaderRenderer(_valueHeaderRenderer);
	}

	private void changeTreeSelection()
	{
		TreePath path = getPath(getSelectedElement());
		myTree.expandPath(path);
		myTree.scrollPathToVisible(path);
		myTree.setSelectionPath(path);
	}

	private TreePath getPath(PsiElement element)
	{
		if(element == null)
		{
			return null;
		}
		LinkedList list = new LinkedList();
		while(element != null && element != _rootElement)
		{
			list.addFirst(element);
			element = element.getParent();
		}
		if(element != null)
		{
			list.addFirst(element);
		}
		TreePath treePath = new TreePath(list.toArray());
		debug("root=" + _rootElement + ", treePath=" + treePath);
		return treePath;
	}

	private void moveEditorCaret()
	{
		if(_projectComponent.isAutoScrollToSource())
		{
			LOG.debug("moving editor caret");
			_caretMover.moveEditorCaret(getSelectedElement());
		}
	}

	public PsiElement getRootElement()
	{
		return _rootElement;
	}

	private void setRootElement(PsiElement rootElement)
	{
		if(rootElement instanceof PsiFile)
		{
			FileViewProvider viewProvider = ((PsiFile) rootElement).getViewProvider();

			// iteration need, because getLanguages returns Set, not list, order is random
			_projectComponent.updateLanguagesList(ContainerUtil.map(viewProvider.getAllFiles(), new Function<PsiFile, Language>()
			{
				@Override
				public Language fun(PsiFile psiFile)
				{
					return psiFile.getLanguage();
				}
			}));
			Language selectedLanguage = _projectComponent.getSelectedLanguage();

			if(selectedLanguage != null)
			{
				PsiElement selectedRoot = viewProvider.getPsi(selectedLanguage);
				if(selectedRoot != null)
				{
					rootElement = selectedRoot;
				}
			}
		}
		_rootElement = rootElement;
		showRootElement();
	}

	public void selectElementAtCaret()
	{
		selectElementAtCaret(FileEditorManager.getInstance(_project).getSelectedTextEditor(), null);
	}

	public void selectElementAtCaret(@Nullable Editor editor, @Nullable String changeSource)
	{
		if(editor == null) /* Vince Mallet (21 Oct 2003) */
		{
			debug("selectElementAtCaret: Can't select element, editor is null");
			return;
		}

		PsiFile psiFile = PsiDocumentManager.getInstance(_project).getPsiFile(editor.getDocument());

		PsiElement elementAtCaret = null;
		if(psiFile != null)
		{
			Language selectedLanguage = _projectComponent.getSelectedLanguage();
			FileViewProvider viewProvider = psiFile.getViewProvider();

			if(selectedLanguage != null)
			{
				PsiFile selectedRoot = viewProvider.getPsi(selectedLanguage);
				if(selectedRoot == null)
				{
					selectedLanguage = null;
				}
			}

			if(selectedLanguage == null)
			{
				selectedLanguage = psiFile.getLanguage();
			}

			elementAtCaret = viewProvider.findElementAt(editor.getCaretModel().getOffset(), selectedLanguage);

			if(elementAtCaret != null && elementAtCaret.getParent() != null)
			{
				if(elementAtCaret.getParent().getChildren().length == 0)
				{
					elementAtCaret = elementAtCaret.getParent();
				}
			}
		}

		if(elementAtCaret != null && elementAtCaret != getSelectedElement())
		{
			debug("new element at caret " + elementAtCaret + ", current root=" + getRootElement());
			if(!PsiTreeUtil.isAncestor(getRootElement(), elementAtCaret, false))
			{
				selectRootElement(psiFile, TITLE_PREFIX_CURRENT_FILE);
			}
			setSelectedElement(elementAtCaret, changeSource == null ? PsiViewerPanel.CARET_MOVED : changeSource);
		}
	}

	private static void debug(String message)
	{
		if(LOG.isDebugEnabled())
		{
			LOG.debug(message);
		}
	}

}
