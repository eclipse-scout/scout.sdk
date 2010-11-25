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
import a.client.form.fields.AbstractExternalTableTemplateTableField;
import a.client.form.fields.AbstractTemplateBox;
import a.client.form.fields.AbstractTemplateStringField;
import a.client.form.fields.AbstractTemplateTableField;

@FormData
public class TemplateFieldForm extends AbstractForm{

  public TemplateFieldForm() throws ProcessingException{
    super();
  }

  public MainBox getMainBox(){
    return getFieldByClass(MainBox.class);
  }

  public class MainBox extends AbstractGroupBox{

    @Order(10.0)
    @FormData("CREATE EXTERNAL")
    public class TemplateStringField extends AbstractTemplateStringField {
    }

    @Order(20.0)
    @FormData("CREATE EXTERNAL")
    public class TemplateBox extends AbstractTemplateBox {

    }

    @Order(30.0)
    @FormData("CREATE EXTERNAL")
    public class TemplateTableField extends AbstractTemplateTableField {

    }

    @Order(40.0)
    @FormData("CREATE EXTERNAL")
    public class ExternalTableTemplateTableField extends AbstractExternalTableTemplateTableField {

    }
  }
}
