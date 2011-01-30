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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.scout.sdk.LogStatus;
import org.eclipse.scout.sdk.ui.internal.ImageRegistry;
import org.eclipse.scout.sdk.ui.view.outline.IScoutExplorerPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Color;
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

/**
 * The activator class controls the plug-in life cycle
 */
public class ScoutSdkUi extends AbstractUIPlugin implements SdkIcons {
  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.scout.sdk.ui";

  // The shared instance
  private static ScoutSdkUi plugin;

  public static final String PROPERTY_RELEASE_NOTES = PLUGIN_ID + ".releaseNotes";
  private static String PROPERTY_PLUGIN_VERSION = "pluginVersion";

  private static final String IMAGE_PATH = "resources/icons/";

  // COLORS
  public static final String COLOR_INACTIVE_FOREGROUND = "inactiveForeground";

  // IMAGES
//  public static final String IMG_TOOL_ADD = "tool/add.gif";
//  public static final String IMG_TOOL_DELETE = "tool/remove.gif";
//  public static final String IMG_TOOL_DOWN = "tool/down.gif";
//  public static final String IMG_TOOL_LOADING = "tool/tool_loading.gif";
//  public static final String IMG_TOOL_PROGRESS = "tool/progress_monitor.gif";
//  public static final String IMG_TOOL_SYNCHONIZE = "tool/synced.gif";
//
//  public static final String IMG_DEFAULT = "build_var_obj.gif";
//  public static final String IMG_TYPE_SEPARATOR = "type_separator.gif";
//  public static final String IMG_FOLDER = "folder";
//  public static final String IMG_FILE = "file";
//  public static final String IMG_SOURCE_FILE = "sourceEditor.gif";
//  public static final String IMG_CLASS = "obj_class";
//  public static final String IMG_INTERFACE = "obj_interface";
//  public static final String IMG_PUBLIC = "obj_public";
//  public static final String IMG_FIELD_PRIVATE = "field_private";
//  public static final String IMG_FIELD_PROTECTED = "field_protected";
//  public static final String IMG_FIELD_PUBLIC = "field_public";
//  public static final String IMG_TOOL_RENAME = "field_rename";
//  public static final String IMG_DIALOG_WIZARD = "dialog_wizard";
//
//  public static final String IMG_CASE_PROJECT = "cake.png";
//  public static final String IMG_UI_BUNDLE = "plugin.png";
//  public static final String IMG_CLIENT = "plugin.png";
//  public static final String IMG_SHARED = "plugin.png";
//  public static final String IMG_SERVER = "plugin.png";
//  public static final String IMG_ICONS = "javaassist_co.gif";
//  public static final String IMG_FORM = "window_dialog.png";
//  public static final String IMG_FORM_HANDLER = IMG_DEFAULT;
//  public static final String IMG_PERMISSION = "key.png";
//  public static final String IMG_PERMISSION_ADD = "key_add.png";
//  public static final String IMG_PERMISSION_DELETE = "key_delete.png";
//  public static final String IMG_CODE_TYPE = IMG_DEFAULT;
//
//  public static final String IMG_FIELD_GROUP_BOX = "bricks.png";
//  public static final String IMG_FIELD_DEFAULT = "brick.png";
//  public static final String IMG_FIELD_DEFAULT_ADD = "brick_add.png";
//  public static final String IMG_FIELD_DEFAULT_DELETE = "brick_delete.png";
//  public static final String IMG_RANGEBOX_FROM = "navigation_back.gif";
//  public static final String IMG_RANGEBOX_TO = "navigation_forward.gif";
//  public static final String IMG_OUTLINE = "layout_co.gif";
//
//  public static final String IMG_SMART_FIELD = "magnifier.png";
//  public static final String IMG_DATE_FIELD = "date_time.png";
//  public static final String IMG_TABLE_FIELD = "tablenode.gif";
//  public static final String IMG_COLUMN = "add_column.gif";
//  public static final String IMG_SERVICE = "connect_alias.gif";
//  public static final String IMG_SERVICE_DISABLED = "serviceDisabled";
//  public static final String IMG_SERVICE_METHOD = IMG_DEFAULT;
//  public static final String IMG_LAUNCHER_SWING = "swingLauncher.gif";
//  public static final String IMG_LAUNCHER_SWT = "swtLauncher.gif";
//  public static final String IMG_LAUNCHER_SERVER = "serverLauncher.gif";
//
//  public static final String IMG_WARNING = "warning.gif";
//  public static final String IMG_ERROR = "error.gif";
//  public static final String IMG_INFO = "info.gif";
//  public static final String IMG_FIND = "search.gif";
//  public static final String IMG_TEXT_CODE = "text.gif";
//  public static final String IMG_CHOOSE = "magnifier.png";
//  public static final String IMG_TABLE = "tablenode.gif";
//  public static final String IMG_TREE = "tree_mode.gif";
//  public static final String IMG_CONTENT_ASSIST = "content_assist_cue.gif";
//  public static final String IMG_PAGE_WITH_TABLE = "copy_r_co.gif";
//  public static final String IMG_PAGE_WITH_NODES = IMG_PAGE_WITH_TABLE;
//
//  public static final String IMG_CHECKBOX_YES = "output_yes.gif";
//  public static final String IMG_CHECKBOX_NO = "output_no.gif";
//  public static final String IMG_CHECKBOX_NO_DISABLED = "output_no_disabled";
//  public static final String IMG_CHECKBOX_YES_DISABLED = "output_yes_disabled";
//
//  public static final String IMG_KEYGROUP = "keygroups_obj.gif";
//  public static final String IMG_TOOL_EDIT = "tool/usereditor.gif";
//  public static final String IMG_PRODUCT_RUN = "tool/run_exc.gif";
//  public static final String IMG_PRODUCT_DEBUG = "tool/debug_exc.gif";
//  public static final String IMG_PRODUCT_STOP = "tool/stop.gif";
//  public static final String IMG_PACKAGE = "package_obj.gif";
//  public static final String IMG_PACKAGE_OBJECT_DISABLED = "package_objDisabled";

  private ColorRegistry m_colorRegistry;

  private Object m_initializeLock = new Object();

  /**
   * The constructor
   */
  public ScoutSdkUi() {
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
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
    super.stop(context);
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
      logImpl((LogStatus) log);
    }
    else {
      logImpl(new LogStatus(ScoutSdkUi.class, log.getSeverity(), log.getPlugin(), log.getMessage(), log.getException()));
    }
  }

  private static void logImpl(LogStatus log) {
    getDefault().getLog().log(log);
  }

  public static void logInfo(String message) {
    logInfo(message, null);
  }

  public static void logInfo(String message, Throwable t) {
    if (message == null) {
      message = "";
    }
    logImpl(new LogStatus(ScoutSdkUi.class, IStatus.INFO, PLUGIN_ID, message, t));
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
    logImpl(new LogStatus(ScoutSdkUi.class, IStatus.WARNING, PLUGIN_ID, message, t));
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
    logImpl(new LogStatus(ScoutSdkUi.class, IStatus.ERROR, PLUGIN_ID, message, t));
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

}
