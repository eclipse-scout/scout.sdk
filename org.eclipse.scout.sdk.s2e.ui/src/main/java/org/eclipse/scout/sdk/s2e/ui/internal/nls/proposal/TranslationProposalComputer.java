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
package org.eclipse.scout.sdk.s2e.ui.internal.nls.proposal;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.callInEclipseEnvironment;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.core.s.nls.TranslationStores;

/**
 * <h3>{@link TranslationProposalComputer}</h3>
 *
 * @since 7.0.0
 */
public class TranslationProposalComputer implements IJavaCompletionProposalComputer {

  private static final Pattern PATTERN = Pattern.compile("([A-Za-z0-9_\\-]*)\\.get\\(\"([a-zA-Z0-9_\\-]*)");

  @Override
  public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    if (!(context instanceof JavaContentAssistInvocationContext)) {
      return emptyList();
    }
    JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
    return computeProposals(javaContext);
  }

  protected static List<ICompletionProposal> computeProposals(JavaContentAssistInvocationContext context) {
    int offset = context.getInvocationOffset();
    IDocument doc = context.getDocument();
    if (doc == null || offset > doc.getLength()) {
      return emptyList();
    }

    try {
      IRegion lineInfo = doc.getLineInformationOfOffset(offset);
      String linePart = doc.get(lineInfo.getOffset(), lineInfo.getLength());
      Matcher m = PATTERN.matcher(linePart);
      int cursorPosInLine = offset - lineInfo.getOffset();
      int matchingStart = -1;
      while (m.find()) {
        int match = m.start(2);
        if (match <= cursorPosInLine && match > matchingStart) {
          matchingStart = match; // find closest match left to the cursor
        }
      }

      if (matchingStart >= 0) {
        String prefix = linePart.substring(matchingStart, offset - lineInfo.getOffset());
        Path path = context.getCompilationUnit().getResource().getLocation().toFile().toPath();

        return callInEclipseEnvironment(
            (env, progress) -> TranslationStores.createFullStack(path, env, progress)
                .map(stack -> collectProposals(stack, prefix, offset))
                .orElseGet(Collections::emptyList))
                    .result();
      }
    }
    catch (RuntimeException | BadLocationException e) {
      SdkLog.warning("Could not compute translation proposals.", e);
    }
    return emptyList();
  }

  protected static List<ICompletionProposal> collectProposals(TranslationStoreStack stack, String prefix, int offset) {
    List<ICompletionProposal> result = stack.allWithPrefix(prefix)
        .map(t -> new TranslationProposal(t, prefix, offset))
        .collect(toList());
    if (stack.isEditable()) {
      result.add(new TranslationNewProposal(stack, prefix, offset));
    }
    return result;
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
  public void sessionStarted() {
    // nop
  }

  @Override
  public void sessionEnded() {
    // nop
  }
}
