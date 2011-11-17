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
package org.eclipse.scout.sdk.ui.internal.extensions.codecompletion;

import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>SqlBindCompletionProposalProcessor</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2010
 */
public class SqlBindCompletionProposalProcessor {
  final IType AbstractFormData = TypeUtility.getType(RuntimeClasses.AbstractFormData);
  final IType AbstractFormFieldData = TypeUtility.getType(RuntimeClasses.AbstractFormFieldData);
  final IType AbstractPropertyData = TypeUtility.getType(RuntimeClasses.AbstractPropertyData);

  private final Image m_image;
  private static final ICompletionProposal[] NO_PROPOSALS = new ICompletionProposal[0];
  private static final IContextInformation[] NO_CONTEXTS = new IContextInformation[0];

  public SqlBindCompletionProposalProcessor() {
    m_image = ScoutSdkUi.getImage(ScoutSdkUi.Default);
  }

  /**
   * {@inheritDoc}
   */
  public ICompletionProposal[] computeCompletionProposals(JavaContentAssistInvocationContext context) {
    try {
      if (!isSqlStatementLocation(context.getViewer(), context.getInvocationOffset())) {
        return NO_PROPOSALS;
      }
      IType formData = getFormDataParameterType(context);
      if (formData == null || !formData.exists()) {
        return NO_PROPOSALS;
      }
      String prefix = getPrefix(context.getViewer(), context.getInvocationOffset());
      TreeMap<String, ICompletionProposal> result = new TreeMap<String, ICompletionProposal>();
      ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(formData);
      for (IType t : TypeUtility.getInnerTypes(formData, TypeFilters.getSubtypeFilter(AbstractFormFieldData, hierarchy))) {
        if (t.getElementName().toLowerCase().startsWith(prefix.toLowerCase())) {
          SqlBindProposal prop = new SqlBindProposal(t.getElementName(), prefix, context.getInvocationOffset(), m_image);
          result.put(prop.getDisplayString(), prop);
        }
      }
      for (IType t : TypeUtility.getInnerTypes(formData, TypeFilters.getSubtypeFilter(AbstractPropertyData, hierarchy))) {
        String propName = t.getElementName();
        propName = propName.replaceAll("Property$", "");
        if (propName.toLowerCase().startsWith(prefix.toLowerCase())) {
          SqlBindProposal prop = new SqlBindProposal(propName, prefix, context.getInvocationOffset(), m_image);
          result.put(prop.getDisplayString(), prop);
        }
      }
      return result.values().toArray(new ICompletionProposal[result.values().size()]);
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("error during creating sql copletion.", e);
    }
    return NO_PROPOSALS;
  }

  private boolean isSqlStatementLocation(ITextViewer viewer, int offset) throws BadLocationException {
    IDocument doc = viewer.getDocument();
    if (doc == null || offset > doc.getLength()) {
      return false;
    }
    IRegion lineInfo = doc.getLineInformationOfOffset(offset);
    String linePart = doc.get(lineInfo.getOffset(), offset - lineInfo.getOffset());
    Matcher m = Pattern.compile("\\\"").matcher(linePart);
    boolean stringLocation = false;
    while (m.find()) {
      stringLocation = !stringLocation;
    }
    if (stringLocation) {
      stringLocation = linePart.matches(".*\\:[A-Za-z0-9_-]*$");
    }
    return stringLocation;
  }

  private IType getFormDataParameterType(JavaContentAssistInvocationContext context) throws JavaModelException {
    ICompilationUnit icu = context.getCompilationUnit();
    IJavaElement element = JdtUtility.findJavaElement(icu, context.getInvocationOffset(), 0);
    if (element.getElementType() == IJavaElement.METHOD) {
      IMethod method = (IMethod) element;
      for (String parameter : method.getParameterTypes()) {
        String fqs = SignatureUtility.getQuallifiedSignature(parameter, method.getDeclaringType());
        if (SignatureUtility.getTypeSignatureKind(fqs) == Signature.CLASS_TYPE_SIGNATURE) {
          String fqn = Signature.getSignatureQualifier(fqs) + "." + Signature.getSignatureSimpleName(fqs);
          IType candidate = TypeUtility.getType(fqn);
          if (candidate.newSupertypeHierarchy(null).contains(AbstractFormData)) {
            return candidate;
          }
        }
      }
    }
    return null;
  }

  private String getPrefix(ITextViewer viewer, int offset) throws BadLocationException {
    IDocument doc = viewer.getDocument();
    if (doc == null || offset > doc.getLength()) {
      return null;
    }

    int length = 0;
    while (--offset >= 0 && Character.isLetterOrDigit(doc.getChar(offset))) {
      length++;
    }
    return doc.get(offset + 1, length);

  }

  /*
   * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
   */
  public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
    // no context informations for hippie completions
    return NO_CONTEXTS;
  }

  /*
   * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
   */
  public char[] getCompletionProposalAutoActivationCharacters() {
    return null;
  }

  /*
   * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
   */
  public char[] getContextInformationAutoActivationCharacters() {
    return null;
  }

  /*
   * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
   */
  public IContextInformationValidator getContextInformationValidator() {
    return null;
  }

  /*
   * @see org.eclipse.jface.text.contentassist.ICompletionProposalComputer#getErrorMessage()
   */
  public String getErrorMessage() {
    return null; // no custom error message
  }
}
