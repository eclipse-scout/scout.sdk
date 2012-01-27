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

import org.eclipse.core.runtime.Plugin;
import org.eclipse.scout.commons.TuningUtility;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataAutoUpdater;
import org.eclipse.scout.sdk.util.log.SdkLogManager;
import org.eclipse.scout.sdk.util.typecache.TypeCacheAccessor;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ScoutSdk extends Plugin {

  public static final String PLUGIN_ID = "org.eclipse.scout.sdk";
  public static final String NATURE_ID = PLUGIN_ID + ".ScoutNature";

  private static ScoutSdk plugin;
  private static SdkLogManager logManager;

  private FormDataAutoUpdater m_formDataUpdateSupport;

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    logManager = new SdkLogManager(this);

    // ensure the caches and emitter are initialized.
    TypeCacheAccessor.getHierarchyCache();
    TypeCacheAccessor.getTypeCache();
    TypeCacheAccessor.getJavaResourceChangedEmitter();

    logInfo("Starting SCOUT SDK Plugin.");

    m_formDataUpdateSupport = new FormDataAutoUpdater();
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    TuningUtility.finishAll();
    m_formDataUpdateSupport.dispose();
    TypeCacheAccessor.getHierarchyCache().dispose();
    TypeCacheAccessor.getTypeCache().dispose();
    TypeCacheAccessor.getJavaResourceChangedEmitter().dispose();

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

  public void setFormDataAutoUpdate(boolean autoUpdate) {
    m_formDataUpdateSupport.setEnabled(autoUpdate);
  }

  public boolean isFormDataAutoUpdate() {
    return m_formDataUpdateSupport.isEnabled();
  }
}
