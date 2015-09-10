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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.classid.ClassIdValidationJob;
import org.eclipse.scout.sdk.s2e.log.SdkLogManager;
import org.eclipse.scout.sdk.s2e.trigger.ITypeChangedManager;
import org.eclipse.scout.sdk.s2e.ui.internal.util.JdtSettingsCommentSourceBuilderDelegate;
import org.eclipse.scout.sdk.s2e.ui.internal.util.OrganizeImportService;
import org.eclipse.scout.sdk.s2e.util.IOrganizeImportService;
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
  public static final String IMAGE_PATH = "icons/";

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
    CommentSourceBuilderFactory.commentSourceBuilderDelegate = new JdtSettingsCommentSourceBuilderDelegate();

    // property change listener (scout preferences)
    if (m_preferencesPropertyListener == null) {
      m_preferencesPropertyListener = new P_PreferenceStorePropertyListener();
    }
    getPreferenceStore().addPropertyChangeListener(m_preferencesPropertyListener);

    // start DTO auto-update manager if required
    getPreferenceStore().setDefault(ITypeChangedManager.PROP_AUTO_UPDATE, true);
    ScoutSdkCore.getTypeChangedManager().setEnabled(getPreferenceStore().getBoolean(ITypeChangedManager.PROP_AUTO_UPDATE));

    // start class id validation
    ClassIdValidationJob.install();
    ClassIdValidationJob.executeAsync(15000);

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

    ClassIdValidationJob.uninstall();

    if (m_preferencesPropertyListener != null) {
      getPreferenceStore().removePropertyChangeListener(m_preferencesPropertyListener);
      m_preferencesPropertyListener = null;
    }

    CommentSourceBuilderFactory.commentSourceBuilderDelegate = null;

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
   * To get a cached image
   *
   * @param name
   *          The full file name of the image located under {@value #IMAGE_PATH} inside this plug-in.
   * @return the cached image
   */
  public static Image getImage(String name) {
    return getDefault().getImageImpl(name);
  }

  private Image getImageImpl(String name) {
    Image image = getImageRegistry().get(name);
    if (image == null) {
      ImageDescriptor desc = getImageDescriptor(name);
      getImageRegistry().put(name, desc);
      image = getImageRegistry().get(name);
    }
    return image;
  }

  /**
   * Creates a new {@link ImageDescriptor} for the given image
   *
   * @param fileName
   *          The full file name of the image located under {@value #IMAGE_PATH} inside this plug-in.
   * @return The new image descriptor
   */
  public static ImageDescriptor getImageDescriptor(String fileName) {
    return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, IMAGE_PATH + fileName);
  }

  public IDialogSettings getDialogSettingsSection(String name, boolean createIfNotExist) {
    IDialogSettings dialogSettings = getDialogSettings();
    IDialogSettings section = dialogSettings.getSection(name);
    if (section == null) {
      section = dialogSettings.addNewSection(name);
    }
    return section;
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
      Job[] triggerJobs = null;
      while (!monitor.isCanceled()) {
        triggerJobs = getJobManager().find(org.eclipse.scout.sdk.s2e.internal.trigger.TypeChangedManager.TYPE_CHANGED_TRIGGER_JOB_FAMILY);
        if (triggerJobs.length < 1) {
          // no job is running -> finish
          return Status.OK_STATUS;
        }
        try {
          Thread.sleep(2000);
        }
        catch (InterruptedException e) {
        }
      }

      // the trigger job should be cancelled
      if (triggerJobs != null && triggerJobs.length > 0) {
        for (Job j : triggerJobs) {
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

      if (ITypeChangedManager.PROP_AUTO_UPDATE.equals(event.getProperty())) {
        boolean autoUpdate = Boolean.parseBoolean(newValue.toString());
        ScoutSdkCore.getTypeChangedManager().setEnabled(autoUpdate);
      }
    }
  }
}
