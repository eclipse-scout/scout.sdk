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
import org.eclipse.scout.sdk.core.util.SdkConsole;
import org.eclipse.scout.sdk.s2e.internal.dto.DtoDerivedResourceHandler;
import org.eclipse.scout.sdk.s2e.internal.trigger.DerivedResourceManager;
import org.eclipse.scout.sdk.s2e.log.SdkLogManager;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class S2ESdkActivator extends Plugin {

  public static final String PLUGIN_ID = "org.eclipse.scout.sdk.s2e";

  private static S2ESdkActivator plugin;

  private SdkLogManager m_logManager;
  private DerivedResourceManager m_derivedResourceManager;

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);

    plugin = this;

    //attach sdk console to workbench
    SdkConsole.spi = new WorkbenchSdkConsoleSpi();

    // log manager
    m_logManager = new SdkLogManager(this);
    m_derivedResourceManager = new DerivedResourceManager();

    // DTO auto update
    m_derivedResourceManager.addDerivedResourceHandler(new DtoDerivedResourceHandler());
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    m_derivedResourceManager.dispose();
    m_derivedResourceManager = null;

    m_logManager = null;

    plugin = null;
    super.stop(context);
  }

  public static S2ESdkActivator getDefault() {
    return plugin;
  }

  public static void logInfo(Throwable t) {
    plugin.m_logManager.logInfo(t);
    SdkConsole.println("S2ESdk: INFO:", t);
  }

  public static void logInfo(String message) {
    plugin.m_logManager.logInfo(message);
    SdkConsole.println("S2ESdk: INFO: " + message);
  }

  public static void logInfo(String message, Throwable t) {
    plugin.m_logManager.logInfo(message, t);
    SdkConsole.println("S2ESdk: INFO: " + message, t);
  }

  public static void logWarning(String message) {
    plugin.m_logManager.logWarning(message);
    SdkConsole.println("S2ESdk: WARNING: " + message);
  }

  public static void logWarning(Throwable t) {
    plugin.m_logManager.logWarning(t);
    SdkConsole.println("S2ESdk: WARNING:", t);
  }

  public static void logWarning(String message, Throwable t) {
    plugin.m_logManager.logWarning(message, t);
    SdkConsole.println("S2ESdk: WARNING: " + message, t);
  }

  public static void logError(Throwable t) {
    plugin.m_logManager.logError(t);
    SdkConsole.println("S2ESdk: ERROR:", t);
  }

  public static void logError(String message) {
    plugin.m_logManager.logError(message);
    SdkConsole.println("S2ESdk: ERROR: " + message);
  }

  public static void logError(String message, Throwable t) {
    plugin.m_logManager.logError(message, t);
    SdkConsole.println("S2ESdk: ERROR: " + message, t);
  }

  public static void log(int level, Throwable t) {
    plugin.m_logManager.log(level, t);
    SdkConsole.println("S2ESdk: LEVEL " + level + ":", t);
  }

  public static void log(int level, String message) {
    plugin.m_logManager.log(level, message);
    SdkConsole.println("S2ESdk: LEVEL " + level + ": " + message);
  }

  public static void log(int level, String message, Throwable t) {
    plugin.m_logManager.log(level, message, t);
    SdkConsole.println("S2ESdk: LEVEL " + level + ": " + message, t);
  }

  public static void log(IStatus status) {
    plugin.m_logManager.log(status);
    SdkConsole.println("S2ESdk: STATUS " + status);
  }

  public DerivedResourceManager getDerivedResourceManager() {
    return m_derivedResourceManager;
  }
}
