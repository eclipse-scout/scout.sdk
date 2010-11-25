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
package org.eclipse.scout.sdk.operation.template;

import java.util.Set;
import java.util.Map.Entry;

public interface ITemplateVariableSet {
  public static final String VAR_OS = "OSGI_OS";
  public static final String VAR_WS = "OSGI_WS";
  public static final String VAR_ARCH = "OSGI_ARCH";
  public static final String VAR_PROJECT_ALIAS = "ALIAS";
  public static final String VAR_PROJECT_NAME = "GROUP";
  public static final String VAR_ROOT_PACKAGE = "ROOT_PACKAGE";
  public static final String VAR_LOCALHOST = "LOCALHOST";
  public static final String VAR_FS_ROOT = "FS_ROOT";
  public static final String VAR_CURRENT_DATE = "CURRENT_DATE";
  public static final String VAR_USER_NAME = "USER_NAME";

  public static final String VAR_BUNDLE_SWING_NAME = "BUNDLE_SWING_NAME";
  public static final String VAR_BUNDLE_SWT_NAME = "BUNDLE_SWT_NAME";
  public static final String VAR_BUNDLE_CLIENT_NAME = "BUNDLE_CLIENT_NAME";
  public static final String VAR_BUNDLE_SHARED_NAME = "BUNDLE_SHARED_NAME";
  public static final String VAR_BUNDLE_SERVER_NAME = "BUNDLE_SERVER_NAME";
  public static final String VAR_BUNDLE_CLIENT_TEST_NAME = "BUNDLE_CLIENT_TEST_NAME";
  public static final String VAR_BUNDLE_SERVER_TEST_NAME = "BUNDLE_SERVER_TEST_NAME";
  public static final String VAR_BUNDLE_PROJECTSETS_NAME = "BUNDLE_PROJECTSETS_NAME";

  String getVariable(String var);

  Set<Entry<String, String>> entrySet();

}
