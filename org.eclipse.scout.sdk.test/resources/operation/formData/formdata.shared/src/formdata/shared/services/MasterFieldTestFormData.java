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
package formdata.shared.services;

import java.util.Map;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated, no manual modifications recommended.
 * 
 * @generated
 */
public class MasterFieldTestFormData extends AbstractFormData {

  private static final long serialVersionUID = 1L;

  public MasterFieldTestFormData() {
  }

  public MyMaster getMyMaster() {
    return getFieldByClass(MyMaster.class);
  }

  public MySlave getMySlave() {
    return getFieldByClass(MySlave.class);
  }

  public static class MyMaster extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;

    public MyMaster() {
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

  public static class MySlave extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;

    public MySlave() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MASTER_VALUE_FIELD, MyMaster.class);
      ruleMap.put(ValidationRule.MAX_LENGTH, 4000);
    }
  }
}
