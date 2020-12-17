/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal;

import static java.lang.System.lineSeparator;

import java.io.IOException;
import java.util.logging.Level;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.core.log.ISdkConsoleSpi;
import org.eclipse.scout.sdk.core.log.LogMessage;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

public class WorkbenchSdkConsoleSpi implements ISdkConsoleSpi {

  @Override
  public void clear() {
    var console = currentConsole(false);
    if (console != null) {
      console.clearConsole();
    }
  }

  @Override
  @SuppressWarnings({"squid:S1166", "squid:S1148", "UseOfSystemOutOrSystemErr", "CallToPrintStackTrace"})
  public void println(LogMessage message) {
    try (var out = currentConsole(true).newOutputStream()) {
      if (Level.SEVERE.equals(message.severity())) {
        out.setActivateOnWrite(true);
        setConsoleColorRed(out);
      }

      out.write(message.all());
      out.write(lineSeparator());
      out.flush();
    }
    catch (IOException e) {
      System.err.println("Unable to write to console:");
      System.err.println(message.all());
      e.printStackTrace();
    }

    // dev mode: also log to Eclipse log
    if (Platform.isRunning() && (Platform.inDebugMode() || Platform.inDevelopmentMode())) {
      logToPluginLog(message);
    }
  }

  protected static void setConsoleColorRed(IOConsoleOutputStream out) {
    if (!PlatformUI.isWorkbenchRunning()) {
      return;
    }
    var workbench = PlatformUI.getWorkbench();
    if (workbench == null) {
      return;
    }
    var display = workbench.getDisplay();
    if (display == null) {
      return;
    }
    display.syncExec(() -> out.setColor(display.getSystemColor(SWT.COLOR_RED)));
  }

  protected static void logToPluginLog(LogMessage message) {
    var activator = S2ESdkActivator.getDefault();
    if (activator == null) {
      return;
    }
    IStatus status = new Status(julToEclipseSeverity(message.severity()), S2ESdkActivator.PLUGIN_ID, message.text(), message.firstThrowable().orElse(null));
    activator.getLog().log(status);
  }

  private static int julToEclipseSeverity(Level l) {
    if (l.intValue() == Level.SEVERE.intValue()) {
      return IStatus.ERROR;
    }
    if (l.intValue() == Level.WARNING.intValue()) {
      return IStatus.WARNING;
    }
    return IStatus.INFO;
  }

  public static final String CONSOLE_NAME = "Scout SDK";
  public static final String CONSOLE_TYPE = "org.eclipse.scout.sdk";

  public static IOConsole currentConsole(boolean autoCreate) {
    var mgr = ConsolePlugin.getDefault().getConsoleManager();
    IOConsole console = null;
    for (var c : mgr.getConsoles()) {
      if (!CONSOLE_NAME.equals(c.getName())) {
        continue;
      }
      if (!CONSOLE_TYPE.equals(c.getType())) {
        continue;
      }
      if (console == null) {
        console = (IOConsole) c;
        continue;
      }
      mgr.removeConsoles(new IConsole[]{c});
    }

    if (console == null && autoCreate) {
      console = new IOConsole(CONSOLE_NAME, CONSOLE_TYPE, S2ESdkUiActivator.getImageDescriptor(ISdkIcons.EclipseScout)) {
        @Override
        public void clearConsole() {
          super.clearConsole();
          //remove the console
          ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[]{this});
        }
      };
      mgr.addConsoles(new IConsole[]{console});
      mgr.showConsoleView(console);
    }
    return console;
  }

}
