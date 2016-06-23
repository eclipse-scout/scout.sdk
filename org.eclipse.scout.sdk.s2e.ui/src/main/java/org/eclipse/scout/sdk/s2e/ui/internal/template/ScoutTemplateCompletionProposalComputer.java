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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.osgi.framework.Bundle;

/**
 * <h3>{@link ScoutTemplateCompletionProposalComputer}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ScoutTemplateCompletionProposalComputer implements IJavaCompletionProposalComputer {

  @Override
  public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    if (!(context instanceof JavaContentAssistInvocationContext)) {
      return Collections.emptyList();
    }

    try {
      Bundle bundle = Platform.getBundle(S2ESdkUiActivator.PLUGIN_ID);
      if (bundle == null || bundle.getState() != Bundle.ACTIVE) {
        return Collections.emptyList();
      }

      JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
      CompletionContext coreContext = javaContext.getCoreContext();
      if (coreContext == null || !coreContext.isExtended() || coreContext.isInJavadoc() || coreContext.isInJavadocText()) {
        return Collections.emptyList();
      }

      int offset = javaContext.getInvocationOffset();
      if (offset < 0) {
        return Collections.emptyList();
      }

      IJavaElement element = coreContext.getEnclosingElement();
      if (!S2eUtils.exists(element) || element.getElementType() != IJavaElement.TYPE) {
        return Collections.emptyList();
      }

      IType t = (IType) element;
      if (t.isBinary()) {
        return Collections.emptyList();
      }

      // check if we are in the middle of a statement. This may happen e.g. on annotations. The enclosing element is even though the IType holding the annotation
      Document d = new Document(t.getCompilationUnit().getSource());
      IRegion lineInformationOfOffset = d.getLineInformationOfOffset(offset);
      String lineSource = d.get(lineInformationOfOffset.getOffset(), lineInformationOfOffset.getLength());
      if (lineSource.indexOf('@') >= 0 || lineSource.indexOf('.') >= 0 || lineSource.indexOf('(') >= 0 || lineSource.indexOf(')') >= 0) {
        return Collections.emptyList();
      }

      String prefix = null;
      CharSequence computedPrefix = javaContext.computeIdentifierPrefix();
      if (StringUtils.isNotEmpty(computedPrefix)) {
        prefix = computedPrefix.toString();
      }

      return ScoutTemplateProposalFactory.createTemplateProposals(t, offset, prefix);
    }
    catch (Exception e) {
      SdkLog.error("Error calculating Scout template proposals.", e);
    }

    return Collections.emptyList();
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
