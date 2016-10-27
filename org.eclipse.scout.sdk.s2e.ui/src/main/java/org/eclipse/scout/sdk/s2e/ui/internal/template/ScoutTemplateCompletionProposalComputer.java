/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.template;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.util.ContentAssistContextInfo;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link ScoutTemplateCompletionProposalComputer}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ScoutTemplateCompletionProposalComputer implements IJavaCompletionProposalComputer {

  @Override
  @SuppressWarnings("pmd:NPathComplexity")
  public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    try {
      ContentAssistContextInfo info = ContentAssistContextInfo.build(context, S2ESdkUiActivator.PLUGIN_ID, monitor);
      if (info == null) {
        return Collections.emptyList();
      }

      ICompilationUnit compilationUnit = info.getCompilationUnit();
      int offset = info.getOffset();

      // check if we are in the middle of a statement. This may happen e.g. on annotations. The enclosing element is even though the IType holding the annotation
      Document d = new Document(compilationUnit.getSource());
      IRegion lineInformationOfOffset = d.getLineInformationOfOffset(offset);
      String lineSource = d.get(lineInformationOfOffset.getOffset(), lineInformationOfOffset.getLength());
      if (lineSource.indexOf('@') >= 0 || lineSource.indexOf('.') >= 0 || lineSource.indexOf('(') >= 0 || lineSource.indexOf(')') >= 0) {
        return Collections.emptyList();
      }

      compilationUnit.reconcile(ICompilationUnit.NO_AST, false, false, null, null); // reconcile in case it was a very fast edit and CTRL+space afterwards.
      IJavaElement element = info.computeEnclosingElement();
      if (!S2eUtils.exists(element) || element.getElementType() != IJavaElement.TYPE) {
        return Collections.emptyList();
      }

      return ScoutTemplateProposalFactory.createTemplateProposals((IType) element, offset, info.getIdentifierPrefix());
    }
    catch (Exception e) {
      SdkLog.error("Error calculating Scout template proposals.", e);
      return Collections.emptyList();
    }
  }

  @Override
  public void sessionStarted() {
    // nop
  }

  @Override
  public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    return Collections.emptyList();
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
