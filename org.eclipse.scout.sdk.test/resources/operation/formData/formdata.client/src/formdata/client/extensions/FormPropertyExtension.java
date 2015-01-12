/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.client.extensions;

import org.eclipse.scout.commons.annotations.Data;
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
