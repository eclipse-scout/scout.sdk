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
package a.client.form;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import a.client.form.AForm.MainBox.TextFieldWithConfiguration;
import a.client.form.AForm.MainBox.TextFieldWithoutConfiguration;

@FormData
public class AForm extends AbstractForm{

  public AForm() throws ProcessingException{
    super();
  }

  public TextFieldWithConfiguration getTextFieldWithConfiguration(){
    return getFieldByClass(TextFieldWithConfiguration.class);
  }

  public TextFieldWithoutConfiguration getTextFieldWithoutConfiguration(){
    return getFieldByClass(TextFieldWithoutConfiguration.class);
  }

  public class MainBox extends AbstractGroupBox{

    @Order(10)
    public class TextFieldWithConfiguration extends AbstractStringField{
      public void foo(){
      }

      @Override
      public String getConfiguredLabel(){
        return "label";
      }

      @Override
      public void execChangedValue() throws ProcessingException{
        super.execChangedValue();
      }
    }

    @Order(10)
    public class TextFieldWithoutConfiguration extends AbstractStringField{
      public void bar() {

      }
    }
  }
}
