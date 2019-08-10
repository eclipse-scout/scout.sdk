/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.annotation;

import org.eclipse.scout.sdk.core.model.api.AbstractManagedAnnotation;

/**
 * <h3>{@link GeneratedAnnotation}</h3> Managed annotation for Generated
 *
 * @since 5.2.0
 */
public class GeneratedAnnotation extends AbstractManagedAnnotation {

  public static final String COMMENTS_ELEMENT_NAME = "comments";
  public static final String DATE_ELEMENT_NAME = "date";
  public static final String VALUE_ELEMENT_NAME = "value";

  public static final String TYPE_NAME = "javax.annotation.Generated";

  public String[] value() {
    return getValue(VALUE_ELEMENT_NAME, String[].class, null);
  }

  public String date() {
    return getValue(DATE_ELEMENT_NAME, String.class, null);
  }

  public String comments() {
    return getValue(COMMENTS_ELEMENT_NAME, String.class, null);
  }
}
