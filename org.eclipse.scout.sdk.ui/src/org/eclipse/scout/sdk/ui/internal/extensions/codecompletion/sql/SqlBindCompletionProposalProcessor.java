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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>SqlBindCompletionProposalProcessor</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2010
 */
public class SqlBindCompletionProposalProcessor {
  private final IType AbstractFormData = TypeUtility.getType(IRuntimeClasses.AbstractFormData);
  private final IType AbstractFormFieldData = TypeUtility.getType(IRuntimeClasses.AbstractFormFieldData);
  private final IType AbstractPropertyData = TypeUtility.getType(IRuntimeClasses.AbstractPropertyData);

  private final Image m_image;
  private static final Pattern REGEX_QUOTES = Pattern.compile("\\\"");
  private static final Pattern REGEX_BIND = Pattern.compile(".*\\:[A-Za-z0-9\\._-]*$");

  public SqlBindCompletionProposalProcessor() {
    m_image = ScoutSdkUi.getImage(ScoutSdkUi.Default);
  }

  public List<ICompletionProposal> computeCompletionProposals(JavaContentAssistInvocationContext context) {
    try {
      if (!isSqlStatementLocation(context.getViewer(), context.getInvocationOffset())) {
        return Collections.emptyList();
      }
      IType formData = getFormDataParameterType(context);
      if (formData == null || !formData.exists()) {
        return Collections.emptyList();
      }
      String prefix = getPrefix(context.getViewer(), context.getInvocationOffset());
      HashSet<ICompletionProposal> collector = new HashSet<ICompletionProposal>();
      ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(formData);
      for (IType t : TypeUtility.getInnerTypes(formData, TypeFilters.getSubtypeFilter(AbstractFormFieldData, hierarchy))) {
        String propName = NamingUtility.ensureStartWithLowerCase(t.getElementName());
        SqlBindProposal prop = new SqlBindProposal(propName, prefix, context.getInvocationOffset(), m_image);
        collector.add(prop);
        addInnerTypesInSuperClasses(t, ScoutSdkCore.getHierarchyCache().getSupertypeHierarchy(t), collector, prefix, propName + ".", context);
      }
      for (IType t : TypeUtility.getInnerTypes(formData, TypeFilters.getSubtypeFilter(AbstractPropertyData, hierarchy))) {
        String propName = t.getElementName();
        propName = NamingUtility.ensureStartWithLowerCase(propName.replaceAll("Property$", ""));
        SqlBindProposal prop = new SqlBindProposal(propName, prefix, context.getInvocationOffset(), m_image);
        collector.add(prop);
      }

      TreeMap<String, ICompletionProposal> sorted = new TreeMap<String, ICompletionProposal>();
      for (ICompletionProposal p : collector) {
        if (p.getDisplayString().toLowerCase().startsWith(prefix.toLowerCase())) {
          sorted.put(p.getDisplayString(), p);
        }
      }

      return CollectionUtility.arrayList(sorted.values());
    }
    catch (BadLocationException e) {
      ScoutSdkUi.logWarning("error during creating sql copletion.", e);
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logWarning("error during creating sql copletion.", e);
    }
    return Collections.emptyList();
  }

  private void addInnerTypesInSuperClasses(IType baseType, ITypeHierarchy baseTypeSuperHierarchy, HashSet<ICompletionProposal> collector, String prefix, String namePrefix, JavaContentAssistInvocationContext context) throws JavaModelException {
    for (IType superClass : baseTypeSuperHierarchy.getSuperClassStack(baseType, false)) {
      ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(superClass);
      for (IType innerType : TypeUtility.getInnerTypes(superClass, TypeFilters.getSubtypeFilter(AbstractFormFieldData, hierarchy))) {
        SqlBindProposal prop = new SqlBindProposal(namePrefix + NamingUtility.ensureStartWithLowerCase(innerType.getElementName()), prefix, context.getInvocationOffset(), m_image);
        collector.add(prop);
        addInnerTypesInSuperClasses(innerType, ScoutSdkCore.getHierarchyCache().getSupertypeHierarchy(innerType), collector, prefix, prop.getDisplayString() + ".", context);
      }
    }
  }

  private static boolean isSqlStatementLocation(ITextViewer viewer, int offset) throws BadLocationException {
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
    IJavaElement element = context.getCoreContext().getEnclosingElement();
    if (element.getElementType() == IJavaElement.METHOD) {
      IMethod method = (IMethod) element;
      for (String parameter : method.getParameterTypes()) {
        String fqs = SignatureUtility.getQualifiedSignature(parameter, method.getDeclaringType());
        if (SignatureUtility.getTypeSignatureKind(fqs) == Signature.CLASS_TYPE_SIGNATURE) {
          String fqn = Signature.getSignatureQualifier(fqs) + "." + Signature.getSignatureSimpleName(fqs);
          IType candidate = TypeUtility.getType(fqn);
          if (ScoutSdkCore.getHierarchyCache().getSupertypeHierarchy(candidate).contains(AbstractFormData)) {
            return candidate;
          }
        }
      }
    }
    return null;
  }

  private static String getPrefix(ITextViewer viewer, int offset) throws BadLocationException {
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
}
