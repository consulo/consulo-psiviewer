/*
 *  Copyright (c) 2002 Sabre, Inc. All rights reserved.
 */
package idea.plugin.psiviewer.util;

import consulo.codeEditor.Editor;
import consulo.language.editor.LangDataKeys;
import consulo.language.editor.PlatformDataKeys;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.ui.ex.action.AnActionEvent;
import consulo.virtualFileSystem.VirtualFile;

public class ActionEventUtil
{
	public static Project getProject(AnActionEvent event)
	{
		return event.getData(PlatformDataKeys.PROJECT);
	}

	public static PsiElement getPsiElement(AnActionEvent event)
	{
		return event.getData(LangDataKeys.PSI_ELEMENT);
	}

	public static Editor getEditor(AnActionEvent event)
	{
		return event.getData(PlatformDataKeys.EDITOR);
	}

	public static PsiFile getPsiFile(AnActionEvent event)
	{
		return event.getData(LangDataKeys.PSI_FILE);
	}

	public static VirtualFile getVirtualFile(AnActionEvent event)
	{
		return event.getData(PlatformDataKeys.VIRTUAL_FILE);
	}
}
