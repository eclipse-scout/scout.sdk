/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator;

import org.eclipse.scout.sdk.core.java.builder.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.java.model.api.IPackage;
import org.eclipse.scout.sdk.core.java.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.java.transformer.SimpleWorkingCopyTransformerBuilder;

public class PackageGenerator extends AbstractAnnotatableGenerator<PackageGenerator> {

  protected PackageGenerator() {
  }

  @SuppressWarnings("TypeMayBeWeakened")
  protected PackageGenerator(IPackage parameter, IWorkingCopyTransformer transformer) {
    super(parameter, transformer);
  }

  /**
   * @return A new empty {@link PackageGenerator}.
   */
  public static PackageGenerator create() {
    return new PackageGenerator();
  }

  /**
   * Creates a new {@link PackageGenerator} based on the given {@link IPackage}.
   * <p>
   * <b>Example:</b> See {@link IWorkingCopyTransformer}.
   *
   * @param pck
   *          The {@link IPackage} that should be converted to an {@link PackageGenerator}. Must not be {@code null}.
   * @param transformer
   *          An optional {@link IWorkingCopyTransformer} callback that is responsible for transforming the method pck
   *          to a working copy. May be {@code null} if no custom transformation is required and the method pck should
   *          be converted into a working copy without any modification.
   * @return A new {@link PackageGenerator} initialized to generate source that is structurally similar to the one from
   *         the given {@link IPackage}.
   * @see DefaultWorkingCopyTransformer
   * @see SimpleWorkingCopyTransformerBuilder
   */
  public static PackageGenerator create(IPackage pck, IWorkingCopyTransformer transformer) {
    return new PackageGenerator(pck, transformer);
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    super.build(builder);
    elementName(builder.context()).ifPresent(pck -> builder.append("package ").append(pck).semicolon());
  }
}
