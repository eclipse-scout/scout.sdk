/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.generator.field;

import org.eclipse.scout.sdk.core.java.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutAbstractApi;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link ScoutFieldGenerator}</h3>
 */
public class ScoutFieldGenerator extends FieldGenerator<ScoutFieldGenerator> {

  protected ScoutFieldGenerator() {
  }

  public static ScoutFieldGenerator create() {
    return new ScoutFieldGenerator();
  }

  /**
   * Creates an {@link ScoutFieldGenerator} which creates a permission id constant of the form
   *
   * <pre>
   * public static final String ID = "SomePermission"
   * </pre>
   *
   * @param permissionName
   *          The simple name of the permission.
   * @param fieldName
   *          The name of the field to be created.
   * @return The created {@link ScoutFieldGenerator}.
   */
  public static ScoutFieldGenerator createPermissionIdField(String permissionName, String fieldName) {
    return create()
        .asPublic()
        .asStatic()
        .asFinal()
        .withDataTypeFrom(IScoutApi.class, IScoutAbstractApi::getPermissionIdFieldDataTypeFqn)
        .withElementName(Ensure.notBlank(fieldName))
        .withValue(b -> b.context().requireApi(IScoutApi.class).appendPermissionIdFieldValue(b, Ensure.notBlank(permissionName)));
  }
}
