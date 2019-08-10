/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.query.HierarchyInnerTypeQuery;
import org.eclipse.scout.sdk.core.model.api.query.SuperTypeQuery;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * Ignore imports when the referenced type is a member type of the enclosing type or super classes of it
 */
public class EnclosingTypeScopedImportCollector extends WrappedImportCollector {
  private final String m_qualifier;
  //as far as these types exist already!
  private final Set<String> m_enclosingQualifiers;
  //as far as these types exist already!
  private final Set<String> m_enclosedSimpleNames;

  public EnclosingTypeScopedImportCollector(IImportCollector inner, ITypeGenerator<?> enclosingTypeGenerator) {
    super(inner);
    m_enclosingQualifiers = new HashSet<>();
    m_qualifier = enclosingTypeGenerator.fullyQualifiedName();

    //self
    m_enclosingQualifiers.add(m_qualifier);

    // inner types
    m_enclosedSimpleNames = enclosingTypeGenerator.types()
        .map(ITypeGenerator::elementName)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(toSet());

    IJavaEnvironment env = getJavaEnvironment();
    if (env == null) {
      return;
    }

    Stream<String> superTypeSignatures = Stream.concat(
        enclosingTypeGenerator.superClass()
            .map(Stream::of)
            .orElseGet(Stream::empty),
        enclosingTypeGenerator.interfaces());

    superTypeSignatures
        .map(JavaTypes::erasure)
        .peek(m_enclosingQualifiers::add)
        .map(env::findType)
        .filter(Optional::isPresent)
        .map(Optional::get)
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
    //same qualifier
    if (m_enclosingQualifiers.contains(cand.getQualifier())) {
      return cand.getSimpleName();
    }

    // check if simpleName (with other qualifier) exists in same enclosing type
    if (m_enclosedSimpleNames.contains(cand.getSimpleName())) {
      //must qualify
      return cand.getQualifiedName();
    }

    return super.checkCurrentScope(cand);
  }
}
