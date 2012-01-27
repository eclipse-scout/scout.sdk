package org.eclipse.scout.sdk.ui.menu;

import org.eclipse.ui.IStartup;

/**
 * This class is called as soon as the eclipse IDE UI is shown.<br>
 * It ensures that the scout SDK plugins get activated.<br>
 * This is important to e.g. ensure that the automatic formdata update
 * is executed even if the scout perspective (or any other scout sdk classes)
 * would never be loaded (e.g. when only working in the java perspective).
 * 
 * @author mvi
 * @since 3.8.0 24.01.2012
 */
public class ScoutSdkStartupExtension implements IStartup {

  @Override
  public void earlyStartup() {
  }
}
