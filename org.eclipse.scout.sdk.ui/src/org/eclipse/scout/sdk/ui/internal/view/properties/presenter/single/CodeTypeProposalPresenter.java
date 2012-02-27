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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.StaticContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementLabelProvider;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractTypeProposalPresenter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>CodeTypePresenter</h3> ...
 */
public class CodeTypeProposalPresenter extends AbstractTypeProposalPresenter {
  final IType iCodeType = TypeUtility.getType(RuntimeClasses.ICodeType);

  public CodeTypeProposalPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected void createProposalFieldProviders(ProposalTextField proposalField) {
    // done in init
    JavaElementLabelProvider labelProvider = new JavaElementLabelProvider();
    getProposalField().setLabelProvider(labelProvider);
    getProposalField().setContentProvider(new P_ContentProvider(labelProvider));
  }

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    if (method != null) {
      getProposalField().setInput(method.getType());
    }
    else {
      getProposalField().setInput(null);
    }
    super.init(method);
  }

  /**
   * <h3>{@link P_ContentProvider}</h3> ...
   * The local lazy content provider.
   * It is kept lazy to ensure the proposals are only loaded when used. So the creation of the property view is
   * performance optimized.
   * 
   * @author Andreas Hoegger
   * @since 3.8.0 15.02.2012
   */
  private class P_ContentProvider extends StaticContentProvider {

    private P_ContentProvider(ILabelProvider labelProvider) {
      super(null, labelProvider);
    }

    @Override
    public Object[] getProposals(String searchPattern, IProgressMonitor monitor) {
      ensureCache();
      return super.getProposals(searchPattern, monitor);
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      setElements(null);
    }

    private void ensureCache() {
      if (getElements() == null) {
        if (getMethod() != null) {
          setElements(TypeUtility.getPrimaryTypeHierarchy(iCodeType).getAllSubtypes(iCodeType, TypeFilters.getTypesOnClasspath(getMethod().getType().getJavaProject()), TypeComparators.getTypeNameComparator()));
        }
        else {
          setElements(new IType[0]);
        }
      }
    }
  }
}
