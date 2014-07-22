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
package org.eclipse.scout.sdk.ui.internal.extensions.codecompletion;

import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.swt.graphics.Point;

/**
 * <h3>{@link AbstractSdkProposal}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 25.10.2013
 */
public abstract class AbstractSdkProposal implements IJavaCompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3, ICompletionProposalExtension4 {

  @Override
  public int getRelevance() {
    return 0;
  }

  @Override
  public void apply(IDocument document) {
  }

  @Override
  public void apply(IDocument document, char trigger, int offset) {
  }

  @Override
  public String getAdditionalProposalInfo() {
    return null;
  }

  @Override
  public IContextInformation getContextInformation() {
    return null;
  }

  @Override
  public int getContextInformationPosition() {
    return 0;
  }

  @Override
  public IInformationControlCreator getInformationControlCreator() {
    return null;
  }

  @Override
  public int getPrefixCompletionStart(IDocument document, int completionOffset) {
    return 0;
  }

  @Override
  public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
    return null;
  }

  @Override
  public Point getSelection(IDocument document) {
    return null;
  }

  @Override
  public boolean isValidFor(IDocument document, int offset) {
    return false;
  }

  @Override
  public char[] getTriggerCharacters() {
    return null;
  }

  @Override
  public boolean isAutoInsertable() {
    return false;
  }

  @Override
  public void unselected(ITextViewer viewer) {
  }

  /**
   * @param declaringType
   * @param offset
   * @param subtypeFilter
   * @return
   * @throws JavaModelException
   */
  protected IJavaElement findSibling(IType declaringType, int offset, ITypeFilter subtypeFilter) throws JavaModelException {
    Set<IType> innerTypes = TypeUtility.getInnerTypes(declaringType, subtypeFilter, TypeComparators.getSourceRangeComparator());
    IType sibling = null;
    for (IType t : innerTypes) {
      ISourceRange sourceRange = t.getSourceRange();
      if (sourceRange.getOffset() > offset) {
        sibling = t;
        break;
      }
    }

    return sibling;
  }
}
