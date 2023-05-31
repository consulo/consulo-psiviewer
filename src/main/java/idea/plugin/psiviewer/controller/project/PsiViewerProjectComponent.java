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
package idea.plugin.psiviewer.controller.project;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.AllIcons;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.language.Language;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.psiviewer.icon.PsiViewerIconGroup;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.ActionManager;
import consulo.ui.ex.action.ActionToolbar;
import consulo.ui.ex.awt.CollectionComboBoxModel;
import consulo.ui.ex.awt.ComboBox;
import consulo.ui.ex.awt.HorizontalLayout;
import idea.plugin.psiviewer.PsiViewerConstants;
import idea.plugin.psiviewer.controller.actions.PropertyToggleAction;
import idea.plugin.psiviewer.view.PsiViewerPanel;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
@State(name = "PsiViewerProjectComponent", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public class PsiViewerProjectComponent implements PersistentStateComponent<PsiViewerProjectComponent.State>, PsiViewerConstants
{
	public static class State
	{
		public boolean HIGHLIGHT = false;
		public boolean FILTER_WHITESPACE = false;
		public boolean SHOW_PROPERTIES = true;
		public boolean AUTOSCROLL_TO_SOURCE = false;
		public boolean AUTOSCROLL_FROM_SOURCE = false;
	}

	private static final Logger LOG = Logger.getInstance(PsiViewerProjectComponent.class);

	private ComboBox myLanguagesComboBox;
	private ItemListener myLanguagesComboBoxListener = new ItemListener()
	{
		@Override
		public void itemStateChanged(ItemEvent e)
		{
			if(e.getStateChange() == ItemEvent.SELECTED)
			{
				myViewerPanel.refreshRootElement();
				myViewerPanel.selectElementAtCaret();
			}
		}
	};

	private final Project myProject;
	private EditorListener _editorListener;
	private PsiViewerPanel myViewerPanel;

	private State myState = new State();

	@Inject
	public PsiViewerProjectComponent(Project project)
	{
		myProject = project;
	}

	public void buildUI()
	{
		myViewerPanel = new PsiViewerPanel(this);

		myViewerPanel.addPropertyChangeListener("ancestor", new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent evt)
			{
				handleCurrentState();
			}
		});
		ActionManager actionManager = ActionManager.getInstance();

		ActionGroup.Builder actionGroup = ActionGroup.newImmutableBuilder();
		actionGroup.add(new PropertyToggleAction("Filter Whitespace", "Remove whitespace elements", PsiViewerIconGroup.filterwhitespace(), this, "filterWhitespace"));
		actionGroup.add(new PropertyToggleAction("Highlight", "Highlight selected PSI element", PsiViewerIconGroup.highlighter(), this, "highlighted"));
		actionGroup.add(new PropertyToggleAction("Properties", "Show PSI element properties", AllIcons.General.Settings, this, "showProperties"));
		actionGroup.add(new PropertyToggleAction("Autoscroll to Source", "Autoscroll to Source", AllIcons.General.AutoscrollToSource, this, "autoScrollToSource"));
		actionGroup.add(new PropertyToggleAction("Autoscroll from Source", "Autoscroll from Source", AllIcons.General.AutoscrollFromSource, this, "autoScrollFromSource"));

		ActionToolbar toolBar = actionManager.createActionToolbar(ID_ACTION_TOOLBAR, actionGroup.build(), true);
		toolBar.setTargetComponent(myViewerPanel);
		
		JPanel panel = new JPanel(new HorizontalLayout(0));
		panel.add(toolBar.getComponent());

		myLanguagesComboBox = new ComboBox();
		panel.add(myLanguagesComboBox);
		updateLanguagesList(Collections.<Language>emptyList());

		myViewerPanel.add(panel, BorderLayout.NORTH);

		_editorListener = new EditorListener(myViewerPanel, myProject);
	}

	private void handleCurrentState()
	{
		if(myViewerPanel == null)
		{
			return;
		}

		if(myViewerPanel.isDisplayable())
		{
			_editorListener.start();
			myViewerPanel.selectElementAtCaret();
		}
		else
		{
			_editorListener.stop();
			myViewerPanel.removeHighlighting();
		}
	}

	@Nullable
	@Override
	public State getState()
	{
		return myState;
	}

	@Override
	public void loadState(State state)
	{
		myState = state;
	}

	public PsiViewerPanel getViewerPanel()
	{
		return myViewerPanel;
	}

	public boolean isHighlighted()
	{
		return myState.HIGHLIGHT;
	}

	public void setHighlighted(boolean highlight)
	{
		debug("set highlight to " + highlight);
		myState.HIGHLIGHT = highlight;
		myViewerPanel.applyHighlighting();
	}

	public boolean isFilterWhitespace()
	{
		return myState.FILTER_WHITESPACE;
	}

	public void setFilterWhitespace(boolean filterWhitespace)
	{
		myState.FILTER_WHITESPACE = filterWhitespace;
		getViewerPanel().applyWhitespaceFilter();
	}

	public boolean isShowProperties()
	{
		return myState.SHOW_PROPERTIES;
	}

	public void setShowProperties(boolean showProperties)
	{
		myState.SHOW_PROPERTIES = showProperties;
		getViewerPanel().showProperties(showProperties);
	}

	public boolean isAutoScrollToSource()
	{
		return myState.AUTOSCROLL_TO_SOURCE;
	}

	public void setAutoScrollToSource(boolean isAutoScrollToSource)
	{
		debug("autoscrolltosource=" + isAutoScrollToSource);
		myState.AUTOSCROLL_TO_SOURCE = isAutoScrollToSource;
	}

	public boolean isAutoScrollFromSource()
	{
		return myState.AUTOSCROLL_FROM_SOURCE;
	}

	public void setAutoScrollFromSource(boolean isAutoScrollFromSource)
	{
		debug("autoscrollfromsource=" + isAutoScrollFromSource);
		myState.AUTOSCROLL_FROM_SOURCE = isAutoScrollFromSource;
	}

	public Project getProject()
	{
		return myProject;
	}

	public static PsiViewerProjectComponent getInstance(Project project)
	{
		return project.getComponent(PsiViewerProjectComponent.class);
	}

	private static void debug(String message)
	{
		if(LOG.isDebugEnabled())
		{
			LOG.debug(message);
		}
	}

	@Nullable
	public Language getSelectedLanguage()
	{
		return (Language) myLanguagesComboBox.getSelectedItem();
	}

	public void updateLanguagesList(Collection<Language> languages)
	{
		Language selectedLanguage = getSelectedLanguage();

		myLanguagesComboBox.removeItemListener(myLanguagesComboBoxListener);

		myLanguagesComboBox.setModel(new CollectionComboBoxModel<>(new ArrayList<>(languages)));

		if(selectedLanguage != null && languages.contains(selectedLanguage))
		{
			myLanguagesComboBox.setSelectedItem(selectedLanguage);
		}

		if(languages.size() < 2)
		{
			myLanguagesComboBox.setVisible(false);
		}
		else
		{
			myLanguagesComboBox.setVisible(true);
		}

		myLanguagesComboBox.addItemListener(myLanguagesComboBoxListener);
	}
}
