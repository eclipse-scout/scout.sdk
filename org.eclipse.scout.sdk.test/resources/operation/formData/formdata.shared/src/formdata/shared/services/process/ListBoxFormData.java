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
package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated, no manual modifications recommended.
 * 
 * @generated
 */
public class ListBoxFormData extends AbstractFormData {

  private static final long serialVersionUID = 1L;

  public ListBoxFormData() {
  }

  public ListBox getListBox() {
    return getFieldByClass(ListBox.class);
  }

  public static class ListBox extends AbstractValueFieldData<Long[]> {

    private static final long serialVersionUID = 1L;

    public ListBox() {
    }
  }
}
