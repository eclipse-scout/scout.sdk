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
package org.eclipse.scout.sdk.s2e.nls.internal.ui.proposal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link AbstractNlsTextCompletionComputer}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 23.10.2013
 */
public abstract class AbstractNlsTextCompletionComputer implements IJavaCompletionProposalComputer {

  private static final Pattern PATTERN = Pattern.compile("([A-Za-z0-9\\_\\-]*)\\.get\\(\\\"([a-zA-Z0-9\\_\\-]*)");

  @Override
  public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    if (!(context instanceof JavaContentAssistInvocationContext)) {
      return new ArrayList<>(0);
    }
    JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
    return computeProposals(javaContext);
  }

  protected List<ICompletionProposal> computeProposals(JavaContentAssistInvocationContext context) {
    List<ICompletionProposal> proposals = new ArrayList<>();
    int offset = context.getInvocationOffset();
    IDocument doc = context.getDocument();
    if (doc == null || offset > doc.getLength()) {
      return proposals;
    }
    try {
      IRegion lineInfo = doc.getLineInformationOfOffset(offset);
      String linePart = doc.get(lineInfo.getOffset(), lineInfo.getLength());
      Matcher m = PATTERN.matcher(linePart);
      int cursorPosInLine = offset - lineInfo.getOffset();
      int matchingStart = -1;
      String refType = null;
      while (m.find()) {
        int match = m.start(2);
        if (match <= cursorPosInLine && match > matchingStart) {
          matchingStart = match; // find closest match left to the cursor
          refType = m.group(1);
        }
      }

      if (matchingStart >= 0 && refType != null) {
        String prefix = linePart.substring(matchingStart, offset - lineInfo.getOffset());
        IType contextType = findContextType(context.getCompilationUnit(), offset);
        if (JdtUtils.exists(contextType)) {
          INlsProject nlsProject = NlsCore.getNlsWorkspace().getNlsProject(new Object[]{contextType, contextType});
          if (nlsProject != null) {
            collectProposals(proposals, nlsProject, prefix, offset);
          }
        }
      }
    }
    catch (Exception e) {
      SdkLog.warning("could not compute nls proposals.", e);
    }
    return proposals;
  }

  protected abstract void collectProposals(List<ICompletionProposal> proposals, INlsProject nlsProject, String prefix, int offset);

  @Override
  public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    return Collections.emptyList();
  }

  @Override
  public String getErrorMessage() {
    return "";
  }

  @Override
  public void sessionStarted() {
  }

  @Override
  public void sessionEnded() {
  }

  private static IType findContextType(ICompilationUnit icu, int offset) throws JavaModelException {
    IJavaElement element = icu.getElementAt(offset);
    if (element == null) {
      return null;
    }
    if (element.getElementType() == IJavaElement.TYPE) {
      return (IType) element;
    }
    return (IType) element.getAncestor(IJavaElement.TYPE);
  }
}
