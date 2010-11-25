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
import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;

import a.client.form.AForm.MainBox.ListBox;
import a.client.form.AForm.MainBox.TextField;

@FormData
public class AForm extends AbstractForm{

  public AForm() throws ProcessingException{
    super();
  }

  public TextField getTextField(){
    return getFieldByClass(TextField.class);
  }

  public ListBox getListBox(){
    return getFieldByClass(ListBox.class);
  }

  public class MainBox extends AbstractGroupBox{

    @Order(10)
    public class TextField extends AbstractStringField{

    }

    @Order(20)
    public class ListBox extends AbstractListBox<Long>{
    }
  }
}
