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
package org.eclipse.scout.sdk.operation.method;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.operation.jdt.method.MethodNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;

/**
 *
 */
public class InnerTypeGetterCreateOperation extends MethodNewOperation {

  private final IType m_field;

  public InnerTypeGetterCreateOperation(IType field, IType getterDeclaringType) throws JavaModelException {
    this(field, getterDeclaringType, false);
  }

  public InnerTypeGetterCreateOperation(IType field, IType getterDeclaringType, boolean formatSource) throws JavaModelException {
    super("get" + field.getElementName(), getterDeclaringType, formatSource);
    m_field = field;
    setFlags(Flags.AccPublic);
    setReturnTypeSignature(SignatureCache.createTypeSignature(m_field.getFullyQualifiedName()));
    setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesMethodGetterCommentBuilder());
    setMethodBodySourceBuilder(new IMethodBodySourceBuilder() {
      @Override
      public void createSource(IMethodSourceBuilder methodBuilder, StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
        source.append("return getFieldByClass(");
        source.append(SignatureUtility.getTypeReference(SignatureCache.createTypeSignature(m_field.getFullyQualifiedName()), m_field, validator) + ".class");
        source.append(");");
      }
    });
  }

  public IType getField() {
    return m_field;
  }
}
