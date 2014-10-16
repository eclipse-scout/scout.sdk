/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.internal.extensions.codecompletion;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.executor.selection.ScoutStructuredSelection;
import org.eclipse.scout.sdk.ui.extensions.executor.IExecutor;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link AbstractSdkWizardProposal}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 11.07.2014
 */
public abstract class AbstractSdkWizardProposal extends AbstractSdkProposal {

  private final IType m_declaringType;
  private final IType m_siblingSubTypeFilter;

  protected AbstractSdkWizardProposal(IType declaringType, IType siblingSubTypeFilter) {
    m_declaringType = declaringType;
    m_siblingSubTypeFilter = siblingSubTypeFilter;
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  protected abstract IExecutor createExecutor();

  protected IJavaElement findSibling(int sourceRangeOffset) {
    IJavaElement sibling = null;
    try {
      ITypeFilter filter = null;
      if (m_siblingSubTypeFilter != null) {
        filter = TypeFilters.getSubtypeFilter(m_siblingSubTypeFilter, TypeUtility.getLocalTypeHierarchy(getDeclaringType()));
      }
      sibling = findSibling(getDeclaringType(), sourceRangeOffset, filter);
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logWarning("could not find sibling in type '" + getDeclaringType().getFullyQualifiedName() + "'.", e);
    }
    return sibling;
  }

  @Override
  public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
    SiblingProposal proposal = null;
    IJavaElement sibling = findSibling(offset);
    if (TypeUtility.exists(sibling)) {
      proposal = new SiblingProposal(sibling);
    }

    ScoutStructuredSelection selection = new ScoutStructuredSelection(new Object[]{getDeclaringType()});
    selection.setSibling(proposal);

    IExecutor executor = createExecutor();
    Shell shell = ScoutSdkUi.getShell();
    ExecutionEvent event = AbstractScoutHandler.createEvent(shell, selection);

    executor.run(shell, selection, event);
  }

  @Override
  public void selected(ITextViewer viewer, boolean smartToggle) {
  }

  @Override
  public boolean validate(IDocument document, int offset, DocumentEvent event) {
    return true;
  }
}
