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
package org.eclipse.scout.sdk.s2e.nls;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.s2e.nls.internal.model.NlsWorkspace;
import org.eclipse.scout.sdk.s2e.nls.model.INlsWorkspace;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class NlsCore extends AbstractUIPlugin {

  // colors
  public static final String COLOR_NLS_ROW_INACTIVE_FOREGROUND = "nlsRowInactiveForeground";
  public static final String COLOR_TABLE_CURSOR_BACKGROUND = "color_table_cursor_background";
  public static final String COLOR_TABLE_CURSOR_FOREGROUND = "color_table_cursor_foreground";
  public static final String COLOR_TABLE_CURSOR_INACTIVE_BACKGROUND = "color_table_cursor_inactive_background";
  public static final String COLOR_TABLE_CURSOR_INACTIVE_FOREGROUND = "color_table_cursor_inactive_foreground";

  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.scout.sdk.s2e.nls";

  // The shared instance
  private static NlsCore plugin;
  private static String imagePath = "icons/";

  private ColorRegistry m_colorRegistry;
  private INlsWorkspace m_nlsWorkspace;

  /**
   * The constructor
   */
  public NlsCore() {
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);

    if (PlatformUI.isWorkbenchRunning()) {
      m_colorRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
      m_colorRegistry.put(COLOR_NLS_ROW_INACTIVE_FOREGROUND, new RGB(178, 178, 178));
      m_colorRegistry.put(COLOR_TABLE_CURSOR_INACTIVE_BACKGROUND, new RGB(255, 255, 255));
      m_colorRegistry.put(COLOR_TABLE_CURSOR_BACKGROUND, new RGB(13, 58, 161));
      m_colorRegistry.put(COLOR_TABLE_CURSOR_FOREGROUND, new RGB(255, 255, 255));
      m_colorRegistry.put(COLOR_TABLE_CURSOR_INACTIVE_FOREGROUND, new RGB(0, 0, 0));
    }

    m_nlsWorkspace = new NlsWorkspace();
    plugin = this;
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    m_nlsWorkspace = null;
    m_colorRegistry = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static NlsCore getDefault() {
    return plugin;
  }

  public static void logInfo(Throwable t) {
    plugin.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, "", t));
  }

  public static void logInfo(String message) {
    plugin.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
  }

  public static void logInfo(String message, Throwable t) {
    plugin.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message, t));
  }

  public static void logWarning(String message) {
    plugin.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message));
  }

  public static void logWarning(Throwable t) {
    plugin.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, "", t));
  }

  public static void logWarning(String message, Throwable t) {
    plugin.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message, t));
  }

  public static void logError(Throwable t) {
    plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "", t));
  }

  public static void logError(String message) {
    plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
  }

  public static void logError(String message, Throwable t) {
    plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, t));
  }

  public static String getLineSeparator(IResource r) {
    String defaultLineSep = System.getProperty("line.separator");
    if (r == null) {
      return defaultLineSep;
    }

    IScopeContext[] scopeContext = new IScopeContext[]{new ProjectScope(r.getProject())};
    return Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, defaultLineSep, scopeContext);
  }

  public static Image getImage(String name) {
    Image img = plugin.getImageRegistry().get(name);
    if (img == null) {
      getImageDescriptor(name);
    }
    return plugin.getImageRegistry().get(name);
  }

  public static Color getColor(String key) {
    return getDefault().m_colorRegistry.get(key);
  }

  public static ImageDescriptor getImageDescriptor(String name) {
    ImageDescriptor desc = plugin.getImageRegistry().getDescriptor(name);
    if (desc == null) {
      // gif
      desc = imageDescriptorFromPlugin(PLUGIN_ID, imagePath + name + ".gif");
      // png
      if (desc == null) {
        desc = imageDescriptorFromPlugin(PLUGIN_ID, imagePath + name + ".png");
      }
      // jpg
      if (desc == null) {
        desc = imageDescriptorFromPlugin(PLUGIN_ID, imagePath + name + ".jpg");
      }
      if (desc == null) {
        System.err.println("could not find image for plugin: " + PLUGIN_ID + " under: " + imagePath + name);
      }
      plugin.getImageRegistry().put(name, desc);
    }
    return desc;
  }

  public static INlsWorkspace getNlsWorkspace() {
    return getDefault().getNlsWorkspaceImpl();
  }

  private INlsWorkspace getNlsWorkspaceImpl() {
    return m_nlsWorkspace;
  }

  public static IStatus getHighestSeverityStatus(IStatus status) {
    return getDefault().getHighestSeverityStatusImpl(status, Status.OK_STATUS);
  }

  private IStatus getHighestSeverityStatusImpl(IStatus status, IStatus highestSeverity) {
    if (status.isMultiStatus()) {
      for (IStatus child : status.getChildren()) {
        highestSeverity = getHighestSeverityStatusImpl(child, highestSeverity);
      }
      return highestSeverity;
    }
    if (highestSeverity.getSeverity() < status.getSeverity()) {
      highestSeverity = status;
    }
    return highestSeverity;
  }

  public IDialogSettings getDialogSettingsSection(String name) {
    return getDialogSettingsSection(name, true);
  }

  public IDialogSettings getDialogSettingsSection(String name, boolean createIfNotExist) {
    IDialogSettings dialogSettings = getDialogSettings();
    IDialogSettings section = dialogSettings.getSection(name);
    if (section == null) {
      section = dialogSettings.addNewSection(name);
    }
    return section;
  }

}
