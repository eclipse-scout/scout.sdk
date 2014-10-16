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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.classidgenerators.ClassIdGenerators;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.operation.util.IOrganizeImportService;
import org.eclipse.scout.sdk.service.IMessageBoxService;
import org.eclipse.scout.sdk.sourcebuilder.comment.IJavaElementCommentBuilderService;
import org.eclipse.scout.sdk.ui.IScoutConstants;
import org.eclipse.scout.sdk.ui.internal.service.SwtMessageBoxService;
import org.eclipse.scout.sdk.ui.services.AstFlattenerProviderService;
import org.eclipse.scout.sdk.ui.services.JavaElementCommentBuilderService;
import org.eclipse.scout.sdk.ui.services.OrganizeImportService;
import org.eclipse.scout.sdk.ui.view.outline.IScoutExplorerPart;
import org.eclipse.scout.sdk.util.log.SdkLogManager;
import org.eclipse.scout.sdk.util.method.ISimpleNameAstFlattenerProviderService;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.dto.IDtoAutoUpdateManager;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
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
public class ScoutSdkUi extends AbstractUIPlugin implements SdkIcons {
  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.scout.sdk.ui";

  // Colors
  public static final String COLOR_INACTIVE_FOREGROUND = "inactiveForeground";

  // Fonts
  public static final String FONT_SYSTEM_BOLD = "fontSystemBold";
  public static final String FONT_SYSTEM_TITLE = "fontSystemTitle";

  // Icons
  private static final String IMAGE_PATH = "resources/icons/";

  private static ScoutSdkUi plugin;
  private static SdkLogManager logManager;

  private ColorRegistry m_colorRegistry;
  private FontRegistry m_fontRegistry;
  private ServiceRegistration<IOrganizeImportService> m_organizeImportServiceRegistration;
  private ServiceRegistration<IMessageBoxService> m_messageBoxServiceRegistration;
  private IPropertyChangeListener m_preferencesPropertyListener;
  private ServiceRegistration<IJavaElementCommentBuilderService> m_javaElementCommentBuilderService;
  private ServiceRegistration<ISimpleNameAstFlattenerProviderService> m_astFlattenerProviderService;
  private IWorkbenchListener m_shutdownListener;

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
      m_organizeImportServiceRegistration = context.registerService(IOrganizeImportService.class, new OrganizeImportService(), null);
    }
    if (m_messageBoxServiceRegistration == null) {
      m_messageBoxServiceRegistration = context.registerService(IMessageBoxService.class, new SwtMessageBoxService(), null);
    }
    if (m_javaElementCommentBuilderService == null) {
      m_javaElementCommentBuilderService = context.registerService(IJavaElementCommentBuilderService.class, new JavaElementCommentBuilderService(), null);
    }
    if (m_astFlattenerProviderService == null) {
      m_astFlattenerProviderService = context.registerService(ISimpleNameAstFlattenerProviderService.class, new AstFlattenerProviderService(), null);
    }

    if (m_preferencesPropertyListener == null) {
      m_preferencesPropertyListener = new P_PreferenceStorePropertyListener();
    }
    getPreferenceStore().addPropertyChangeListener(m_preferencesPropertyListener);

    getPreferenceStore().setDefault(IDtoAutoUpdateManager.PROP_AUTO_UPDATE, true);
    ScoutSdkCore.getDtoAutoUpdateManager().setEnabled(getPreferenceStore().getBoolean(IDtoAutoUpdateManager.PROP_AUTO_UPDATE));

    getPreferenceStore().setDefault(DefaultTargetPackage.PROP_USE_LEGACY_TARGET_PACKAGE, false);
    DefaultTargetPackage.setIsPackageConfigurationEnabled(!getPreferenceStore().getBoolean(DefaultTargetPackage.PROP_USE_LEGACY_TARGET_PACKAGE));

    getPreferenceStore().setDefault(ClassIdGenerators.PROP_AUTOMATICALLY_CREATE_CLASS_ID_ANNOTATION, false);
    ClassIdGenerators.setAutomaticallyCreateClassIdAnnotation(getPreferenceStore().getBoolean(ClassIdGenerators.PROP_AUTOMATICALLY_CREATE_CLASS_ID_ANNOTATION));

    m_shutdownListener = new IWorkbenchListener() {
      @Override
      public boolean preShutdown(IWorkbench workbench, boolean forced) {
        try {
          new P_AutoUpdateOperationsShutdownJob().schedule();
        }
        catch (Exception e) {
          ScoutSdkUi.logError(e);
        }
        catch (NoClassDefFoundError er) {
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

    logManager = null;
    plugin = null;

    super.stop(context);

    if (m_organizeImportServiceRegistration != null) {
      m_organizeImportServiceRegistration.unregister();
      m_organizeImportServiceRegistration = null;
    }
    if (m_javaElementCommentBuilderService != null) {
      m_javaElementCommentBuilderService.unregister();
      m_javaElementCommentBuilderService = null;
    }
    if (m_messageBoxServiceRegistration != null) {
      m_messageBoxServiceRegistration.unregister();
      m_messageBoxServiceRegistration = null;
    }
    if (m_astFlattenerProviderService != null) {
      m_astFlattenerProviderService.unregister();
      m_astFlattenerProviderService = null;
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
    if (activePage != null) {
      IViewPart view = activePage.findView(IScoutConstants.SCOUT_EXPLORER_VIEW);
      if (view == null && createIfNotOpen) {
        try {
          view = activePage.showView(IScoutConstants.SCOUT_EXPLORER_VIEW);
          if (view instanceof IScoutExplorerPart) {
            part = (IScoutExplorerPart) view;
          }
        }
        catch (PartInitException e) {
          logWarning("could not open view '" + IScoutConstants.SCOUT_EXPLORER_VIEW + "'.", e);
        }
      }
      else if (view instanceof IScoutExplorerPart) {
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
    // If we are in the UI Thread use that
    if (Display.getCurrent() != null) {
      return new ColorRegistry(Display.getCurrent());
    }
    if (PlatformUI.isWorkbenchRunning()) {
      return new ColorRegistry(PlatformUI.getWorkbench().getDisplay());
    }
    // Invalid thread access if it is not the UI Thread and the workbench is not created.
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

      // bold font
      FontData[] systemBoldData = new FontData[systemFontData.length];
      for (int i = 0; i < systemFontData.length; i++) {
        systemBoldData[i] = new FontData(systemFontData[i].getName(), systemFontData[i].getHeight(), SWT.BOLD);
      }
      m_fontRegistry.put(FONT_SYSTEM_BOLD, systemBoldData);

      // title font
      FontData[] systemTitleData = new FontData[systemFontData.length];
      for (int i = 0; i < systemFontData.length; i++) {
        systemTitleData[i] = new FontData(systemFontData[i].getName(), 12, SWT.NORMAL);
      }
      m_fontRegistry.put(FONT_SYSTEM_TITLE, systemTitleData);
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
    reg.put(WebDisabled, ImageDescriptor.createWithFlags(ScoutSdkUi.imageDescriptorFromPlugin(ScoutSdkUi.PLUGIN_ID, "resources/icons/" + Web), SWT.IMAGE_DISABLE));
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
      switch (element.getElementType()) {
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

  @SuppressWarnings("restriction")
  private void showJavaElementInEditorImpl(IJavaElement e, boolean createNew) {
    if (!TypeUtility.exists(e)) {
      return;
    }

    try {
      IEditorPart editor = null;
      if (createNew) {
        editor = JavaUI.openInEditor(e);
      }
      else {
        editor = org.eclipse.jdt.internal.ui.javaeditor.EditorUtility.isOpenInEditor(e);
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
      if (IDtoAutoUpdateManager.PROP_AUTO_UPDATE.equals(event.getProperty())) {
        Boolean autoUpdate = (Boolean) event.getNewValue();
        if (autoUpdate != null) {
          ScoutSdkCore.getDtoAutoUpdateManager().setEnabled(autoUpdate.booleanValue());
        }
      }
      else if (DefaultTargetPackage.PROP_USE_LEGACY_TARGET_PACKAGE.equals(event.getProperty())) {
        Boolean useLegacy = (Boolean) event.getNewValue();
        if (useLegacy != null) {
          DefaultTargetPackage.setIsPackageConfigurationEnabled(!useLegacy.booleanValue());
        }
      }
      else if (ClassIdGenerators.PROP_AUTOMATICALLY_CREATE_CLASS_ID_ANNOTATION.equals(event.getProperty())) {
        Boolean automaticallyCreate = (Boolean) event.getNewValue();
        if (automaticallyCreate != null) {
          ClassIdGenerators.setAutomaticallyCreateClassIdAnnotation(automaticallyCreate.booleanValue());
        }
      }
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
    @SuppressWarnings("restriction")
    protected IStatus run(IProgressMonitor monitor) {
      Job[] dtoUpdateJobs = null;
      while (!monitor.isCanceled()) {
        dtoUpdateJobs = getJobManager().find(org.eclipse.scout.sdk.internal.workspace.dto.DtoAutoUpdateManager.AUTO_UPDATE_JOB_FAMILY);
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
}
