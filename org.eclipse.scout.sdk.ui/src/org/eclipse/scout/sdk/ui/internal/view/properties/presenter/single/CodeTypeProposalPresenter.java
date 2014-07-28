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

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementLabelProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractTypeProposalPresenter;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>CodeTypePresenter</h3>
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
   * <h3>{@link P_ContentProvider}</h3> The local lazy content provider.
   * It is kept lazy to ensure the proposals are only loaded when used. So the creation of the property view is
   * performance optimized.
   *
   * @author Andreas Hoegger
   * @since 3.8.0 15.02.2012
   */
  private final class P_ContentProvider extends AbstractCachedTypeContentProposalProvider {

    private P_ContentProvider(ILabelProvider labelProvider) {
      super(labelProvider);
    }

    @Override
    protected Set<?> computeProposals() {
      IType iCodeType = TypeUtility.getType(IRuntimeClasses.ICodeType);

      String genericSignature = null;
      try {
        String paramName = IRuntimeClasses.TYPE_PARAM_VALUEFIELD__VALUE_TYPE;
        IMethod defaultMethod = getMethod().getDefaultMethod();
        String[] typeParameters = Signature.getTypeArguments(defaultMethod.getReturnType());
        if (typeParameters != null && typeParameters.length == 1) {
          typeParameters = Signature.getTypeArguments(typeParameters[0]);
          if (typeParameters != null && typeParameters.length == 1) {
            paramName = Signature.getSignatureSimpleName(typeParameters[0]);
          }
        }
        ITypeHierarchy supertypeHierarchy = TypeUtility.getSupertypeHierarchy(getType());
        genericSignature = SignatureUtility.resolveGenericParameterInSuperHierarchy(getType(), supertypeHierarchy, defaultMethod.getDeclaringType().getFullyQualifiedName(), paramName);

      }
      catch (CoreException e) {
        ScoutSdkUi.logError(e);
      }

      ITypeFilter filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getNoGenericTypesFilter(), TypeFilters.getTypeParamSubTypeFilter(genericSignature, IRuntimeClasses.ICodeType, IRuntimeClasses.TYPE_PARAM_CODETYPE__CODE_ID));
      return TypeUtility.getClassesOnClasspath(iCodeType, getType().getJavaProject(), filter);
    }
  }
}
