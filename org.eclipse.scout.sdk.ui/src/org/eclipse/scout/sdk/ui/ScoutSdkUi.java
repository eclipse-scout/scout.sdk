/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.LogStatus;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataAutoUpdater;
import org.eclipse.scout.sdk.operation.form.formdata.ICreateFormDataRequest;
import org.eclipse.scout.sdk.ui.internal.CreateFormDataRequest;
import org.eclipse.scout.sdk.ui.internal.ImageRegistry;
import org.eclipse.scout.sdk.ui.view.outline.IScoutExplorerPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class ScoutSdkUi extends AbstractUIPlugin implements SdkIcons {
  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.scout.sdk.ui";
  public static final String LOG_LEVEL = PLUGIN_ID + ".loglevel";

  // The shared instance
  private static ScoutSdkUi plugin;

  public static final String PROPERTY_RELEASE_NOTES = PLUGIN_ID + ".releaseNotes";
  private static String PROPERTY_PLUGIN_VERSION = "pluginVersion";

  private static final String IMAGE_PATH = "resources/icons/";

  // COLORS
  public static final String COLOR_INACTIVE_FOREGROUND = "inactiveForeground";

  // FONTS
  public static final String FONT_SYSTEM_BOLD = "fontSystemBold";

  private ColorRegistry m_colorRegistry;
  private FontRegistry m_fontRegistry;
  private ServiceRegistration m_formDataServiceRegistration;
  private IPropertyChangeListener m_preferencesPropertyListener;
  private int m_loglevel;

  /**
   * The constructor
   */
  public ScoutSdkUi() {
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    m_loglevel = parseLogLevel(context.getProperty(LOG_LEVEL));
    plugin = this;
    CreateFormDataRequest requestService = new CreateFormDataRequest();
    if (m_formDataServiceRegistration == null) {
      m_formDataServiceRegistration = context.registerService(ICreateFormDataRequest.class.getName(), requestService, null);
    }
    if (m_preferencesPropertyListener == null) {
      m_preferencesPropertyListener = new P_PreferenceStorePropertyListener();
    }
    getPreferenceStore().setDefault(FormDataAutoUpdater.PROP_FORMDATA_AUTO_UPDATE, true);
    ScoutSdk.getDefault().setFormDataAutoUpdate(getPreferenceStore().getBoolean(FormDataAutoUpdater.PROP_FORMDATA_AUTO_UPDATE));
    getPreferenceStore().addPropertyChangeListener(m_preferencesPropertyListener);
//    contributeMenus(context);

    // version++ -> releasenotes
//    try {
//      IEclipsePreferences node = new InstanceScope().getNode(getBundle().getSymbolicName());
//      Version lastVersion = Version.emptyVersion;
//      if (node != null) {
//        lastVersion = Version.parseVersion(node.get(PROPERTY_PLUGIN_VERSION, "0.0.0"));
//        lastVersion = new Version(lastVersion.getMajor(), lastVersion.getMinor(), lastVersion.getMicro());
//      }
//      Version newVersion = Version.parseVersion((String) context.getBundle().getHeaders().get("Bundle-Version"));
//      newVersion = new Version(newVersion.getMajor(), newVersion.getMinor(), newVersion.getMicro());
//      ScoutSdkUi.logWarning("before compare new version '" + newVersion + "' to old version '" + lastVersion + "'");
//      if (newVersion.compareTo(lastVersion) > 0) {
//        if (node != null) {
//          node.put(PROPERTY_PLUGIN_VERSION, newVersion.toString());
//        }
//        ShowReleaseNotesJob job = new ShowReleaseNotesJob(newVersion);
//        job.schedule(100);
//      }
//    }
//    catch (Exception e) {
//      ScoutSdkUi.logError("could not versioncheck...", e);
//    }
//    m_formDataMarkerSupport = new FormDataMarkerSupport();
//    JavaCore.addElementChangedListener(m_formDataMarkerSupport);

  }

  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    if (m_preferencesPropertyListener != null) {
      getPreferenceStore().removePropertyChangeListener(m_preferencesPropertyListener);
      m_preferencesPropertyListener = null;
    }
    super.stop(context);
    if (m_formDataServiceRegistration != null) {
      m_formDataServiceRegistration.unregister();
      m_formDataServiceRegistration = null;
    }
//    JavaCore.removeElementChangedListener(m_formDataMarkerSupport);
  }

  public static Display getDisplay() {
    return PlatformUI.getWorkbench().getDisplay();
  }

  public static IWorkbenchWindow getWorkbenchWindow() {
    return getDefault().getWorkbenchWindowImpl();
  }

  public IWorkbenchWindow getWorkbenchWindowImpl() {
    IWorkbenchWindow activeWorkbenchWindow = getWorkbench().getActiveWorkbenchWindow();
    if (activeWorkbenchWindow != null) {
      return activeWorkbenchWindow;
    }
    else {
      IWorkbenchWindow[] workbenchWindows = getWorkbench().getWorkbenchWindows();
      if (workbenchWindows.length > 0) {
        return workbenchWindows[0];
      }
    }
    return null;
  }

  public static Shell getShell() {
    return getDefault().getShellImpl();
  }

  private Shell getShellImpl() {
    IWorkbenchWindow workbenchWindow = getWorkbenchWindow();

    if (workbenchWindow != null) {
      return workbenchWindow.getShell();
    }
    return null;
  }

  public static IScoutExplorerPart getExplorer(boolean createIfNotOpen) {
    return getDefault().getExplorerImpl(createIfNotOpen);
  }

  private IScoutExplorerPart getExplorerImpl(boolean createIfNotOpen) {
    IWorkbenchPage activePage = getWorkbench().getActiveWorkbenchWindow().getActivePage();
    IScoutExplorerPart part = null;
    if (activePage == null) {
      logWarning("no active part found.");
      return part;
    }
    else {
      IViewPart view = activePage.findView(IScoutConstants.SCOUT_EXPLORER_VIEW);
      if (view == null && createIfNotOpen) {
        try {
          view = activePage.showView(IScoutConstants.SCOUT_EXPLORER_VIEW);
          part = (IScoutExplorerPart) view;
        }
        catch (PartInitException e) {
          logWarning("could not open view '" + IScoutConstants.SCOUT_EXPLORER_VIEW + "'.", e);
        }
      }
      else {
        part = (IScoutExplorerPart) view;
      }
    }
    return part;
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static ScoutSdkUi getDefault() {
    return plugin;
  }

  public static void log(IStatus log) {
    if (log instanceof LogStatus) {
      getDefault().logImpl((LogStatus) log);
    }
    else {
      getDefault().logImpl(new LogStatus(ScoutSdkUi.class, log.getSeverity(), log.getPlugin(), log.getMessage(), log.getException()));
    }
  }

  private void logImpl(LogStatus log) {
    if ((log.getSeverity() & m_loglevel) != 0) {
      getLog().log(log);
    }
  }

  public static void logInfo(String message) {
    logInfo(message, null);
  }

  public static void logInfo(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    getDefault().logImpl(new LogStatus(ScoutSdkUi.class, IStatus.INFO, PLUGIN_ID, message, t));
  }

  public static void logWarning(String message) {
    logWarning(message, null);
  }

  public static void logWarning(Throwable t) {
    logWarning(null, t);
  }

  public static void logWarning(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    getDefault().logImpl(new LogStatus(ScoutSdkUi.class, IStatus.WARNING, PLUGIN_ID, message, t));
  }

  public static void logError(Throwable t) {
    logError("", t);
  }

  public static void logError(String message) {
    logError(message, null);
  }

  public static void logError(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    getDefault().logImpl(new LogStatus(ScoutSdkUi.class, IStatus.ERROR, PLUGIN_ID, message, t));
  }

  protected ColorRegistry createColorRegistry() {
    // m_colorRegistry=PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
    // If we are in the UI Thread use that
    if (Display.getCurrent() != null) {
      return new ColorRegistry(Display.getCurrent());
    }
    if (PlatformUI.isWorkbenchRunning()) {
      return new ColorRegistry(PlatformUI.getWorkbench().getDisplay());
    }
    // Invalid thread access if it is not the UI Thread
    // and the workbench is not created.
    throw new SWTError(SWT.ERROR_THREAD_INVALID_ACCESS);
  }

  public ColorRegistry getColorRegistry() {
    if (m_colorRegistry == null) {
      m_colorRegistry = createColorRegistry();
      initializeColorRegistry(m_colorRegistry);
    }
    return m_colorRegistry;
  }

  protected void initializeColorRegistry(ColorRegistry colorRegistry) {
    colorRegistry.put(COLOR_INACTIVE_FOREGROUND, new RGB(178, 178, 178));
  }

  public static Color getColor(String colorId) {
    return getDefault().getColorRegistry().get(colorId);
  }

  public FontRegistry getFontRegistry() {
    if (m_fontRegistry == null) {
      m_fontRegistry = new FontRegistry(getDisplay());
      FontData[] systemFontData = getDisplay().getSystemFont().getFontData();
      FontData[] systemBoldData = new FontData[systemFontData.length];
      for (int i = 0; i < systemFontData.length; i++) {
        systemBoldData[i] = new FontData(systemFontData[i].getName(), systemFontData[i].getHeight(), SWT.BOLD);
      }
      m_fontRegistry.put(FONT_SYSTEM_BOLD, systemBoldData);
    }
    return m_fontRegistry;
  }

  public static Font getFont(String fontId) {
    return getDefault().getFontRegistry().get(fontId);
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

    reg.put(CheckboxYesDisabled, ImageDescriptor.createWithFlags(ScoutSdkUi.imageDescriptorFromPlugin(ScoutSdkUi.PLUGIN_ID, "resources/icons/" + CheckboxYes), SWT.IMAGE_DISABLE));
    reg.put(CheckboxNoDisabled, ImageDescriptor.createWithFlags(ScoutSdkUi.imageDescriptorFromPlugin(ScoutSdkUi.PLUGIN_ID, "resources/icons/" + CheckboxNo), SWT.IMAGE_DISABLE));

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

  public static void showJavaElementInEditor(IJavaElement e, boolean createNew) {
    getDefault().showJavaElementInEditorImpl(e, createNew);
  }

  private void showJavaElementInEditorImpl(IJavaElement e, boolean createNew) {
    try {
      IEditorPart editor = null;
      if (createNew) {
        editor = JavaUI.openInEditor(e);
      }
      else {
        editor = EditorUtility.isOpenInEditor(e);
        if (editor != null) {
          PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(editor);
        }
      }
      if (editor != null) {
        JavaUI.revealInEditor(editor, e);
        if (editor instanceof ITextEditor) {
          ITextEditor textEditor = (ITextEditor) editor;
          IRegion reg = textEditor.getHighlightRange();
          if (reg != null) {
            textEditor.setHighlightRange(reg.getOffset(), reg.getLength(), true);
          }
        }
      }
    }
    catch (Exception ex) {
      ScoutSdkUi.logWarning(ex);
    }
  }

  private int parseLogLevel(String loglevel) {
    int level = IStatus.INFO | IStatus.WARNING | IStatus.ERROR | IStatus.CANCEL;
    if (!StringUtility.isNullOrEmpty(loglevel)) {
      String lowerLoglevel = loglevel.toLowerCase();
      if (lowerLoglevel.equals("warning")) {
        level = IStatus.WARNING | IStatus.ERROR | IStatus.CANCEL;
      }
      else if (lowerLoglevel.equals("error")) {
        level = IStatus.ERROR | IStatus.CANCEL;
      }
      else if (lowerLoglevel.equals("cancel")) {
        level = IStatus.CANCEL;
      }
    }
    return level;
  }

//  private void contributeMenus(BundleContext context) {
//    ServiceReference serviceReference = context.getServiceReference(IMenuService.class.getName());
//    try {
//      Object service = context.getService(serviceReference);
//    }
//    finally {
//      context.ungetService(serviceReference);
//    }
//  }

  private class P_PreferenceStorePropertyListener implements IPropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent event) {
      if (FormDataAutoUpdater.PROP_FORMDATA_AUTO_UPDATE.equals(event.getProperty())) {
        Boolean autoUpdate = (Boolean) event.getNewValue();
        ScoutSdk.getDefault().setFormDataAutoUpdate(autoUpdate);
      }
    }
  }
}
