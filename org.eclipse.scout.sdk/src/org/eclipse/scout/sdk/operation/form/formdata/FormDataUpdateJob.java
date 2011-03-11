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
package org.eclipse.scout.sdk.operation.form.formdata;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * <h3>{@link FormDataUpdateJob}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 14.02.2011
 */
public class FormDataUpdateJob extends Job {

  private boolean m_canceled;
  private final FormDataUpdateOperation m_formDataUpdateOperation;

  public FormDataUpdateJob(FormDataUpdateOperation formDataUpdateOperation) {
    // does not need a name since is background job
    super("");
    m_formDataUpdateOperation = formDataUpdateOperation;
    setRule(new FormDataJobRule());

    setSystem(true);
    setUser(false);
  }

  @Override
  public boolean shouldSchedule() {
    return !isCanceled();
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    if (isCanceled()) {
      return Status.CANCEL_STATUS;
    }
    m_formDataUpdateOperation.setup(monitor);

    m_formDataUpdateOperation.createSourceBuilder(monitor);
    if (isCanceled()) {
      return Status.CANCEL_STATUS;
    }
    m_formDataUpdateOperation.storeFormData(monitor);
    return Status.OK_STATUS;
  }

  /**
   * @return the formDataUpdateOperation
   */
  public FormDataUpdateOperation getFormDataUpdateOperation() {
    return m_formDataUpdateOperation;
  }

  /**
   * @param canceled
   *          the canceled to set
   */
  public void setCanceled(boolean canceled) {
    m_canceled = canceled;
  }

  /**
   * @return the canceled
   */
  public boolean isCanceled() {
    return m_canceled;
  }

  public class FormDataJobRule implements ISchedulingRule {
    @Override
    public boolean contains(ISchedulingRule rule) {
      if (rule instanceof FormDataJobRule) {
        return true;
      }
      return false;
    }

    @Override
    public boolean isConflicting(ISchedulingRule rule) {
      if (rule instanceof FormDataJobRule) {
        return true;
      }
      return false;
    }
  }

}
