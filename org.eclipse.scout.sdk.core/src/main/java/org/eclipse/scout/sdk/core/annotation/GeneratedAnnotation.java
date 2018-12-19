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
package org.eclipse.scout.sdk.core.annotation;

import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.model.sugar.AbstractManagedAnnotation;

/**
 * <h3>{@link GeneratedAnnotation}</h3> Managed annotation for Generated
 *
 * @author Ivan Motsch
 * @since 5.2.0
 */
public class GeneratedAnnotation extends AbstractManagedAnnotation {
  public static final String TYPE_NAME = IJavaRuntimeTypes.Generated;

  public String[] value() {
    return getValue("value", String[].class, null);
  }

  public String date(String... optionalCustomDefaultValue) {
    return getValue("date", String.class, optionalCustomDefaultValue);
  }

  public String comments(String... optionalCustomDefaultValue) {
    return getValue("comments", String.class, optionalCustomDefaultValue);
  }

}
