/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.generator.field;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.eclipse.scout.sdk.core.util.Strings.notBlank;

import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.builder.ITypeScriptSourceBuilder;
import org.eclipse.scout.sdk.core.typescript.builder.nodeelement.INodeElementBuilder;
import org.eclipse.scout.sdk.core.typescript.builder.nodeelement.NodeElementBuilder;
import org.eclipse.scout.sdk.core.typescript.generator.nodeelement.AbstractNodeElementGenerator;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link FieldGenerator}</h3>
 *
 * @since 13.0
 */
public class FieldGenerator<TYPE extends IFieldGenerator<TYPE>> extends AbstractNodeElementGenerator<TYPE> implements IFieldGenerator<TYPE> {

  private String m_dataType;

  protected FieldGenerator() {
  }

  /**
   * @return A new empty {@link IFieldGenerator}.
   */
  public static IFieldGenerator<?> create() {
    return new FieldGenerator<>();
  }

  @Override
  protected void build(ITypeScriptSourceBuilder<?> builder) {
    super.build(builder);
    buildFieldSource(NodeElementBuilder.create(builder));
  }

  protected void buildFieldSource(INodeElementBuilder<?> builder) {
    var elementName = elementName().filter(Strings::hasText);
    if (elementName.isPresent()) {
      builder
          .appendModifiers(modifiers())
          .space()
          .append(elementName.orElseThrow())
          .colon()
          .space()
          .append(dataType().orElseThrow(() -> newFail("Field data type missing for builder {}", this)));
      builder.semicolon();
    }
  }

  @Override
  public Optional<String> dataType() {
    return notBlank(m_dataType);
  }

  @Override
  public TYPE withDataType(String dataType) {
    m_dataType = dataType;
    return thisInstance();
  }
}
