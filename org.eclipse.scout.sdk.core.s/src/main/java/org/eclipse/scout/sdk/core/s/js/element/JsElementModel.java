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
package org.eclipse.scout.sdk.core.s.js.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.structured.IStructuredType;
import org.eclipse.scout.sdk.core.s.structured.IStructuredType.Categories;
import org.eclipse.scout.sdk.core.s.structured.StructuredType;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

public class JsElementModel {

  private final IType m_source;
  private final String m_identifier;
  private final String m_objectType;
  private final List<IJsElementModelNode> m_nodes;

  public JsElementModel(IType source, String identifier, String objectType, List<IJsElementModelNode> nodes) {
    m_source = Ensure.notNull(source);
    m_identifier = Ensure.notNull(identifier);
    m_objectType = Ensure.notNull(objectType);
    m_nodes = Ensure.notNull(nodes);
  }

  public IType source() {
    return m_source;
  }

  public String identifier() {
    return m_identifier;
  }

  public String objectType() {
    return m_objectType;
  }

  public List<IJsElementModelNode> nodes() {
    return Collections.unmodifiableList(m_nodes);
  }

  public static JsElementModel wrap(IType element) {
    return Optional.ofNullable(element)
        .filter(JsElementModel::isValid)
        .map(JsElementModel::parse)
        .orElse(null);
  }

  protected static boolean isValid(IType candidate) {
    //noinspection DuplicatedCode
    var flags = candidate.flags();
    if (Flags.isInterface(flags) || !Flags.isPublic(flags) || Flags.isEnum(flags)) {
      return false;
    }
    var scoutApi = candidate.javaEnvironment().api(IScoutApi.class);
    if (scoutApi.isEmpty()) {
      return false;
    }
    return validSuperTypes(scoutApi.orElseThrow())
        .anyMatch(candidate::isInstanceOf);
  }

  public static Stream<ITypeNameSupplier> validSuperTypes(IScoutApi scoutApi) {
    // TODO fsh add more elements
    return Stream.of(
        scoutApi.IForm(),
        scoutApi.IFormField(),
        scoutApi.IPage(),
        scoutApi.ITable(),
        scoutApi.IColumn());
  }

  protected static JsElementModel parse(IType element) {
    // TODO fsh add namespace
    var identifier = element.elementName();
    var objectType = parseObjectType(element);

    var nodes = new ArrayList<IJsElementModelNode>();

    var structuredType = StructuredType.of(element);
    // TODO fsh parse other parts
    parseTypes(structuredType, nodes);
    parseMethods(structuredType, nodes);
//    parseFields(structuredType, nodes);
//    parseEnums(structuredType, nodes);

    return new JsElementModel(element, identifier, objectType, nodes);
  }

  protected static String parseObjectType(IType element) {
    return Strings.removePrefix(element.superClass().orElseThrow().elementName(), ISdkConstants.PREFIX_ABSTRACT);
  }

  protected static void parseTypes(IStructuredType structuredType, List<IJsElementModelNode> nodes) {
    var types = structuredType.getElements(Categories.TYPE_UNCATEGORIZED, IType.class);

    types = types.stream()
        .filter(type -> {
          var jsElementModel = wrap(type);
          if (jsElementModel == null) {
            return true;
          }
          try {
            nodes.add(JsElementModelElementModelTypeNode.create(type, jsElementModel));
            return false;
          }
          catch (Exception e) {
            SdkLog.warning("Could not create JsElementModelElementModelTypeNode for type '{}'", type);
          }
          return true;
        })
        .collect(Collectors.toList());

    types.stream()
        .filter(Objects::nonNull)
        .forEach(type -> nodes.add(JsElementModelTypeNode.create(type)));
  }

  protected static void parseMethods(IStructuredType structuredType, List<IJsElementModelNode> nodes) {
    var methods = structuredType.getElements(Categories.METHOD_UNCATEGORIZED, IMethod.class);

    structuredType.getElements(Categories.METHOD_CONFIG_PROPERTY, IMethod.class)
        .forEach(method -> {
          try {
            nodes.add(JsElementModelGetConfiguredMethodNode.create(method));
            methods.remove(method);
          }
          catch (Exception e) {
            SdkLog.warning("Could not create JsElementModelGetConfiguredMethodNode for method '{}'", method);
          }
        });

    methods.stream()
        .filter(Objects::nonNull)
        .forEach(method -> nodes.add(JsElementModelMethodNode.create(method)));
  }
}
