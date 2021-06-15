/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.scout.sdk.core.model.api.IType;

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

  /**
   * Appends a link reference to the {@link CharSequence} specified. No leading or trailing space is appended. Imports
   * are created as necessary.<br>
   * <br>
   * Sample output:
   * 
   * <pre>
   * {&#64;link MyClass#myMethod(int, Collection)}
   * </pre>
   *
   * @param ref
   *          The reference for which a link should be added. <br>
   *          Examples:
   *          <ul>
   *          <li>{@code "java.util.List#addAll(int, java.util.Collection)"}</li>
   *          <li>{@code "#myMethod(org.eclipse.scout.MyClass)"}</li>
   *          <li>{@code "org.eclipse.scout.MyClass"}</li>
   *          </ul>
   * @return A reference to this object.
   */
  TYPE appendLink(CharSequence ref);

  /**
   * Appends a link reference to the {@link CharSequence} specified. No leading or trailing space is appended. Imports
   * are created as necessary.<br>
   * <br>
   * Sample output:
   * 
   * <pre>
   * {&#64;link MyClass#myMethod(int, Collection) myLabel}
   * </pre>
   * 
   * @param ref
   *          The reference for which a link should be added. <br>
   *          Examples:
   *          <ul>
   *          <li>{@code "java.util.List#addAll(int, java.util.Collection)"}</li>
   *          <li>{@code "#myMethod(org.eclipse.scout.MyClass)"}</li>
   *          <li>{@code "org.eclipse.scout.MyClass"}</li>
   *          </ul>
   * @param label
   *          An optional label which may be appended to the link.
   * @return A reference to this object.
   */
  TYPE appendLink(CharSequence ref, CharSequence label);

  /**
   * Appends a link to the {@link IType} specified. No leading or trailing space is appended. Imports are created as
   * necessary.<br>
   * <br>
   * Sample output:
   * 
   * <pre>
   * {&#64;link MyClass[]}
   * </pre>
   *
   * @param ref
   *          The {@link IType} to which a JavaDoc link should be added.
   * @return A reference to this object.
   */
  TYPE appendLink(IType ref);

  /**
   * Appends a link to the {@link IType} specified. No leading or trailing space is appended. Imports are created as
   * necessary.<br>
   * <br>
   * Sample output:
   * 
   * <pre>
   * {&#64;link MyClass[] myLabel}
   * </pre>
   * 
   * @param ref
   *          The {@link IType} to which a JavaDoc link should be added.
   * @param label
   *          An optional label which may be appended to the link.
   * @return A reference to this object.
   */
  TYPE appendLink(IType ref, CharSequence label);
}
