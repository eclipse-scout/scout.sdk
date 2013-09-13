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
package org.eclipse.scout.sdk.util.type;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;

/**
 *
 */
public final class MethodFilters {

  private MethodFilters() {
  }

  public static IMethodFilter getFilterWithAnnotation(final IType annotationType) {
    return new IMethodFilter() {
      @Override
      public boolean accept(IMethod method) {
        IAnnotation annotation = method.getAnnotation(annotationType.getElementName());
        if (TypeUtility.exists(annotation)) {
          return true;
        }
        annotation = method.getAnnotation(annotationType.getFullyQualifiedName());
        return TypeUtility.exists(annotation);
      }

      @Override
      public String toString() {
        StringBuilder text = new StringBuilder();
        text.append("filter for method with annotation ");
        if (annotationType != null) {
          text.append(annotationType.getFullyQualifiedName());
        }
        return text.toString();
      }
    };
  }

  /**
   * @param methodName
   * @return a filter stop iterating after the first method name match
   */
  public static IMethodFilter getNameFilter(final String methodName) {
    return new IMethodFilter() {
      @Override
      public boolean accept(IMethod method) {
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

  public static IMethodFilter getNameRegexFilter(final Pattern regex) {
    return new IMethodFilter() {
      @Override
      public boolean accept(IMethod method) {
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

  public static IMethodFilter getFlagsFilter(final int flags) {
    return new IMethodFilter() {
      @Override
      public boolean accept(IMethod method) {
        try {
          return (method.getFlags() & flags) == flags;
        }
        catch (JavaModelException e) {
          SdkUtilActivator.logError("could not get flags of method '" + method.getElementName() + "' in type '" + method.getDeclaringType().getFullyQualifiedName() + "'.", e);
          return false;
        }
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

  public static IMethodFilter getSuperMethodFilter(final IMethod method) {
    return new IMethodFilter() {
      @Override
      public boolean accept(IMethod candidate) {
        if (TypeUtility.exists(candidate)) {
          if (candidate.getElementName().equals(method.getElementName())) {
            String[] candidateParameters = candidate.getParameterTypes();
            String[] methodParameters = method.getParameterTypes();
            if (methodParameters.length == candidateParameters.length) {
              for (int i = 0; i < candidateParameters.length; i++) {
                String cParam = candidateParameters[i];
                String mParam = methodParameters[i];
                int cArrCount = Signature.getArrayCount(cParam);
                int mArrCount = Signature.getArrayCount(mParam);
                if (cArrCount != mArrCount) {
                  return false;
                }
                cParam = cParam.substring(cArrCount);
                mParam = mParam.substring(mArrCount);
                mParam = Signature.getTypeErasure(mParam);
                cParam = Signature.getTypeErasure(cParam);
                if (Signature.getTypeSignatureKind(cParam) == Signature.TYPE_VARIABLE_SIGNATURE ||
                    Signature.getTypeSignatureKind(mParam) == Signature.TYPE_VARIABLE_SIGNATURE) {
                  continue;
                }
                else if (!Signature.getSignatureSimpleName(mParam).equals(Signature.getSignatureSimpleName(cParam))) {
                  return false;

                }
              }
              return true;
            }
          }
        }
        return false;
      }
    };
  }

  public static IMethodFilter getMultiMethodFilter(final IMethodFilter... filters) {
    return new IMethodFilter() {
      @Override
      public boolean accept(IMethod method) throws CoreException {
        if (filters == null) {
          return true;
        }
        else {
          for (IMethodFilter f : filters) {
            if (!f.accept(method)) {
              return false;
            }

          }
          return true;
        }
      }

      @Override
      public String toString() {
        StringBuilder text = new StringBuilder();
        text.append("filters{");
        if (filters != null) {
          for (int i = 0; i < filters.length; i++) {
            text.append(filters[i]);
            if (i < filters.length - 1) {
              text.append(", ");
            }
          }
        }
        return text.toString();
      }
    };
  }

}
