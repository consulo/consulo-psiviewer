/*
 *  Copyright (c) 2002 Sabre, Inc. All rights reserved.
 */
package idea.plugin.psiviewer.view;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.ApplicationManager;
import consulo.codeEditor.CodeInsightColors;
import consulo.codeEditor.Editor;
import consulo.codeEditor.markup.HighlighterTargetArea;
import consulo.codeEditor.markup.RangeHighlighter;
import consulo.colorScheme.EditorColorsManager;
import consulo.colorScheme.TextAttributes;
import consulo.document.FileDocumentManager;
import consulo.document.util.TextRange;
import consulo.fileEditor.FileEditorManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiWhiteSpace;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import idea.plugin.psiviewer.PsiViewerConstants;
import idea.plugin.psiviewer.controller.project.PsiViewerProjectComponent;
import idea.plugin.psiviewer.util.PluginPsiUtil;
import jakarta.annotation.Nullable;

class EditorPsiElementHighlighter
{
	private static final Logger LOG = Logger.getInstance(EditorPsiElementHighlighter.class);

	private final Project _project;
	private RangeHighlighter _highlighter;
	private RangeHighlighter _referenceHighlighter;
	private Editor _editor;

	EditorPsiElementHighlighter(Project project)
	{
		_project = project;
	}

	void highlightElement(@Nullable PsiElement psiElement)
	{
		ApplicationManager.getApplication().runReadAction(() -> apply(psiElement));

		if(psiElement != null && psiElement.getReference() != null)
		{
			ApplicationManager.getApplication().runReadAction(() -> applyReference(psiElement.getReference()));
		}
	}

	void removeHighlight()
	{
		ApplicationManager.getApplication().runReadAction(this::remove);
	}

	@RequiredReadAction
	private void apply(@Nullable PsiElement element)
	{
		remove();

		if(element == null)
		{
			return;
		}

		_editor = FileEditorManager.getInstance(_project).getSelectedTextEditor();
		if(_editor == null)
		{
			debug("no editor => no need to highlight");
			return;
		}

		if(element instanceof PsiWhiteSpace && isWhiteSpaceFiltered())
		{
			return;
		}

		if(isHighlightOn() && isElementInEditor(_editor, element))
		{
			TextRange textRange = element.getTextRange();
			debug("Adding highlighting for " + textRange);
			final int docTextLength = _editor.getDocument().getTextLength();
			TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(CodeInsightColors.BLINKING_HIGHLIGHTS_ATTRIBUTES);
			_highlighter = _editor.getMarkupModel().addRangeHighlighter(textRange.getStartOffset(), Math.min(textRange.getEndOffset(), docTextLength), PsiViewerConstants.PSIVIEWER_HIGHLIGHT_LAYER,
					attributes, HighlighterTargetArea.EXACT_RANGE);
		}
	}

	@RequiredReadAction
	private void applyReference(PsiReference reference)
	{
		_editor = FileEditorManager.getInstance(_project).getSelectedTextEditor();
		if(_editor == null)
		{
			debug("no editor => no need to highlight");
			return;
		}

		if(isHighlightOn() && isElementInEditor(_editor, reference.getElement()))
		{
			TextRange textRange = reference.getElement().getTextRange().cutOut(reference.getRangeInElement());

			debug("Adding reference highlighting for " + textRange);
			final int docTextLength = _editor.getDocument().getTextLength();
			TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(CodeInsightColors.BLINKING_HIGHLIGHTS_ATTRIBUTES);
			_referenceHighlighter = _editor.getMarkupModel().addRangeHighlighter(textRange.getStartOffset(), Math.min(textRange.getEndOffset(), docTextLength), PsiViewerConstants
					.PSIVIEWER_REFERENCE_HIGHLIGHT_LAYER, attributes, HighlighterTargetArea.EXACT_RANGE);
		}
	}

	private void remove()
	{
		if(_highlighter != null && _highlighter.isValid())
		{
			debug("Removing highlighter for " + _highlighter);
			_editor.getMarkupModel().removeHighlighter(_highlighter);
			_highlighter = null;
		}

		if(_referenceHighlighter != null && _referenceHighlighter.isValid())
		{
			debug("Removing highlighter for " + _referenceHighlighter);
			_editor.getMarkupModel().removeHighlighter(_referenceHighlighter);
			_referenceHighlighter = null;
		}
	}

	private boolean isWhiteSpaceFiltered()
	{
		return PsiViewerProjectComponent.getInstance(_project).isFilterWhitespace();
	}

	private boolean isHighlightOn()
	{
		return PsiViewerProjectComponent.getInstance(_project).isHighlighted();
	}

	private boolean isElementInEditor(Editor editor, PsiElement psiElement)
	{
		if(psiElement == null || PluginPsiUtil.getContainingFile(psiElement) == null)
		{
			return false;
		}
		VirtualFile elementFile = psiElement.getContainingFile().getVirtualFile();
		if(elementFile == null)
		{
			return false;   // 20050826
		}
		VirtualFile editorFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
		return elementFile.equals(editorFile);
	}

	private static void debug(String message)
	{
		if(LOG.isDebugEnabled())
		{
			LOG.debug(message);
		}
	}
}
