/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
