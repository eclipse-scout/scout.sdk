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
package org.eclipse.scout.sdk.workspace.type.config.property;

import org.eclipse.scout.commons.CompareUtility;

/**
 * <h3>{@link SourceProperty}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 06.03.2013
 */
public abstract class SourceProperty<T> {
  private T m_value;

  public SourceProperty(T value) {
    m_value = value;
  }

  public T getValue() {
    return m_value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SourceProperty<?>) {
      return CompareUtility.equals(((SourceProperty<?>) obj).getValue(), getValue());
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (getValue() != null) {
      return getValue().hashCode();
    }
    return 0;
  }
}
