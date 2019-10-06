package consulo.psiviewer.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import idea.plugin.psiviewer.controller.project.PsiViewerProjectComponent;
import idea.plugin.psiviewer.view.PsiViewerPanel;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2019-10-06
 */
public class PsiViewerToolwindowFactory implements ToolWindowFactory
{
	@Override
	public void createToolWindowContent(@Nonnull Project project, @Nonnull ToolWindow toolWindow)
	{
		PsiViewerProjectComponent component = PsiViewerProjectComponent.getInstance(project);

		component.buildUI();

		PsiViewerPanel viewerPanel = component.getViewerPanel();

		Content content = ContentFactory.getInstance().createContent(viewerPanel, null, false);

		toolWindow.getContentManager().addContent(content);
	}
}
