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
import org.eclipse.scout.rt.platform.Replace;

import formdata.client.ui.template.formfield.replace.levels.FirstLevelForm.MainBox.FirstInnerBox;
import formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData;

@FormData(value = SecondLevelFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class SecondLevelForm extends FirstLevelForm {

  @Replace
  public class SecondInnerBox extends FirstInnerBox {
    public SecondInnerBox(FirstLevelForm.MainBox m) {
      m.super();
    }

    @Replace
    public class SecondLevel extends FirstLevelForm.MainBox.FirstInnerBox.FirstLevel {
      public SecondLevel(FirstLevelForm.MainBox.FirstInnerBox m) {
        m.super();
      }
    }
  }

}
