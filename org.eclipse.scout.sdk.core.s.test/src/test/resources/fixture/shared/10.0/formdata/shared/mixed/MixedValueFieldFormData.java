/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.shared.mixed;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "formdata.client.ui.forms.mixed.MixedValueFieldForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class MixedValueFieldFormData extends AbstractFormData {

  private static final long serialVersionUID = 1L;

  public First getFirst() {
    return getFieldByClass(First.class);
  }

  public static class First extends AbstractMixedValueFieldData<Short> {

    private static final long serialVersionUID = 1L;

    public ChangedAttributeNameFieldEx getChangedAttributeNameFieldEx() {
      return getFieldByClass(ChangedAttributeNameFieldEx.class);
    }

    @Replace
    public static class ChangedAttributeNameFieldEx extends AbstractValueFieldData<String> {

      private static final long serialVersionUID = 1L;
    }
  }
}