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
import java.util.Map.Entry;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;

/**
 * <h3>{@link MethodReturnExpression}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.10.0 30.09.2013
 */
public class MethodReturnExpression {
  private Expression m_returnExpression;
  private String m_returnClause;
  private volatile Map<SimpleName, IType> m_referencedTypes;

  public MethodReturnExpression() {
  }

  public Expression getReturnExpression() {
    return m_returnExpression;
  }

  public void setReturnExpression(Expression returnExpression) {
    m_returnExpression = returnExpression;
  }

  public String getReturnStatement() {
    return getReturnStatement(null);
  }

  @Override
  public String toString() {
    return getReturnStatement();
  }

  public String getReturnStatement(IImportValidator validator) {
    if (validator == null) {
      // favor the return clause if no IImportValidator is given
      if (m_returnClause != null) {
        return m_returnClause;
      }
      else {
        return getReturnExpressionAsString(null);
      }
    }
    else {
      // favor creating source from the expression if a IImportValidator is given
      if (m_returnExpression != null) {
        return getReturnExpressionAsString(validator);
      }
      else {
        return m_returnClause;
      }
    }
  }

  public void setReturnClause(String returnClause) {
    m_returnClause = returnClause;
  }

  public Map<SimpleName, IType> getReferencedTypes() {
    return Collections.unmodifiableMap(getReferencedTypesInternal());
  }

  public void addReferencedType(SimpleName node, IType t) {
    getReferencedTypesInternal().put(node, t);
  }

  private String getReturnExpressionAsString(IImportValidator validator) {
    if (m_returnExpression == null) {
      return null;
    }

    String originalExpression = m_returnExpression.toString();
    if (validator == null) {
      return originalExpression;
    }

    int returnExpressionStart = m_returnExpression.getStartPosition();
    int lastPos = 0;
    StringBuilder builder = new StringBuilder(originalExpression.length() * 2);
    for (Entry<SimpleName, IType> typeNodes : getReferencedTypesInternal().entrySet()) {
      String fqn = typeNodes.getValue().getFullyQualifiedName();
      String scopeQualifiedName = validator.getTypeName(SignatureCache.createTypeSignature(fqn));

      boolean isPrimaryType = fqn.indexOf('$') < 0;
      boolean isSimpleName = scopeQualifiedName.indexOf('.') < 0;
      if (!isSimpleName && isPrimaryType) {
        // we must qualify the type reference
        int relPos = typeNodes.getKey().getStartPosition() - returnExpressionStart;
        builder.append(originalExpression.substring(lastPos, relPos));
        builder.append(scopeQualifiedName);
        lastPos = relPos + typeNodes.getKey().getLength();
      }
    }
    builder.append(originalExpression.substring(lastPos, originalExpression.length()));
    return builder.toString();
  }

  private Map<SimpleName, IType> getReferencedTypesInternal() {
    Map<SimpleName, IType> result = m_referencedTypes;
    if (result == null) {
      synchronized (this) {
        result = m_referencedTypes;
        if (result == null) {
          result = new LinkedHashMap<SimpleName, IType>();
          m_referencedTypes = result;
        }
      }
    }
    return result;
  }
}
