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
