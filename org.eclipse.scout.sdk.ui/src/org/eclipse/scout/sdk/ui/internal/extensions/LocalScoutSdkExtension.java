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
package org.eclipse.scout.sdk.ui.internal.extensions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.scout.sdk.ui.extensions.IScoutSdkExtension;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

public class LocalScoutSdkExtension implements IScoutSdkExtension {

  public void contributePageMenus(IMenuManager manager, IPage page) {
  }

  @Override
  public void contributePageChildren(IPage page) {
    // TODO Auto-generated method stub

  }

}
