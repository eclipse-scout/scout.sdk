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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.core.util.SdkConsole;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

public class WorkbenchSdkConsoleSpi implements SdkConsole.SdkConsoleSpi {

  @Override
  public void clear() {
    IOConsole console = currentConsole(false);
    if (console != null) {
      console.clearConsole();
    }
  }

  @Override
  public void println(String s) {
    try (IOConsoleOutputStream out = currentConsole(true).newOutputStream()) {
      out.write(s);
      out.write('\n');
    }
    catch (IOException e) {
      //nop
    }

    // dev mode: also log to Eclipse log
    if (Platform.isRunning() && (Platform.inDebugMode() || Platform.inDevelopmentMode())) {
      S2ESdkActivator activator = S2ESdkActivator.getDefault();
      if (activator != null) {
        activator.getLog().log(new Status(parseSeverity(s), S2ESdkActivator.PLUGIN_ID, s));
      }
    }
  }

  private static int parseSeverity(String s) {
    if (StringUtils.length(s) < 1) {
      return IStatus.OK;
    }

    if (s.charAt(0) != '[') {
      return IStatus.OK;
    }

    int endPos = s.indexOf(']');
    if (endPos <= 0) {
      return IStatus.OK;
    }

    String level = s.substring(1, endPos);
    try {
      Level l = Level.parse(level);
      int num = l.intValue();
      if (num == Level.SEVERE.intValue()) {
        return IStatus.ERROR;
      }
      else if (num == Level.WARNING.intValue()) {
        return IStatus.WARNING;
      }
      else {
        return IStatus.INFO;
      }
    }
    catch (IllegalArgumentException e) {
      // cannot parse
      return IStatus.OK;
    }
  }

  private static final String CONSOLE_NAME = "Scout SDK";
  private static final String CONSOLE_TYPE = "org.eclipse.scout.sdk";

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
