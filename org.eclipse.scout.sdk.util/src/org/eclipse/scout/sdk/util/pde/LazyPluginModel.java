package org.eclipse.scout.sdk.util.pde;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.build.IBuildModel;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PDEState;
import org.eclipse.pde.internal.core.build.WorkspaceBuildModel;
import org.eclipse.pde.internal.core.bundle.BundlePluginModel;
import org.eclipse.pde.internal.core.bundle.BundlePluginModelBase;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundleModel;
import org.eclipse.pde.internal.core.ibundle.IBundle;
import org.eclipse.pde.internal.core.plugin.WorkspaceExtensionsModel;
import org.eclipse.pde.internal.core.project.PDEProject;

/**
 * Base class for PDE model access of workspace plugins.<br>
 * This implementation uses lazy initialization of the PDE models.<br>
 * This class is thread safe.
 * 
 * @author mvi
 * @since 3.7.1
 */
@SuppressWarnings("restriction")
public final class LazyPluginModel {
  private final IProject m_project;
  private final IFile m_manifestFile;
  private final IFile m_pluginXmlFile;
  private final IFile m_buildPropertiesFile;

  // lazily instantiated models
  private BundleDescription m_desc;
  private BundlePluginModelBase m_bundlePluginModel;
  private IPluginBase m_pluginBase;
  private IBundle m_bundle;
  private WorkspaceBundleModel m_bundleModel;
  private WorkspaceBuildModel m_buildModel;
  private WorkspaceExtensionsModel m_extensionsModel;

  public LazyPluginModel(IProject project) {
    if (project == null) throw new IllegalArgumentException("null project not allowed.");
    m_project = project;
    m_manifestFile = PDEProject.getManifest(getProject());
    m_pluginXmlFile = PDEProject.getPluginXml(getProject());
    m_buildPropertiesFile = PDEProject.getBuildProperties(getProject());

    if (!isInteresting()) throw new IllegalArgumentException("the passed project is not a valid plugin.");
  }

  private boolean isInteresting() {
    return getProject() != null && getProject().isOpen() && getProject().exists() &&
        getManifestFile() != null && getManifestFile().exists() &&
        getPluginXmlFile() != null && /* plugin.xml must not exist yet */
        getBuildPropertiesFile() != null && getBuildPropertiesFile().exists();
  }

  public synchronized BundlePluginModelBase getBundlePluginModel() {
    if (m_bundlePluginModel == null) {
      m_bundlePluginModel = new BundlePluginModel();
      m_bundlePluginModel.setEnabled(true);
      m_bundlePluginModel.setBundleDescription(getBundleDescription());
      m_bundlePluginModel.setBundleModel(getBundleModel());
      m_bundlePluginModel.setExtensionsModel(getExtensionsModel());
      m_bundlePluginModel.setBuildModel(getBuildModel());
    }
    return m_bundlePluginModel;
  }

  public synchronized WorkspaceExtensionsModel getExtensionsModel() {
    if (m_extensionsModel == null) {
      m_extensionsModel = new WorkspaceExtensionsModel(getPluginXmlFile());
      m_extensionsModel.setBundleModel(getBundlePluginModel());
      m_extensionsModel.load(getBundleDescription(), getPdeState());
      m_extensionsModel.setDirty(false); // the model is marked dirty after a fresh load
    }
    return m_extensionsModel;
  }

  public synchronized IPluginBase getPluginBase() {
    if (m_pluginBase == null) {
      m_pluginBase = getBundlePluginModel().getPluginBase(true);
    }
    return m_pluginBase;
  }

  public synchronized IBuildModel getBuildModel() {
    if (m_buildModel == null) {
      m_buildModel = new WorkspaceBuildModel(getBuildPropertiesFile());
      m_buildModel.load();
      m_buildModel.setDirty(false); // the model is marked dirty after a fresh load
    }
    return m_buildModel;
  }

  public synchronized IBundle getBundle() {
    if (m_bundle == null) {
      m_bundle = getBundleModel().getBundle();
    }
    return m_bundle;
  }

  public synchronized WorkspaceBundleModel getBundleModel() {
    if (m_bundleModel == null) {
      m_bundleModel = new WorkspaceBundleModel(getManifestFile());
      m_bundleModel.load();
      m_bundleModel.setDirty(false); // the model is marked dirty after a fresh load
    }
    return m_bundleModel;
  }

  public final static PDEState getPdeState() {
    return PDECore.getDefault().getModelManager().getState();
  }

  public synchronized BundleDescription getBundleDescription() {
    if (m_desc == null) {
      IPluginModelBase pluginBase = PDECore.getDefault().getModelManager().findModel(getProject());
      if (pluginBase != null) {
        m_desc = pluginBase.getBundleDescription();
      }

      if (m_desc == null) {
        throw new IllegalArgumentException("project '" + getProject().getName() + "' could not be found in the workspace.");
      }
    }
    return m_desc;
  }

  public synchronized void save() {
    if (m_bundlePluginModel != null && m_bundlePluginModel.isDirty()) {
      // if available: saves bundle model and extensions model
      m_bundlePluginModel.save();
    }
    else {
      if (m_bundleModel != null && m_bundleModel.isDirty()) {
        m_bundleModel.save();
      }
      if (m_extensionsModel != null && m_extensionsModel.isDirty()) {
        m_extensionsModel.save();
      }
    }

    if (m_buildModel != null && m_buildModel.isDirty()) {
      m_buildModel.save();
    }
  }

  public IProject getProject() {
    return m_project;
  }

  public IFile getManifestFile() {
    return m_manifestFile;
  }

  public IFile getPluginXmlFile() {
    return m_pluginXmlFile;
  }

  public IFile getBuildPropertiesFile() {
    return m_buildPropertiesFile;
  }
}
