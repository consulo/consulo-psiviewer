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

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.intellij.psi.PsiBinaryFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPlainTextFile;
import com.intellij.psi.PsiWhiteSpace;
import idea.plugin.psiviewer.PsiViewerConstants;

class PsiViewerTreeCellRenderer extends DefaultTreeCellRenderer implements PsiViewerConstants {
    private final ElementVisitor _elementVisitor = new ElementVisitor();

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean isExpanded,
                                                  boolean isLeaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, isSelected, isExpanded, isLeaf, row, hasFocus);
        setIcon(IconCache.DEFAULT_ICON);

        PsiElement psiElement = (PsiElement) value;

        psiElement.accept(_elementVisitor);

//        try {
//            psiElement.accept(new PsiViewerTreeCellJavaElementVisitor(this));
//        } catch (Exception e) {
//        }

        return this;
    }

    public PsiViewerTreeCellRenderer() {
        setOpaque(false);
    }

    private class ElementVisitor extends PsiElementVisitor {

        private static final int MAX_TEXT_LENGTH = 80;


        public void visitBinaryFile(PsiBinaryFile psiElement) {
            setIcon(IconCache.getIcon(PsiBinaryFile.class));
            setText("PsiBinaryFile: " + psiElement.getName());
        }


        public void visitComment(PsiComment psiElement) {
            setIcon(IconCache.getIcon(PsiComment.class));
            setText("PsiComment: " + truncate(psiElement.getText()));
        }

        public void visitDirectory(PsiDirectory psiElement) {
            setIcon(IconCache.getIcon(PsiDirectory.class));
            setText("PsiDirectory: " + psiElement.getName());
        }

        public void visitElement(PsiElement psiElement) {
            setText(psiElement.toString());
        }


        public void visitFile(PsiFile psiElement) {
            setText("PsiFile: " + psiElement.getName());
        }


        public void visitPlainTextFile(PsiPlainTextFile psiElement) {
            setIcon(IconCache.getIcon(PsiPlainTextFile.class));
            setText("PsiPlainTextFile: " + psiElement.getName());
        }


        public void visitWhiteSpace(PsiWhiteSpace psiElement) {
            setIcon(IconCache.getIcon(PsiWhiteSpace.class));
            setText("PsiWhiteSpace");
        }

        private String truncate(String text) {
            if (text.length() > MAX_TEXT_LENGTH)
                return text.substring(0, MAX_TEXT_LENGTH).trim() + "...";
            else
                return text;
        }

        private ElementVisitor() {
        }
    }
}
