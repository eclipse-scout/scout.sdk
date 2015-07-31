/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.s2e.IOrganizeImportService;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.dto.IDtoAutoUpdateManager;
import org.eclipse.scout.sdk.s2e.log.SdkLogManager;
import org.eclipse.scout.sdk.s2e.ui.internal.util.JdtSettingsCommentBuilder;
import org.eclipse.scout.sdk.s2e.ui.internal.util.OrganizeImportService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 *
 */
public class S2ESdkUiActivator extends AbstractUIPlugin {

  public static final String PLUGIN_ID = "org.eclipse.scout.sdk.s2e.ui";
  private static final String IMAGE_PATH = "icons/";

  private static S2ESdkUiActivator plugin;

  private IWorkbenchListener m_shutdownListener;
  private SdkLogManager m_logManager;
  private ServiceRegistration<?> m_organizeImportServiceRegistration;
  private IPropertyChangeListener m_preferencesPropertyListener;

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);

    // shared instance
    plugin = this;

    // logger
    m_logManager = new SdkLogManager(this);

    // organize import service
    m_organizeImportServiceRegistration = context.registerService(IOrganizeImportService.class.getName(), new OrganizeImportService(), null);
    CommentSourceBuilderFactory.javaElementCommentBuilder = new JdtSettingsCommentBuilder();

    // property change listener (scout preferences)
    if (m_preferencesPropertyListener == null) {
      m_preferencesPropertyListener = new P_PreferenceStorePropertyListener();
    }
    getPreferenceStore().addPropertyChangeListener(m_preferencesPropertyListener);

    // start DTO auto-update manager if required
    getPreferenceStore().setDefault(IDtoAutoUpdateManager.PROP_AUTO_UPDATE, true);
    ScoutSdkCore.getDtoAutoUpdateManager().setEnabled(getPreferenceStore().getBoolean(IDtoAutoUpdateManager.PROP_AUTO_UPDATE));

    // listener for workspace shutdown (to wait for DTO updates to complete)
    m_shutdownListener = new IWorkbenchListener() {
      @Override
      public boolean preShutdown(IWorkbench workbench, boolean forced) {
        try {
          new P_AutoUpdateOperationsShutdownJob().schedule();
        }
        catch (Exception e) {
          logError(e);
        }
        catch (NoClassDefFoundError er) {
          // can happen if the workbench is so far in shutdown, that no more classes are loaded for bundles.
        }
        return true;
      }

      @Override
      public void postShutdown(IWorkbench workbench) {
      }
    };
    PlatformUI.getWorkbench().addWorkbenchListener(m_shutdownListener);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    PlatformUI.getWorkbench().removeWorkbenchListener(m_shutdownListener);

    if (m_preferencesPropertyListener != null) {
      getPreferenceStore().removePropertyChangeListener(m_preferencesPropertyListener);
      m_preferencesPropertyListener = null;
    }

    CommentSourceBuilderFactory.javaElementCommentBuilder = null;

    if (m_organizeImportServiceRegistration != null) {
      m_organizeImportServiceRegistration.unregister();
      m_organizeImportServiceRegistration = null;
    }

    m_logManager = null;
    plugin = null;

    super.stop(context);
  }

  public static S2ESdkUiActivator getDefault() {
    return plugin;
  }

  public static void logInfo(Throwable t) {
    plugin.m_logManager.logInfo(t);
  }

  public static void logInfo(String message) {
    plugin.m_logManager.logInfo(message);
  }

  public static void logInfo(String message, Throwable t) {
    plugin.m_logManager.logInfo(message, t);
  }

  public static void logWarning(String message) {
    plugin.m_logManager.logWarning(message);
  }

  public static void logWarning(Throwable t) {
    plugin.m_logManager.logWarning(t);
  }

  public static void logWarning(String message, Throwable t) {
    plugin.m_logManager.logWarning(message, t);
  }

  public static void logError(Throwable t) {
    plugin.m_logManager.logError(t);
  }

  public static void logError(String message) {
    plugin.m_logManager.logError(message);
  }

  public static void logError(String message, Throwable t) {
    plugin.m_logManager.logError(message, t);
  }

  public static void log(int level, Throwable t) {
    plugin.m_logManager.log(level, t);
  }

  public static void log(int level, String message) {
    plugin.m_logManager.log(level, message);
  }

  public static void log(int level, String message, Throwable t) {
    plugin.m_logManager.log(level, message, t);
  }

  public static void log(IStatus status) {
    plugin.m_logManager.log(status);
  }

  @Override
  protected ImageRegistry createImageRegistry() {
    // If we are in the UI Thread use that
    if (Display.getCurrent() != null) {
      return new ImageRegistry(Display.getCurrent());
    }
    if (PlatformUI.isWorkbenchRunning()) {
      return new ImageRegistry(PlatformUI.getWorkbench().getDisplay());
    }
    // Invalid thread access if it is not the UI Thread
    // and the workbench is not created.
    throw new SWTError(SWT.ERROR_THREAD_INVALID_ACCESS);
  }

  @Override
  public ImageRegistry getImageRegistry() {
    return (ImageRegistry) super.getImageRegistry();
  }

  /**
   * To get a cached image with one of the extensions [gif | png | jpg]
   *
   * @param name
   *          the name without extension located under resources/icons e.g. "person"
   * @return the cached image
   */
  public static Image getImage(String name) {
    return getDefault().getImageImpl(name);
  }

  private Image getImageImpl(String name) {
    Image image = getImageRegistry().get(name);
    if (image == null) {
      loadImage(name);
      image = getImageRegistry().get(name);
    }
    return image;
  }

  private void loadImage(String name) {
    ImageDescriptor desc = null;
    if (name.startsWith(IMAGE_PATH)) {
      desc = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, name);
    }
    if (desc == null) {
      // try already extension
      desc = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, IMAGE_PATH + name);
    }
    // gif
    if (desc == null) {
      desc = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, IMAGE_PATH + name + ".gif");
    }
    // png
    if (desc == null) {
      desc = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, IMAGE_PATH + name + ".png");
    }
    // jpg
    if (desc == null) {
      desc = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, IMAGE_PATH + name + ".jpg");
    }
    if (desc == null) {
      logWarning("could not find image for plugin: '" + PLUGIN_ID + "' under: '" + IMAGE_PATH + name + "'.");
    }
    else {
      getImageRegistry().put(name, desc);
    }
  }

  private static final class P_AutoUpdateOperationsShutdownJob extends Job {
    private P_AutoUpdateOperationsShutdownJob() {
      super("Waiting until all derived resources have been updated...");
      setUser(true);

      // ensures the shutdown is blocked until update is complete or the user decides to cancel
      setRule(ResourcesPlugin.getWorkspace().getRoot());
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      Job[] dtoUpdateJobs = null;
      while (!monitor.isCanceled()) {
        dtoUpdateJobs = getJobManager().find(org.eclipse.scout.sdk.s2e.internal.dto.DtoAutoUpdateManager.AUTO_UPDATE_JOB_FAMILY);
        if (dtoUpdateJobs.length < 1) {
          // no job is running -> finish
          return Status.OK_STATUS;
        }
        try {
          Thread.sleep(2000);
        }
        catch (InterruptedException e) {
        }
      }

      // the dto job should be cancelled
      if (dtoUpdateJobs != null && dtoUpdateJobs.length > 0) {
        for (Job j : dtoUpdateJobs) {
          j.cancel(); // will cancel as soon as possible
        }
      }

      return Status.CANCEL_STATUS;
    }
  }

  private static final class P_PreferenceStorePropertyListener implements IPropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent event) {
      Object newValue = event.getNewValue();
      if (newValue == null) {
        return;
      }

      if (IDtoAutoUpdateManager.PROP_AUTO_UPDATE.equals(event.getProperty())) {
        boolean autoUpdate = Boolean.parseBoolean(newValue.toString());
        ScoutSdkCore.getDtoAutoUpdateManager().setEnabled(autoUpdate);
      }
    }
  }
}
