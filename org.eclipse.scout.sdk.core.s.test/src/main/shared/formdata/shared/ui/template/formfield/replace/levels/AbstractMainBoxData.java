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

import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "formdata.client.ui.template.formfield.replace.levels.AbstractMainBox", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public abstract class AbstractMainBoxData extends AbstractFormFieldData {

  private static final long serialVersionUID = 1L;

  public FirstLevel getFirstLevel() {
    return getFieldByClass(FirstLevel.class);
  }

  public static class FirstLevel extends AbstractTemplateFieldData<Number> {

    private static final long serialVersionUID = 1L;
  }
}
