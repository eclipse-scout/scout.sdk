/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.shared.services.process;

import java.util.Map;

import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated, no manual modifications recommended.
 * 
 * @generated
 */
public abstract class AbstractExternalGroupBoxData extends AbstractFormFieldData {

  private static final long serialVersionUID = 1L;

  public AbstractExternalGroupBoxData() {
  }

  public ExternalString getExternalString() {
    return getFieldByClass(ExternalString.class);
  }

  public static class ExternalString extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;

    public ExternalString() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MAX_LENGTH, 4000);
    }
  }
}
