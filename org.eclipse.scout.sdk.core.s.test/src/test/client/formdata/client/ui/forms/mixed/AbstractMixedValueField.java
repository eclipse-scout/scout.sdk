/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package formdata.client.ui.forms.mixed;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;
import org.eclipse.scout.rt.platform.Order;

import formdata.shared.mixed.AbstractMixedValueFieldData;

/**
 * <h3>{@link AbstractMixedValueField}</h3>
 *
 * @since 6.1.0
 */
@FormData(value = AbstractMixedValueFieldData.class, defaultSubtypeSdkCommand = FormData.DefaultSubtypeSdkCommand.CREATE, sdkCommand = FormData.SdkCommand.USE, genericOrdinal = 0)
public abstract class AbstractMixedValueField<T> extends AbstractGroupBox {
  private T m_defaultValue;

  public T getDefaultValue() {
    return m_defaultValue;
  }

  public void setDefaultValue(T defaultValue) {
    m_defaultValue = defaultValue;
  }

  @Order(10)
  public class InnerNameField extends AbstractLabelField {
    @Override
    protected String getConfiguredLabel() {
      return "whatever";
    }
  }
}
