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
package org.eclipse.scout.sdk.ui.internal.extensions.codecompletion.sql;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.osgi.framework.Bundle;

/**
 * <h3>SqlBindFromFormDataCompletionComputer</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2010
 */
public class SqlBindFromFormDataCompletionComputer implements IJavaCompletionProposalComputer {

  private SqlBindCompletionProposalProcessor m_processor = null;

  @Override
  public List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    if (!(context instanceof JavaContentAssistInvocationContext)
        || Platform.getBundle(ScoutSdkUi.PLUGIN_ID).getState() != Bundle.ACTIVE) {
      return Collections.EMPTY_LIST;
    }
    if (m_processor == null) {
      m_processor = new SqlBindCompletionProposalProcessor();
    }
    JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;

    return Arrays.asList(m_processor.computeCompletionProposals(javaContext));
  }

  @Override
  public List computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    if (m_processor == null) {
      m_processor = new SqlBindCompletionProposalProcessor();
    }
    return Arrays.asList(m_processor.computeContextInformation(context.getViewer(), context.getInvocationOffset()));
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalComputer#getErrorMessage()
   */
  @Override
  public String getErrorMessage() {
    if (m_processor == null) {
      return "";
    }
    return m_processor.getErrorMessage();
  }

  @Override
  public void sessionEnded() {
  }

  @Override
  public void sessionStarted() {
  }
}
