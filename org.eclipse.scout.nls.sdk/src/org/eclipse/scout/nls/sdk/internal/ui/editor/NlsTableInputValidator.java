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
package org.eclipse.scout.nls.sdk.internal.ui.editor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.ui.IStructuredInputValidator;
import org.eclipse.swt.SWT;

public class NlsTableInputValidator implements IStructuredInputValidator {
  private final INlsProject m_project;
  private String m_regexp = "\\b[A-Za-z][a-zA-Z0-9_]{0,200}\\b";

  public NlsTableInputValidator(INlsProject project) {
    m_project = project;
  }

  public IStatus validate(String input, int column) {
    switch (column) {
      case NlsTable.INDEX_COLUMN_KEYS: {
        if (!input.matches(m_regexp)) {
          return new Status(IStatus.ERROR, NlsCore.PLUGIN_ID, SWT.OK, "Ensure input is a valid java field name.", null);
        }
        if (m_project.getEntry(input) != null) {
          return new Status(IStatus.ERROR, NlsCore.PLUGIN_ID, SWT.OK, "A key " + input + " already exists!", null);
        }
        break;
      }

    }
    return Status.OK_STATUS;
  }

}
