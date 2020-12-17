/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.fields.proposal.content;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
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
import org.eclipse.scout.sdk.s2e.ui.util.NormalizedPattern;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * <h3>{@link AbstractContentProviderAdapter}</h3> Adapter implementation for content provider to be used in
 * {@link ProposalTextField#setContentProvider(IProposalContentProvider)} and
 * {@link ProposalTextField#setLabelProvider(org.eclipse.jface.viewers.IBaseLabelProvider)}.
 *
 * @since 5.2.0
 */
public abstract class AbstractContentProviderAdapter extends BaseLabelProvider
    implements IProposalContentProvider, ILabelProvider, ISearchRangeConsumer, IDialogSettingsProvider, ISelectionStateLabelProvider, IProposalDescriptionProvider {

  private final Map<Object, int[]> m_searchRanges;
  private ILabelProvider m_decoratingWorkbenchLabelProvider;
  private volatile Collection<?> m_allProposals; // lazy loaded
  private final Object m_proposalsLock; // lock object. Because 'this' is already used by super.clearListeners() called by dispose()

  protected AbstractContentProviderAdapter() {
    m_searchRanges = new HashMap<>();
    m_proposalsLock = new Object();
    m_decoratingWorkbenchLabelProvider = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  @Override
  public /* do not synchronize */ void dispose() {
    super.dispose();
    m_allProposals = null; /* do not call clearCache() because of locking! */
    m_searchRanges.clear();
    if (m_decoratingWorkbenchLabelProvider != null) {
      m_decoratingWorkbenchLabelProvider.dispose();
      m_decoratingWorkbenchLabelProvider = null;
    }
  }

  protected void clearCache() {
    synchronized (m_proposalsLock) {
      m_allProposals = null;
    }
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
  public Object createDescriptionContent(Object element, IProgressMonitor monitor) {
    if (!(element instanceof IJavaElement)) {
      return null;
    }
    return JavaDocBrowser.getJavaDoc((IJavaElement) element);
  }

  @Override
  public Control createDescriptionControl(Composite parent, Object content) {
    var browser = JavaDocBrowser.create(parent, content.toString());
    if (browser == null) {
      return null;
    }

    GridDataFactory
        .defaultsFor(browser)
        .align(SWT.FILL, SWT.FILL)
        .grab(true, true)
        .applyTo(browser);
    return browser;
  }

  @Override
  public Collection<Object> getProposals(NormalizedPattern searchPattern, IProgressMonitor monitor) {
    try {
      startRecordMatchRegions();
      if (monitor != null && monitor.isCanceled()) {
        return emptyList();
      }

      var proposals = doLoadProposals();
      if (proposals == null || proposals.isEmpty()) {
        return emptyList();
      }

      Collection<Object> result = new ArrayList<>(proposals.size());
      for (Object o : proposals) {
        if (monitor != null && monitor.isCanceled()) {
          break;
        }
        var matchRegions = searchPattern.getMatchingRegions(getText(o));
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

  protected Collection<?> doLoadProposals() {
    var loadedProposals = m_allProposals;
    if (loadedProposals != null) {
      return loadedProposals;
    }

    synchronized (m_proposalsLock) {
      loadedProposals = m_allProposals;
      if (loadedProposals != null) {
        return loadedProposals;
      }

      // Do not pass the input monitor to loadProposals() to ensure the load is not canceled.
      // We want to completely load on the first request and filter only afterwards.
      loadedProposals = loadProposals(new NullProgressMonitor());
      if (loadedProposals == null) {
        loadedProposals = emptyList();
      }
      m_allProposals = loadedProposals;
      return loadedProposals;
    }
  }

  @Override
  public IDialogSettings getDialogSettings() {
    return S2ESdkUiActivator.getDefault().getDialogSettingsSection(getClass().getName());
  }

  protected abstract Collection<?> loadProposals(IProgressMonitor monitor);
}
