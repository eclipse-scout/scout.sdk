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

import formdata.shared.ui.template.formfield.replace.levels.ThirdLevelFormData;

@FormData(value = ThirdLevelFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class ThirdLevelForm extends SecondLevelForm {

  @Replace
  public class ThirdInnerBox extends SecondInnerBox {
    public ThirdInnerBox(FirstLevelForm.MainBox m) {
      super(m);
    }

    @Replace
    public class ThirdLevel extends SecondLevelForm.SecondInnerBox.SecondLevel {
      public ThirdLevel(FirstLevelForm.MainBox.FirstInnerBox m) {
        super(m);
      }
    }
  }
}
