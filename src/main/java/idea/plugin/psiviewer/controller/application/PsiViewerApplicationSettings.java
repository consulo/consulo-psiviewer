package idea.plugin.psiviewer.controller.application;

import javax.annotation.Nullable;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import idea.plugin.psiviewer.PsiViewerConstants;

/**
 * Created by Jon on 10/7/2016.
 */
@State(name = PsiViewerConstants.CONFIGURATION_COMPONENT_NAME, storages = @Storage("other.xml"))
public class PsiViewerApplicationSettings implements PersistentStateComponent<PsiViewerApplicationSettings> {
    public boolean PLUGIN_ENABLED;

    public PsiViewerApplicationSettings() {
        PLUGIN_ENABLED = true;
    }

    public static PsiViewerApplicationSettings getInstance() {
        return ServiceManager.getService(PsiViewerApplicationSettings.class);
    }

    @Nullable
    @Override
    public PsiViewerApplicationSettings getState() {
        return this;
    }

    @Override
    public void loadState(PsiViewerApplicationSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
