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
import org.eclipse.jdt.core.ITypeHierarchy;
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
public class CodeTypeProposalPresenter extends AbstractTypeProposalPresenter {

  private P_ContentProvider m_contentProvider;

  public CodeTypeProposalPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected void createProposalFieldProviders(ProposalTextField proposalField) {
    // done in init
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
      IType iCodeType = TypeUtility.getType(IRuntimeClasses.ICodeType);

      String genericSignature = null;
      try {
        ITypeHierarchy supertypeHierarchy = getType().newSupertypeHierarchy(null);
        if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IValueField))) {
          genericSignature = SignatureUtility.resolveGenericParameterInSuperHierarchy(getType(), supertypeHierarchy, IRuntimeClasses.IValueField, IRuntimeClasses.TYPE_PARAM_VALUEFIELD__VALUE_TYPE);
        }
        else if (supertypeHierarchy.contains(TypeUtility.getType(IRuntimeClasses.IColumn))) {
          genericSignature = SignatureUtility.resolveGenericParameterInSuperHierarchy(getType(), supertypeHierarchy, IRuntimeClasses.IColumn, IRuntimeClasses.TYPE_PARAM_COLUMN_VALUE_TYPE);
        }
      }
      catch (CoreException e) {
        ScoutSdkUi.logError(e);
      }

      ICachedTypeHierarchy typeHierarchy = TypeUtility.getPrimaryTypeHierarchy(iCodeType);
      ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(),
          TypeFilters.getNoGenericTypesFilter(),
          TypeFilters.getTypesOnClasspath(getType().getJavaProject()),
          TypeFilters.getTypeParamSubTypeFilter(genericSignature, IRuntimeClasses.ICodeType, IRuntimeClasses.TYPE_PARAM_CODETYPE__CODE_ID));
      return typeHierarchy.getAllSubtypes(iCodeType, filter, TypeComparators.getTypeNameComparator());
    }
  }
}
