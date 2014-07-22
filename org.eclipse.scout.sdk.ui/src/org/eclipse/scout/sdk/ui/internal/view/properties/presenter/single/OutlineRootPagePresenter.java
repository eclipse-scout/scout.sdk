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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementLabelProvider;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractTypeProposalPresenter;
import org.eclipse.scout.sdk.util.IRegEx;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>OutlineRootPagePresenter</h3>
 */
public class OutlineRootPagePresenter extends AbstractTypeProposalPresenter {

  public OutlineRootPagePresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected void createProposalFieldProviders(ProposalTextField proposalField) {
    JavaElementLabelProvider labelProvider = new JavaElementLabelProvider();
    getProposalField().setLabelProvider(labelProvider);
    getProposalField().setContentProvider(new P_ContentProvider(labelProvider));
  }

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    if (method != null) {
      getProposalField().setInput(method.getType());
      super.init(method);
    }
    else {
      getProposalField().setInput(null);
    }
  }

  /**
   * <h3>{@link P_ContentProvider}</h3>
   * The local lazy content provider.
   * It is kept lazy to ensure the proposals are only loaded when used. So the creation of the property view is
   * performance optimized.
   *
   * @author Andreas Hoegger
   * @since 3.8.0 15.02.2012
   */
  private final class P_ContentProvider extends ContentProposalProvider {

    private Set<IType> m_proposals;
    private final ILabelProvider m_labelProvider;

    private P_ContentProvider(ILabelProvider labelProvider) {
      m_labelProvider = labelProvider;

    }

    @Override
    public Object[] getProposals(String searchPattern, IProgressMonitor monitor) {
      ensureCache();
      if (!StringUtility.hasText(searchPattern)) {
        searchPattern = "*";
      }
      else {
        searchPattern = IRegEx.STAR_END.matcher(searchPattern).replaceAll("") + "*";
      }
      char[] pattern = CharOperation.toLowerCase(searchPattern.toCharArray());
      ArrayList<Object> collector = new ArrayList<Object>(m_proposals.size());
      for (Object proposal : m_proposals) {
        if (CharOperation.match(pattern, m_labelProvider.getText(proposal).toCharArray(), false)) {
          collector.add(proposal);
        }
      }
      return collector.toArray(new Object[collector.size()]);
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      if (m_proposals != null) {
        m_proposals = null;
      }
    }

    private void ensureCache() {
      if (m_proposals == null) {
        if (getMethod() != null) {
          IType iPage = TypeUtility.getType(IRuntimeClasses.IPage);
          m_proposals = TypeUtility.getClassesOnClasspath(iPage, getMethod().getType().getJavaProject(), null);
        }
        else {
          m_proposals = CollectionUtility.hashSet();
        }
      }
    }
  }
}
