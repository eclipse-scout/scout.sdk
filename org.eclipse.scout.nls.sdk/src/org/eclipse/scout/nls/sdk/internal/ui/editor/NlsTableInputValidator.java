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
import org.eclipse.scout.nls.sdk.internal.ui.formatter.IInputValidator;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.ui.IStructuredInputValidator;
import org.eclipse.scout.nls.sdk.ui.InputValidator;

public class NlsTableInputValidator implements IStructuredInputValidator {
  private final IInputValidator m_keyValidator;

  public NlsTableInputValidator(INlsProject project) {
    m_keyValidator = InputValidator.getNlsKeyValidator(project);
  }

  @Override
  public IStatus validate(String input, int column) {
    switch (column) {
      case NlsTable.INDEX_COLUMN_KEYS: {
        return m_keyValidator.isValid(input);
      }
    }
    return Status.OK_STATUS;
  }
}
