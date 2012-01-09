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
package org.eclipse.scout.sdk.util.jdt;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.osgi.framework.Version;

public final class JdtUtility {
  private JdtUtility() {
  }

  public static IJavaElement findJavaElement(IFile javaFile, ITextSelection selection) throws JavaModelException {
    IJavaElement javaElement = JavaCore.create(javaFile);
    javaElement = findJavaElement(javaElement, selection.getOffset(), selection.getLength());
    return javaElement;
  }

  public static boolean hasAnnotation(IAnnotatable element, String fullyQuallifiedAnnotation) {
    return TypeUtility.exists(getAnnotation(element, fullyQuallifiedAnnotation));
  }

  public static boolean isPlatformE4() {
    return JdtUtility.getPlatformVersion().getMajor() == 4;
  }

  public static Version getPlatformVersion() {
    return Platform.getProduct().getDefiningBundle().getVersion();
  }

  public static IAnnotation getAnnotation(IAnnotatable element, String fullyQuallifiedAnnotation) {
    try {
      IAnnotation annotation = element.getAnnotation(fullyQuallifiedAnnotation);
      // workaround since annotations are not cached properly from jdt
      if (TypeUtility.exists(annotation) && (annotation.getSource() == null || annotation.getSource().startsWith("@"))) {
        return annotation;
      }
      else {
        String simpleName = Signature.getSimpleName(fullyQuallifiedAnnotation);
        annotation = element.getAnnotation(simpleName);
        if (TypeUtility.exists(annotation) && (annotation.getSource() == null || annotation.getSource().startsWith("@"))) {
          return annotation;
        }
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logError("could not get annotation '" + fullyQuallifiedAnnotation + "' of '" + element + "'", e);
    }
    return null;
  }

  /**
   * Escape the given string in java style.
   * 
   * @param s
   *          The string to escape.
   * @return A new string with backslashes and double-quotes escaped in java style.
   */
  public static String escapeStringJava(String s) {
    if (s == null) return null;
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  /**
   * converts the given string into a string literal with leading and ending double-quotes including escaping of the
   * given value.
   * 
   * @param s
   *          the string to convert.
   * @return the literal string.
   */
  public static String toStringLiteral(String s) {
    return "\"" + escapeStringJava(s) + "\"";
  }

  public static IJavaElement findJavaElement(IJavaElement element, int offset, int lenght) throws JavaModelException {
    switch (element.getElementType()) {
      case IJavaElement.COMPILATION_UNIT:
        ICompilationUnit icu = (ICompilationUnit) element;
        IType[] icuTypes = icu.getTypes();
        for (IType t : icuTypes) {
          if (t.getSourceRange().getOffset() < offset && (t.getSourceRange().getOffset() + t.getSourceRange().getLength()) > (offset + lenght)) {
            // step in
            return findJavaElement(t, offset, lenght);
          }
        }
        if (icuTypes.length > 0) {
          return icuTypes[0];
        }

        break;
      case IJavaElement.TYPE:
        for (IType t : ((IType) element).getTypes()) {
          if (t.getSourceRange().getOffset() < offset && (t.getSourceRange().getOffset() + t.getSourceRange().getLength()) > (offset + lenght)) {
            // step in
            return findJavaElement(t, offset, lenght);
          }
        }
        // methods
        for (IMethod m : ((IType) element).getMethods()) {
          if (m.getSourceRange().getOffset() < offset && (m.getSourceRange().getOffset() + m.getSourceRange().getLength()) > (offset + lenght)) {
            // step in
            return findJavaElement(m, offset, lenght);
          }
        }
        break;
    }
    return element;
  }

  public static IType findDeclaringType(IJavaElement element) {
    if (element == null) {
      return null;
    }
    if (element.getElementType() == IJavaElement.TYPE) {
      return (IType) element;
    }
    return findDeclaringType(element.getParent());
  }
}
