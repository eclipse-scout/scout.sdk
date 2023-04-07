/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.generator.type;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.typescript.builder.ITypeScriptSourceBuilder;
import org.eclipse.scout.sdk.core.typescript.generator.nodeelement.AbstractNodeElementGenerator;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link CompositeTypeGenerator}</h3>
 *
 * @since 13.0
 */
public class CompositeTypeGenerator<TYPE extends ICompositeTypeGenerator<TYPE>> extends AbstractNodeElementGenerator<TYPE> implements ICompositeTypeGenerator<TYPE> {

  private DataTypeFlavor m_flavor = DataTypeFlavor.Single;
  private int m_arrayDimension = 1;
  private final Collection<ISourceGenerator<? super ITypeScriptSourceBuilder<?>>> m_types;

  protected CompositeTypeGenerator() {
    m_types = new LinkedHashSet<>();
  }

  /**
   * @return A new empty {@link ITypeGenerator}.
   */
  public static ICompositeTypeGenerator<?> create() {
    return new CompositeTypeGenerator<>();
  }

  @Override
  protected void build(ITypeScriptSourceBuilder<?> builder) {
    super.build(builder);

    var flavor = flavor().orElseThrow(() -> newFail("Composite must have a flavor."));
    var delimiter = switch (flavor) {
      case Union -> " | ";
      case Intersection -> " & ";
      default -> null;
    };
    var suffix = flavor == DataTypeFlavor.Array ? "[]".repeat(arrayDimension()) : null;
    builder.append(types().map(t -> t.generalize(builder)), null, delimiter, suffix);
  }

  @Override
  public Optional<DataTypeFlavor> flavor() {
    return Optional.ofNullable(m_flavor);
  }

  @Override
  public TYPE withFlavor(DataTypeFlavor flavor) {
    m_flavor = flavor;
    return thisInstance();
  }

  @Override
  public int arrayDimension() {
    return m_arrayDimension;
  }

  @Override
  public TYPE withArrayDimension(int dimension) {
    m_arrayDimension = dimension;
    return thisInstance();
  }

  @Override
  public Stream<ISourceGenerator<? super ITypeScriptSourceBuilder<?>>> types() {
    return m_types.stream();
  }

  @Override
  public TYPE withType(ISourceGenerator<? super ITypeScriptSourceBuilder<?>> type) {
    m_types.add(Ensure.notNull(type));
    return thisInstance();
  }
}
