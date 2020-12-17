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
package formdata.shared.ui.template.formfield.replace.levels;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "formdata.client.ui.template.formfield.replace.levels.FirstLevelForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class FirstLevelFormData extends AbstractFormData {

  private static final long serialVersionUID = 1L;

  public FirstInnerBox getFirstInnerBox() {
    return getFieldByClass(FirstInnerBox.class);
  }

  public static class FirstInnerBox extends AbstractMainBoxData {

    private static final long serialVersionUID = 1L;
  }
}
