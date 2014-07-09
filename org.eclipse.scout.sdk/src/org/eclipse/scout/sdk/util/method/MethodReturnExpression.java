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
package org.eclipse.scout.sdk.util.method;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.core.dom.NaiveASTFlattener;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * <h3>{@link MethodReturnExpression}</h3>
 *
 * @author Matthias Villiger
 * @since 3.10.0 30.09.2013
 */
@SuppressWarnings("restriction")
public class MethodReturnExpression {

  private Expression m_returnExpression;
  private String m_returnClause;
  private volatile Map<SimpleName, IJavaElement> m_referencedElements;

  private static ISimpleNameAstFlattenerProviderService astFlattenerProviderService;
  static {
    BundleContext context = ScoutSdk.getDefault().getBundle().getBundleContext();
    ServiceReference<ISimpleNameAstFlattenerProviderService> reference = context.getServiceReference(ISimpleNameAstFlattenerProviderService.class);
    try {
      if (reference != null) {
        ISimpleNameAstFlattenerProviderService service = context.getService(reference);
        if (service != null) {
          astFlattenerProviderService = service;
        }
        else {
          ScoutSdk.logWarning("No valid AST Flattener Provider service has been registered.");
        }
      }
      else {
        ScoutSdk.logWarning("No AST Flattener Provider service has been registered.");
      }
    }
    finally {
      context.ungetService(reference);
    }
  }

  public MethodReturnExpression() {
  }

  public Expression getReturnExpression() {
    return m_returnExpression;
  }

  public void setReturnExpression(Expression returnExpression) {
    m_returnExpression = returnExpression;
  }

  public String getReturnStatement() {
    return getReturnStatement((IImportValidator) null);
  }

  @Override
  public String toString() {
    return getReturnStatement();
  }

  public String getReturnStatement(IImportValidator validator) {
    return getReturnStatement(validator, null);
  }

  public String getReturnStatement(IImportValidator validator, IJavaProject classPath) {
    return getReturnStatement(validator, classPath, null);
  }

  public String getReturnStatement(IImportValidator validator, IJavaProject classPath, IMethodReturnExpressionRewrite rewrite) {
    if (validator == null) {
      // favor the return clause if no IImportValidator is given
      if (m_returnClause != null) {
        return m_returnClause;
      }
      else {
        return getReturnExpressionAsString(new DefaultRewrite(null, classPath, rewrite));
      }
    }
    else {
      // favor creating source from the expression if a IImportValidator is given
      if (m_returnExpression != null) {
        return getReturnExpressionAsString(new DefaultRewrite(validator, classPath, rewrite));
      }
      else {
        return m_returnClause;
      }
    }
  }

  public String getReturnStatement(ISimpleNameAstFlattenerCallback callback) {
    return getReturnExpressionAsString(callback);
  }

  public void setReturnClause(String returnClause) {
    m_returnClause = returnClause;
  }

  public Map<SimpleName, IJavaElement> getReferencedElements() {
    return Collections.unmodifiableMap(getReferencedElementsInternal());
  }

  public void addReferencedElement(SimpleName node, IJavaElement e) {
    getReferencedElementsInternal().put(node, e);
  }

  private IAstRewriteFlattener createFlattener(ISimpleNameAstFlattenerCallback callback) {
    if (astFlattenerProviderService != null) {
      IAstRewriteFlattener flattener = astFlattenerProviderService.createAstFlattener(callback);
      if (flattener != null) {
        return flattener;
      }
    }
    return new NaiveAstFlattener(callback);
  }

  private String getReturnExpressionAsString(ISimpleNameAstFlattenerCallback callback) {
    if (m_returnExpression == null) {
      return null;
    }
    IAstRewriteFlattener flattener = createFlattener(callback);
    return flattener.rewrite(m_returnExpression);
  }

  private Map<SimpleName, IJavaElement> getReferencedElementsInternal() {
    Map<SimpleName, IJavaElement> result = m_referencedElements;
    if (result == null) {
      synchronized (this) {
        result = m_referencedElements;
        if (result == null) {
          result = new LinkedHashMap<SimpleName, IJavaElement>();
          m_referencedElements = result;
        }
      }
    }
    return result;
  }

  protected class DefaultRewrite implements ISimpleNameAstFlattenerCallback {

    private final IMethodReturnExpressionRewrite m_nestedRewrite;
    private final IImportValidator m_validator;
    private final IJavaProject m_classPath;

    public DefaultRewrite(IImportValidator validator, IJavaProject classPath, IMethodReturnExpressionRewrite followUpRewrite) {
      m_nestedRewrite = followUpRewrite;
      m_validator = validator;
      m_classPath = classPath;
    }

    @Override
    public boolean rewriteElement(SimpleName node, StringBuffer buffer) {
      IJavaElement element = getReferencedElementsInternal().get(node);
      if (TypeUtility.exists(element)) {
        switch (element.getElementType()) {
          case IJavaElement.FIELD:
            return rewriteField(node, (IField) element, m_validator, m_classPath, buffer);
          case IJavaElement.TYPE:
            return rewriteType(node, (IType) element, m_validator, m_classPath, buffer);
        }
      }
      return false;
    }

    private boolean rewriteField(SimpleName node, IField f, IImportValidator validator, IJavaProject classPath, StringBuffer buffer) {
      int fieldFlags = 0;
      int typeFlags = 0;
      IType declaringType = f.getDeclaringType();

      try {
        fieldFlags = f.getFlags();
        typeFlags = declaringType.getFlags();
      }
      catch (JavaModelException e) {
        ScoutSdk.logError(e);
      }

      if (Flags.isInterface(typeFlags) || (Flags.isStatic(fieldFlags) && Flags.isFinal(fieldFlags))) { // only constants
        if ((Flags.isInterface(typeFlags) || Flags.isPublic(fieldFlags)) && (classPath == null || TypeUtility.isOnClasspath(declaringType, classPath))) {
          // reference to field possible
          buffer.append(f.getElementName());
          return true;
        }
        else {
          // extract value
          try {
            String value = PropertyMethodSourceUtility.getFieldValue(f);
            if (validator != null) {
              String typeSignature = f.getTypeSignature();
              int kind = SignatureUtility.getTypeSignatureKind(typeSignature);
              if (kind != Signature.BASE_TYPE_SIGNATURE) { // no need to rewrite primitive types
                // check if we must qualify the source of the field
                String scopeQualifiedName = validator.getTypeName(typeSignature);
                String simpleName = Signature.getSignatureSimpleName(typeSignature);
                if (CompareUtility.notEquals(scopeQualifiedName, simpleName)) {
                  if (!value.contains("." + simpleName)) {
                    // the type must be qualified
                    value = value.replace(simpleName, scopeQualifiedName);
                  }
                }
              }
            }

            if (value != null) {
              if (node.getParent() instanceof QualifiedName) {
                buffer.delete(0, buffer.length());
              }
              buffer.append(value);
              return true;
            }
            else if (m_nestedRewrite != null) {
              return m_nestedRewrite.rewriteElement(node, f, validator, classPath, buffer);
            }
          }
          catch (JavaModelException e) {
            // no source found
            if (m_nestedRewrite != null) {
              return m_nestedRewrite.rewriteElement(node, f, validator, classPath, buffer);
            }
          }
        }
      }
      else if (m_nestedRewrite != null) {
        return m_nestedRewrite.rewriteElement(node, f, validator, classPath, buffer);
      }
      return false;
    }

    private boolean rewriteType(SimpleName node, IType type, IImportValidator validator, IJavaProject classPath, StringBuffer buffer) {
      if (validator != null && (classPath == null || TypeUtility.isOnClasspath(type, classPath))) {
        String fqn = type.getFullyQualifiedName();
        String scopeQualifiedName = validator.getTypeName(SignatureCache.createTypeSignature(fqn));
        boolean isPrimaryType = fqn.indexOf('$') < 0;
        boolean isSimpleName = scopeQualifiedName.indexOf('.') < 0;
        if (!isSimpleName && isPrimaryType) {
          // we must qualify the type reference
          buffer.append(scopeQualifiedName);
          return true;
        }
      }
      else if (m_nestedRewrite != null) {
        return m_nestedRewrite.rewriteElement(node, type, validator, classPath, buffer);
      }
      return false;
    }
  }

  public static boolean visit(SimpleName node, ISimpleNameAstFlattenerCallback callback, StringBuffer buffer) {
    if (callback != null) {
      if (callback.rewriteElement(node, buffer)) {
        // rewrite done
        return false;
      }
    }
    buffer.append(node.getIdentifier()); // default
    return false;
  }

  public static boolean visit(QualifiedName node, StringBuffer buffer, ASTVisitor v) {
    int lengthBefore = buffer.length();
    node.getQualifier().accept(v);
    int lengthAfter = buffer.length();
    if (lengthAfter > lengthBefore) {
      buffer.append('.');
    }
    node.getName().accept(v);
    return false;
  }

  private static final class NaiveAstFlattener implements IAstRewriteFlattener {

    private final ISimpleNameAstFlattenerCallback m_callback;

    public NaiveAstFlattener(ISimpleNameAstFlattenerCallback callback) {
      m_callback = callback;
    }

    @Override
    public String rewrite(ASTNode n) {
      NaiveASTFlattener flattener = new NaiveASTFlattener() {
        @Override
        public boolean visit(SimpleName node) {
          return MethodReturnExpression.visit(node, m_callback, buffer);
        }

        @Override
        public boolean visit(QualifiedName node) {
          return MethodReturnExpression.visit(node, buffer, this);
        }
      };
      n.accept(flattener);
      return flattener.getResult();
    }
  }
}
