/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.fields.proposal.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.sdk.s2e.ui.fields.javadoc.JavaDocBrowser;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.IDialogSettingsProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.IProposalContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.IProposalDescriptionProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ISearchRangeConsumer;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ISelectionStateLabelProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.util.NormalizedPattern;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * <h3>{@link AbstractContentProviderAdapter}</h3> Adapter implementation for content provider to be used in
 * {@link ProposalTextField#setContentProvider(IProposalContentProvider)} and
 * {@link ProposalTextField#setLabelProvider(org.eclipse.jface.viewers.IBaseLabelProvider)}.
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public abstract class AbstractContentProviderAdapter extends BaseLabelProvider
    implements IProposalContentProvider, ILabelProvider, ISearchRangeConsumer, IDialogSettingsProvider, ISelectionStateLabelProvider, IProposalDescriptionProvider {

  private final Map<Object, int[]> m_searchRanges;
  private ILabelProvider m_decoratingWorkbenchLabelProvider;
  private Collection<? extends Object> m_allProposals; // lazy loaded

  protected AbstractContentProviderAdapter() {
    m_searchRanges = new HashMap<>();
    m_decoratingWorkbenchLabelProvider = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  @Override
  public synchronized void dispose() {
    super.dispose();
    clearCache();
    m_searchRanges.clear();
    m_decoratingWorkbenchLabelProvider = null;
  }

  protected synchronized void clearCache() {
    m_allProposals = null;
  }

  @Override
  public Image getImage(Object element) {
    return m_decoratingWorkbenchLabelProvider.getImage(element);
  }

  @Override
  public Image getImageSelected(Object element) {
    return getImage(element);
  }

  @Override
  public String getTextSelected(Object element) {
    return getText(element);
  }

  @Override
  public Object createDescriptionContent(Object proposal, IProgressMonitor monitor) {
    if (!(proposal instanceof IJavaElement)) {
      return null;
    }
    return JavaDocBrowser.getJavaDoc((IJavaElement) proposal);
  }

  @Override
  public Control createDescriptionControl(Composite parent, Object content) {
    Browser browser = JavaDocBrowser.create(parent, content.toString());
    if (browser == null) {
      return null;
    }

    browser.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
    return browser;
  }

  @Override
  public Collection<Object> getProposals(NormalizedPattern searchPattern, IProgressMonitor monitor) {
    try {
      startRecordMatchRegions();
      if (monitor != null && monitor.isCanceled()) {
        return Collections.emptyList();
      }

      Collection<? extends Object> proposals = doLoadProposals(monitor);
      if (proposals.isEmpty()) {
        return Collections.emptyList();
      }

      List<Object> result = new ArrayList<>();
      for (Object o : proposals) {
        if (monitor != null && monitor.isCanceled()) {
          break;
        }
        int[] matchRegions = searchPattern.getMatchingRegions(getText(o));
        if (matchRegions != null) {
          result.add(o);
        }
        addMatchRegions(o, matchRegions);
      }
      return result;
    }
    finally {
      endRecordMatchRegions();
    }
  }

  protected void addProposal() {

  }

  @Override
  public int[] getMatchRanges(Object element) {
    return m_searchRanges.get(element);
  }

  @Override
  public void startRecordMatchRegions() {
    m_searchRanges.clear();
  }

  @Override
  public void addMatchRegions(Object element, int[] matchRegions) {
    m_searchRanges.put(element, matchRegions);
  }

  @Override
  public void endRecordMatchRegions() {
  }

  /**
   * @param monitor
   * @return
   */
  protected synchronized Collection<? extends Object> doLoadProposals(IProgressMonitor monitor) {
    if (m_allProposals == null) {
      // Do not pass the input monitor to loadProposals() to ensure the load is not cancelled.
      // We want to completely load on the first request and filter only afterwards.
      Collection<? extends Object> proposalCandidates = loadProposals(new NullProgressMonitor());
      if (proposalCandidates == null) {
        proposalCandidates = Collections.emptyList();
      }
      m_allProposals = proposalCandidates;
    }
    return m_allProposals;
  }

  @Override
  public IDialogSettings getDialogSettings() {
    return S2ESdkUiActivator.getDefault().getDialogSettingsSection(getClass().getName());
  }

  /**
   * @param monitor
   * @return
   */
  protected abstract Collection<? extends Object> loadProposals(IProgressMonitor monitor);
}
