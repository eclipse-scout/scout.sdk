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
package org.eclipse.scout.sdk.core.s.permission;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import org.eclipse.scout.sdk.core.generator.IJavaElementGenerator;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;

/**
 * <h3>{@link PermissionGenerator}</h3>
 *
 * @since 5.2.0
 */
public class PermissionGenerator<TYPE extends PermissionGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  @Override
  protected void setup() {
    withField(FieldGenerator.createSerialVersionUid())
        .withSuperClassFrom(IScoutApi.class, api -> api.AbstractPermission().fqn())
        .withMethod(createConstructor(this));
  }

  protected static IMethodGenerator<?, ?> createConstructor(IJavaElementGenerator<?> constructorOwner) {
    var permissionName = constructorOwner.elementName().orElseThrow(() -> newFail("Permission name is missing"));
    return MethodGenerator.create()
        .asPublic()
        .withElementName(permissionName)
        .withBody(b -> b.superClause().parenthesisOpen().stringLiteral(permissionName).parenthesisClose().semicolon());
  }
}
