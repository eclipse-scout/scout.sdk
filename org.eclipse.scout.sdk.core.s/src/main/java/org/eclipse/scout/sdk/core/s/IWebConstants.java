/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s;

public interface IWebConstants {

  /**
   * The file extension for JavaScript files. Value is '{@code js}'.
   */
  String JS_FILE_EXTENSION = "js";

  /**
   * The file suffix for JavaScript files. Value is '{@code .js}'.
   */
  String JS_FILE_SUFFIX = '.' + JS_FILE_EXTENSION;

  /**
   * The file extension for HTML files. Value is '{@code html}'.
   */
  String HTML_FILE_EXTENSION = "html";

  /**
   * The file suffix for HTML files. Value is '{@code .html}'.
   */
  String HTML_FILE_SUFFIX = '.' + HTML_FILE_EXTENSION;

  /**
   * Scout JS Chart module name including namespace
   */
  String SCOUT_JS_CHART_MODULE_NAME = "@eclipse-scout/chart";

  /**
   * Scout JS Core module name including namespace
   */
  String SCOUT_JS_CORE_MODULE_NAME = "@eclipse-scout/core";
}
