/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.shared.mixed;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;

/**
 * <h3>{@link AbstractMixedValueFieldData}</h3>
 *
 * @since 6.1.0
 */
public abstract class AbstractMixedValueFieldData<T> extends AbstractFormFieldData {
  private static final long serialVersionUID = 1L;
  private T m_value;

  public T getValue() {
    return m_value;
  }

  public void setValue(T value) {
    m_value = value;
  }
}
