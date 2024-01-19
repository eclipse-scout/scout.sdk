/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.client.extensions;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;

import formdata.client.ui.forms.ListBoxForm;
import formdata.shared.extension.PropertyExtensionData;

@Data(PropertyExtensionData.class)
public class FormPropertyExtension extends AbstractFormExtension<ListBoxForm> {

  private Long m_longValue;

  public FormPropertyExtension(ListBoxForm ownerForm) {
    super(ownerForm);
  }

  @Data
  public Long getLongValue() {
    return m_longValue;
  }

  @Data
  public void setLongValue(Long value) {
    m_longValue = value;
  }
}
