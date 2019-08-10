/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.builder.java.comment;

import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;

/**
 * <h3>{@link IJavaElementCommentBuilder}</h3>
 *
 * @since 6.1.0
 */
public interface IJavaElementCommentBuilder<TYPE extends IJavaElementCommentBuilder<TYPE>> extends ICommentBuilder<TYPE> {
  /**
   * Appends the default comment for the owner {@link IJavaElement}.
   * <p>
   * If the receiver of the comment is an {@link ICompilationUnitGenerator}, {@link ITypeGenerator},
   * {@link IMethodGenerator} or {@link IFieldGenerator} the default element comment will be appended. This may include
   * e.g. standard file headers with copyright or standard method comments describing arguments and return type.
   * <p>
   * The style and content of the default comment can be configured by implementing
   * {@link IDefaultElementCommentGeneratorSpi}.
   *
   * @return A reference to this object.
   * @see IDefaultElementCommentGeneratorSpi
   */
  TYPE appendDefaultElementComment();
}
