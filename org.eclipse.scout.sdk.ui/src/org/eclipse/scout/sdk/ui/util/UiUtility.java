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
package org.eclipse.scout.sdk.ui.util;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.jdt.compile.ScoutSeverityManager;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * <h3>{@link UiUtility}</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 07.03.2011
 */
@SuppressWarnings("restriction")
public class UiUtility {
  public static void showJavaElementInEditor(IJavaElement e, boolean createNew) {
    try {
      IEditorPart editor = null;
      if (createNew) {
        editor = JavaUI.openInEditor(e);
      }
      else {
        editor = EditorUtility.isOpenInEditor(e);
        if (editor != null) {
          PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(editor);
        }
      }
      if (editor != null) {
        JavaUI.revealInEditor(editor, e);
        if (editor instanceof ITextEditor) {
          ITextEditor textEditor = (ITextEditor) editor;
          IRegion reg = textEditor.getHighlightRange();
          if (reg != null) {
            textEditor.setHighlightRange(reg.getOffset(), reg.getLength(), true);
          }
        }
      }
    }
    catch (Exception ex) {
      ScoutSdkUi.logWarning(ex);
    }
  }

  public static ImageDescriptor getTypeImageDescriptor(IType type) {
    int flags = -1;
    try {
      flags = type.getFlags();
    }
    catch (JavaModelException e) {
    }

    boolean isInterface = Flags.isInterface(flags);
    boolean isInner = TypeUtility.exists(type.getDeclaringType());
    int severity = ScoutSeverityManager.getInstance().getSeverityOf(type);
    ImageDescriptor desc = JavaElementImageProvider.getTypeImageDescriptor(isInner, isInterface, flags, false);

    int adornmentFlags = 0;
    if (Flags.isFinal(flags)) {
      adornmentFlags |= JavaElementImageDescriptor.FINAL;
    }
    if (Flags.isAbstract(flags) && !isInterface) {
      adornmentFlags |= JavaElementImageDescriptor.ABSTRACT;
    }
    if (Flags.isStatic(flags)) {
      adornmentFlags |= JavaElementImageDescriptor.STATIC;
    }
    if (Flags.isDeprecated(flags)) {
      adornmentFlags |= JavaElementImageDescriptor.DEPRECATED;
    }

    if (severity == IMarker.SEVERITY_ERROR) {
      adornmentFlags |= JavaElementImageDescriptor.ERROR;
    }
    else if (severity == IMarker.SEVERITY_WARNING) {
      adornmentFlags |= JavaElementImageDescriptor.WARNING;
    }

    return new JavaElementImageDescriptor(desc, adornmentFlags, JavaElementImageProvider.BIG_SIZE);
  }

  public static <T> boolean equals(T a, T b) {
    if (a instanceof String) {
      if (((String) a).equalsIgnoreCase("null")) {
        a = null;
      }
    }
    if (b instanceof String) {
      if (((String) b).equalsIgnoreCase("null")) {
        b = null;
      }
    }
    return CompareUtility.equals(a, b);
  }
}
