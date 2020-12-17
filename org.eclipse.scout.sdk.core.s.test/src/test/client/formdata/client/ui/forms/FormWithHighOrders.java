/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package formdata.client.ui.forms;

import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.Order;

public class FormWithHighOrders extends AbstractForm {

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(100000000000000000.0)
    public class AGroupBox extends AbstractGroupBox {
    }

    public abstract class AbstractTestBox1 extends AbstractGroupBox {
    }

    // no order here
    public class IgnoredGroupBox extends AbstractGroupBox {
    }
  }
}
