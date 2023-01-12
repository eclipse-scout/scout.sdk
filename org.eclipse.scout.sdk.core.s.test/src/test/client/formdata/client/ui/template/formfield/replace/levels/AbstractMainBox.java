/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.client.ui.template.formfield.replace.levels;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;

import formdata.shared.ui.template.formfield.replace.levels.AbstractMainBoxData;

@FormData(value = AbstractMainBoxData.class, sdkCommand = FormData.SdkCommand.CREATE, defaultSubtypeSdkCommand = FormData.DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractMainBox extends AbstractGroupBox {
  public FirstLevel getFirstLevel() {
    return getFieldByClass(FirstLevel.class);
  }

  public class FirstLevel extends AbstractTemplateField<Number> {
  }
}
