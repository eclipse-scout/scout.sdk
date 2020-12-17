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
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.Order;

import formdata.shared.ui.template.formfield.replace.levels.FirstLevelFormData;

@FormData(value = FirstLevelFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class FirstLevelForm extends AbstractForm {

  @Order(1000.0)
  public class MainBox extends AbstractGroupBox {
    public class FirstInnerBox extends AbstractMainBox {
    }
  }
}
