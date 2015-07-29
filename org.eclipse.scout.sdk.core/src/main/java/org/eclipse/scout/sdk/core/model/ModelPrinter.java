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
package org.eclipse.scout.sdk.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Helper class to print the model objects
 */
public final class ModelPrinter {
  private ModelPrinter() {
  }

  protected static void printAnnotations(IAnnotatable anotatable, StringBuilder sb) {
    if (anotatable.getAnnotations().size() > 0) {
      print(anotatable.getAnnotations().get(0), sb);
      for (int i = 1; i < anotatable.getAnnotations().size(); i++) {
        sb.append('\n');
        print(anotatable.getAnnotations().get(i), sb);
      }
    }
  }

  public static void print(IAnnotation a, StringBuilder sb) {
    sb.append('@').append(a.getType().getName());
    List<IAnnotationValue> values = new ArrayList<>(a.getValues().values());
    if (values.size() > 0) {
      sb.append('(');
      print(values.get(0), sb);
      for (int i = 1; i < values.size(); i++) {
        sb.append(", ");
        print(values.get(i), sb);
      }
      sb.append(')');
    }
  }

  public static void print(IPackage p, StringBuilder sb) {
    sb.append("package ").append(p.getName());
  }

  public static void print(IMethodParameter mp, StringBuilder sb) {
    sb.append(mp.getType().getName()).append(' ').append(mp.getName());
  }

  public static void print(IImportDeclaration id, StringBuilder sb) {
    sb.append("import ").append(id.getName());
  }

  public static void print(ICompilationUnit icu, StringBuilder sb) {
    print(icu.getPackage(), sb);
    sb.append('\n');
    for (IImportDeclaration id : icu.getImports().values()) {
      print(id, sb);
      sb.append('\n');
    }
    for (IType t : icu.getTypes()) {
      print(t, sb);
      sb.append('\n');
    }
  }

  public static void print(ITypeParameter tp, StringBuilder sb) {
    sb.append(tp.getName());
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
    printAnnotations(t, sb);
    if (t.getAnnotations().size() > 0) {
      sb.append('\n');
    }
    sb.append(Flags.toString(t.getFlags()));
    if (Flags.isAnnotation(t.getFlags())) {
      sb.append(" @interface ");
    }
    else if (Flags.isInterface(t.getFlags())) {
      sb.append(" interface ");
    }
    else {
      sb.append(" class ");
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
    if (t.getSuperClass() != null) {
      sb.append(" extends ");
      sb.append(t.getSuperClass().getName());
    }
    if (t.getSuperInterfaces().size() > 0) {
      sb.append(" implements ");
      sb.append(t.getSuperInterfaces().get(0).getName());
      for (int i = 1; i < t.getSuperInterfaces().size(); i++) {
        sb.append(", ");
        sb.append(t.getSuperInterfaces().get(i).getName());
      }
    }
    boolean needsBody = t.getFields().size() > 0 || t.getMethods().size() > 0;
    if (needsBody) {
      sb.append(" {\n");
    }
    if (t.getFields().size() > 0) {
      for (IField f : t.getFields()) {
        sb.append('\n');
        print(f, sb);
        sb.append('\n');
      }
    }
    if (t.getMethods().size() > 0) {
      for (IMethod m : t.getMethods()) {
        sb.append('\n');
        print(m, sb);
        sb.append('\n');
      }
    }
    if (needsBody) {
      sb.append('}');
    }
  }

  public static void print(IMethod m, StringBuilder sb) {
    printAnnotations(m, sb);
    if (m.getAnnotations().size() > 0) {
      sb.append('\n');
    }
    String flagsString = Flags.toString(m.getFlags());
    if (StringUtils.isNotBlank(flagsString)) {
      sb.append(flagsString);
      sb.append(' ');
    }
    if (!m.isConstructor()) {
      sb.append(m.getReturnType().getName());
      sb.append(' ');
    }
    sb.append(m.getName());
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
    List<IType> exceptionTypes = m.getExceptionTypes();
    if (exceptionTypes.size() > 0) {
      sb.append(" throws ");
      sb.append(exceptionTypes.get(0).getName());
      for (int i = 1; i < exceptionTypes.size(); i++) {
        sb.append(", ");
        sb.append(exceptionTypes.get(i).getName());
      }
    }
  }

  public static void print(IField f, StringBuilder sb) {
    sb.append(Flags.toString(f.getFlags()));
    sb.append(' ');
    sb.append(f.getDataType().getName());
    sb.append(' ');
    sb.append(f.getName());
  }

  public static void print(BaseType bt, StringBuilder sb) {
    sb.append(bt.getName());
    for (int i = 0; i < bt.getArrayDimension(); i++) {
      sb.append("[]");
    }
  }

  public static void print(IAnnotationValue av, StringBuilder sb) {
    sb.append(av.getName()).append(" = ");
    if (av.getValueType() == ExpressionValueType.Array) {
      sb.append('{');
      IAnnotationValue[] val = (IAnnotationValue[]) av.getValue();
      if (val.length > 0) {
        print(val[0], sb);
        for (int i = 1; i < val.length; i++) {
          sb.append(", ");
          print(val[1], sb);
        }
      }
      sb.append('}');
    }
    else if (av.getValueType() == ExpressionValueType.Type) {
      sb.append(((IType) av.getValue()).getName());
    }
    else {
      Object value = av.getValue();
      if (value != null) {
        sb.append(value.toString());
      }
    }
  }
}
