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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.executor.AbstractWizardExecutor;
import org.eclipse.scout.sdk.ui.executor.selection.ScoutStructuredSelection;
import org.eclipse.scout.sdk.ui.extensions.executor.IExecutor;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.DeleteEdit;

/**
 * <h3>{@link AbstractSdkWizardProposal}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 11.07.2014
 */
public abstract class AbstractSdkWizardProposal extends AbstractSdkProposal {

  private final IType m_declaringType;
  private final IType m_siblingSubTypeFilter;
  private final int m_startOffset;

  protected AbstractSdkWizardProposal(IType declaringType, IType siblingSubTypeFilter, int startOffset) {
    m_declaringType = declaringType;
    m_siblingSubTypeFilter = siblingSubTypeFilter;
    m_startOffset = startOffset;
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
        // do not use the filter using the TypeUtility.getLocalTypeHierarchy(getDeclaringType()) because in dirty compilation units, this hierarchy may be empty!
        filter = TypeFilters.getSubtypeFilter(m_siblingSubTypeFilter);
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

    String name = null;
    IOperation removePrefixOp = null;
    if (m_startOffset >= 0) {
      ICompilationUnit icu = getDeclaringType().getCompilationUnit();
      IDocument document = viewer.getDocument();
      String source = document.get();
      if (source != null && source.length() >= offset && offset >= m_startOffset) {
        name = NamingUtility.ensureStartWithUpperCase(source.substring(m_startOffset, offset));
        removePrefixOp = new P_RemovePrefixOperation(icu, document, viewer.getTextWidget().getDisplay(), m_startOffset, offset);
      }
    }

    ScoutStructuredSelection selection = new ScoutStructuredSelection(new Object[]{getDeclaringType()});
    selection.setSibling(proposal);
    selection.setTypeName(name);

    IExecutor executor = createExecutor();
    if (removePrefixOp != null && executor instanceof AbstractWizardExecutor) {
      AbstractWizardExecutor wizEx = (AbstractWizardExecutor) executor;
      wizEx.addAdditionalPerformFinishOperation(removePrefixOp, -10.0d); // execute before wizard finish
    }

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

  private static final class P_RemovePrefixOperation implements IOperation {

    private final IDocument m_document;
    private final int m_start, m_end;
    private final ICompilationUnit m_icu;
    private final Display m_display;

    private P_RemovePrefixOperation(ICompilationUnit icu, IDocument doc, Display display, int start, int end) {
      m_document = doc;
      m_display = display;
      m_start = start;
      m_end = end;
      m_icu = icu;
    }

    @Override
    public String getOperationName() {
      return "Remove Prefix";
    }

    @Override
    public void validate() {
    }

    @Override
    public void run(final IProgressMonitor monitor, final IWorkingCopyManager workingCopyManager) throws CoreException {
      final DeleteEdit removePrefix = new DeleteEdit(m_start, m_end - m_start);
      final IHolder<Exception> exHolder = new Holder<Exception>(Exception.class, null);
      m_display.syncExec(new Runnable() {
        @Override
        public void run() {
          try {
            workingCopyManager.register(m_icu, monitor);
            removePrefix.apply(m_document);
          }
          catch (Exception e) {
            exHolder.setValue(e);
          }
        }
      });

      Exception e = exHolder.getValue();
      if (e != null) {
        throw new CoreException(new ScoutStatus("Unable to delete prefix.", e));
      }

      m_icu.getBuffer().setContents(m_document.get());
      workingCopyManager.reconcile(m_icu, monitor);
    }
  }
}
