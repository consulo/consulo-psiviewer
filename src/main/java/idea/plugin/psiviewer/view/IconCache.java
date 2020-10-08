/*
    IDEA Plugin
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

import com.intellij.psi.*;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.psiviewer.icon.PsiViewerIconGroup;
import consulo.ui.image.Image;
import idea.plugin.psiviewer.PsiViewerConstants;

import java.util.HashMap;
import java.util.Map;

class IconCache implements PsiViewerConstants
{
	public static final Image DEFAULT_ICON = PsiViewerIconGroup.psi18x18();
	public static final Map<Class, Image> _iconCache = new HashMap<>();

	static
	{
		_iconCache.put(PsiBinaryFile.class, PlatformIconGroup.fileTypesUnknown());
		_iconCache.put(PsiPlainTextFile.class, PlatformIconGroup.fileTypesText());

		_iconCache.put(PsiWhiteSpace.class, PsiViewerIconGroup.whitespace());
		_iconCache.put(PsiComment.class, PsiViewerIconGroup.comment());

		_iconCache.put(PsiDirectory.class, PlatformIconGroup.nodesFolder());
	}

	public static Image getIcon(Class clazz)
	{
		return _iconCache.get(clazz);
	}

}
