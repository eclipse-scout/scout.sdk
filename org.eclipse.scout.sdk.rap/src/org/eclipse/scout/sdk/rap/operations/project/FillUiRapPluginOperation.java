package org.eclipse.scout.sdk.rap.operations.project;

import java.io.File;
import java.io.FilenameFilter;

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
    STRATEGY_LOCAL_EXISTING,
    STRATEGY_LOCAL_EXTRACT,
    STRATEGY_LATER
  }

  public static final String UPDATE_SITE_URL = "http://download.eclipse.org/releases/kepler";
  public static final String SCOUT_RT_RAP_FEATURE = "org.eclipse.scout.rt.rap.feature.group";
  public static final String SCOUT_RT_FEATURE = "org.eclipse.scout.rt.feature.group";
  public static final String ECLIPSE_RT_RAP_FEATURE = "org.eclipse.rap.runtime.feature.group";
  public static final String ECLIPSE_RT_RAP_REQUIREMENTS_FEATURE = "org.eclipse.rap.runtime.requirements.feature.group";
  public static final String ECLIPSE_PLATFORM_FEATURE = "org.eclipse.platform.feature.group";
  public static final String ECLIPSE_RPC_FEATURE = "org.eclipse.rcp.feature.group";
  public static final String ECLIPSE_RPC_E4_FEATURE = "org.eclipse.e4.rcp.feature.group";
  public static final String ECLIPSE_HELP_FEATURE = "org.eclipse.help.feature.group";
  public static final String ECLIPSE_EMF_ECORE_FEATURE = "org.eclipse.emf.ecore.feature.group";
  public static final String ECLIPSE_EMF_COMMON_FEATURE = "org.eclipse.emf.common.feature.group";

  public final static String PROP_TARGET_STRATEGY = "propTargetStrategy";
  public final static String PROP_EXTRACT_TARGET_FOLDER = "propExtractTargetFolder";
  public final static String PROP_LOCAL_TARGET_FOLDER = "propLocalTargetFolder";
  public final static String PROP_TARGET_FILE = "propTargetFile";
  public final static String PROP_DOWNLOAD_ECLIPSE_PLATFORM = "propDownloadEclipsePlatform";

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

  private boolean isPluginAvailable(String folder, final String symbolicName) {
    File plugins = new File(new File(folder), "plugins");
    if (!plugins.exists()) {
      return false;
    }

    String[] pluginNames = plugins.list(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith(symbolicName);
      }
    });
    return pluginNames.length > 0;
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

    if (getTargetStrategy() == TARGET_STRATEGY.STRATEGY_LATER) {
      // no target set
      return;
    }

    InstallTargetPlatformFileOperation op = new InstallTargetPlatformFileOperation(m_project);
    if (getTargetStrategy() == TARGET_STRATEGY.STRATEGY_LOCAL_EXISTING) {
      // existing local RAP target
      op.addLocalDirectory(getLocalTargetFolder());

      // try to detect if the given folder is a complete platform or only contains the rap plugins
      if (!isPluginAvailable(getLocalTargetFolder(), "org.eclipse.platform_") || !isPluginAvailable(getLocalTargetFolder(), "org.eclipse.help.ui_")) {
        op.addRunningEclipseEntries();
      }
    }
    else if (getTargetStrategy() == TARGET_STRATEGY.STRATEGY_REMOTE) {
      // remote target using the update sites
      op.addUpdateSite(UPDATE_SITE_URL, ECLIPSE_RT_RAP_FEATURE);
      op.addUpdateSite(UPDATE_SITE_URL, SCOUT_RT_RAP_FEATURE);
      if (isDownloadEclipsePlatform()) {
        op.addUpdateSite(UPDATE_SITE_URL, SCOUT_RT_FEATURE);
        op.addUpdateSite(UPDATE_SITE_URL, ECLIPSE_RT_RAP_REQUIREMENTS_FEATURE);
        op.addUpdateSite(UPDATE_SITE_URL, ECLIPSE_PLATFORM_FEATURE);
        op.addUpdateSite(UPDATE_SITE_URL, ECLIPSE_RPC_FEATURE);
        op.addUpdateSite(UPDATE_SITE_URL, ECLIPSE_HELP_FEATURE);
        op.addUpdateSite(UPDATE_SITE_URL, ECLIPSE_RPC_E4_FEATURE);
        op.addUpdateSite(UPDATE_SITE_URL, ECLIPSE_EMF_ECORE_FEATURE);
        op.addUpdateSite(UPDATE_SITE_URL, ECLIPSE_EMF_COMMON_FEATURE);
      }
      else {
        op.addRunningEclipseEntries();
      }
    }
    else if (getTargetStrategy() == TARGET_STRATEGY.STRATEGY_LOCAL_EXTRACT) {
      // locally extracted, new target from rap.target plug-in
      ScoutRapTargetCreationOperation scoutRapTargetExtractOp = new ScoutRapTargetCreationOperation();
      scoutRapTargetExtractOp.setDestinationDirectory(new File(getExtractTargetFolder()));
      scoutRapTargetExtractOp.validate();
      scoutRapTargetExtractOp.run(monitor, workingCopyManager);

      op.addRunningEclipseEntries();
      op.addLocalDirectory(getExtractTargetFolder());
    }
    op.validate();
    op.run(monitor, workingCopyManager);
    getProperties().setProperty(PROP_TARGET_FILE, op.getCreatedFile());
  }

  protected String getLocalTargetFolder() {
    return getProperties().getProperty(PROP_LOCAL_TARGET_FOLDER, String.class);
  }

  protected String getExtractTargetFolder() {
    return getProperties().getProperty(PROP_EXTRACT_TARGET_FOLDER, String.class);
  }

  protected Boolean isDownloadEclipsePlatform() {
    return getProperties().getProperty(PROP_DOWNLOAD_ECLIPSE_PLATFORM, Boolean.class);
  }

  protected TARGET_STRATEGY getTargetStrategy() {
    return getProperties().getProperty(PROP_TARGET_STRATEGY, TARGET_STRATEGY.class);
  }
}
