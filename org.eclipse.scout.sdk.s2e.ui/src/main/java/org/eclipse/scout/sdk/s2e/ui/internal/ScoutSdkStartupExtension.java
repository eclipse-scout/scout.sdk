/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal;

import org.eclipse.ui.IStartup;

/**
 * This class is called as soon as the eclipse IDE UI is shown.<br>
 * It ensures that the scout SDK plug-ins get activated.<br>
 * This is important to ensure e.g. that the automatic DTO update is executed (even if no scout sdk classes are loaded).
 *
 * @since 3.8.0 2012-01-24
 */
public class ScoutSdkStartupExtension implements IStartup {

  @Override
  public void earlyStartup() {
    //noinspection ResultOfMethodCallIgnored
    S2ESdkUiActivator.getDefault();
  }
}
