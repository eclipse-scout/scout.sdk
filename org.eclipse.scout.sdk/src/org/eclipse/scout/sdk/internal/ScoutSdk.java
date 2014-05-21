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
package org.eclipse.scout.sdk.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.scout.commons.TuningUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.classid.ClassIdValidationJob;
import org.eclipse.scout.sdk.internal.workspace.ScoutWorkspace;
import org.eclipse.scout.sdk.internal.workspace.dto.DtoAutoUpdateManager;
import org.eclipse.scout.sdk.internal.workspace.dto.formdata.FormDataDtoUpdateHandler;
import org.eclipse.scout.sdk.internal.workspace.dto.pagedata.PageDataAutoUpdateHandler;
import org.eclipse.scout.sdk.util.log.SdkLogManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ScoutSdk extends Plugin {

  public static final String PLUGIN_ID = "org.eclipse.scout.sdk";
  public static final String LIBRARY_NATURE_ID = PLUGIN_ID + ".ScoutLibraryNature";

  private static ScoutSdk plugin;
  private static SdkLogManager logManager;

  private DtoAutoUpdateManager m_autoUpdateManager;

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);

    plugin = this;
    logManager = new SdkLogManager(this);

    logInfo("Starting SCOUT SDK Plugin.");

    // ensure the caches and emitters are initialized.
    ScoutSdkCore.getHierarchyCache();
    ScoutSdkCore.getTypeCache();
    ScoutSdkCore.getJavaResourceChangedEmitter();

    // start class id validation
    ClassIdValidationJob.install();
    ClassIdValidationJob.execute(5000);

    // DTO auto update
    m_autoUpdateManager = new DtoAutoUpdateManager();
    m_autoUpdateManager.addModelDataUpdateHandler(new FormDataDtoUpdateHandler());
    m_autoUpdateManager.addModelDataUpdateHandler(new PageDataAutoUpdateHandler());
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    TuningUtility.finishAll();
    m_autoUpdateManager.dispose();
    ClassIdValidationJob.uninstall();
    ScoutWorkspace.getInstance().dispose();
    ScoutSdkCore.getHierarchyCache().dispose();
    ScoutSdkCore.getTypeCache().dispose();
    ScoutSdkCore.getJavaResourceChangedEmitter().dispose();

    m_autoUpdateManager = null;
    logManager = null;
    plugin = null;
    super.stop(context);
  }

  public static ScoutSdk getDefault() {
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

  /**
   * @see SdkLogManager#log(int, String, Throwable)
   */
  public static void log(int level, Throwable t) {
    logManager.log(level, t);
  }

  /**
   * @see SdkLogManager#log(int, String, Throwable)
   */
  public static void log(int level, String message) {
    logManager.log(level, message);
  }

  /**
   * @see SdkLogManager#log(int, String, Throwable)
   */
  public static void log(int level, String message, Throwable t) {
    logManager.log(level, message, t);
  }

  public static void log(IStatus status) {
    logManager.log(status);
  }

  /**
   * gets the DTO auto update manager
   * 
   * @return the manager instance or null if this plug-in has not yet been started.
   */
  public DtoAutoUpdateManager getAutoUpdateManager() {
    return m_autoUpdateManager;
  }
}
