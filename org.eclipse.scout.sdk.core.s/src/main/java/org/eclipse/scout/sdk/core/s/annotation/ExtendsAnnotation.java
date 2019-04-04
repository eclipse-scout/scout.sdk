/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.annotation;

import org.eclipse.scout.sdk.core.model.api.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;

/**
 * <h3>{@link ExtendsAnnotation}</h3>
 *
 * @since 6.1.0
 */
public class ExtendsAnnotation extends AbstractManagedAnnotation {
  public static final String TYPE_NAME = IScoutRuntimeTypes.Extends;

  public IType value() {
    return getValue("value", IType.class, null);
  }

  public IType[] pathToContainer() {
    return getValue("pathToContainer", IType[].class, null);
  }
}
