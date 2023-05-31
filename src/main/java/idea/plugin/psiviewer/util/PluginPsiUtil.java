/*
 *  Copyright (c) 2002 Sabre, Inc. All rights reserved.
 */
package idea.plugin.psiviewer.util;

import consulo.codeEditor.Editor;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorManager;
import consulo.fileEditor.TextEditor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nullable;

public class PluginPsiUtil
{
    @Nullable
    private static VirtualFile getVirtualFile(Project project, PsiElement psiElement)
    {
        if (psiElement == null || !psiElement.isValid() || psiElement.getContainingFile() == null)
        {
            return null;
        }
        return psiElement.getContainingFile().getVirtualFile();
    }

    @Nullable
    public static PsiFile getContainingFile(PsiElement psiElement)
    {
        if (psiElement == null || !psiElement.isValid())
        {
            return null;
        }

        return psiElement.getContainingFile();
    }

    public static boolean isElementInSelectedFile(Project project, PsiElement psiElement)
    {
        VirtualFile elementFile = getVirtualFile(project, psiElement);
        if (elementFile == null)
        {
            return false;
        }

        VirtualFile[] currentEditedFiles = FileEditorManager.getInstance(project).getSelectedFiles();

        for (VirtualFile file : currentEditedFiles)
        {
            if (elementFile.equals(file))
            {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static Editor getEditorIfSelected(Project project, PsiElement psiElement)
    {
        VirtualFile elementFile = getVirtualFile(project, psiElement);
        if (elementFile == null)
        {
            return null;
        }

        FileEditor fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(elementFile);

        Editor editor = null;

        if (fileEditor != null && fileEditor instanceof TextEditor)
        {
            editor = ((TextEditor) fileEditor).getEditor();
        }

        return editor;
    }
}
