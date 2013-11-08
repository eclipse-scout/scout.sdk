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
package org.eclipse.scout.sdk.help.internal.action;

import java.util.Properties;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.scout.sdk.help.internal.ScoutSdkHelpActivator;
import org.eclipse.scout.sdk.ui.action.create.ScoutProjectNewAction;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;

/**
 * @author Matthias Villiger
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
