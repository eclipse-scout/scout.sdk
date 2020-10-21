/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import static java.util.Collections.emptyList;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.util.ContentAssistContextInfo;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link ScoutTemplateCompletionProposalComputer}</h3>
 *
 * @since 5.2.0
 */
public class ScoutTemplateCompletionProposalComputer implements IJavaCompletionProposalComputer {

  @Override
  @SuppressWarnings("pmd:NPathComplexity")
  public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    try {
      var info = ContentAssistContextInfo.build(context, S2ESdkUiActivator.PLUGIN_ID, monitor);
      if (info == null) {
        return emptyList();
      }

      var compilationUnit = info.getCompilationUnit();
      var offset = info.getOffset();

      // check if we are in the middle of a statement. This may happen e.g. on annotations. The enclosing element is even though the IType holding the annotation
      IDocument d = new Document(compilationUnit.getSource());
      var lineInformationOfOffset = d.getLineInformationOfOffset(offset);
      var lineSource = d.get(lineInformationOfOffset.getOffset(), lineInformationOfOffset.getLength());
      if (lineSource.indexOf('@') >= 0 || lineSource.indexOf(JavaTypes.C_DOT) >= 0 || lineSource.indexOf('(') >= 0 || lineSource.indexOf(')') >= 0) {
        return emptyList();
      }

      compilationUnit.reconcile(ICompilationUnit.NO_AST, false, false, null, null); // reconcile in case it was a very fast edit and CTRL+space afterwards.
      var element = info.computeEnclosingElement();
      if (!JdtUtils.exists(element) || element.getElementType() != IJavaElement.TYPE) {
        return emptyList();
      }
      if (monitor != null && monitor.isCanceled()) {
        return emptyList();
      }

      return ScoutTemplateProposalFactory.createTemplateProposals((IType) element, offset, info.getIdentifierPrefix(), info.getViewer());
    }
    catch (JavaModelException | BadLocationException e) {
      SdkLog.error("Error calculating Scout template proposals.", e);
      return emptyList();
    }
  }

  @Override
  public void sessionStarted() {
    // nop
  }

  @Override
  public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    return emptyList();
  }

  @Override
  public String getErrorMessage() {
    return null;
  }

  @Override
  public void sessionEnded() {
    // nop
  }
}
