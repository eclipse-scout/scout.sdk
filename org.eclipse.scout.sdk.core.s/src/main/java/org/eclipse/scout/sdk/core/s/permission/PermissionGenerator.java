/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.permission;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.security.BasicPermission;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;

/**
 * <h3>{@link PermissionGenerator}</h3>
 *
 * @since 5.2.0
 */
public class PermissionGenerator<TYPE extends PermissionGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  @Override
  protected void fillMainType(ITypeGenerator<? extends ITypeGenerator<?>> mainType) {
    mainType
        .withField(FieldGenerator.createSerialVersionUid())
        .withSuperClass(BasicPermission.class.getName())
        .withMethod(createConstructor(mainType));
  }

  protected IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> createConstructor(ITypeGenerator<? extends ITypeGenerator<?>> constructorOwner) {
    return MethodGenerator.create()
        .asPublic()
        .withElementName(constructorOwner.elementName().orElseThrow(() -> newFail("Permission name is missing")))
        .withBody(b -> b.superClause().parenthesisOpen().classLiteral(constructorOwner.fullyQualifiedName()).append(".getSimpleName()").parenthesisClose().semicolon());
  }
}
