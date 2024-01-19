/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.shared.scope.field;

import java.util.Set;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "formdata.client.scope.field.AbstractScopeTestGroupBox", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public abstract class AbstractScopeTestGroupBoxData extends AbstractFormFieldData {

  private static final long serialVersionUID = 1L;

  public Process getProcess() {
    return getFieldByClass(Process.class);
  }

  public static class Process extends AbstractValueFieldData<Set<Long>> {

    private static final long serialVersionUID = 1L;
  }
}
