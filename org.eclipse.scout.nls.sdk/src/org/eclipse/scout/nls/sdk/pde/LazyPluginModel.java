package org.eclipse.scout.nls.sdk.pde;

import org.eclipse.core.resources.IProject;
import org.eclipse.pde.core.build.IBuildModel;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundlePluginModel;
import org.eclipse.pde.internal.core.ibundle.IBundle;
import org.eclipse.pde.internal.core.ibundle.IBundleModel;
import org.eclipse.pde.internal.core.project.PDEProject;

/**
 * Base class for Plugin modifications using the PDE model classes.<br>
 * This implementation uses lazy initialization of the PDE models.<br>
 * This class is thread safe.
 * 
 * @author mvi
 * @since 3.7.1
 */
@SuppressWarnings("restriction")
public final class LazyPluginModel {
  private final IProject m_project;

  private WorkspaceBundlePluginModel m_bundlePluginModel;
  private IPluginBase m_pluginBase;
  private IBundle m_bundle;
  private IBundleModel m_bundleModel;
  private IBuildModel m_buildModel;

  public LazyPluginModel(IProject project) {
    if (project == null) throw new IllegalArgumentException("project cannot be null");
    m_project = project;
  }

  public final IProject getProject() {
    return m_project;
  }

  public final synchronized WorkspaceBundlePluginModel getBundlePluginModel() {
    if (m_bundlePluginModel == null) {
      m_bundlePluginModel = new WorkspaceBundlePluginModel(PDEProject.getManifest(m_project), PDEProject.getPluginXml(m_project));
    }
    return m_bundlePluginModel;
  }

  public final synchronized IPluginBase getPluginBase() {
    if (m_pluginBase == null) {
      m_pluginBase = getBundlePluginModel().getPluginBase(true);
    }
    return m_pluginBase;
  }

  public final synchronized IBuildModel getBuildModel() {
    if (m_buildModel == null) {
      m_buildModel = getBundlePluginModel().getBuildModel();
    }
    return m_buildModel;
  }

  public final synchronized IBundle getBundle() {
    if (m_bundle == null) {
      m_bundle = getBundleModel().getBundle();
    }
    return m_bundle;
  }

  public final synchronized IBundleModel getBundleModel() {
    if (m_bundleModel == null) {
      m_bundleModel = getBundlePluginModel().getBundleModel();
    }
    return m_bundleModel;
  }
}
