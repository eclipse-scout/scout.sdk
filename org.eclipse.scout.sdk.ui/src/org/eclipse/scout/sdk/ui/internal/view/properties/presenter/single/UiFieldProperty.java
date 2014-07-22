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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import org.eclipse.jdt.core.IField;
import org.eclipse.scout.sdk.workspace.type.config.property.FieldProperty;

/**
 * <h3>{@link UiFieldProperty}</h3>
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 01.03.2013
 */
public class UiFieldProperty<T> extends FieldProperty<T> {

  private final String m_diplayValue;

  /**
   * @param field
   */
  public UiFieldProperty(IField field, String diplayValue) {
    super(field);
    m_diplayValue = diplayValue;
  }

  public String getDiplayValue() {
    return m_diplayValue;
  }

  @Override
  public String toString() {
    return m_diplayValue;
  }
}
