/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.api.internal;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationValue;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IImport;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IPackage;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;

/**
 * Helper class to print the model objects of both source and binary java code
 */
public final class JavaModelPrinter {
  private JavaModelPrinter() {
  }

  protected static void printAnnotations(IAnnotatable anotatable, StringBuilder sb, boolean includeDetails) {
    if (anotatable.getAnnotations().size() > 0) {
      for (IAnnotation a : anotatable.getAnnotations()) {
        printAnnotation(a, sb, includeDetails);
        sb.append(includeDetails ? '\n' : ' ');
      }
    }
  }

  protected static void printAnnotation(IAnnotation a, StringBuilder sb, boolean includeDetails) {
    sb.append("@");
    sb.append(a.getType().getSimpleName());
    Map<String, IAnnotationValue> values = a.getValues();
    int n = 0;
    for (Map.Entry<String, IAnnotationValue> e : values.entrySet()) {
      if (!e.getValue().isSyntheticDefaultValue()) {
        n++;
      }
    }
    if (n > 0) {
      sb.append("(");
      if (includeDetails) {
        for (Map.Entry<String, IAnnotationValue> e : values.entrySet()) {
          if (e.getValue().isSyntheticDefaultValue()) {
            continue;
          }
          sb.append(e.getKey());
          sb.append("=");
          sb.append(e.getValue().getMetaValue().toString());
          sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
      }
      else {
        sb.append("...");
      }
      sb.append(")");
    }
  }

  public static void print(IAnnotation a, StringBuilder sb) {
    printAnnotation(a, sb, true);
  }

  public static void print(IPackage p, StringBuilder sb) {
    if (p.getName() == null) {
      sb.append("(default-package)");
      return;
    }
    sb.append("package ").append(p.getName());
  }

  public static void print(IMethodParameter mp, StringBuilder sb) {
    printAnnotations(mp, sb, false);
    sb.append(mp.getDataType().getName()).append(' ').append(mp.getElementName());
  }

  public static void print(IImport id, StringBuilder sb) {
    sb.append("import ").append(id.isStatic() ? "static " : "").append(id.getName());
  }

  public static void print(ICompilationUnit icu, StringBuilder sb) {
    sb.append(icu.getElementName());
  }

  public static void print(ITypeParameter tp, StringBuilder sb) {
    printAnnotations(tp, sb, false);
    sb.append(tp.getElementName());
    if (tp.getBounds().size() > 0) {
      sb.append(" extends ");
      sb.append(tp.getBounds().get(0).getName());
      for (int i = 1; i < tp.getBounds().size(); i++) {
        sb.append(" & ");
        sb.append(tp.getBounds().get(i).getName());
      }
    }
  }

  public static void print(IType t, StringBuilder sb) {
    printAnnotations(t, sb, false);
    if (Flags.isAnnotation(t.getFlags())) {
      sb.append("@interface ");
    }
    else if (Flags.isInterface(t.getFlags())) {
      sb.append("interface ");
    }
    else {
      sb.append("class ");
    }
    sb.append(t.getName());
    if (t.getTypeParameters().size() > 0) {
      sb.append('<');
      print(t.getTypeParameters().get(0), sb);
      for (int i = 1; i < t.getTypeParameters().size(); i++) {
        sb.append(", ");
        print(t.getTypeParameters().get(i), sb);
      }
      sb.append('>');
    }
  }

  public static void print(IMethod m, StringBuilder sb) {
    printAnnotations(m, sb, false);
    if (m.getTypeParameters().size() > 0) {
      sb.append('<');
      print(m.getTypeParameters().get(0), sb);
      for (int i = 1; i < m.getTypeParameters().size(); i++) {
        sb.append(", ");
        print(m.getTypeParameters().get(i), sb);
      }
      sb.append('>');
    }
    if (!m.isConstructor()) {
      sb.append(m.getReturnType().getName());
      sb.append(' ');
    }
    sb.append(m.getElementName());
    sb.append('(');
    List<IMethodParameter> parameters = m.getParameters();
    if (parameters.size() > 0) {
      print(parameters.get(0), sb);
      for (int i = 1; i < parameters.size(); i++) {
        sb.append(", ");
        print(parameters.get(i), sb);
      }
    }
    sb.append(')');
  }

  public static void print(IField f, StringBuilder sb) {
    printAnnotations(f, sb, false);
    sb.append(f.getDataType().getSimpleName());
    sb.append(' ');
    sb.append(f.getElementName());
    if (f.getConstantValue() != null) {
      sb.append(" = ");
      sb.append(f.getConstantValue().toString());
    }
  }

  public static void print(IAnnotationValue av, StringBuilder sb) {
    sb.append(av.getMetaValue().toString());
  }
}
