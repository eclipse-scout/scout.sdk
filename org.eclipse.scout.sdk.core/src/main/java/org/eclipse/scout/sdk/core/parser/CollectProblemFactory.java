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
package org.eclipse.scout.sdk.core.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

/**
 * {@link IProblemFactory} that collects all problems for later use.
 */
class CollectProblemFactory extends DefaultProblemFactory {

  private final List<CharSequence> m_errorMessages;

  CollectProblemFactory() {
    m_errorMessages = new ArrayList<>(0); // we do not expect errors
  }

  @Override
  public CategorizedProblem createProblem(char[] originatingFileName, int problemId, String[] problemArguments, String[] messageArguments, int severity, int startPosition, int endPosition, int lineNumber, int columnNumber) {
    return createProblem(originatingFileName, problemId, problemArguments, 0, messageArguments, severity, startPosition, endPosition, lineNumber, columnNumber);
  }

  @Override
  public CategorizedProblem createProblem(char[] originatingFileName, int problemId, String[] problemArguments,
      int elaborationId, String[] messageArguments, int severity, int startPosition, int endPosition, int lineNumber, int columnNumber) {
    if ((severity & (ProblemSeverities.Error | ProblemSeverities.Fatal | ProblemSeverities.InternalError)) != 0) {
      StringBuilder msg = new StringBuilder();
      if (originatingFileName != null) {
        msg.append(originatingFileName).append(":");
      }
      if (lineNumber > 0) {
        msg.append(lineNumber);
      }
      if (msg.length() > 0) {
        msg.append(" ");
      }
      String txt = getLocalizedMessage(problemId, elaborationId, messageArguments);
      if (txt != null) {
        msg.append(txt);
      }

      m_errorMessages.add(msg);
    }
    return null;
  }

  public void reset() {
    m_errorMessages.clear();
  }

  public List<CharSequence> getErrors() {
    return m_errorMessages;
  }
}
