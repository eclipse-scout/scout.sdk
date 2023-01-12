/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
