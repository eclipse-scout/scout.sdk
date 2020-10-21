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
package org.eclipse.scout.sdk.core.imports;

import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.query.HierarchyInnerTypeQuery;
import org.eclipse.scout.sdk.core.model.api.query.SuperTypeQuery;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * Ignore imports when the referenced type is a member type of the enclosing type or super classes of it
 */
public class EnclosingTypeScopedImportCollector extends WrappedImportCollector {
  private final String m_qualifier;
  private final Set<String> m_enclosingQualifiers; // as far as these types exist already!
  private final Set<String> m_enclosedSimpleNames; // as far as these types exist already!

  public EnclosingTypeScopedImportCollector(IImportCollector inner, ITypeGenerator<?> enclosingTypeGenerator) {
    super(inner);
    m_enclosingQualifiers = new HashSet<>();
    m_qualifier = enclosingTypeGenerator.fullyQualifiedName();

    //self
    m_enclosingQualifiers.add(m_qualifier);

    // inner types
    m_enclosedSimpleNames = enclosingTypeGenerator.types()
        .map(ITypeGenerator::elementName)
        .flatMap(Optional::stream)
        .collect(toSet());

    var env = getJavaEnvironment();
    if (env == null) {
      return;
    }

    var superTypeSignatures = Stream.concat(
        enclosingTypeGenerator.superClass()
            .flatMap(af -> af.apply(env))
            .stream(),
        enclosingTypeGenerator.interfaces()
            .map(af -> af.apply(env))
            .flatMap(Optional::stream));

    superTypeSignatures
        .map(JavaTypes::erasure)
        .peek(m_enclosingQualifiers::add)
        .map(env::findType)
        .flatMap(Optional::stream)
        .map(IType::superTypes)
        .flatMap(SuperTypeQuery::stream)
        .filter(s -> !Object.class.getName().equals(s.name()))
        .peek(s -> m_enclosingQualifiers.add(s.name()))
        .map(IType::innerTypes)
        .flatMap(HierarchyInnerTypeQuery::stream)
        .map(IType::elementName)
        .forEach(m_enclosedSimpleNames::add);
  }

  @Override
  public String getQualifier() {
    return m_qualifier;
  }

  @Override
  public String checkCurrentScope(TypeReferenceDescriptor cand) {
    if (m_enclosingQualifiers.contains(cand.getQualifier())) {
      if (m_enclosedSimpleNames.contains(cand.getSimpleName()) && !m_qualifier.equals(cand.getQualifier())) {
        return cand.getQualifiedName();
      }
      return cand.getSimpleName();
    }

    // check if simpleName (with other qualifier) exists in same enclosing type
    if (m_enclosedSimpleNames.contains(cand.getSimpleName())) {
      return cand.getQualifiedName();
    }

    return super.checkCurrentScope(cand);
  }
}
