/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.util;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.osgi.framework.Bundle;

/**
 * <h3>{@link ContentAssistContextInfo}</h3> Context info helper to be used in a
 * {@link IJavaCompletionProposalComputer}.<br>
 * Use {@link #build(ContentAssistInvocationContext, String, IProgressMonitor)} to create instances.
 *
 * @author Matthias Villiger
 * @since 6.0.200
 */
public class ContentAssistContextInfo {
  private final int m_offset;
  private final ICompilationUnit m_compilationUnit;
  private final String m_identifierPrefix;

  protected ContentAssistContextInfo(int offset, ICompilationUnit compilationUnit, String identifierPrefix) {
    m_offset = offset;
    m_compilationUnit = compilationUnit;
    m_identifierPrefix = identifierPrefix;
  }

  /**
   * Creates a new instance of {@link ContentAssistContextInfo}.
   *
   * @param context
   *          The invocation context from the {@link IJavaCompletionProposalComputer}.
   * @param callingPluginId
   *          The plug-in symbolic name of the plug-in containing the proposal computer.
   * @param monitor
   *          The {@link IProgressMonitor} to use or <code>null</code> if no monitoring is used.
   * @return An {@link ContentAssistContextInfo} instance or <code>null</code> if it could not be build based on the
   *         given input.
   */
  public static ContentAssistContextInfo build(ContentAssistInvocationContext context, String callingPluginId, IProgressMonitor monitor) {
    if (!(context instanceof JavaContentAssistInvocationContext)) {
      return null;
    }

    Bundle bundle = Platform.getBundle(callingPluginId);
    if (bundle == null || bundle.getState() != Bundle.ACTIVE) {
      return null;
    }
    if (monitor != null && monitor.isCanceled()) {
      return null;
    }

    JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
    ICompilationUnit compilationUnit = javaContext.getCompilationUnit();
    if (!S2eUtils.exists(compilationUnit)) {
      return null;
    }
    int offset = javaContext.getInvocationOffset();
    if (offset < 0) {
      return null;
    }
    if (monitor != null && monitor.isCanceled()) {
      return null;
    }

    String identifierPrefix = null;
    try {
      CharSequence prefix = javaContext.computeIdentifierPrefix();
      if (StringUtils.isNotBlank(prefix)) {
        identifierPrefix = prefix.toString().trim();
      }
    }
    catch (BadLocationException e) {
      SdkLog.warning("Unable to compute identifier prefix.", e);
      return null;
    }
    if (monitor != null && monitor.isCanceled()) {
      return null;
    }
    return new ContentAssistContextInfo(offset, compilationUnit, identifierPrefix);
  }

  /**
   * @return The offset in the file where the content assist is invoked (is always >0).
   */
  public int getOffset() {
    return m_offset;
  }

  /**
   * @return The {@link ICompilationUnit} in which the content assist was invoked. Is never <code>null</code>.
   */
  public ICompilationUnit getCompilationUnit() {
    return m_compilationUnit;
  }

  /**
   * @return A {@link String} with the identifier prefix or <code>null</code> if there was no prefix.
   */
  public String getIdentifierPrefix() {
    return m_identifierPrefix;
  }

  /**
   * @return computes the innermost {@link IJavaElement} that surrounds the offset of the invocation (see
   *         {@link #getOffset()}. May return <code>null</code> in case no element could be computed.
   */
  public IJavaElement computeEnclosingElement() {
    try {
      IJavaElement element = m_compilationUnit.getElementAt(m_offset);
      if (S2eUtils.exists(element)) {
        return element;
      }
    }
    catch (JavaModelException e) {
      SdkLog.warning("Unable to compute enclosing java element for offset {} in compilation unit '{}'.", m_offset, m_compilationUnit.getElementName(), e);
    }
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_compilationUnit == null) ? 0 : m_compilationUnit.hashCode());
    result = prime * result + ((m_identifierPrefix == null) ? 0 : m_identifierPrefix.hashCode());
    result = prime * result + m_offset;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ContentAssistContextInfo other = (ContentAssistContextInfo) obj;
    if (m_compilationUnit == null) {
      if (other.m_compilationUnit != null) {
        return false;
      }
    }
    else if (!m_compilationUnit.equals(other.m_compilationUnit)) {
      return false;
    }
    if (m_identifierPrefix == null) {
      if (other.m_identifierPrefix != null) {
        return false;
      }
    }
    else if (!m_identifierPrefix.equals(other.m_identifierPrefix)) {
      return false;
    }
    if (m_offset != other.m_offset) {
      return false;
    }
    return true;
  }
}
