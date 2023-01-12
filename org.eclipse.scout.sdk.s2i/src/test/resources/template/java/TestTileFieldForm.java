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
import org.eclipse.scout.rt.client.ui.form.fields.tilefield.AbstractTileField;
import org.eclipse.scout.rt.client.ui.tile.AbstractTileGrid;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;

@SuppressWarnings("ALL")
@ClassId("49409d96-e2fe-4b9e-8fde-67d1a4e5b135")
public class TestTileFieldForm extends AbstractForm {
  @Order(1000)
  @ClassId("c803d2d9-e45d-4126-ad29-a2d831fcfb54")
  public class MainBox extends AbstractGroupBox {
    @Order(1000)
    @ClassId("3dc6003e-4fc0-4b8c-ae6a-199af33cea7e")
    public class TestField extends AbstractTileField<TestField.TileGrid> {
      @ClassId("e6d054ce-7325-45c7-983e-88bcaf119592")
      public class TileGrid extends AbstractTileGrid<ITile> {
        <caret>
      }
    }
  }
}
