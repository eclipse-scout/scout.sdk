/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.js.element.gen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.js.element.IJsElementModelElementModelTypeNode;
import org.eclipse.scout.sdk.core.s.js.element.IJsElementModelGetConfiguredMethodNode;
import org.eclipse.scout.sdk.core.s.js.element.JsElementModel;

public class JsModelGenerator<TYPE extends JsModelGenerator<TYPE>> extends AbstractJsSourceGenerator<TYPE> implements IJsModelGenerator<TYPE>, IJsValueGenerator<JsElementModel, TYPE> {

  private static final String ROOT_GROUP_BOX = "rootGroupBox";
  private static final String FIELDS = "fields";
  private static final String MENUS = "menus";
  private static final String COLUMNS = "columns";
  private static final String DETAIL_TABLE = "detailTable";

  private static final String GROUP = "Group";
  private static final String GROUP_BOX = "GroupBox";
  private static final String RADIO_BUTTON = "RadioButton";
  @SuppressWarnings("StaticCollection")
  private static final List<String> FIELD_SUFFIXES = List.of(GROUP, GROUP_BOX, ISdkConstants.SUFFIX_FORM_FIELD, RADIO_BUTTON);
  @SuppressWarnings("StaticCollection")
  private static final List<String> MENU_SUFFIXES = List.of(ISdkConstants.SUFFIX_MENU, ISdkConstants.SUFFIX_BUTTON);

  private final List<IJsModelPropertyGenerator<?>> m_jsModelPropertyGenerators;
  private JsElementModel m_jsElementModel;

  protected JsModelGenerator() {
    m_jsModelPropertyGenerators = new ArrayList<>();
  }

  public static JsModelGenerator<?> create() {
    return new JsModelGenerator<>();
  }

  @Override
  public void generate(IJsSourceBuilder<?> builder) {
    super.generate(builder);

    builder.objectStart().nl();

    var appendCommaAndNl = new AtomicBoolean();
    jsModelPropertyGenerators()
        .forEach(gen -> {
          if (appendCommaAndNl.getAndSet(true)) {
            builder.comma().nl();
          }
          gen.generate(builder);
        });

    builder.nl().objectEnd();
  }

  @Override
  protected void setupImpl() {
    super.setupImpl();

    var jsElementModel = jsElementModel().orElseThrow();

    withJsModelPropertyGenerator(JsModelPropertyGenerator.create()
        .withIdentifier(IJsModelPropertyGenerator.ID_PROPERTY)
        .withJsValueGenerator(JsStringValueGenerator.create()
            .withValue(jsElementModel.identifier())));

    withJsModelPropertyGenerator(JsModelPropertyGenerator.create()
        .withIdentifier(IJsModelPropertyGenerator.OBJECT_TYPE_PROPERTY)
        .withJsValueGenerator(JsStringValueGenerator.create()
            .withValue(jsElementModel.objectType())));

    jsElementModel.nodes().stream()
        .filter(IJsElementModelGetConfiguredMethodNode.class::isInstance)
        .map(IJsElementModelGetConfiguredMethodNode.class::cast)
        .forEach(node -> withJsModelPropertyGenerator(JsModelPropertyGenerator.create()
            .withIdentifier(node.propertyIdentifier())
            .withJsValueGenerator(createJsValueGenerator(node))));

    createJsModelPropertyGenerators(jsElementModel.nodes().stream()
        .filter(IJsElementModelElementModelTypeNode.class::isInstance)
        .map(IJsElementModelElementModelTypeNode.class::cast)
        .collect(Collectors.toList()))
            .stream()
            .filter(Objects::nonNull)
            .forEach(this::withJsModelPropertyGenerator);
  }

  @SuppressWarnings("MethodMayBeStatic")
  protected IJsValueGenerator<?, ?> createJsValueGenerator(IJsElementModelGetConfiguredMethodNode node) {
    switch (node.propertyType()) {
      case TEXT_KEY:
        return JsTextKeyValueGenerator.create().withValue(node.textKey());
      case STRING:
        return JsStringValueGenerator.create().withValue(node.stringValue());
      case BOOLEAN:
        return JsBooleanValueGenerator.create().withValue(node.booleanValue());
      case BIG_INTEGER:
        return JsNumberValueGenerator.create().withValue(node.bigIntValue());
      default:
        return null;
    }
  }

  protected List<IJsModelPropertyGenerator<?>> createJsModelPropertyGenerators(Collection<IJsElementModelElementModelTypeNode> nodes) {
    var generators = new ArrayList<IJsModelPropertyGenerator<?>>();

    var objectType = jsElementModel().orElseThrow().objectType();

    if (objectType.endsWith(ISdkConstants.SUFFIX_FORM)) {
      nodes.stream()
          .filter(node -> node.jsElementModel().objectType().endsWith(GROUP_BOX))
          .map(node -> JsModelPropertyGenerator.create()
              .withIdentifier(ROOT_GROUP_BOX)
              .withJsValueGenerator(create().withJsElementModel(node.jsElementModel())))
          .forEach(generators::add);
    }

    if (FIELD_SUFFIXES.stream().anyMatch(objectType::endsWith)) {
      var fields = new ArrayList<JsElementModel>();
      nodes = nodes.stream()
          .filter(node -> {
            if (FIELD_SUFFIXES.stream().anyMatch(node.jsElementModel().objectType()::endsWith)) {
              fields.add(node.jsElementModel());
              return false;
            }
            return true;
          })
          .collect(Collectors.toList());
      if (!fields.isEmpty()) {
        generators.add(JsModelPropertyGenerator.create()
            .withIdentifier(FIELDS)
            .withJsValueGenerator(JsArrayValueGenerator.create(JsElementModel.class)
                .withValues(fields)
                .withJsValueGeneratorSupplier(JsModelGenerator::create)));
      }

      var menus = nodes.stream()
          .map(IJsElementModelElementModelTypeNode::jsElementModel)
          .filter(jsElementModel -> MENU_SUFFIXES.stream().anyMatch(jsElementModel.objectType()::endsWith))
          .collect(Collectors.toList());
      if (!menus.isEmpty()) {
        generators.add(JsModelPropertyGenerator.create()
            .withIdentifier(MENUS)
            .withJsValueGenerator(JsArrayValueGenerator.create(JsElementModel.class)
                .withValues(menus)
                .withJsValueGeneratorSupplier(JsModelGenerator::create)));
      }
    }

    if (objectType.endsWith(ISdkConstants.SUFFIX_PAGE_WITH_TABLE)) {
      nodes.stream()
          .filter(node -> node.jsElementModel().objectType().endsWith(ISdkConstants.INNER_TABLE_TYPE_NAME))
          .map(node -> JsModelPropertyGenerator.create()
              .withIdentifier(DETAIL_TABLE)
              .withJsValueGenerator(create().withJsElementModel(node.jsElementModel())))
          .forEach(generators::add);
    }

    if (objectType.endsWith(ISdkConstants.INNER_TABLE_TYPE_NAME)) {
      var columns = new ArrayList<JsElementModel>();
      nodes = nodes.stream()
          .filter(node -> {
            if (node.jsElementModel().objectType().endsWith(ISdkConstants.SUFFIX_COLUMN)) {
              columns.add(node.jsElementModel());
              return false;
            }
            return true;
          })
          .collect(Collectors.toList());
      if (!columns.isEmpty()) {
        generators.add(JsModelPropertyGenerator.create()
            .withIdentifier(COLUMNS)
            .withJsValueGenerator(JsArrayValueGenerator.create(JsElementModel.class)
                .withValues(columns)
                .withJsValueGeneratorSupplier(JsModelGenerator::create)));
      }

      var menus = nodes.stream()
          .map(IJsElementModelElementModelTypeNode::jsElementModel)
          .filter(jsElementModel -> MENU_SUFFIXES.stream().anyMatch(jsElementModel.objectType()::endsWith))
          .collect(Collectors.toList());
      if (!menus.isEmpty()) {
        generators.add(JsModelPropertyGenerator.create()
            .withIdentifier(MENUS)
            .withJsValueGenerator(JsArrayValueGenerator.create(JsElementModel.class)
                .withValues(menus)
                .withJsValueGeneratorSupplier(JsModelGenerator::create)));
      }
    }

    return generators;
  }

  protected List<IJsModelPropertyGenerator<?>> jsModelPropertyGenerators() {
    return Collections.unmodifiableList(m_jsModelPropertyGenerators);
  }

  @Override
  public TYPE withJsModelPropertyGenerator(IJsModelPropertyGenerator<?> jsModelPropertyGenerator) {
    m_jsModelPropertyGenerators.add(jsModelPropertyGenerator);
    return thisInstance();
  }

  @Override
  public TYPE withoutJsModelPropertyGenerator(Predicate<IJsModelPropertyGenerator<?>> removalFilter) {
    m_jsModelPropertyGenerators.removeIf(removalFilter);
    return thisInstance();
  }

  protected Optional<JsElementModel> jsElementModel() {
    return Optional.ofNullable(m_jsElementModel);
  }

  @Override
  public TYPE withJsElementModel(JsElementModel jsElementModel) {
    m_jsElementModel = jsElementModel;
    return thisInstance();
  }

  @Override
  public TYPE withValue(JsElementModel jsElementModel) {
    return withJsElementModel(jsElementModel);
  }
}
