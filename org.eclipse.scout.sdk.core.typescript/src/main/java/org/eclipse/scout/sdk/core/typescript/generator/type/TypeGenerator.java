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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.builder.ITypeScriptSourceBuilder;
import org.eclipse.scout.sdk.core.typescript.builder.nodeelement.INodeElementBuilder;
import org.eclipse.scout.sdk.core.typescript.builder.nodeelement.NodeElementBuilder;
import org.eclipse.scout.sdk.core.typescript.generator.ITypeScriptElementGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.nodeelement.AbstractNodeElementGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.nodeelement.INodeElementGenerator;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;

/**
 * <h3>{@link TypeGenerator}</h3>
 *
 * @since 13.0
 */
public class TypeGenerator<TYPE extends ITypeGenerator<TYPE>> extends AbstractNodeElementGenerator<TYPE> implements ITypeGenerator<TYPE> {

  private boolean m_asClass;
  private boolean m_asInterface;
  private IES6Class m_superClass;
  private final List<SortedNodeElementEntry> m_nodeElements;

  protected TypeGenerator() {
    m_nodeElements = new ArrayList<>();
  }

  /**
   * @return A new empty {@link ITypeGenerator}.
   */
  public static ITypeGenerator<?> create() {
    return new TypeGenerator<>();
  }

  @Override
  protected void build(ITypeScriptSourceBuilder<?> builder) {
    super.build(builder);
    buildType(builder);
  }

  protected void buildType(ITypeScriptSourceBuilder<?> builder) {
    buildTypeDeclaration(NodeElementBuilder.create(builder));
    builder
        .blockStart().nl();
    buildTypeBody(builder);
    builder
        .nl().blockEnd();
  }

  protected void buildTypeDeclaration(INodeElementBuilder<?> builder) {
    if (!m_asClass && !m_asInterface) {
      return;
    }

    builder.appendModifiers(modifiers());

    // type definition
    if (m_asClass) {
      builder.append("class ");
    }
    else {
      builder.append("interface ");
    }
    builder.append(elementName().orElseThrow(() -> newFail("Type must have a name.")));

    if (m_asClass) {
      superClass().ifPresent(superClass -> builder.append(" extends ").ref(superClass));
    }

    builder.space();
  }

  protected void buildTypeBody(ITypeScriptSourceBuilder<?> builder) {
    builder.append(
        m_nodeElements.stream()
            .sorted()
            .map(SortedNodeElementEntry::generator),
        null, builder.context().lineDelimiter(), null);
  }

  @Override
  public TYPE asClass() {
    m_asClass = true;
    return thisInstance();
  }

  @Override
  public TYPE asInterface() {
    m_asInterface = true;
    return thisInstance();
  }

  @Override
  public Optional<IES6Class> superClass() {
    return Optional.ofNullable(m_superClass);
  }

  @Override
  public TYPE withSuperClass(IES6Class superClass) {
    m_superClass = superClass;
    return thisInstance();
  }

  @Override
  public Stream<IFieldGenerator<?>> fields() {
    return m_nodeElements.stream()
        .filter(SortedNodeElementEntry::isField)
        .map(SortedNodeElementEntry::generator)
        .map(g -> (IFieldGenerator<?>) g);
  }

  @Override
  public TYPE withField(IFieldGenerator<?> generator, Object... sortObject) {
    m_nodeElements.add(new SortedNodeElementEntry(applyConnection(generator, this), sortObject));
    return thisInstance();
  }

  @Override
  public TYPE withoutField(Predicate<IFieldGenerator<?>> removalFilter) {
    removeNodeElementIf(IFieldGenerator.class, removalFilter);
    return thisInstance();
  }

  @SuppressWarnings("unchecked")
  protected <T extends INodeElementGenerator<?>, P extends INodeElementGenerator<?>> void removeNodeElementIf(Class<T> type, Predicate<P> removalFilter) {
    for (var it = m_nodeElements.iterator(); it.hasNext();) {
      var entry = it.next();
      if (entry.hasType(type)) {
        var generator = (P) entry.generator();
        if (removalFilter == null || removalFilter.test(generator)) {
          it.remove();
          applyConnection(generator, null);
        }
      }
    }
  }

  protected static <T extends INodeElementGenerator<?>> T applyConnection(T child, ITypeScriptElementGenerator<?> parent) {
    if (child instanceof AbstractNodeElementGenerator<?>) {
      ((AbstractNodeElementGenerator<?>) child).withDeclaringGenerator(parent);
    }
    return child;
  }
}
