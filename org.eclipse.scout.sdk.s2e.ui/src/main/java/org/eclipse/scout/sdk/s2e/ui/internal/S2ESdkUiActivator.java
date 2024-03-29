/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.debug.ui.DetailFormatter;
import org.eclipse.jdt.internal.debug.ui.JavaDetailFormattersManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.m2e.core.internal.preferences.MavenPreferenceConstants;
import org.eclipse.m2e.core.ui.internal.M2EUIPluginActivator;
import org.eclipse.scout.sdk.core.java.builder.comment.JavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.log.SdkConsole;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.classid.ClassIds;
import org.eclipse.scout.sdk.core.s.java.apidef.ScoutApi;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.classid.ClassIdValidationJob;
import org.eclipse.scout.sdk.s2e.derived.IDerivedResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class S2ESdkUiActivator extends AbstractUIPlugin {

  public static final String PLUGIN_ID = "org.eclipse.scout.sdk.s2e.ui";
  public static final String PROP_AUTOMATICALLY_CREATE_CLASS_ID_ANNOTATION = "org.eclipse.scout.sdk.propAutoCreateClassId";
  public static final String IMAGE_PATH = "icons/";

  private static S2ESdkUiActivator plugin;

  private IPropertyChangeListener m_preferencesPropertyListener;
  private DetailFormatter m_iDataObjectDetailFormatter;

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);

    // shared instance
    plugin = this;

    // init Sdk log level
    getPreferenceStore().setDefault(SdkLog.LOG_LEVEL_PROPERTY_NAME, SdkLog.DEFAULT_LOG_LEVEL.getName());
    @SuppressWarnings("AccessOfSystemProperties")
    var isLoggingConfiguredInSystem = Strings.hasText(System.getProperty(SdkLog.LOG_LEVEL_PROPERTY_NAME));
    var isLoggingConfiguredInWorkspace = !getPreferenceStore().isDefault(SdkLog.LOG_LEVEL_PROPERTY_NAME);
    if (!isLoggingConfiguredInWorkspace && !isLoggingConfiguredInSystem && (Platform.inDebugMode() || Platform.inDevelopmentMode())) {
      // nothing is specified in the workspace and in the system: use full logging in debug mode by default
      // note: This value is not stored in the workspace preferences! Therefore not shown in the preference page.
      //       This is because it is only valid for this one debug launch and should not touch the actual workspace setting
      SdkLog.setLogLevel(Level.INFO);
    }
    else if (isLoggingConfiguredInWorkspace) {
      SdkLog.setLogLevel(getPreferenceStore().getString(SdkLog.LOG_LEVEL_PROPERTY_NAME));
    }

    //attach sdk console to workbench
    SdkConsole.setConsoleSpi(new WorkbenchSdkConsoleSpi());

    // comment source builder
    JavaElementCommentBuilder.setCommentGeneratorSpi(new JdtSettingsCommentGenerator());

    // property change listener (scout preferences)
    if (m_preferencesPropertyListener == null) {
      m_preferencesPropertyListener = new P_PreferenceStorePropertyListener();
    }
    getPreferenceStore().addPropertyChangeListener(m_preferencesPropertyListener);

    // modify default m2e settings
    setDefaultMavenSettings();

    // register detail formatters
    registerDetailFormatters();

    // start DTO auto-update manager if required
    getPreferenceStore().setDefault(IDerivedResourceManager.PROP_AUTO_UPDATE, true);
    var mgr = S2ESdkActivator.getDefault().getDerivedResourceManager();
    if (mgr != null) {
      mgr.setEnabled(getPreferenceStore().getBoolean(IDerivedResourceManager.PROP_AUTO_UPDATE));
    }

    // class id generation
    getPreferenceStore().setDefault(PROP_AUTOMATICALLY_CREATE_CLASS_ID_ANNOTATION, false);
    ClassIds.setAutomaticallyCreateClassIdAnnotation(getPreferenceStore().getBoolean(PROP_AUTOMATICALLY_CREATE_CLASS_ID_ANNOTATION));

    // start class id validation
    ClassIdValidationJob.executeAsync(TimeUnit.MINUTES.toMillis(5), false);
  }

  private void registerDetailFormatters() {
    ScoutApi.allKnown()
        .collect(groupingBy(api -> api.IDataObject().fqn(),
            mapping(api -> new SimpleEntry<>(api.BEANS().fqn(), api.IPrettyPrintDataObjectMapper().fqn()), toSet())))
        .forEach(this::registerDetailFormatter);
  }

  private void registerDetailFormatter(String iDataObjectFqn, Collection<SimpleEntry<String, String>> fqn) {
    if (fqn.size() > 1) {
      SdkLog.warning("Ambiguous formatter specifications for object '{}'. Using the first.", iDataObjectFqn);
    }
    var entry = fqn.iterator().next();
    var beans = entry.getKey();
    var iPrettyPrintDataObjectMapper = entry.getValue();
    var src = "return " + beans + ".get(" + iPrettyPrintDataObjectMapper + ".class).writeValue(this);";
    m_iDataObjectDetailFormatter = new DetailFormatter(iDataObjectFqn, src, true);
    JavaDetailFormattersManager.getDefault().setAssociatedDetailFormatter(m_iDataObjectDetailFormatter);
  }

  private void deregisterDetailFormatters() {
    m_iDataObjectDetailFormatter = null;
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    if (m_preferencesPropertyListener != null) {
      getPreferenceStore().removePropertyChangeListener(m_preferencesPropertyListener);
      m_preferencesPropertyListener = null;
    }

    JavaElementCommentBuilder.setCommentGeneratorSpi(null);

    SdkConsole.setConsoleSpi(null); // reset to default

    deregisterDetailFormatters();

    plugin = null;

    super.stop(context);
  }

  public static S2ESdkUiActivator getDefault() {
    return plugin;
  }

  private static void setDefaultMavenSettings() {
    try {
      var m2eUiActivator = M2EUIPluginActivator.getDefault();
      if (m2eUiActivator == null) {
        return;
      }

      var m2eUiPrefs = m2eUiActivator.getPreferenceStore();
      if (m2eUiPrefs == null) {
        return;
      }

      if (m2eUiPrefs.isDefault(MavenPreferenceConstants.P_DOWNLOAD_SOURCES) && !m2eUiPrefs.getBoolean(MavenPreferenceConstants.P_DOWNLOAD_SOURCES)) {
        m2eUiPrefs.setDefault(MavenPreferenceConstants.P_DOWNLOAD_SOURCES, true);
        m2eUiPrefs.firePropertyChangeEvent(MavenPreferenceConstants.P_DOWNLOAD_SOURCES, Boolean.FALSE, Boolean.TRUE);
      }
    }
    catch (RuntimeException e) {
      SdkLog.info("Unable to set default maven options", e);
    }
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
    var image = getImageRegistry().get(name);
    if (image == null) {
      var desc = getImageDescriptor(name);
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
    return ResourceLocator.imageDescriptorFromBundle(PLUGIN_ID, IMAGE_PATH + fileName).orElse(null);
  }

  public IDialogSettings getDialogSettingsSection(String name) {
    var dialogSettings = getDialogSettings();
    var section = dialogSettings.getSection(name);
    if (section == null) {
      section = dialogSettings.addNewSection(name);
    }
    return section;
  }

  private static final class P_PreferenceStorePropertyListener implements IPropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent event) {
      var newValue = event.getNewValue();

      if (IDerivedResourceManager.PROP_AUTO_UPDATE.equals(event.getProperty())) {
        var autoUpdate = newValue == null || Boolean.parseBoolean(newValue.toString());
        var mgr = S2ESdkActivator.getDefault().getDerivedResourceManager();
        if (mgr != null) {
          mgr.setEnabled(autoUpdate);
        }
      }
      else if (PROP_AUTOMATICALLY_CREATE_CLASS_ID_ANNOTATION.equals(event.getProperty())) {
        var automaticallyCreate = newValue != null && Boolean.parseBoolean(newValue.toString());
        ClassIds.setAutomaticallyCreateClassIdAnnotation(automaticallyCreate);
      }
      else if (SdkLog.LOG_LEVEL_PROPERTY_NAME.equals(event.getProperty())) {
        if (newValue == null) {
          SdkLog.setInitialLogLevel();
        }
        else {
          SdkLog.setLogLevel(newValue.toString());
        }
      }
    }
  }
}
