/*
 *  Copyright (c) 2002 Sabre, Inc. All rights reserved.
 */
package idea.plugin.psiviewer.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

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
