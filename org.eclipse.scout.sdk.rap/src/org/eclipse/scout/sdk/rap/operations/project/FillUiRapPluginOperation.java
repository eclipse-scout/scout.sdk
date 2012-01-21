package org.eclipse.scout.sdk.rap.operations.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.CreateServerPluginOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class FillUiRapPluginOperation extends AbstractScoutProjectNewOperation {

  public static enum TARGET_STRATEGY {
    STRATEGY_REMOTE,
    STRATEGY_LOCAL,
    STRATEGY_LATER
  }

  public final static String PROP_TARGET_STRATEGY = "propTargetStrategy";
  public final static String PROP_LOCAL_TARGET_FOLDER = "propLocalTargetFolder";
  public final static String PROP_TARGET_FILE = "propTargetFile";

  private IProject m_project;

  @Override
  public String getOperationName() {
    return "Fill UI RAP Plugin and Install Target Platform";
  }

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateUiRapPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    String rapPluginName = getProperties().getProperty(CreateUiRapPluginOperation.PROP_BUNDLE_RAP_NAME, String.class);
    m_project = getCreatedBundle(rapPluginName).getProject();
  }

  @Override
  public void validate() throws IllegalArgumentException {
    super.validate();
    if (m_project == null) {
      throw new IllegalArgumentException("project can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    String serverPluginName = getProperties().getProperty(CreateServerPluginOperation.PROP_BUNDLE_SERVER_NAME, String.class);
    IJavaProject serverProject = getCreatedBundle(serverPluginName);
    if (serverProject != null) {
      ResourcesPlugin.getWorkspace().checkpoint(false);
      CreateAjaxServletOperation createAjaxServletOperation = new CreateAjaxServletOperation(serverProject);
      createAjaxServletOperation.run(monitor, workingCopyManager);
    }

    InstallTargetPlatformFileOperation op = new InstallTargetPlatformFileOperation(m_project);
    switch (getTargetStrategy()) {
      case STRATEGY_LOCAL:
        op.setRapTargetLocalFolder(getLocalTargetFolder());
        break;
      case STRATEGY_REMOTE:
        // do nothing
        break;
      case STRATEGY_LATER:
        return;
    }
    op.run(monitor, workingCopyManager);
    getProperties().setProperty(PROP_TARGET_FILE, op.getCreatedFile());
  }

  protected String getLocalTargetFolder() {
    return getProperties().getProperty(PROP_LOCAL_TARGET_FOLDER, String.class);
  }

  protected TARGET_STRATEGY getTargetStrategy() {
    return getProperties().getProperty(PROP_TARGET_STRATEGY, TARGET_STRATEGY.class);
  }
}
