/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s;

public interface IWebConstants {

  /**
   * The file extension for JavaScript files. Value is '{@code js}'.
   */
  String JS_FILE_EXTENSION = "js";

  /**
   * The file extension for TypeScript files. Value is '{@code ts}'.
   */
  String TS_FILE_EXTENSION = "ts";

  /**
   * The file suffix for JavaScript files. Value is '{@code .js}'.
   */
  String JS_FILE_SUFFIX = '.' + JS_FILE_EXTENSION;

  /**
   * The file suffix for TypeScript files. Value is '{@code .ts}'.
   */
  String TS_FILE_SUFFIX = '.' + TS_FILE_EXTENSION;

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
