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
package idea.plugin.psiviewer.controller.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import idea.plugin.psiviewer.util.ActionEventUtil;

public class ViewFileElementAction extends BaseGlobalAction
{
    public PsiElement getTargetElement(AnActionEvent event)
    {
        return ActionEventUtil.getPsiFile(event);
    }

    public String getToolWindowTitle()
    {
        return "psi.File";
    }

    public void update(AnActionEvent event)
    {
        super.update(event);
        Presentation presentation = event.getPresentation();
        VirtualFile file = ActionEventUtil.getVirtualFile(event);

        if (file != null && file.isDirectory())
        {
            presentation.setEnabled(false);
        }
    }

}
