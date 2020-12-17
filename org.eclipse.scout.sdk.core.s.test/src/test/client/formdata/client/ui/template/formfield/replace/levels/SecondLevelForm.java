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
