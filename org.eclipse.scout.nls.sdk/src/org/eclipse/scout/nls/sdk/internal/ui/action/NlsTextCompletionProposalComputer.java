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
package org.eclipse.scout.nls.sdk.internal.ui.action;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;

/** <h4>TextCompletionProposalComputer</h4> */
public class NlsTextCompletionProposalComputer implements IJavaCompletionProposalComputer {
  /** The wrapped processor. */
  private NlsTextCompletionProposalProcessor m_processor = new NlsTextCompletionProposalProcessor();;

  @SuppressWarnings("unchecked")
  public List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    if (!(context instanceof JavaContentAssistInvocationContext)) {
      return Collections.EMPTY_LIST;
    }
    JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
    return Arrays.asList(m_processor.computeCompletionProposals(javaContext.getViewer(), context.getInvocationOffset()));
  }

  @SuppressWarnings("unchecked")
  public List computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    return Arrays.asList(m_processor.computeContextInformation(context.getViewer(), context.getInvocationOffset()));
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalComputer#getErrorMessage()
   */
  public String getErrorMessage() {
    return m_processor.getErrorMessage();
  }

  /*
   * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer#sessionStarted()
   */
  public void sessionStarted() {
  }

  /*
   * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer#sessionEnded()
   */
  public void sessionEnded() {
  }

}
