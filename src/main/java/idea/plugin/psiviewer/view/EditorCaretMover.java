/*
 *  Copyright (c) 2002 Sabre, Inc. All rights reserved.
 */
package idea.plugin.psiviewer.view;

import consulo.codeEditor.Editor;
import consulo.codeEditor.ScrollType;
import consulo.fileEditor.FileEditorManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.navigation.OpenFileDescriptor;
import consulo.navigation.OpenFileDescriptorFactory;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import idea.plugin.psiviewer.util.PluginPsiUtil;

class EditorCaretMover
{
    private final Project _project;
    private boolean _shouldMoveCaret = true;

    public EditorCaretMover(Project project)
    {
        _project = project;
    }

    private void disableMovementOneTime()
    {
        _shouldMoveCaret = false;
    }

    public void moveEditorCaret(PsiElement element)
    {
        if (element == null) return;
        if (shouldMoveCaret(element))
        {
            Editor editor = getEditor(element);
            if (editor == null) return;

            int textOffset = element.getTextOffset();
            if (textOffset < editor.getDocument().getTextLength())
            {
                editor.getCaretModel().moveToOffset(textOffset);
                editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
            }
        }
        _shouldMoveCaret = true;
    }

    private boolean shouldMoveCaret(PsiElement element)
    {
        return _shouldMoveCaret && PluginPsiUtil.isElementInSelectedFile(_project, element);
    }

    private Editor getEditor(PsiElement element)
    {
        return PluginPsiUtil.getEditorIfSelected(_project, element);
    }

    public Editor openInEditor(PsiElement element)
    {
        PsiFile psiFile;
        int i;
        if (element instanceof PsiFile)
        {
            psiFile = (PsiFile) element;
            i = -1;
        }
        else
        {
            psiFile =  PluginPsiUtil.getContainingFile(element);
            i = element.getTextOffset();
        }
        
        if (psiFile == null) return null;

        final VirtualFile virtualFile = psiFile.getVirtualFile();

        if (virtualFile == null) return null;

        OpenFileDescriptor fileDesc = OpenFileDescriptorFactory.getInstance(_project).builder(virtualFile).offset(i).build(); 
        disableMovementOneTime();
        return FileEditorManager.getInstance(_project).openTextEditor(fileDesc, false);
    }

}
