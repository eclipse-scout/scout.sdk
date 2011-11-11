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
package org.eclipse.scout.sdk.ws.jaxws;

/**
 * This class provides the nls support.
 * Do not change any member nor field of this class anytime otherwise the
 * nls support is not anymore garanteed.
 * This calss is auto generated and is maintained by the plugins
 * translations.nls file in the root directory of the plugin.
 * 
 * @see translations.nls
 */
public class Texts extends org.eclipse.scout.sdk.Texts {
  public static final String RESOURCE_BUNDLE_NAME = "resources.texts.Texts";//$NON-NLS-1$
  private static Texts instance = new Texts();

  public static Texts getInstance() {
    return instance;
  }

  public static String get(String key, String... messageArguments) {
    return instance.getText(key, messageArguments);
  }

  public static String get(String key) {
    return instance.getText(key, new String[0]);
  }

  protected Texts() {
    registerResourceBundle(RESOURCE_BUNDLE_NAME, Texts.class);
  }
}
