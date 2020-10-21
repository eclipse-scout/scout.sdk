/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import java.io.Serializable;
import java.util.Comparator;

import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * <h3>{@link SourcePositionComparators}</h3>
 *
 * @since 6.1.0
 */
final class SourcePositionComparators {

  private static final int UNKNOWN_SOURCE_POS = -1;

  private SourcePositionComparators() {
  }

  public static final class TypeBindingComparator implements Comparator<TypeBinding>, Serializable {

    public static final Comparator<TypeBinding> INSTANCE = new TypeBindingComparator();
    private static final long serialVersionUID = 1L;

    private TypeBindingComparator() {
    }

    static int getSourcePosition(TypeBinding rb) {
      var tb = SpiWithEcjUtils.nvl(rb.original(), rb);
      if (!(tb instanceof SourceTypeBinding)) {
        return UNKNOWN_SOURCE_POS;
      }

      var decl = ((SourceTypeBinding) tb).scope.referenceContext;
      if (decl == null) {
        return UNKNOWN_SOURCE_POS;
      }

      return decl.declarationSourceStart;
    }

    @Override
    public int compare(TypeBinding o1, TypeBinding o2) {
      var pos1 = getSourcePosition(o1);
      var pos2 = getSourcePosition(o2);
      return Integer.compare(pos1, pos2);
    }
  }

  public static final class MethodBindingComparator implements Comparator<MethodBinding>, Serializable {

    public static final Comparator<MethodBinding> INSTANCE = new MethodBindingComparator();
    private static final long serialVersionUID = 1L;

    private MethodBindingComparator() {
    }

    static int getSourcePosition(MethodBinding mb) {
      var methodBinding = SpiWithEcjUtils.nvl(mb.original(), mb);
      var decl = SpiWithEcjUtils.sourceMethodOf(methodBinding);
      if (decl == null) {
        return UNKNOWN_SOURCE_POS;
      }
      return decl.declarationSourceStart;
    }

    @Override
    public int compare(MethodBinding o1, MethodBinding o2) {
      var pos1 = getSourcePosition(o1);
      var pos2 = getSourcePosition(o2);
      return Integer.compare(pos1, pos2);
    }
  }

  public static final class FieldBindingComparator implements Comparator<FieldBinding>, Serializable {

    public static final Comparator<FieldBinding> INSTANCE = new FieldBindingComparator();
    private static final long serialVersionUID = 1L;

    private FieldBindingComparator() {
    }

    static int getSourcePosition(FieldBinding fb) {
      var fieldBinding = SpiWithEcjUtils.nvl(fb.original(), fb);
      var decl = fieldBinding.sourceField();
      if (decl == null) {
        return UNKNOWN_SOURCE_POS;
      }
      return decl.declarationSourceStart;
    }

    @Override
    public int compare(FieldBinding o1, FieldBinding o2) {
      var pos1 = getSourcePosition(o1);
      var pos2 = getSourcePosition(o2);
      return Integer.compare(pos1, pos2);
    }
  }
}
