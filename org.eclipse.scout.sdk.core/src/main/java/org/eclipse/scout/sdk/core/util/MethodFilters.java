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
package org.eclipse.scout.sdk.core.util;

import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IMethod;

/**
 * Contains {@link IFilter}s for {@link IMethod}s.
 */
public final class MethodFilters {

  private MethodFilters() {
  }

  /**
   * Creates and gets a filter accepting all {@link IMethod}s having an annotation with given annotation name.
   *
   * @param annotationTypeFqn
   *          The fully qualified annotation name.
   * @return The new created {@link IFilter}.
   */
  public static IFilter<IMethod> annotationName(final String annotationTypeFqn) {
    return new IFilter<IMethod>() {
      @Override
      public boolean evaluate(IMethod method) {
        return CoreUtils.getAnnotation(method, annotationTypeFqn) != null;
      }

      @Override
      public String toString() {
        StringBuilder text = new StringBuilder("filter for methods with annotation ");
        text.append(annotationTypeFqn);
        return text.toString();
      }
    };
  }

  /**
   * Creates and gets a filter accepting all {@link IMethod}s having the given method name.
   *
   * @param methodName
   *          The method name
   * @return The new created {@link IFilter}.
   */
  public static IFilter<IMethod> name(final String methodName) {
    return new IFilter<IMethod>() {
      @Override
      public boolean evaluate(IMethod method) {
        return method.getElementName().equals(methodName);
      }

      @Override
      public String toString() {
        StringBuilder text = new StringBuilder();
        text.append("filter for method with method name ");
        if (methodName != null) {
          text.append(methodName);
        }
        return text.toString();
      }
    };
  }

  /**
   * Creates and gets a filter accepting all {@link IMethod}s whose method name matches the given regular expression.
   *
   * @param regex
   *          The regular expression pattern
   * @return The new created {@link IFilter}.
   * @see Pattern
   */
  public static IFilter<IMethod> nameRegex(final Pattern regex) {
    return new IFilter<IMethod>() {
      @Override
      public boolean evaluate(IMethod method) {
        return regex.matcher(method.getElementName()).matches();
      }

      @Override
      public String toString() {
        StringBuilder text = new StringBuilder();
        text.append("filter for method where method name matches regex ");
        if (regex != null) {
          text.append(regex.toString());
        }
        return text.toString();
      }
    };
  }

  /**
   * Creates and gets a method filter that accepts all methods having ALL of the given flags
   *
   * @param flags
   *          The flags that each accepted method must have.
   * @return The new created {@link IFilter}.
   */
  public static IFilter<IMethod> flags(final int flags) {
    return new IFilter<IMethod>() {
      @Override
      public boolean evaluate(IMethod method) {
        return (method.getFlags() & flags) == flags;
      }

      @Override
      public String toString() {
        StringBuilder text = new StringBuilder();
        text.append("filter for method with method flags [ ");
        text.append(Flags.toString(flags));
        text.append("]");
        return text.toString();
      }
    };
  }

}
