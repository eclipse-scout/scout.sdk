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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.sql.binding.FormDataSqlBindingValidator;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link FormDataSqlBindingValidateAction}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 24.02.2011
 */
public class FormDataSqlBindingValidateAction extends AbstractScoutHandler {

  private IType[] m_services;
  private ITypeResolver m_typeresolver;

  public FormDataSqlBindingValidateAction() {
    super(Texts.get("ValidateSqlBindings"), null, null, false, Category.DELETE);
  }

  public ITypeResolver getTyperesolver() {
    return m_typeresolver;
  }

  public void setTyperesolver(ITypeResolver typeresolver) {
    m_typeresolver = typeresolver;
  }

  public void setServices(IType... services) {
    m_services = services;
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    Job j = new Job("Validate FormData SQL Bindings") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          new FormDataSqlBindingValidator(getServices()).run(monitor);
        }
        catch (Exception e) {
          ScoutSdkUi.logError("could not execute formdata sql binding validation.", e);
        }
        return Status.OK_STATUS;
      }
    };
    j.schedule();
    return null;
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
