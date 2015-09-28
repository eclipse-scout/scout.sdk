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
    for (IAnnotation a : anotatable.annotations().list()) {
      printAnnotation(a, sb, includeDetails);
      sb.append(includeDetails ? '\n' : ' ');
    }
  }

  protected static void printAnnotation(IAnnotation a, StringBuilder sb, boolean includeDetails) {
    sb.append("@");
    sb.append(a.type().elementName());
    Map<String, IAnnotationValue> values = a.values();
    int n = 0;
    for (Map.Entry<String, IAnnotationValue> e : values.entrySet()) {
      if (!e.getValue().isDefaultValue()) {
        n++;
      }
    }
    if (n > 0) {
      sb.append("(");
      if (includeDetails) {
        for (Map.Entry<String, IAnnotationValue> e : values.entrySet()) {
          if (e.getValue().isDefaultValue()) {
            continue;
          }
          sb.append(e.getKey());
          sb.append("=");
          sb.append(e.getValue().metaValue().toString());
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
    if (p.name() == null) {
      sb.append("(default-package)");
      return;
    }
    sb.append("package ").append(p.name());
  }

  public static void print(IMethodParameter mp, StringBuilder sb) {
    printAnnotations(mp, sb, false);
    sb.append(mp.dataType().name()).append(' ').append(mp.elementName());
  }

  public static void print(IImport id, StringBuilder sb) {
    sb.append("import ").append(id.isStatic() ? "static " : "").append(id.name());
  }

  public static void print(ICompilationUnit icu, StringBuilder sb) {
    sb.append(icu.elementName());
  }

  public static void print(ITypeParameter tp, StringBuilder sb) {
    sb.append(tp.elementName());
    if (tp.bounds().size() > 0) {
      sb.append(" extends ");
      sb.append(tp.bounds().get(0).name());
      for (int i = 1; i < tp.bounds().size(); i++) {
        sb.append(" & ");
        sb.append(tp.bounds().get(i).name());
      }
    }
  }

  public static void print(IType t, StringBuilder sb) {
    printAnnotations(t, sb, false);
    if (Flags.isAnnotation(t.flags())) {
      sb.append("@interface ");
    }
    else if (Flags.isInterface(t.flags())) {
      sb.append("interface ");
    }
    else {
      sb.append("class ");
    }
    sb.append(t.name());
    if (t.typeParameters().size() > 0) {
      sb.append('<');
      print(t.typeParameters().get(0), sb);
      for (int i = 1; i < t.typeParameters().size(); i++) {
        sb.append(", ");
        print(t.typeParameters().get(i), sb);
      }
      sb.append('>');
    }
  }

  public static void print(IMethod m, StringBuilder sb) {
    printAnnotations(m, sb, false);
    if (m.typeParameters().size() > 0) {
      sb.append('<');
      print(m.typeParameters().get(0), sb);
      for (int i = 1; i < m.typeParameters().size(); i++) {
        sb.append(", ");
        print(m.typeParameters().get(i), sb);
      }
      sb.append('>');
    }
    if (!m.isConstructor()) {
      sb.append(m.returnType().name());
      sb.append(' ');
    }
    sb.append(m.elementName());
    sb.append('(');
    List<IMethodParameter> parameters = m.parameters().list();
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
    sb.append(f.dataType().elementName());
    sb.append(' ');
    sb.append(f.elementName());
    if (f.constantValue() != null) {
      sb.append(" = ");
      sb.append(f.constantValue().toString());
    }
  }

  public static void print(IAnnotationValue av, StringBuilder sb) {
    sb.append(av.metaValue().toString());
  }
}
