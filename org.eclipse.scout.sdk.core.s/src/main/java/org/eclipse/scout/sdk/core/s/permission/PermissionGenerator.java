/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.permission;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;

import org.eclipse.scout.sdk.core.java.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.java.generator.field.ScoutFieldGenerator;
import org.eclipse.scout.sdk.core.s.java.generator.method.ScoutMethodGenerator;
import org.eclipse.scout.sdk.core.util.Strings;

public class PermissionGenerator extends PrimaryTypeGenerator<PermissionGenerator> {

  public static final String ID_FIELD_NAME = "ID";

  private String m_idRef;

  public static PermissionGenerator create() {
    return new PermissionGenerator();
  }

  @Override
  protected void setup() {
    withPreProcessor((e, c) -> {
      withoutField(field -> field.elementName(c).map(ID_FIELD_NAME::equals).orElse(false));
      idRef()
          .filter(ID_FIELD_NAME::equals)
          .ifPresent(idRef -> withIdRef(null));
      if (idRef().isEmpty() && c.requireApi(IScoutApi.class).createPermissionIdField()) {
        withField(ScoutFieldGenerator.createPermissionIdField(elementName().orElseThrow(() -> newFail("Permission name is missing")), ID_FIELD_NAME));
        withIdRef(ID_FIELD_NAME);
      }
    });
    withField(FieldGenerator.createSerialVersionUid())
        .withSuperClassFrom(IScoutApi.class, api -> api.AbstractPermission().fqn())
        .withMethod(createConstructor());
  }

  protected IMethodGenerator<?, ?> createConstructor() {
    var permissionName = elementName().orElseThrow(() -> newFail("Permission name is missing"));
    return ScoutMethodGenerator.create()
        .asPublic()
        .withElementName(permissionName)
        .withBody(b -> {
          b.superClause().parenthesisOpen();
          idRef().ifPresentOrElse(
              b::appendFieldReference,
              () -> b.context().requireApi(IScoutApi.class).appendPermissionIdFieldValue(b, permissionName));
          b.parenthesisClose().semicolon();
        });
  }

  public Optional<String> idRef() {
    return Strings.notBlank(m_idRef);
  }

  public PermissionGenerator withIdRef(String idRef) {
    m_idRef = idRef;
    return this;
  }
}
