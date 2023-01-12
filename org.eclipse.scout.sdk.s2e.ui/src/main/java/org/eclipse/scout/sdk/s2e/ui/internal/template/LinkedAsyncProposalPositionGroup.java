/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.environment.RunnableJob;

/**
 * <h3>{@link LinkedAsyncProposalPositionGroup}</h3>
 *
 * @since 5.2.0
 */
public class LinkedAsyncProposalPositionGroup extends LinkedProposalPositionGroup implements ICompletionProposalProvider {

  private final RunnableFuture<Proposal[]> m_future;
  private final List<ILinkedAsyncProposalListener> m_listeners;

  public LinkedAsyncProposalPositionGroup(String groupId, RunnableFuture<Proposal[]> future) {
    super(groupId);
    m_future = Ensure.notNull(future);
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
      return m_future.get(100L, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException | TimeoutException e) {
      SdkLog.debug(e);
      return new Proposal[]{new Proposal("Loading...", null, 10)};
    }
    catch (ExecutionException e) {
      throw new SdkException(e);
    }
  }

  @Override
  public void load() {
    Job job = new RunnableJob("Load template proposals", m_future);
    job.setUser(false);
    job.setSystem(true);
    job.setPriority(Job.INTERACTIVE);
    job.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        job.removeJobChangeListener(this);
        var result = event.getResult();
        if (result.isOK()) {
          fireLoaded();
        }
        else if (result.getSeverity() != IStatus.CANCEL) {
          SdkLog.error(result.getMessage(), result.getException());
        }
      }

      private void fireLoaded() {
        ILinkedAsyncProposalListener[] listeners;
        //noinspection SynchronizeOnThis
        synchronized (this) {
          listeners = m_listeners.toArray(new ILinkedAsyncProposalListener[0]);
        }
        for (var listener : listeners) {
          listener.loaded();
        }
      }
    });
    job.schedule();
  }
}
