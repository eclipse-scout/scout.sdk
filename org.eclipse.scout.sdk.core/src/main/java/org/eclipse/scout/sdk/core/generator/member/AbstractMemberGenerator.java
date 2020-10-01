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
package org.eclipse.scout.sdk.core.generator.member;

import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.generator.AbstractAnnotatableGenerator;
import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IMember;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;

/**
 * <h3>{@link AbstractMemberGenerator}</h3>
 *
 * @since 6.1.0
 */
public abstract class AbstractMemberGenerator<TYPE extends IMemberGenerator<TYPE>> extends AbstractAnnotatableGenerator<TYPE> implements IMemberGenerator<TYPE> {

  private int m_flags;

  protected AbstractMemberGenerator(IMember member, IWorkingCopyTransformer transformer) {
    super(member, transformer);
    withFlags(member.flags());
    member.javaDoc()
        .map(ISourceRange::asCharSequence)
        .<ISourceGenerator<IJavaElementCommentBuilder<?>>> map(s -> b -> b.append(s).nl())
        .ifPresent(this::withComment);
  }

  protected AbstractMemberGenerator() {
  }

  @Override
  public TYPE withFlags(int flags) {
    m_flags |= flags;
    return thisInstance();
  }

  @Override
  public int flags() {
    return m_flags;
  }

  @Override
  public TYPE withoutFlags(int flags) {
    m_flags &= ~flags;
    return thisInstance();
  }

  @Override
  public TYPE asPublic() {
    return applyVisibility(Flags.AccPublic);
  }

  @Override
  public TYPE asPrivate() {
    return applyVisibility(Flags.AccPrivate);
  }

  @Override
  public TYPE asProtected() {
    return applyVisibility(Flags.AccProtected);
  }

  @Override
  public TYPE asPackagePrivate() {
    return applyVisibility(Flags.AccDefault);
  }

  protected TYPE applyVisibility(int visibility) {
    m_flags &= ~(Flags.AccPrivate | Flags.AccPublic | Flags.AccProtected); // remove all visibility markers
    return withFlags(visibility);
  }

  @Override
  public TYPE asStatic() {
    return withFlags(Flags.AccStatic);
  }

  @Override
  public TYPE asFinal() {
    return withFlags(Flags.AccFinal);
  }
}
