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

import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.scout.commons.beans.FastBeanUtility;
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

      IType abstractFormFieldData = TypeUtility.getType(IRuntimeClasses.AbstractFormFieldData);
      IType abstractPropertyData = TypeUtility.getType(IRuntimeClasses.AbstractPropertyData);
      if (!TypeUtility.exists(abstractPropertyData) || !TypeUtility.exists(abstractFormFieldData)) {
        return Collections.emptyList();
      }

      String prefix = getPrefix(context.getViewer(), context.getInvocationOffset());
      HashSet<ICompletionProposal> collector = new HashSet<>();
      ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(formData);
      for (IType t : TypeUtility.getInnerTypes(formData, TypeFilters.getSubtypeFilter(abstractFormFieldData, hierarchy))) {
        String propName = FastBeanUtility.decapitalize(NamingUtility.ensureStartWithUpperCase(t.getElementName()));
        SqlBindProposal prop = new SqlBindProposal(propName, prefix, context.getInvocationOffset(), m_image);
        collector.add(prop);
        addInnerTypesInSuperClasses(t, TypeUtility.getSupertypeHierarchy(t), collector, prefix, propName + ".", context);
      }
      for (IType t : TypeUtility.getInnerTypes(formData, TypeFilters.getSubtypeFilter(abstractPropertyData, hierarchy))) {
        String propName = NamingUtility.ensureStartWithUpperCase(t.getElementName());
        propName = FastBeanUtility.decapitalize(propName.replaceAll("Property$", ""));
        SqlBindProposal prop = new SqlBindProposal(propName, prefix, context.getInvocationOffset(), m_image);
        collector.add(prop);
      }

      TreeMap<String, ICompletionProposal> sorted = new TreeMap<>();
      for (ICompletionProposal p : collector) {
        if (p.getDisplayString().toLowerCase().startsWith(prefix.toLowerCase())) {
          sorted.put(p.getDisplayString(), p);
        }
      }

      return CollectionUtility.arrayList(sorted.values());
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning("error while creating sql completion.", e);
      return Collections.emptyList();
    }
  }

  private void addInnerTypesInSuperClasses(IType baseType, ITypeHierarchy baseTypeSuperHierarchy, HashSet<ICompletionProposal> collector, String prefix, String namePrefix, JavaContentAssistInvocationContext context) throws JavaModelException {
    IType abstractFormFieldData = TypeUtility.getType(IRuntimeClasses.AbstractFormFieldData);

    for (IType superClass : baseTypeSuperHierarchy.getSuperClassStack(baseType, false)) {
      ITypeHierarchy hierarchy = TypeUtility.getLocalTypeHierarchy(superClass);
      for (IType innerType : TypeUtility.getInnerTypes(superClass, TypeFilters.getSubtypeFilter(abstractFormFieldData, hierarchy))) {
        SqlBindProposal prop = new SqlBindProposal(namePrefix + FastBeanUtility.decapitalize(NamingUtility.ensureStartWithUpperCase(innerType.getElementName())), prefix, context.getInvocationOffset(), m_image);
        collector.add(prop);
        addInnerTypesInSuperClasses(innerType, TypeUtility.getSupertypeHierarchy(innerType), collector, prefix, prop.getDisplayString() + ".", context);
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

  private IType getFormDataParameterType(JavaContentAssistInvocationContext context) throws CoreException {
    IType abstractFormData = TypeUtility.getType(IRuntimeClasses.AbstractFormData);
    if (TypeUtility.exists(abstractFormData)) {
      IJavaElement element = context.getCoreContext().getEnclosingElement();
      if (element.getElementType() == IJavaElement.METHOD) {
        IMethod method = (IMethod) element;
        for (String parameter : method.getParameterTypes()) {
          String fqs = SignatureUtility.getResolvedSignature(parameter, method.getDeclaringType());
          if (SignatureUtility.getTypeSignatureKind(fqs) == Signature.CLASS_TYPE_SIGNATURE) {
            IType candidate = TypeUtility.getTypeBySignature(fqs);
            if (TypeUtility.exists(candidate)) {
              ITypeHierarchy supertypeHierarchy = TypeUtility.getSupertypeHierarchy(candidate);
              if (supertypeHierarchy != null && supertypeHierarchy.contains(abstractFormData)) {
                return candidate;
              }
            }
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
