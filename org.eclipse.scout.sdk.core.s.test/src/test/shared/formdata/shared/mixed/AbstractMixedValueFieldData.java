/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
