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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.DocumentAdapter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

@SuppressWarnings("restriction")
public class JdtUtility {
  private static JdtUtility instance = new JdtUtility();

  private JdtUtility() {
  }

  public static IJavaElement findJavaElement(IFile javaFile, ITextSelection selection) throws JavaModelException {
    return instance.findJavaElementImpl(javaFile, selection);
  }

  private IJavaElement findJavaElementImpl(IFile javaFile, ITextSelection selection) throws JavaModelException {
    IJavaElement javaElement = JavaCore.create(javaFile);
    javaElement = findBestMatch(javaElement, selection.getOffset(), selection.getLength());
    return javaElement;

  }

  public static String getLineSeparator(ICompilationUnit icu) {
    if (icu != null) {
      // since jdt is not capable of decently handling various new line ranges, we must use this internal class
      // to find out what new line char jdt expects....
      try {
        if (icu.getBuffer() instanceof DocumentAdapter) {
          IDocument doc = ((DocumentAdapter) icu.getBuffer()).getDocument();
          if (doc instanceof Document) {
            return ((Document) doc).getDefaultLineDelimiter();
          }
        }
      }
      catch (Throwable t) {
        // nop
      }
    }
    return System.getProperty("line.separator");
  }

  public static IJavaElement findJavaElement(IJavaElement element, int offset, int lenght) throws JavaModelException {
    return instance.findBestMatch(element, offset, lenght);
  }

  private IJavaElement findBestMatch(IJavaElement element, int offset, int lenght) throws JavaModelException {
    switch (element.getElementType()) {
      case IJavaElement.COMPILATION_UNIT:
        ICompilationUnit icu = (ICompilationUnit) element;
        IType[] icuTypes = icu.getTypes();
        for (IType t : icuTypes) {
          if (t.getSourceRange().getOffset() < offset && (t.getSourceRange().getOffset() + t.getSourceRange().getLength()) > (offset + lenght)) {
            // step in
            return findBestMatch(t, offset, lenght);
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
            return findBestMatch(t, offset, lenght);
          }
        }
        // methods
        for (IMethod m : ((IType) element).getMethods()) {
          if (m.getSourceRange().getOffset() < offset && (m.getSourceRange().getOffset() + m.getSourceRange().getLength()) > (offset + lenght)) {
            // step in
            return findBestMatch(m, offset, lenght);
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
