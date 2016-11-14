/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.shared.scope.field;

import java.util.Set;

import javax.annotation.Generated;

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