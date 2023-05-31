/*
 *  Copyright (c) 2002 Sabre, Inc. All rights reserved.
 */
package idea.plugin.psiviewer.controller.actions;

import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import consulo.project.ui.wm.ToolWindowManager;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.action.Presentation;
import consulo.ui.ex.toolWindow.ToolWindow;
import consulo.virtualFileSystem.VirtualFile;
import idea.plugin.psiviewer.PsiViewerConstants;
import idea.plugin.psiviewer.controller.project.PsiViewerProjectComponent;
import idea.plugin.psiviewer.util.ActionEventUtil;
import idea.plugin.psiviewer.view.PsiViewerPanel;

abstract class BaseGlobalAction extends DumbAwareAction
{
    public void update(AnActionEvent event)
    {
        Presentation presentation = event.getPresentation();
        Project project = ActionEventUtil.getProject(event);
        if (project == null)
        { // project isn't accessible from the context
            presentation.setEnabled(false);
            presentation.setVisible(false);
            return;
        }
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(PsiViewerConstants.ID_TOOL_WINDOW);
        if (toolWindow == null)
        { // tool window isn't registered
            presentation.setEnabled(false);
            presentation.setVisible(false);
            return;
        }
        VirtualFile file = ActionEventUtil.getVirtualFile(event);

        if (file == null)
        {
            presentation.setEnabled(false);
            presentation.setVisible(false);
            return;
        }
        presentation.setEnabled(toolWindow.isAvailable());
        presentation.setVisible(true);
    }

    public void actionPerformed(AnActionEvent event)
    {
        Project project = ActionEventUtil.getProject(event);
        PsiDocumentManager.getInstance(project).commitAllDocuments();

        PsiViewerPanel viewer = PsiViewerProjectComponent.getInstance(project).getViewerPanel();

        if (getTargetElement(event) == null)
            return;

        viewer.selectRootElement(getTargetElement(event),
                                 getToolWindowTitle());

        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(PsiViewerConstants.ID_TOOL_WINDOW);
        toolWindow.activate(viewer);
    }

    protected abstract String getToolWindowTitle();

    protected abstract PsiElement getTargetElement(AnActionEvent event);
}
