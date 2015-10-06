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
package org.eclipse.scout.sdk.core.s.annotation;

import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.sugar.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;

/**
 * <h3>{@link OrderAnnotation}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class OrderAnnotation extends AbstractManagedAnnotation {

  public static final String TYPE_NAME = IScoutRuntimeTypes.Order;

  public double value() {
    return getValue("value", double.class, null);
  }

  public static double valueOf(IAnnotatable owner, boolean isBean) {
    OrderAnnotation orderAnnotation = owner.annotations().withManagedWrapper(OrderAnnotation.class).first();
    if (orderAnnotation == null) {
      if (isBean) {
        return ISdkProperties.DEFAULT_BEAN_ORDER; // default order of the scout runtime for beans
      }
      return ISdkProperties.DEFAULT_VIEW_ORDER; // default order of the scout runtime for views
    }
    return orderAnnotation.value();
  }
}
