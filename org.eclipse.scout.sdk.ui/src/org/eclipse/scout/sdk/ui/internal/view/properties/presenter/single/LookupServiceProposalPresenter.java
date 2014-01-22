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
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementLabelProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractTypeProposalPresenter;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>CodeTypePresenter</h3> ...
 */
public class LookupServiceProposalPresenter extends AbstractTypeProposalPresenter {

  private P_ContentProvider m_contentProvider;

  public LookupServiceProposalPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected void createProposalFieldProviders(ProposalTextField proposalField) {
    JavaElementLabelProvider labelProvider = new JavaElementLabelProvider();
    m_contentProvider = new P_ContentProvider(labelProvider);
    getProposalField().setLabelProvider(labelProvider);
    getProposalField().setContentProvider(m_contentProvider);
  }

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    if (method != null) {
      m_contentProvider.setType(method.getType());
      getProposalField().setInput(method.getType());
      super.init(method);
    }
    else {
      m_contentProvider.setType(null);
      getProposalField().setInput(null);
    }
  }

  @Override
  public synchronized void dispose() {
    m_contentProvider.dispose();
    super.dispose();
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
  private static final class P_ContentProvider extends AbstractCachedTypeContentProposalProvider {

    private P_ContentProvider(ILabelProvider labelProvider) {
      super(labelProvider);
    }

    @Override
    protected Object[] computeProposals() {
      IType iLookupService = TypeUtility.getType(IRuntimeClasses.ILookupService);

      String genericSignature = null;
      try {
        genericSignature = SignatureUtility.resolveGenericParameterInSuperHierarchy(getType(), getType().newSupertypeHierarchy(null), IRuntimeClasses.ILookupCall, IRuntimeClasses.TYPE_PARAM_LOOKUPCALL__KEY_TYPE);
      }
      catch (CoreException e) {
        ScoutSdkUi.logError(e);
      }

      ICachedTypeHierarchy typeHierarchy = TypeUtility.getPrimaryTypeHierarchy(iLookupService);
      ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getNoGenericTypesFilter(),
          TypeFilters.getTypesOnClasspath(getType().getJavaProject()),
          TypeFilters.getTypeParamSubTypeFilter(genericSignature, IRuntimeClasses.ILookupService, IRuntimeClasses.TYPE_PARAM_LOOKUPSERVICE__KEY_TYPE));
      return typeHierarchy.getAllSubtypes(iLookupService, filter, TypeComparators.getTypeNameComparator());
    }
  }
}
