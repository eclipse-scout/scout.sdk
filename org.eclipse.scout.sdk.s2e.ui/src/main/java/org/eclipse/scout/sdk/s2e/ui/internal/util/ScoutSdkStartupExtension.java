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
package org.eclipse.scout.sdk.s2e.ui.internal.util;

import org.eclipse.ui.IStartup;

/**
 * This class is called as soon as the eclipse IDE UI is shown.<br>
 * It ensures that the scout SDK plugins get activated.<br>
 * This is important to e.g. ensure that the automatic formdata update
 * is executed even if the scout perspective (or any other scout sdk classes)
 * would never be loaded (e.g. when only working in the java perspective).
 *
 * @author Matthias Villiger
 * @since 3.8.0 24.01.2012
 */
public class ScoutSdkStartupExtension implements IStartup {

  @Override
  public void earlyStartup() {
  }
}
