/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.util;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.osgi.framework.Bundle;

/**
 * <h3>{@link ContentAssistContextInfo}</h3> Context info helper to be used in a
 * {@link IJavaCompletionProposalComputer}.<br>
 * Use {@link #build(ContentAssistInvocationContext, String, IProgressMonitor)} to create instances.
 *
 * @since 6.0.200
 */
public class ContentAssistContextInfo {
  private final int m_offset;
  private final ICompilationUnit m_compilationUnit;
  private final String m_identifierPrefix;
  private final SourceViewer m_viewer;

  protected ContentAssistContextInfo(int offset, ICompilationUnit compilationUnit, String identifierPrefix, SourceViewer sourceViewer) {
    m_offset = offset;
    m_compilationUnit = compilationUnit;
    m_identifierPrefix = identifierPrefix;
    m_viewer = sourceViewer;
  }

  /**
   * Creates a new instance of {@link ContentAssistContextInfo}.
   *
   * @param context
   *          The invocation context from the {@link IJavaCompletionProposalComputer}.
   * @param callingPluginId
   *          The plug-in symbolic name of the plug-in containing the proposal computer.
   * @param monitor
   *          The {@link IProgressMonitor} to use or {@code null} if no monitoring is used.
   * @return An {@link ContentAssistContextInfo} instance or {@code null} if it could not be build based on the given
   *         input.
   */
  @SuppressWarnings("pmd:NPathComplexity")
  public static ContentAssistContextInfo build(ContentAssistInvocationContext context, String callingPluginId, IProgressMonitor monitor) {
    if (!(context instanceof JavaContentAssistInvocationContext javaContext)) {
      return null;
    }

    var bundle = Platform.getBundle(callingPluginId);
    if (bundle == null || bundle.getState() != Bundle.ACTIVE) {
      return null;
    }

    var compilationUnit = javaContext.getCompilationUnit();
    if (!JdtUtils.exists(compilationUnit) || !JdtUtils.exists(compilationUnit.getJavaProject())) {
      return null;
    }

    var offset = javaContext.getInvocationOffset();
    if (offset < 0) {
      return null;
    }

    if (!(context.getViewer() instanceof SourceViewer)) {
      return null;
    }

    if (monitor != null && monitor.isCanceled()) {
      return null;
    }

    var identifierPrefix = computeIdentifierPrefix(javaContext);
    if (monitor != null && monitor.isCanceled()) {
      return null;
    }
    return new ContentAssistContextInfo(offset, compilationUnit, identifierPrefix, (SourceViewer) context.getViewer());
  }

  protected static String computeIdentifierPrefix(ContentAssistInvocationContext javaContext) {
    try {
      var prefix = javaContext.computeIdentifierPrefix();
      if (Strings.hasText(prefix)) {
        return Strings.trim(prefix).toString();
      }
    }
    catch (BadLocationException e) {
      SdkLog.warning("Unable to compute identifier prefix.", e);
    }
    return null;
  }

  /**
   * @return The offset in the file where the content assist is invoked (is always >0).
   */
  public int getOffset() {
    return m_offset;
  }

  /**
   * @return The {@link ICompilationUnit} in which the content assist was invoked. Is never {@code null}.
   */
  public ICompilationUnit getCompilationUnit() {
    return m_compilationUnit;
  }

  /**
   * @return A {@link String} with the identifier prefix or {@code null} if there was no prefix.
   */
  public String getIdentifierPrefix() {
    return m_identifierPrefix;
  }

  /**
   * @return The {@link SourceViewer} in which the content assist is performed. Never returns {@code null}.
   */
  public SourceViewer getViewer() {
    return m_viewer;
  }

  /**
   * @return computes the innermost {@link IJavaElement} that surrounds the offset of the invocation (see
   *         {@link #getOffset()}. May return {@code null} in case no element could be computed.
   */
  public IJavaElement computeEnclosingElement() {
    try {
      var element = m_compilationUnit.getElementAt(m_offset);
      if (JdtUtils.exists(element) && JdtUtils.exists(element.getJavaProject())) {
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
    var prime = 31;
    var result = 1;
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
    var other = (ContentAssistContextInfo) obj;
    return m_offset == other.m_offset
        && Objects.equals(m_identifierPrefix, other.m_identifierPrefix)
        && Objects.equals(m_compilationUnit, other.m_compilationUnit);
  }
}
