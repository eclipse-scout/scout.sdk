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

import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.util.SdkConsole;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.ScoutSdkCore;
import org.eclipse.scout.sdk.s2e.classid.ClassIdGenerators;
import org.eclipse.scout.sdk.s2e.classid.ClassIdValidationJob;
import org.eclipse.scout.sdk.s2e.trigger.IDerivedResourceManager;
import org.eclipse.scout.sdk.s2e.ui.internal.util.JdtSettingsCommentSourceBuilderDelegate;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class S2ESdkUiActivator extends AbstractUIPlugin {

  public static final String PLUGIN_ID = "org.eclipse.scout.sdk.s2e.ui";
  public static final String IMAGE_PATH = "icons/";

  private static S2ESdkUiActivator plugin;

  private IPropertyChangeListener m_preferencesPropertyListener;

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);

    // shared instance
    plugin = this;

    //attach sdk console to workbench
    SdkConsole.spi = new WorkbenchSdkConsoleSpi();

    // init Sdk log level to all logs if running in dev mode
    if (StringUtils.isBlank(System.getProperty(SdkLog.LOG_LEVEL_PROPERTY_NAME)) && (Platform.inDebugMode() || Platform.inDevelopmentMode())) {
      System.setProperty(SdkLog.LOG_LEVEL_PROPERTY_NAME, Level.ALL.getName());
    }

    // comment source builder
    CommentSourceBuilderFactory.commentSourceBuilderSpi = new JdtSettingsCommentSourceBuilderDelegate();

    // property change listener (scout preferences)
    if (m_preferencesPropertyListener == null) {
      m_preferencesPropertyListener = new P_PreferenceStorePropertyListener();
    }
    getPreferenceStore().addPropertyChangeListener(m_preferencesPropertyListener);

    // start DTO auto-update manager if required
    getPreferenceStore().setDefault(IDerivedResourceManager.PROP_AUTO_UPDATE, true);
    ScoutSdkCore.getDerivedResourceManager().setEnabled(getPreferenceStore().getBoolean(IDerivedResourceManager.PROP_AUTO_UPDATE));

    // class id generation
    getPreferenceStore().setDefault(ClassIdGenerators.PROP_AUTOMATICALLY_CREATE_CLASS_ID_ANNOTATION, false);
    ClassIdGenerators.setAutomaticallyCreateClassIdAnnotation(getPreferenceStore().getBoolean(ClassIdGenerators.PROP_AUTOMATICALLY_CREATE_CLASS_ID_ANNOTATION));

    // start class id validation
    ClassIdValidationJob.install();
    ClassIdValidationJob.executeAsync(15000);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    ClassIdValidationJob.uninstall();

    if (m_preferencesPropertyListener != null) {
      getPreferenceStore().removePropertyChangeListener(m_preferencesPropertyListener);
      m_preferencesPropertyListener = null;
    }

    CommentSourceBuilderFactory.commentSourceBuilderSpi = null;

    plugin = null;

    super.stop(context);
  }

  public static S2ESdkUiActivator getDefault() {
    return plugin;
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

  private static final class P_PreferenceStorePropertyListener implements IPropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent event) {
      Object newValue = event.getNewValue();
      if (newValue == null) {
        return;
      }

      if (IDerivedResourceManager.PROP_AUTO_UPDATE.equals(event.getProperty())) {
        boolean autoUpdate = Boolean.parseBoolean(newValue.toString());
        ScoutSdkCore.getDerivedResourceManager().setEnabled(autoUpdate);
      }
      else if (ClassIdGenerators.PROP_AUTOMATICALLY_CREATE_CLASS_ID_ANNOTATION.equals(event.getProperty())) {
        boolean automaticallyCreate = Boolean.parseBoolean(newValue.toString());
        ClassIdGenerators.setAutomaticallyCreateClassIdAnnotation(automaticallyCreate);
      }
    }
  }
}
