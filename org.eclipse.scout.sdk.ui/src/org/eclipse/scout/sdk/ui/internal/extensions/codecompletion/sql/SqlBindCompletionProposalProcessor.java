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

import java.util.HashSet;
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
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
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
  private final IType AbstractFormData = TypeUtility.getType(RuntimeClasses.AbstractFormData);
  private final IType AbstractFormFieldData = TypeUtility.getType(RuntimeClasses.AbstractFormFieldData);
  private final IType AbstractPropertyData = TypeUtility.getType(RuntimeClasses.AbstractPropertyData);

  private final Image m_image;
  private static final Pattern REGEX_QUOTES = Pattern.compile("\\\"");
  private static final Pattern REGEX_BIND = Pattern.compile(".*\\:[A-Za-z0-9\\._-]*$");
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
      HashSet<ICompletionProposal> collector = new HashSet<ICompletionProposal>();
      ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(formData);
      for (IType t : TypeUtility.getInnerTypes(formData, TypeFilters.getSubtypeFilter(AbstractFormFieldData, hierarchy))) {
        String propName = getBeanName(t.getElementName());
        SqlBindProposal prop = new SqlBindProposal(propName, prefix, context.getInvocationOffset(), m_image);
        collector.add(prop);
        addInnerTypesInSuperClasses(t, t.newSupertypeHierarchy(null), collector, prefix, propName + ".", context);
      }
      for (IType t : TypeUtility.getInnerTypes(formData, TypeFilters.getSubtypeFilter(AbstractPropertyData, hierarchy))) {
        String propName = t.getElementName();
        propName = getBeanName(propName.replaceAll("Property$", ""));
        SqlBindProposal prop = new SqlBindProposal(propName, prefix, context.getInvocationOffset(), m_image);
        collector.add(prop);
      }

      TreeMap<String, ICompletionProposal> sorted = new TreeMap<String, ICompletionProposal>();
      for (ICompletionProposal p : collector) {
        if (p.getDisplayString().toLowerCase().startsWith(prefix.toLowerCase())) {
          sorted.put(p.getDisplayString(), p);
        }
      }

      return sorted.values().toArray(new ICompletionProposal[sorted.values().size()]);
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("error during creating sql copletion.", e);
    }
    return NO_PROPOSALS;
  }

  private String getBeanName(String elementName) {
    if (elementName == null) {
      return null;
    }
    if (elementName.length() > 1) {
      return Character.toLowerCase(elementName.charAt(0)) + elementName.substring(1);
    }
    return elementName.toLowerCase();
  }

  private void addInnerTypesInSuperClasses(IType baseType, org.eclipse.jdt.core.ITypeHierarchy baseTypeSuperHierarchy, HashSet<ICompletionProposal> collector,
      String prefix, String namePrefix, JavaContentAssistInvocationContext context) throws JavaModelException {
    for (IType superClass : baseTypeSuperHierarchy.getAllSuperclasses(baseType)) {
      ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(superClass);
      for (IType innerType : TypeUtility.getInnerTypes(superClass, TypeFilters.getSubtypeFilter(AbstractFormFieldData, hierarchy))) {
        SqlBindProposal prop = new SqlBindProposal(namePrefix + getBeanName(innerType.getElementName()), prefix, context.getInvocationOffset(), m_image);
        collector.add(prop);
        addInnerTypesInSuperClasses(innerType, innerType.newSupertypeHierarchy(null), collector, prefix, prop.getDisplayString() + ".", context);
      }
    }
  }

  private boolean isSqlStatementLocation(ITextViewer viewer, int offset) throws BadLocationException {
    IDocument doc = viewer.getDocument();
    if (doc == null || offset > doc.getLength()) {
      return false;
    }
    IRegion lineInfo = doc.getLineInformationOfOffset(offset);
    String linePart = doc.get(lineInfo.getOffset(), offset - lineInfo.getOffset());
    Matcher m = REGEX_QUOTES.matcher(linePart);
    boolean stringLocation = false;
    while (m.find()) {
      stringLocation = !stringLocation;
    }
    if (stringLocation) {
      stringLocation = REGEX_BIND.matcher(linePart).matches();
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
    while (--offset >= 0 && (Character.isLetterOrDigit(doc.getChar(offset)) || doc.getChar(offset) == '.' || doc.getChar(offset) == '_' || doc.getChar(offset) == '-')) {
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
