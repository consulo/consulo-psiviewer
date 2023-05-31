package consulo.psiviewer.toolWindow;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.project.ui.wm.ToolWindowFactory;
import consulo.psiviewer.icon.PsiViewerIconGroup;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.content.Content;
import consulo.ui.ex.content.ContentFactory;
import consulo.ui.ex.toolWindow.ToolWindow;
import consulo.ui.ex.toolWindow.ToolWindowAnchor;
import consulo.ui.image.Image;
import idea.plugin.psiviewer.controller.project.PsiViewerProjectComponent;
import idea.plugin.psiviewer.view.PsiViewerPanel;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2019-10-06
 */
@ExtensionImpl
public class PsiViewerToolwindowFactory implements ToolWindowFactory, DumbAware
{
	@Nonnull
	@Override
	public String getId()
	{
		return "psi-viewer";
	}

	@RequiredUIAccess
	@Override
	public void createToolWindowContent(@Nonnull Project project, @Nonnull ToolWindow toolWindow)
	{
		PsiViewerProjectComponent component = PsiViewerProjectComponent.getInstance(project);

		component.buildUI();

		PsiViewerPanel viewerPanel = component.getViewerPanel();

		Content content = ContentFactory.getInstance().createContent(viewerPanel, null, false);

		toolWindow.getContentManager().addContent(content);
	}

	@Nonnull
	@Override
	public ToolWindowAnchor getAnchor()
	{
		return ToolWindowAnchor.RIGHT;
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return PsiViewerIconGroup.psitoolwindow();
	}

	@Nonnull
	@Override
	public LocalizeValue getDisplayName()
	{
		return LocalizeValue.localizeTODO("Psi Viewer");
	}
}
