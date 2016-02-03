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
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.sugar.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;

/**
 * <h3>{@link DataAnnotation}</h3> Describes a {@link IScoutRuntimeTypes#Data} or {@link IScoutRuntimeTypes#PageData}
 * annotation.
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class DataAnnotation extends AbstractManagedAnnotation {

  public static final String VALUE_ELEMENT_NAME = "value";
  public static final String TYPE_NAME = IScoutRuntimeTypes.Data;

  public IType value() {
    return getValue(VALUE_ELEMENT_NAME, IType.class, null);
  }

  public static IType valueOf(IAnnotatable owner) {
    DataAnnotation dataAnnotation = owner.annotations().withManagedWrapper(DataAnnotation.class).first();
    if (dataAnnotation == null) {
      return null;
    }
    return dataAnnotation.value();
  }
}
