/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.job.RunnableJob;

/**
 * <h3>{@link LinkedAsyncProposalPositionGroup}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class LinkedAsyncProposalPositionGroup extends LinkedProposalPositionGroup implements ICompletionProposalProvider {

  private final RunnableFuture<Proposal[]> m_future;
  private final List<ILinkedAsyncProposalListener> m_listeners;

  public LinkedAsyncProposalPositionGroup(String groupId, RunnableFuture<Proposal[]> future) {
    super(groupId);
    m_future = Validate.notNull(future);
    m_listeners = new ArrayList<>();
  }

  @Override
  public synchronized void addListener(ILinkedAsyncProposalListener listener) {
    m_listeners.add(listener);
  }

  @Override
  @SuppressWarnings("squid:S1166")
  public Proposal[] getProposals() {
    try {
      return m_future.get(100, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException | TimeoutException e) {
      return new Proposal[]{new Proposal("Loading...", null, 10)};
    }
    catch (ExecutionException e) {
      throw new SdkException(e);
    }
  }

  private void fireLoaded() {
    ILinkedAsyncProposalListener[] listeners = null;
    synchronized (this) {
      listeners = m_listeners.toArray(new ILinkedAsyncProposalListener[m_listeners.size()]);
    }
    for (ILinkedAsyncProposalListener listener : listeners) {
      listener.loaded();
    }
  }

  @Override
  public void load() {
    final RunnableJob job = new RunnableJob("Load template proposals", m_future);
    job.setUser(false);
    job.setSystem(true);
    job.setPriority(Job.INTERACTIVE);
    job.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        job.removeJobChangeListener(this);
        IStatus result = event.getResult();
        if (result.isOK()) {
          fireLoaded();
        }
        else if (result.getSeverity() != IStatus.CANCEL) {
          SdkLog.error(result.getMessage(), result.getException());
        }
      }
    });
    job.schedule();
  }
}
