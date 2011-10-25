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
package org.eclipse.scout.nls.sdk.ui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.ui.formatter.IInputValidator;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.swt.SWT;

/** <h4>InputValidator</h4> */
public class InputValidator {
  private static final InputValidator instance = new InputValidator();
  private final static Pattern REGEX_NLS_KEY_NAME = Pattern.compile("\\b[A-Za-z][a-zA-Z0-9_.]{0,200}\\b");

  private InputValidator() {
  }

  public static IInputValidator getNlsKeyValidator(INlsProject project) {
    return getNlsKeyValidator(project, new String[0]);
  }

  public static IInputValidator getNlsKeyValidator(INlsProject project, String[] exceptions) {
    return instance.getNlsKeyValidatorInternal(project, exceptions);
  }

  private IInputValidator getNlsKeyValidatorInternal(INlsProject project, String[] exceptions) {
    return new P_KeyEntryValidator(project, exceptions);
  }

  public static IInputValidator getDefaultTranslationValidator() {
    return new IInputValidator() {
      @Override
      public IStatus isValid(String value) {
        if (value.length() > 0) {
          return Status.OK_STATUS;
        }
        else {
          return new Status(IStatus.ERROR, NlsCore.PLUGIN_ID, SWT.OK, "The default translation must be set.", null);
        }

      }
    };
  }

  private class P_KeyEntryValidator implements IInputValidator {
    private HashSet<String> m_keys = new HashSet<String>();

    public P_KeyEntryValidator(INlsProject project, String[] exeptions) {
      m_keys = new HashSet<String>(Arrays.asList(NlsCore.getProjectKeys(project)));
      for (String k : exeptions) {
        m_keys.remove(k);
      }
    }

    @Override
    public IStatus isValid(String value) {
      if (m_keys.contains(value)) {
        return new Status(IStatus.ERROR, NlsCore.PLUGIN_ID, SWT.OK, "A key '" + value + "' already exists!", null);
      }
      if (!REGEX_NLS_KEY_NAME.matcher(value).matches()) {
        return new Status(IStatus.ERROR, NlsCore.PLUGIN_ID, SWT.OK, "The key name is not valid.", null);
      }
      return Status.OK_STATUS;
    }
  } // end class P_KeyEntryValidator
}
