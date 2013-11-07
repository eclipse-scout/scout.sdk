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

import org.eclipse.scout.sdk.workspace.type.config.property.SourceProperty;

/**
 * <h3>{@link UiSourceProperty}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 06.03.2013
 */
public class UiSourceProperty<T> extends SourceProperty<T> {

  private final String m_displayValue;

  /**
   * @param sourceValue
   */
  public UiSourceProperty(T sourceValue, String displayValue) {
    super(sourceValue);
    m_displayValue = displayValue;
  }

  public String getDisplayValue() {
    return m_displayValue;
  }

  @Override
  public String toString() {
    return getDisplayValue();
  }
}
