package org.eclipse.scout.sdk.s2e.internal;

import java.io.IOException;

import org.eclipse.scout.sdk.core.util.SdkConsole;
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
      out.write(s + "\n");
    }
    catch (IOException e) {
      //nop
    }
  }

  private static final String CONSOLE_NAME = "Scout SDK";
  private static final String CONSOLE_TYPE = "org.eclipse.scout.sdk";

  private static IOConsole currentConsole(boolean autoCreate) {
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
      console = new IOConsole(CONSOLE_NAME, CONSOLE_TYPE, null) {
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
