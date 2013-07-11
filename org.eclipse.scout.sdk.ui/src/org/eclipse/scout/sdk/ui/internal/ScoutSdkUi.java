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
package org.eclipse.scout.sdk.ui.internal;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.scout.sdk.IMessageBoxService;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.data.AutoUpdateManager;
import org.eclipse.scout.sdk.operation.util.IOrganizeImportService;
import org.eclipse.scout.sdk.ui.IScoutConstants;
import org.eclipse.scout.sdk.ui.services.OrganizeImportService;
import org.eclipse.scout.sdk.ui.view.outline.IScoutExplorerPart;
import org.eclipse.scout.sdk.util.log.SdkLogManager;
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
  private static SdkLogManager logManager;

  public static final String PROPERTY_RELEASE_NOTES = PLUGIN_ID + ".releaseNotes";

  private static final String IMAGE_PATH = "resources/icons/";

  // COLORS
  public static final String COLOR_INACTIVE_FOREGROUND = "inactiveForeground";

  // FONTS
  public static final String FONT_SYSTEM_BOLD = "fontSystemBold";

  private ColorRegistry m_colorRegistry;
  private FontRegistry m_fontRegistry;
  private ServiceRegistration m_organizeImportServiceRegistration;
  private ServiceRegistration m_messageBoxServiceRegistration;
  private IPropertyChangeListener m_preferencesPropertyListener;

  /**
   * The constructor
   */
  public ScoutSdkUi() {
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    logManager = new SdkLogManager(this);

    if (m_organizeImportServiceRegistration == null) {
      m_organizeImportServiceRegistration = context.registerService(IOrganizeImportService.class.getName(), new OrganizeImportService(), null);
    }
    if (m_messageBoxServiceRegistration == null) {
      m_messageBoxServiceRegistration = context.registerService(IMessageBoxService.class.getName(), new SwtMessageBoxService(), null);
    }

    if (m_preferencesPropertyListener == null) {
      m_preferencesPropertyListener = new P_PreferenceStorePropertyListener();
    }
    getPreferenceStore().addPropertyChangeListener(m_preferencesPropertyListener);

    getPreferenceStore().setDefault(AutoUpdateManager.PROP_AUTO_UPDATE, true);
    ScoutSdk.getDefault().setAutoUpdateEnabled(getPreferenceStore().getBoolean(AutoUpdateManager.PROP_AUTO_UPDATE));

    getPreferenceStore().setDefault(DefaultTargetPackage.PROP_USE_LEGACY_TARGET_PACKAGE, false);
    DefaultTargetPackage.setIsPackageConfigurationEnabled(!getPreferenceStore().getBoolean(DefaultTargetPackage.PROP_USE_LEGACY_TARGET_PACKAGE));
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    logManager = null;
    plugin = null;
    if (m_preferencesPropertyListener != null) {
      getPreferenceStore().removePropertyChangeListener(m_preferencesPropertyListener);
      m_preferencesPropertyListener = null;
    }
    super.stop(context);
    if (m_organizeImportServiceRegistration != null) {
      m_organizeImportServiceRegistration.unregister();
      m_organizeImportServiceRegistration = null;
    }
    if (m_messageBoxServiceRegistration != null) {
      m_messageBoxServiceRegistration.unregister();
      m_messageBoxServiceRegistration = null;
    }
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
   * Returns the image for the given descriptor.
   */
  public static Image getImage(ImageDescriptor imageDescriptor) {
    return getDefault().getImageImpl(imageDescriptor);
  }

  private Image getImageImpl(ImageDescriptor imageDescriptor) {
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
   *          the file name (with or without extension) located under resources/icons
   * @return the cached image
   */
  public static ImageDescriptor getImageDescriptor(String name) {
    return getDefault().getImageDescriptorImpl(name);
  }

  /**
   * @param imageName
   *          the file name (with or without extension) of the base image located under resources/icons
   * @param decorationImageName
   *          the file name (with or without extension) of the decoration image located under resources/icons
   * @param quadrant
   *          specifies where on the base image the decoration should be placed (one of {@link IDecoration#TOP_LEFT},
   *          {@link IDecoration#TOP_RIGHT}, {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT} or
   *          {@link IDecoration#UNDERLAY})
   * @return
   */
  public static ImageDescriptor getImageDescriptor(String imageName, String decorationImageName, int quadrant) {
    ImageDescriptor baseIcon = getImageDescriptor(imageName);
    return getImageDescriptor(baseIcon, decorationImageName, quadrant);
  }

  /**
   * @param baseIcon
   *          base icon on which the decoration should be placed.
   * @param decorationImageName
   *          the file name (with or without extension) of the decoration image located under resources/icons
   * @param quadrant
   *          specifies where on the base image the decoration should be placed (one of {@link IDecoration#TOP_LEFT},
   *          {@link IDecoration#TOP_RIGHT}, {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT} or
   *          {@link IDecoration#UNDERLAY})
   * @return
   */
  public static ImageDescriptor getImageDescriptor(ImageDescriptor baseIcon, String decorationImageName, int quadrant) {
    // get the base image
    Image baseImage = ScoutSdkUi.getImage(baseIcon);

    // get the decoration image
    ImageDescriptor decorationIcon = ScoutSdkUi.getImageDescriptor(decorationImageName);

    // combine
    ImageDescriptor decoratedIcon = new DecorationOverlayIcon(baseImage, decorationIcon, quadrant);
    return decoratedIcon;
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

  public static Image getImage(IJavaElement element) {
    Image img = null;
    try {
      switch (((IJavaElement) element).getElementType()) {
        case IJavaElement.TYPE:
          if (((IType) element).isInterface()) {
            img = getImage(Interface);
          }
          else {
            img = getImage(Class);
          }
          break;
        case IJavaElement.METHOD:
          img = getImage(Public);
          break;
        case IJavaElement.FIELD:
          img = getImage(FieldPrivate);
          break;
        default:
          img = getImage(Default);
          break;
      }
    }
    catch (JavaModelException e) {
      logWarning(e);
    }
    return img;
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
      logWarning(ex);
    }
  }

  private class P_PreferenceStorePropertyListener implements IPropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent event) {
      if (AutoUpdateManager.PROP_AUTO_UPDATE.equals(event.getProperty())) {
        Boolean autoUpdate = (Boolean) event.getNewValue();
        ScoutSdk.getDefault().setAutoUpdateEnabled(autoUpdate);
      }
      else if (DefaultTargetPackage.PROP_USE_LEGACY_TARGET_PACKAGE.equals(event.getProperty())) {
        Boolean useLegacy = (Boolean) event.getNewValue();
        if (useLegacy != null) {
          DefaultTargetPackage.setIsPackageConfigurationEnabled(!useLegacy.booleanValue());
        }
      }
    }
  }
}
