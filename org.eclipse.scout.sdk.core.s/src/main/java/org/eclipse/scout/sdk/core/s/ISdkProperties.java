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
package org.eclipse.scout.sdk.core.s;

/**
 * Defaults used for scout objects
 */
public interface ISdkProperties {
  String SUFFIX_FORM_FIELD = "Field";
  String SUFFIX_BUTTON = "Button";
  String SUFFIX_TABLE_COLUMN = "Column";
  String SUFFIX_OUTLINE_PAGE = "Page";
  String SUFFIX_FORM_HANDLER = "Handler";

  String CONTEXT_PROPERTY_JAVA_PROJECT = "JavaProject";

  /**
   * The default view order.
   * <p>
   * The value of this constant is "rather large, but not <i>that</i> large". For most projects it is the biggest of all
   * orders. But it is not as big that precision is lost during calculations due to the IEEE floating point arithmetic.
   * In particular, subtracting {@link #DEFAULT_ORDER_STEP} from this value must result in a different number (which,
   * for example, would not be the case for {@link Double#MAX_VALUE}). As a rule of thumb, this number should be smaller
   * than {@link Long#MAX_VALUE}.<br>
   * Must match the default order number used in the Scout runtime. See
   * org.eclipse.scout.rt.platform.IOrdered.DEFAULT_ORDER
   */
  double DEFAULT_VIEW_ORDER = 98765432123456789d;

  /**
   * The default bean order if no @Order annotation is present (according to the Scout Runtime)
   */
  double DEFAULT_BEAN_ORDER = 5000d;
}
