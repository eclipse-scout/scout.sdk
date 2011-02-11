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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.swt.graphics.Image;

/** <h4>TextCompletionProposalComputer</h4> */
public class NlsTextCompletionProposalComputer implements IJavaCompletionProposalComputer {

  private static final ICompletionProposal[] NO_PROPOSALS = new ICompletionProposal[0];
  private final Image m_image = NlsCore.getImage(NlsCore.ICON_COMMENT);

  @SuppressWarnings("unchecked")
  public List computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    if (!(context instanceof JavaContentAssistInvocationContext)) {
      return Collections.EMPTY_LIST;
    }
    JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
    return Arrays.asList(computeProposals(javaContext));
  }

  @SuppressWarnings("unchecked")
  public List computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
    return new ArrayList<IContextInformation>();
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalComputer#getErrorMessage()
   */
  public String getErrorMessage() {
    return "";
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

  private ICompletionProposal[] computeProposals(JavaContentAssistInvocationContext context) {
    int offset = context.getInvocationOffset();
    IDocument doc = context.getDocument();
    if (doc == null || offset > doc.getLength()) {
      return null;
    }
    try {
      IRegion lineInfo = doc.getLineInformationOfOffset(offset);
      String linePart = doc.get(lineInfo.getOffset(), lineInfo.getLength());//offset - lineInfo.getOffset());
      Matcher m = Pattern.compile("([A-Za-z0-9\\_\\-]*)\\.get\\(\\\"([a-zA-Z0-9\\_\\-]*)").matcher(linePart);
      if (m.find()) {
        String prefix = linePart.substring(m.start(2), offset - lineInfo.getOffset());
        String replacement = m.group(2);
        IType referencedType = getReferencedType(findContextType(context.getCompilationUnit(), offset), m.group(1));
        if (referencedType != null) {
          INlsProject nlsProject = NlsCore.getNlsWorkspace().findNlsProject(referencedType, new NullProgressMonitor());
          if (nlsProject == null) {
            return NO_PROPOSALS;
          }
          List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
          for (INlsEntry e : nlsProject.getEntries(prefix, false)) {
            result.add(new NlsProposal(e, prefix, replacement, offset, m_image));
          }
          return result.toArray(new ICompletionProposal[result.size()]);
        }
      }

    }
    catch (Exception e) {
      NlsCore.logWarning("could not compute nls proposals.",e);
    }
    return NO_PROPOSALS;
  }

  private IType findContextType(ICompilationUnit icu, int offset) throws JavaModelException {
    IJavaElement element = icu.getElementAt(offset);
    if (element == null) {
      return null;
    }
    if (element.getElementType() == IJavaElement.TYPE) {
      return (IType) element;
    }
    return (IType) element.getAncestor(IJavaElement.TYPE);
  }

  private IType getReferencedType(IType declaringType, String typeName) throws JavaModelException {
    ICompilationUnit compilationUnit = declaringType.getCompilationUnit();
    if (compilationUnit != null) {
      IImportDeclaration[] imports = compilationUnit.getImports();
      for (IImportDeclaration imp : imports) {
        if (imp.getElementName().endsWith("." + typeName)) {
          IType foundType = findType(imp.getElementName());
          if (foundType != null) {
            return foundType;
          }
        }
      }
    }
    String[][] resolvedTypeName = declaringType.resolveType(typeName);
    if (resolvedTypeName != null && resolvedTypeName.length == 1) {
      String fqName = resolvedTypeName[0][0];
      if (fqName != null && fqName.length() > 0) {
        fqName = fqName + ".";
      }
      fqName = fqName + resolvedTypeName[0][1];
      IType foundType = findType(fqName);
      if (foundType != null) {
        return foundType;
      }
    }
    NlsCore.logWarning("could not find referenced type '" + typeName + "' in '" + declaringType.getFullyQualifiedName() + "'.");
    return null;
  }

  private IType findType(String fqn) {
    SearchEngine searchEngine = new SearchEngine();
    final List<IType> matchList = new ArrayList<IType>();
    try {
      searchEngine.search(
          SearchPattern.createPattern(
              fqn,
              IJavaSearchConstants.TYPE,
              IJavaSearchConstants.DECLARATIONS,
              SearchPattern.R_EXACT_MATCH),
          new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
          SearchEngine.createWorkspaceScope(),
          new SearchRequestor() {
            @Override
            public void acceptSearchMatch(SearchMatch match) throws CoreException {
              if (match instanceof TypeDeclarationMatch) {
                TypeDeclarationMatch typeMatch = (TypeDeclarationMatch) match;
                IType t = (IType) typeMatch.getElement();
                matchList.add(t);
              }
            }
          }, null);
      if (matchList.size() > 0) {
        return matchList.get(0);
      }
    }
    catch (CoreException e) {
      NlsCore.logWarning(e);
    }
    return null;

  }

}
