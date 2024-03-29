/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import org.eclipse.scout.rt.client.ui.accordion.AbstractAccordion;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.accordionfield.AbstractAccordionField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

@SuppressWarnings("ALL")
@ClassId("49409d96-e2fe-4b9e-8fde-67d1a4e5b135")
public class TestAccordionFieldForm extends AbstractForm {
  @Order(1000)
  @ClassId("c803d2d9-e45d-4126-ad29-a2d831fcfb54")
  public class MainBox extends AbstractGroupBox {
    @Order(1000)
    @ClassId("0026c534-5cdf-468f-b8a0-ba463daa6e61")
    public class TestAccordionField extends AbstractAccordionField<TestAccordionField.Accordion> {
      @ClassId("028f5af3-cd15-4b40-bb99-e0f53d9a130c")
      public class Accordion extends AbstractAccordion {
        <caret>
      }
    }
  }
}
