/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.lookupcall;

import java.util.Optional;

import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodOverrideGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link LookupCallGenerator}</h3>
 *
 * @since 5.2.0
 */
public class LookupCallGenerator<TYPE extends LookupCallGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  private String m_lookupServiceInterface;
  private String m_classIdValue;

  @Override
  protected void fillMainType(ITypeGenerator<? extends ITypeGenerator<?>> mainType) {
    mainType
        .withAnnotation(classIdValue()
            .map(ScoutAnnotationGenerator::createClassId)
            .orElse(null))
        .withField(FieldGenerator.createSerialVersionUid());

    if (lookupServiceInterface().isPresent()) {
      mainType.withMethod(MethodOverrideGenerator.createOverride()
          .withElementName("getConfiguredService")
          .withBody(b -> b.returnClause().classLiteral(lookupServiceInterface().get()).semicolon()));
    }
  }

  public Optional<String> lookupServiceInterface() {
    return Strings.notBlank(m_lookupServiceInterface);
  }

  public TYPE withLookupServiceInterface(String lookupServiceIfc) {
    m_lookupServiceInterface = lookupServiceIfc;
    return currentInstance();
  }

  public Optional<String> classIdValue() {
    return Strings.notBlank(m_classIdValue);
  }

  public TYPE withClassIdValue(String classIdValue) {
    m_classIdValue = classIdValue;
    return currentInstance();
  }
}
