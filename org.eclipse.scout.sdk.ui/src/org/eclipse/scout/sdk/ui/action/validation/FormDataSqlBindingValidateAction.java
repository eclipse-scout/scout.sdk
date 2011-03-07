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
package org.eclipse.scout.sdk.ui.action.validation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.sql.binding.FormDataSqlBindingValidator;

/**
 * <h3>{@link FormDataSqlBindingValidateAction}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 24.02.2011
 */
public class FormDataSqlBindingValidateAction extends Action {

  private IType[] m_services;
  private ITypeResolver m_typeresolver;

  public FormDataSqlBindingValidateAction(IType... services) {
    super("Validate formdata sql bindings...");
    m_services = services;
  }

  public FormDataSqlBindingValidateAction(ITypeResolver typeresolver) {
    super("Validate formdata sql binding...");
    m_typeresolver = typeresolver;

  }

  @Override
  public void run() {
    Job j = new Job(getText()) {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          new FormDataSqlBindingValidator(getServices()).run(monitor);
        }
        catch (Exception e) {
          ScoutSdk.logError("could not execute formdata sql binding validation.", e);
        }
        return Status.OK_STATUS;
      }
    };
    j.schedule();
  }

  /**
   * @return the services
   */
  public IType[] getServices() {
    if (m_services != null) {
      return m_services;
    }
    else if (m_typeresolver != null) {
      return m_typeresolver.getTypes();
    }
    return new IType[0];
  }
}
