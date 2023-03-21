/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js.enums;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IExportFrom;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.IVariable;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class ES6ClassTypeAliasScoutEnum implements IScoutJsEnum {

  protected static final Pattern TYPE_ALIAS_ENUM_PATTERN = Pattern.compile("export\\s+type\\s+\\w+\\s*=\\s*EnumObject\\s*<\\s*typeof\\s+(\\w+)(?:\\s*\\.\\s*(\\w+))?\\s*>\\s*;");

  private final ScoutJsModel m_scoutJsModel;
  private final IES6Class m_class;
  private final IScoutJsEnum m_wrappedEnum;

  protected ES6ClassTypeAliasScoutEnum(ScoutJsModel scoutJsModel, IES6Class clazz, IScoutJsEnum wrappedEnum) {
    m_scoutJsModel = scoutJsModel;
    m_class = Ensure.notNull(clazz);
    m_wrappedEnum = Ensure.notNull(wrappedEnum);
  }

  public static Optional<IScoutJsEnum> create(ScoutJsModel owner, IES6Class clazz) {
    if (owner == null || clazz == null || !clazz.isTypeAlias()) {
      return Optional.empty();
    }
    return clazz.source()
        .map(SourceRange::asCharSequence)
        .map(TYPE_ALIAS_ENUM_PATTERN::matcher)
        .filter(Matcher::matches)
        .flatMap(matcher -> owner.nodeModule()
            .export(matcher.group(1))
            .map(IExportFrom::referencedElement)
            .flatMap(reference -> {
              if (reference instanceof IVariable variable) {
                return VariableScoutEnum.create(owner, variable)
                    .map(scoutJsEnum -> new ES6ClassTypeAliasScoutEnum(owner, clazz, scoutJsEnum));
              }
              if (reference instanceof IES6Class element && matcher.groupCount() > 1) {
                return element.field(matcher.group(2))
                    .flatMap(field -> VariableScoutEnum.create(owner, field))
                    .map(scoutJsEnum -> new ES6ClassTypeAliasScoutEnum(owner, clazz, scoutJsEnum));
              }
              return Optional.empty();
            }));
  }

  @Override
  public ScoutJsModel scoutJsModel() {
    return m_scoutJsModel;
  }

  protected IScoutJsEnum wrappedEnum() {
    return m_wrappedEnum;
  }

  @Override
  public String name() {
    return declaringClass().name();
  }

  @Override
  public IES6Class declaringClass() {
    return m_class;
  }

  @Override
  public String referenceName() {
    return wrappedEnum().referenceName();
  }

  @Override
  public INodeElement topLevelReference() {
    return wrappedEnum().topLevelReference();
  }

  @Override
  public List<String> constants() {
    return wrappedEnum().constants();
  }

  @Override
  public boolean fulfills(IDataType dataType) {
    return dataType == declaringClass() || wrappedEnum().fulfills(dataType);
  }

  @Override
  public String toString() {
    return declaringClass().toString();
  }
}
