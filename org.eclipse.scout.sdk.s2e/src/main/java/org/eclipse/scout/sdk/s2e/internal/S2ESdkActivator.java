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
package org.eclipse.scout.sdk.s2e.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.scout.sdk.s2e.internal.dto.DtoAutoUpdateManager;
import org.eclipse.scout.sdk.s2e.internal.dto.FormDataDtoUpdateHandler;
import org.eclipse.scout.sdk.s2e.internal.dto.PageDataAutoUpdateHandler;
import org.eclipse.scout.sdk.s2e.internal.dto.RowDataAutoUpdateHandler;
import org.eclipse.scout.sdk.s2e.log.SdkLogManager;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class S2ESdkActivator extends Plugin {

  public static final String PLUGIN_ID = "org.eclipse.scout.sdk.s2e";

  private static S2ESdkActivator plugin;

  private SdkLogManager m_logManager;
  private DtoAutoUpdateManager m_autoUpdateManager;

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);

    plugin = this;

    // log manager
    m_logManager = new SdkLogManager(this);

    // DTO auto update
    m_autoUpdateManager = new DtoAutoUpdateManager();
    m_autoUpdateManager.addModelDataUpdateHandler(new FormDataDtoUpdateHandler());
    m_autoUpdateManager.addModelDataUpdateHandler(new PageDataAutoUpdateHandler());
    m_autoUpdateManager.addModelDataUpdateHandler(new RowDataAutoUpdateHandler());
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    m_autoUpdateManager.dispose();
    m_autoUpdateManager = null;

    m_logManager = null;
    plugin = null;

    super.stop(context);
  }

  public static S2ESdkActivator getDefault() {
    return plugin;
  }

  public static void logInfo(Throwable t) {
    plugin.m_logManager.logInfo(t);
  }

  public static void logInfo(String message) {
    plugin.m_logManager.logInfo(message);
  }

  public static void logInfo(String message, Throwable t) {
    plugin.m_logManager.logInfo(message, t);
  }

  public static void logWarning(String message) {
    plugin.m_logManager.logWarning(message);
  }

  public static void logWarning(Throwable t) {
    plugin.m_logManager.logWarning(t);
  }

  public static void logWarning(String message, Throwable t) {
    plugin.m_logManager.logWarning(message, t);
  }

  public static void logError(Throwable t) {
    plugin.m_logManager.logError(t);
  }

  public static void logError(String message) {
    plugin.m_logManager.logError(message);
  }

  public static void logError(String message, Throwable t) {
    plugin.m_logManager.logError(message, t);
  }

  public static void log(int level, Throwable t) {
    plugin.m_logManager.log(level, t);
  }

  public static void log(int level, String message) {
    plugin.m_logManager.log(level, message);
  }

  public static void log(int level, String message, Throwable t) {
    plugin.m_logManager.log(level, message, t);
  }

  public static void log(IStatus status) {
    plugin.m_logManager.log(status);
  }

  public DtoAutoUpdateManager getAutoUpdateManager() {
    return m_autoUpdateManager;
  }
}
