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
package a.client.form.fields;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;

@FormData
public abstract class AbstractTemplateBox extends AbstractGroupBox {

  public MyStringField getMyStringField() {
    return getFieldByClass(MyStringField.class);
  }

  public ComputedValueField getComputedValueField() {
    return getFieldByClass(ComputedValueField.class);
  }

  @Order(10.0)
  public class MyStringField extends AbstractStringField {
    @Override
    protected String getConfiguredLabel(){
      return "My String";
    }
  }

  @Order(20.0)
  @FormData("IGNORE")
  public class ComputedValueField extends AbstractStringField {
    @Override
    protected String getConfiguredLabel(){
      return "Computed Value";
    }

    @Override
    protected Class<? extends IValueField> getConfiguredMasterField(){
      return MyStringField.class;
    }

    @Override
    protected void execChangedMasterValue(Object newMasterValue) throws ProcessingException{
      setValue(StringUtility.uppercase(getMyStringField().getValue()));
    }
  }
}
