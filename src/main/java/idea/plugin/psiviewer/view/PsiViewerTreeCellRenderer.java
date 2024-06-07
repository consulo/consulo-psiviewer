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

import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.psi.PsiElement;
import consulo.ui.ex.awtUnsafe.TargetAWT;
import consulo.ui.image.Image;
import idea.plugin.psiviewer.PsiViewerConstants;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

class PsiViewerTreeCellRenderer extends DefaultTreeCellRenderer implements PsiViewerConstants
{
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean isExpanded,
												  boolean isLeaf, int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, isSelected, isExpanded, isLeaf, row, hasFocus);

		PsiElement psiElement = (PsiElement) value;

		setIcon(IconDescriptorUpdaters.getIcon(psiElement, 0));

		return this;
	}

	public PsiViewerTreeCellRenderer()
	{
		setOpaque(false);
	}

	public void setIcon(Image icon)
	{
		setIcon(TargetAWT.to(icon));
	}
}
