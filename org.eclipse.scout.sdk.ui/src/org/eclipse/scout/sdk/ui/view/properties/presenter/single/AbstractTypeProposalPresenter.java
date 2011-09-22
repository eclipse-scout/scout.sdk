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
package org.eclipse.scout.sdk.ui.view.properties.presenter.single;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.internal.fields.proposal.JavaClassProposal;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtilities;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

public abstract class AbstractTypeProposalPresenter extends AbstractProposalPresenter<JavaClassProposal> {

  private String m_labelMethodName;
  private boolean m_includeNullProposal;
  private static final JavaClassProposal NULL_PROPOSAL = new JavaClassProposal(ScoutIdeProperties.NULL, ScoutSdkUi.getImage(ScoutSdkUi.Default), null);

  public AbstractTypeProposalPresenter(FormToolkit toolkit, Composite parent, String labelMethodName, boolean includeNullProposal) {
    super(toolkit, parent);
    m_labelMethodName = labelMethodName;
    m_includeNullProposal = includeNullProposal;
  }

  protected abstract IType[] provideScoutTypes(IJavaProject project, IType ownerType) throws CoreException;

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    ArrayList<JavaClassProposal> proposals = new ArrayList<JavaClassProposal>();
    for (IType t : provideScoutTypes(method.getType().getJavaProject(), method.getType())) {
      proposals.add(new JavaClassProposal(ScoutProposalUtility.getFieldName(t, m_labelMethodName), ScoutSdkUi.getImage(ScoutSdkUi.Default), t));
    }
    if (m_includeNullProposal) {
      proposals.add(NULL_PROPOSAL);
    }
    setProposals(proposals.toArray(new JavaClassProposal[proposals.size()]));
    super.init(method);
  }

  @Override
  protected JavaClassProposal parseInput(String input) throws CoreException {
    IType referedType = PropertyMethodSourceUtilities.parseReturnParameterClass(input, getMethod().peekMethod());
    return findProposal(referedType);
  }

  @Override
  protected synchronized void storeValue(final JavaClassProposal value) {
    IOperation op = null;
    if (ScoutSdkUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName()) {
        @Override
        protected String createMethodBody(IMethod methodToOverride, IImportValidator validator) throws JavaModelException {
          StringBuilder source = new StringBuilder();
          source.append("  return ");
          if (value != null && value.getJavaClass() != null) {
            IType javaClass = value.getJavaClass();
            source.append(ScoutSdkUtility.getSimpleTypeRefName(Signature.createTypeSignature(javaClass.getFullyQualifiedName(), true), validator) + ".class;");
          }
          else {
            source.append("null;");
          }
          return source.toString();
        }
      };
      ((ConfigPropertyMethodUpdateOperation) op).setFormatSource(true);
    }
    new OperationJob(op).schedule();
  }

  private JavaClassProposal findProposal(IType type) {
    for (JavaClassProposal prop : getProposals()) {
      if (CompareUtility.equals(prop.getJavaClass(), type)) {
        return prop;
      }
    }
    return null;
  }

  @Override
  protected void createContextMenu(MenuManager manager) {
    super.createContextMenu(manager);
    if (getCurrentSourceValue() != null) {
      final IType t = getCurrentSourceValue().getJavaClass();
      if (t != null) {
        manager.add(new Action(Texts.get("GoTo") + t.getElementName(), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.StatusInfo)) {
          @Override
          public void run() {
            showJavaElementInEditor(t);
          }
        });
      }
    }
  }

}
