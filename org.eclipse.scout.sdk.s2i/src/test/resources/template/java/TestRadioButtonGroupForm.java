/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

@SuppressWarnings("ALL")
@ClassId("49409d96-e2fe-4b9e-8fde-67d1a4e5b135")
public class TestRadioButtonGroupForm extends AbstractForm {

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  @Order(1000)
  @ClassId("c803d2d9-e45d-4126-ad29-a2d831fcfb54")
  public class MainBox extends AbstractGroupBox {
    @Order(1000)
    @ClassId("d91c55db-b1c0-4db6-8c89-10a83e4c4c7c")
    public class TestRadioButtonGroup extends AbstractRadioButtonGroup<Long> {
      <caret>
    }
  }
}
