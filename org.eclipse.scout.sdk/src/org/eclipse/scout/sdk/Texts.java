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
package org.eclipse.scout.sdk;

import org.eclipse.scout.commons.nls.DynamicNls;

/**
 * This class provides the nls support.
 * Do not change any member nor field of this class anytime otherwise the
 * nls support is not anymore garanteed.
 * This calss is auto generated and is maintained by the plugins
 * Texts.nls file in the root directory of the plugin.
 *
 * @see translation.nls
 */
public class Texts extends DynamicNls {
  public static final String RESOURCE_BUNDLE_NAME = "resources.texts.Texts";//$NON-NLS-1$
  private static Texts instance = new Texts();

  protected Texts() {
    registerResourceBundle(RESOURCE_BUNDLE_NAME, Texts.class);
  }

  public static Texts getInstance() {
    return instance;
  }

  public static String get(String key, String... messageArguments) {
    return getInstance().getText(key, messageArguments);
  }

  public static String get(String key) {
    return getInstance().getText(key, new String[0]);
  }

}
