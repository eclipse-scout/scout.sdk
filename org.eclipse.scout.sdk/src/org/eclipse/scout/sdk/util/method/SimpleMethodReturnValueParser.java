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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.internal.ScoutSdk;

/**
 * <h3>{@link SimpleMethodReturnValueParser}</h3> Simple and fast method return clause parser that will only handle
 * simple (e.g. literal) clauses (without references to other types). Furthermore it is capable to handle the
 * TEXTS.get(...) methods of the nls methods.
 * 
 * @author Matthias Villiger
 * @since 3.10.0 30.09.2013
 */
public final class SimpleMethodReturnValueParser implements IMethodReturnValueParser {

  public static final Pattern REGEX_PROPERTY_METHOD_REPRESENTER_VALUE = Pattern.compile("\\{.*return\\s*((.*\\\".*\\\".*)|([^\\\"]*))\\s*\\;.*\\}", Pattern.DOTALL);
  public static final Pattern REGEX_TYPE_REFERENCE = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]{0,200})\\.{1}", Pattern.DOTALL);

  public static final IMethodReturnValueParser INSTANCE = new SimpleMethodReturnValueParser();

  private SimpleMethodReturnValueParser() {
  }

  @Override
  public MethodReturnExpression parse(IMethod method) {
    try {
      String src = method.getSource();
      if (src != null) {
        Matcher m = REGEX_PROPERTY_METHOD_REPRESENTER_VALUE.matcher(src);
        if (m.find()) {
          String returnClause = m.group(1).trim();
          if (returnClause.indexOf('.') < 0) {
            // fast pre-check: no dot in the return clause -> literal
            return createExpression(returnClause);
          }
          else {
            // check if it is a reference to another type
            Matcher typeRefCheck = REGEX_TYPE_REFERENCE.matcher(returnClause);
            if (typeRefCheck.find()) {
              // a reference to another type was found
              String typeRefName = typeRefCheck.group(1).trim();
              if ("TEXTS".equals(typeRefName) || "ScoutTexts".equals(typeRefName)) {
                // it is the nls texts class -> directly parse
                return createExpression(returnClause);
              }
            }
            else {
              // no type reference. use the result
              return createExpression(returnClause);
            }
          }
        }
      }
      else {
        ScoutSdk.logWarning("Could not find source for method '" + method.getElementName() + "' in type '" + method.getDeclaringType().getFullyQualifiedName() + "'.");
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logError("Could not find return value of method '" + method.getElementName() + "' in type '" + method.getDeclaringType().getFullyQualifiedName() + "'.", e);
    }
    return null; // fall back to the next parser that may be able to calculate a value.
  }

  private MethodReturnExpression createExpression(String clause) {
    MethodReturnExpression e = new MethodReturnExpression();
    e.setReturnClause(clause);
    return e;
  }
}
