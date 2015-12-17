/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.shared.ui.template.formfield.replace.levels;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.Replace;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "formdata.client.ui.template.formfield.replace.levels.ThirdLevelForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class ThirdLevelFormData extends SecondLevelFormData {

  private static final long serialVersionUID = 1L;

  public ThirdInnerBox getThirdInnerBox() {
    return getFieldByClass(ThirdInnerBox.class);
  }

  @Replace
  public static class ThirdInnerBox extends SecondInnerBox {

    private static final long serialVersionUID = 1L;

    public ThirdLevel getThirdLevel() {
      return getFieldByClass(ThirdLevel.class);
    }

    @Replace
    public static class ThirdLevel extends formdata.shared.ui.template.formfield.replace.levels.SecondLevelFormData.SecondInnerBox.SecondLevel {

      private static final long serialVersionUID = 1L;
    }
  }
}
