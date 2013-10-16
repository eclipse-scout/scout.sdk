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

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

import formdata.shared.IConstants;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated, no manual modifications recommended.
 * 
 * @generated
 */
public class UsingTemplateFormData extends AbstractFormData {

  private static final long serialVersionUID = 1L;

  public UsingTemplateFormData() {
  }

  public ExternalGroupBox getExternalGroupBox() {
    return getFieldByClass(ExternalGroupBox.class);
  }

  public InternalHtml getInternalHtml() {
    return getFieldByClass(InternalHtml.class);
  }

  public TestCheckbox getTestCheckbox() {
    return getFieldByClass(TestCheckbox.class);
  }

  public TestLimitedString getTestLimitedString() {
    return getFieldByClass(TestLimitedString.class);
  }

  public static class ExternalGroupBox extends AbstractExternalGroupBoxData {

    private static final long serialVersionUID = 1L;

    public ExternalGroupBox() {
    }
  }

  public static class InternalHtml extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;

    public InternalHtml() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MAX_LENGTH, Integer.MAX_VALUE);
    }
  }

  public static class TestCheckbox extends AbstractTestCheckboxFieldData {

    private static final long serialVersionUID = 1L;

    public TestCheckbox() {
    }
  }

  public static class TestLimitedString extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;

    public TestLimitedString() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MAX_LENGTH, IConstants.MAX_LENGTH * 4);
    }
  }
}
