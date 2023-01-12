/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.annotation;

import java.util.Optional;

import org.eclipse.scout.sdk.core.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.model.api.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;

/**
 * <h3>{@link ColumnDataAnnotation}</h3>
 *
 * @since 5.2.0
 */
public class ColumnDataAnnotation extends AbstractManagedAnnotation {

  protected static final ApiFunction<?, ITypeNameSupplier> TYPE_NAME = new ApiFunction<>(IScoutApi.class, IScoutApi::ColumnData);
  public static final SdkColumnCommand DEFAULT_VALUE = SdkColumnCommand.CREATE;

  /**
   * Gets the {@link SdkColumnCommand} value of the given {@link IAnnotatable}.
   *
   * @param owner
   *          The owner to be asked. Must not be {@code null}.
   * @return An {@link Optional} describing the {@link SdkColumnCommand} of the given {@link IAnnotatable}.<br>
   *         The {@link Optional} can have one of the following values:
   *         <ul>
   *         <li>{@link SdkColumnCommand#CREATE}: Specifies that a DTO container for this column should be created. This
   *         is the default value of the {@code @ColumnData} annotation.</li>
   *         <li>{@link SdkColumnCommand#IGNORE}: Specifies that no DTO container for this column should be
   *         created.</li>
   *         <li>Empty {@link Optional}: There is no {@code @ColumnData} annotation on the given {@link IAnnotatable}
   *         which means the value is specified by the super class.</li>
   *         </ul>
   */
  public static Optional<SdkColumnCommand> valueOf(IAnnotatable owner) {
    return owner.annotations()
        .withManagedWrapper(ColumnDataAnnotation.class)
        .first()
        .map(ColumnDataAnnotation::value);
  }

  /**
   * @return The {@link SdkColumnCommand} value of this @ColumnData annotation. Never returns {@code null}. In case the
   *         value is not explicitly given in the annotation the default value {@link #DEFAULT_VALUE} is returned.
   */
  public SdkColumnCommand value() {
    return getValueAsEnumFrom(IScoutApi.class, api -> api.ColumnData().valueElementName(), SdkColumnCommand.class);
  }

  public boolean isValueDefault() {
    return isDefault(IScoutApi.class, api -> api.ColumnData().valueElementName());
  }

  public enum SdkColumnCommand {
    CREATE, IGNORE
  }

  /**
   * Parses the possible available ColumnData annotation on the given type. The
   * {@link Optional} is empty if the given type is {@code null} or the column does not define a {@code @ColumnData}
   * annotation (including super classes).
   *
   * @since 3.10.0-M5
   */
  public static Optional<SdkColumnCommand> sdkColumnCommandOf(IType type) {
    if (type == null) {
      return Optional.empty();
    }

    var sdkColumnCommand = valueOf(type);
    if (sdkColumnCommand.orElse(DEFAULT_VALUE) == SdkColumnCommand.IGNORE || !type.annotations().withManagedWrapper(ReplaceAnnotation.class).existsAny()) {
      return sdkColumnCommand;
    }

    // we are replacing. Only if the parent is set to IGNORE and we are set to CREATE we have to create anything.
    // otherwise the column data is already available and nothing has to be done (IGNORE).
    var replacedType = type.superClass();
    if (replacedType.isPresent()) {
      var sdkColumnCommandOfReplaced = sdkColumnCommandOf(replacedType.orElseThrow());
      if (sdkColumnCommand.orElse(SdkColumnCommand.IGNORE) == SdkColumnCommand.CREATE
          && sdkColumnCommandOfReplaced.orElse(SdkColumnCommand.CREATE) == SdkColumnCommand.IGNORE) {
        return Optional.of(SdkColumnCommand.CREATE);
      }
    }
    return Optional.of(SdkColumnCommand.IGNORE);
  }
}
