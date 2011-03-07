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
package org.eclipse.scout.sdk.ui.internal;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.sdk.operation.form.formdata.ICreateFormDataRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

/**
 * <h3>{@link CreateFormDataRequest}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 17.02.2011
 */
public class CreateFormDataRequest implements ICreateFormDataRequest {

  @Override
  public boolean createFormData(IType type, String packageName, String simpleName) {
    final BooleanHolder result = new BooleanHolder(false);
    final String question = "The form data '" + packageName + "." + simpleName + "' for '" + type.getElementName() + "' does not exist.\n Do you want to create it?";
    Display.getDefault().syncExec(new Runnable() {
      @Override
      public void run() {
        MessageBox box = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        box.setMessage(question);
        result.setValue(box.open() == SWT.YES);
      }
    });
    return result.getValue();
  }

}
