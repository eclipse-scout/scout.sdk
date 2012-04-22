/**
 *
 */
package org.eclipse.scout.sdk.help.internal.action;

import java.util.Properties;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.scout.sdk.help.internal.ScoutSdkHelpActivator;
import org.eclipse.scout.sdk.ui.action.create.ScoutProjectNewAction;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;

/**
 * @author mvi
 */
public class CreateNewScoutProjectHandler implements IIntroAction {

  @Override
  public void run(IIntroSite site, Properties params) {
    try {
      ScoutProjectNewAction action = new ScoutProjectNewAction();
      action.execute(site.getShell(), null, null);
    }
    catch (ExecutionException e) {
      ScoutSdkHelpActivator.getInstance().getLog().log(new ScoutStatus(e));
    }
  }
}
