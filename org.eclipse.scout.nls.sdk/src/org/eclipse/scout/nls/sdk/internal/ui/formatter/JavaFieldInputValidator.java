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
package org.eclipse.scout.nls.sdk.internal.ui.formatter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.swt.SWT;

public class JavaFieldInputValidator implements IInputValidator {
  public static String REGEX_JAVA_FIELD = "\\b[A-Za-z][a-zA-Z0-9_]{0,200}\\b";

  public IStatus isValid(String value) {
    if (!value.matches(REGEX_JAVA_FIELD)) {
      return new Status(IStatus.ERROR, NlsCore.PLUGIN_ID, SWT.OK, "Ensure input is a valid java field name.", null);
    }
    return Status.OK_STATUS;

  }

}
