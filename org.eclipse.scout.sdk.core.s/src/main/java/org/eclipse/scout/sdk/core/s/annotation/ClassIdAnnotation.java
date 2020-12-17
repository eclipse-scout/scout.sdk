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
package org.eclipse.scout.sdk.core.s.annotation;

import java.util.Optional;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.core.model.api.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;

/**
 * <h3>{@link ClassIdAnnotation}</h3>
 *
 * @since 5.2.0
 */
public class ClassIdAnnotation extends AbstractManagedAnnotation {

  protected static final ApiFunction<?, IClassNameSupplier> TYPE_NAME = new ApiFunction<>(IScoutApi.class, IScoutApi::ClassId);

  public static Optional<String> valueOf(IAnnotatable owner) {
    return owner.annotations()
        .withManagedWrapper(ClassIdAnnotation.class)
        .first()
        .map(ClassIdAnnotation::value);
  }

  public String value() {
    return getValueFrom(IScoutApi.class, api -> api.ClassId().valueElementName(), String.class, null);
  }
}
