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
package org.eclipse.scout.sdk.s2e.ui.internal;

import java.io.IOException;
import java.util.logging.Level;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.core.log.ISdkConsoleSpi;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

public class WorkbenchSdkConsoleSpi implements ISdkConsoleSpi {

  @Override
  public void clear() {
    IOConsole console = currentConsole(false);
    if (console != null) {
      console.clearConsole();
    }
  }

  @Override
  @SuppressWarnings({"squid:S1166", "squid:S1148"})
  public void println(Level level, String s, Throwable... exceptions) {
    try (IOConsoleOutputStream out = currentConsole(true).newOutputStream()) {
      if (Level.SEVERE.equals(level)) {
        out.setActivateOnWrite(true);
        if (PlatformUI.isWorkbenchRunning()) {
          IWorkbench workbench = PlatformUI.getWorkbench();
          if (workbench != null) {
            Display display = workbench.getDisplay();
            display.syncExec(() -> out.setColor(display.getSystemColor(SWT.COLOR_RED)));
          }
        }
      }

      out.write(s);
      if (exceptions == null || exceptions.length < 1) {
        out.write('\n');
      }
      else {
        for (Throwable t : exceptions) {
          if (t != null) {
            String trace = Strings.fromThrowable(t);
            out.write(trace);
          }
        }
      }
      out.flush();
    }
    catch (IOException e) {
      System.err.println("Unable to write to console:");
      System.err.println(s);
      e.printStackTrace();
    }

    // dev mode: also log to Eclipse log
    if (Platform.isRunning() && (Platform.inDebugMode() || Platform.inDevelopmentMode())) {
      S2ESdkActivator activator = S2ESdkActivator.getDefault();
      if (activator == null) {
        return;
      }
      Throwable t = null;
      if (exceptions != null && exceptions.length > 0) {
        t = exceptions[0];
      }
      activator.getLog().log(new Status(julToEclipseSeverity(level), S2ESdkActivator.PLUGIN_ID, s, t));
    }
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
    IConsoleManager mgr = ConsolePlugin.getDefault().getConsoleManager();
    IOConsole console = null;
    for (IConsole c : mgr.getConsoles()) {
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
