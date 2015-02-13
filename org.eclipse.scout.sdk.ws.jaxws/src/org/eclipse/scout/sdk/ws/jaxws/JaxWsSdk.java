/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.ui.internal.ImageRegistry;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.log.SdkLogManager;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.IMarkerCommand;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPageReloadNotification;
import org.eclipse.scout.sdk.ws.jaxws.worker.MarkerQueueManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class JaxWsSdk extends AbstractUIPlugin implements JaxWsIcons {

  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.scout.sdk.ws.jaxws";
  private static final String IMAGE_PATH = "resources/icons/";

  private static JaxWsSdk plugin;
  private static SdkLogManager logManager;

  private Map<String, IMarkerCommand> m_markerCommands;
  private MarkerQueueManager m_markerQueueManager;
  private Object m_pageRegistryLock;
  private Map<Class<? extends IPage>, Set<IPageReloadNotification>> m_pageRegistry;

  public JaxWsSdk() {
    m_markerCommands = new ConcurrentHashMap<>();
    m_pageRegistry = new HashMap<>();
    m_pageRegistryLock = new Object();
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    logManager = new SdkLogManager(this);
    m_markerQueueManager = new MarkerQueueManager();
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    if (m_markerQueueManager != null) {
      m_markerQueueManager.cancelWorker();
      m_markerQueueManager = null;
    }
    logManager = null;
    super.stop(context);
  }

  public static JaxWsSdk getDefault() {
    return plugin;
  }

  public static void logInfo(Throwable t) {
    logManager.logInfo(t);
  }

  public static void logInfo(String message) {
    logManager.logInfo(message);
  }

  public static void logInfo(String message, Throwable t) {
    logManager.logInfo(message, t);
  }

  public static void logWarning(String message) {
    logManager.logWarning(message);
  }

  public static void logWarning(Throwable t) {
    logManager.logWarning(t);
  }

  public static void logWarning(String message, Throwable t) {
    logManager.logWarning(message, t);
  }

  public static void logError(Throwable t) {
    logManager.logError(t);
  }

  public static void logError(String message) {
    logManager.logError(message);
  }

  public static void logError(String message, Throwable t) {
    logManager.logError(message, t);
  }

  public IMarkerCommand getMarkerCommand(String markerSourceId) {
    return m_markerCommands.get(markerSourceId);
  }

  public IMarkerCommand addMarkerCommand(String markerSourceId, IMarkerCommand markerCommand) {
    return m_markerCommands.put(markerSourceId, markerCommand);
  }

  public void removeMarkerCommand(String markerSourceId) {
    m_markerCommands.remove(markerSourceId);
  }

  public void removeMarkerCommands(String markerGroupUUID) {
    for (String markerSourceId : m_markerCommands.keySet()) {
      if (markerSourceId.startsWith(markerGroupUUID)) {
        m_markerCommands.remove(markerSourceId);
      }
    }
  }

  public boolean containsMarkerCommands(String markerGroupUUID) {
    Set<String> markerSourceIds = m_markerCommands.keySet();
    for (String markerSourceId : markerSourceIds) {
      if (markerSourceId.startsWith(markerGroupUUID)) {
        return true;
      }
    }
    return false;
  }

  public MarkerQueueManager getMarkerQueueManager() {
    return m_markerQueueManager;
  }

  /**
   * To load the model of a page
   *
   * @param pageClazz
   * @param markerGroupUUID
   * @param dataMask
   */
  public void notifyPageReload(Class<? extends IPage> pageClazz, final String markerGroupUUID, final int dataMask) {
    final IPageReloadNotification[] pages;
    synchronized (m_pageRegistryLock) {
      Set<IPageReloadNotification> pageSet = m_pageRegistry.get(pageClazz);
      if (pageSet == null || pageSet.size() == 0) {
        return;
      }
      pages = pageSet.toArray(new IPageReloadNotification[pageSet.size()]);
    }

    Job job = new Job("Reload page content") {

      @Override
      protected IStatus run(IProgressMonitor monitor) {
        for (IPageReloadNotification page : pages) {
          try {
            if (CompareUtility.equals(markerGroupUUID, page.getMarkerGroupUUID())) {
              page.reloadPage(dataMask);
            }
          }
          catch (Exception e) {
            logError("failed to notify listener", e);
          }
        }
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }

  /**
   * Register page to receive reload requests
   *
   * @param pageClazz
   * @param page
   */
  public void registerPage(Class<? extends IPage> pageClazz, IPageReloadNotification page) {
    synchronized (m_pageRegistryLock) {
      Set<IPageReloadNotification> pages = m_pageRegistry.get(pageClazz);
      if (pages == null) {
        pages = new HashSet<>();
        m_pageRegistry.put(pageClazz, pages);
      }
      pages.add(page);
    }
  }

  /**
   * Unregister page from receiving reload requests
   *
   * @param pageClazz
   * @param page
   */
  public void unregisterPage(Class<? extends IPage> pageClazz, IPageReloadNotification page) {
    synchronized (m_pageRegistryLock) {
      Set<IPageReloadNotification> pages = m_pageRegistry.get(pageClazz);
      if (pages == null) {
        return;
      }
      pages.remove(page);
    }
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
  protected void initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry reg) {
  }

  @Override
  public ImageRegistry getImageRegistry() {
    return (ImageRegistry) super.getImageRegistry();
  }

  /**
   * Returns the image for the given composite descriptor.
   */
  public static Image getImage(CompositeImageDescriptor imageDescriptor) {
    return getDefault().getImageImpl(imageDescriptor);
  }

  private Image getImageImpl(CompositeImageDescriptor imageDescriptor) {
    return getImageRegistry().get(imageDescriptor);
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

  /**
   * To get a cached image with one of the extensions [gif | png | jpg]
   *
   * @param name
   *          the name without extension located under resources/icons e.g. "person"
   * @return the cached image
   */
  public static ImageDescriptor getImageDescriptor(String name) {
    return getDefault().getImageDescriptorImpl(name);
  }

  private ImageDescriptor getImageDescriptorImpl(String name) {
    ImageDescriptor imageDesc = getImageRegistry().getDescriptor(name);
    if (imageDesc == null) {
      loadImage(name);
      imageDesc = getImageRegistry().getDescriptor(name);
    }
    return imageDesc;
  }

  private void loadImage(String name) {
    ImageDescriptor desc = null;
    // try already extension
    desc = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, IMAGE_PATH + name);
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
}
