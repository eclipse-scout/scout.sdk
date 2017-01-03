/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.spi.internal;

import java.io.Serializable;
import java.util.Comparator;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * <h3>{@link SourcePositionComparator}</h3>
 *
 * @author Matthias Villiger
 * @since 6.1.0
 */
class SourcePositionComparator implements Comparator<Object>, Serializable {

  private static final long serialVersionUID = 1L;
  private static final int UNKNOWN_SOURCE_POS = -1;

  @Override
  public int compare(Object o1, Object o2) {
    int pos1 = getSourcePosition(o1);
    int pos2 = getSourcePosition(o2);
    return Integer.compare(pos1, pos2);
  }

  protected int getSourcePosition(Object o) {
    if (o == null) {
      return UNKNOWN_SOURCE_POS;
    }
    if (o instanceof ReferenceBinding) {
      return getSourcePosition((ReferenceBinding) o);
    }
    if (o instanceof MethodBinding) {
      return getSourcePosition((MethodBinding) o);
    }
    if (o instanceof FieldBinding) {
      return getSourcePosition((FieldBinding) o);
    }
    throw new IllegalArgumentException("unsupported type: " + o.getClass().getName());
  }

  protected int getSourcePosition(ReferenceBinding rb) {
    TypeBinding tb = SpiWithJdtUtils.nvl(rb.original(), rb);
    if (tb == null) {
      return UNKNOWN_SOURCE_POS;
    }

    TypeDeclaration decl = null;
    if (tb instanceof SourceTypeBinding) {
      decl = ((SourceTypeBinding) tb).scope.referenceContext;
    }
    if (decl == null) {
      return UNKNOWN_SOURCE_POS;
    }

    return decl.declarationSourceStart;
  }

  protected int getSourcePosition(MethodBinding mb) {
    MethodBinding methodBinding = SpiWithJdtUtils.nvl(mb.original(), mb);
    if (methodBinding == null) {
      return UNKNOWN_SOURCE_POS;
    }

    AbstractMethodDeclaration decl = methodBinding.sourceMethod();
    if (decl == null) {
      return UNKNOWN_SOURCE_POS;
    }
    return decl.declarationSourceStart;
  }

  protected int getSourcePosition(FieldBinding fb) {
    FieldBinding fieldBinding = SpiWithJdtUtils.nvl(fb.original(), fb);
    if (fieldBinding == null) {
      return UNKNOWN_SOURCE_POS;
    }

    FieldDeclaration decl = fieldBinding.sourceField();
    if (decl == null) {
      return UNKNOWN_SOURCE_POS;
    }
    return decl.declarationSourceStart;
  }
}
