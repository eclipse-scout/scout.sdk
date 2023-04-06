/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.builder;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.builder.SourceBuilderWrapper;
import org.eclipse.scout.sdk.core.typescript.generator.AbstractTypeScriptElementGenerator;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;

/**
 * <h3>{@link TypeScriptSourceBuilderWrapper}</h3>
 *
 * @since 13.0
 */
public class TypeScriptSourceBuilderWrapper<TYPE extends ITypeScriptSourceBuilder<TYPE>> extends SourceBuilderWrapper<TYPE> implements ITypeScriptSourceBuilder<TYPE> {

  public TypeScriptSourceBuilderWrapper(ISourceBuilder<?> inner) {
    super(AbstractTypeScriptElementGenerator.ensureTypeScriptSourceBuilder(inner));
  }

  @Override
  public ITypeScriptSourceBuilder<?> inner() {
    return (ITypeScriptSourceBuilder<?>) super.inner();
  }

  @Override
  public ITypeScriptBuilderContext context() {
    return (ITypeScriptBuilderContext) super.context();
  }

  @Override
  public TYPE blockStart() {
    inner().blockStart();
    return thisInstance();
  }

  @Override
  public TYPE blockEnd() {
    inner().blockEnd();
    return thisInstance();
  }

  @Override
  public TYPE ref(IDataType ref) {
    inner().ref(ref);
    return thisInstance();
  }
}
