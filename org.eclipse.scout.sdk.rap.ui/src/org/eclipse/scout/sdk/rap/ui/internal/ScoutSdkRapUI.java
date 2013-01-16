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
package org.eclipse.scout.sdk.rap.ui.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.rap.target.RapTargetVariable;
import org.eclipse.scout.sdk.rap.target.RapTargetVariableListenerAdapter;
import org.eclipse.scout.sdk.rap.ui.SdkRapIcons;
import org.eclipse.scout.sdk.rap.ui.internal.wizard.target.RapTargetNewWizard;
import org.eclipse.scout.sdk.ui.internal.ImageRegistry;
import org.eclipse.scout.sdk.util.log.SdkLogManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class ScoutSdkRapUI extends AbstractUIPlugin implements SdkRapIcons {
  public static final String PLUGIN_ID = "org.eclipse.scout.sdk.rap.ui";

  private static final String RAP_TARGET_VARIABLE_EMPTY_MSG = "scoutRapTargetVarEmptyMsg";
  private static final String IMAGE_PATH = "resources/icons/";

  private static ScoutSdkRapUI plugin;
  private static SdkLogManager logManager;
  private static P_ScoutTargetVariableListener listener;

  @Override
  public void start(BundleContext bundleContext) throws Exception {
    super.start(bundleContext);
    plugin = this;
    logManager = new SdkLogManager(this);
    listener = new P_ScoutTargetVariableListener();
    RapTargetVariable.get().addListener(listener);
  }

  @Override
  public void stop(BundleContext bundleContext) throws Exception {
    super.stop(bundleContext);
    RapTargetVariable.get().removeListener(listener);
    listener = null;
    logManager = null;
    plugin = null;
  }

  public static ScoutSdkRapUI getDefault() {
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

  private class P_ScoutTargetVariableListener extends RapTargetVariableListenerAdapter {
    @Override
    public void emptyVariableInUse(final IFile targetFile) {
      String doNotShowAgainString = getPreferenceStore().getString(RAP_TARGET_VARIABLE_EMPTY_MSG);
      boolean doNotShowAgain = MessageDialogWithToggle.ALWAYS.equals(doNotShowAgainString);

      if (!doNotShowAgain) {
        final Display display = getDisplay();
        display.syncExec(new Runnable() {
          @Override
          public void run() {
            MessageDialogWithToggle msgbox = MessageDialogWithToggle.openYesNoQuestion(display.getActiveShell(), Texts.get("NoRAPTargetLocationDefined"),
                Texts.get("EmptyRapTargetVarFoundMsg", RapTargetVariable.RAP_TARGET_KEY, targetFile.getLocation().toOSString()), Texts.get("DoNotShowAgain"),
                false, getPreferenceStore(), RAP_TARGET_VARIABLE_EMPTY_MSG);
            if (msgbox.getReturnCode() == IDialogConstants.YES_ID) {
              WizardDialog dialog = new WizardDialog(display.getActiveShell(), new RapTargetNewWizard());
              dialog.open();
            }
          }
        });
      }
    }
  }

  private static Display getDisplay() {
    Display d = Display.getDefault();
    if (d == null) {
      d = PlatformUI.getWorkbench().getDisplay();
    }
    return d;
  }
}
