/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore store = JaxWsSdk.getDefault().getPreferenceStore();
    store.setDefault(IPreferenceConstants.PREF_STUB_GENERATION_DEBUG_MODE, false);
    store.setDefault(IPreferenceConstants.PREF_STUB_GENERATION_KEEP_LAUNCH_CONFIG, false);
  }
}
