/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package formdata.client.ui.template.formfield;

import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;

public abstract class AbstractExternalWithNoAnnotationBox extends AbstractGroupBox {

  public NameField getNameField() {
    return getFieldByClass(NameField.class);
  }

  public PlzField getPlzField() {
    return getFieldByClass(PlzField.class);
  }

  @Order(10.0)
  public class NameField extends AbstractStringField {
  }

  @Order(20.0)
  public class PlzField extends AbstractIntegerField {
  }
}
