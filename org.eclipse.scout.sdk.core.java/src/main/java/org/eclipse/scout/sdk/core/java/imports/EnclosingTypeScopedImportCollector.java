/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.imports;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.query.HierarchyInnerTypeQuery;
import org.eclipse.scout.sdk.core.java.model.api.query.SuperTypeQuery;

/**
 * Ignore imports when the referenced type is a member type of the enclosing type or in a super type of it
 */
public class EnclosingTypeScopedImportCollector extends WrappedImportCollector {
  private final String m_qualifier;
  private final Set<String> m_visibleInnerTypeInSuperHierarchyNames; // fully qualified names as far as these types exist already!
  private final Set<String> m_innerTypeSimpleNames; // simple names as far as these types exist already!

  public EnclosingTypeScopedImportCollector(IImportCollector inner, ITypeGenerator<?> enclosingTypeGenerator) {
    super(inner);
    m_qualifier = enclosingTypeGenerator.fullyQualifiedName();

    // inner types
    m_innerTypeSimpleNames = enclosingTypeGenerator.types()
        .map(t -> t.elementName(getContext().orElse(null)))
        .flatMap(Optional::stream)
        .collect(toSet());

    var context = getContext().orElse(null);
    if (context == null) {
      m_visibleInnerTypeInSuperHierarchyNames = emptySet();
      return;
    }
    var env = getJavaEnvironment().orElse(null);
    if (env == null) {
      m_visibleInnerTypeInSuperHierarchyNames = emptySet();
      return;
    }

    m_visibleInnerTypeInSuperHierarchyNames = Stream.concat(enclosingTypeGenerator.superClassFunc().stream(), enclosingTypeGenerator.interfacesFunc())
        .map(af -> af.apply(context))
        .map(JavaTypes::erasure)
        .map(env::findType)
        .flatMap(Optional::stream)
        .map(IType::superTypes)
        .flatMap(SuperTypeQuery::stream)
        .filter(s -> !Object.class.getName().equals(s.name()))
        .map(IType::innerTypes)
        .flatMap(HierarchyInnerTypeQuery::stream)
        .filter(t -> !Flags.isPrivate(t.flags()))
        .map(IType::name)
        .map(s -> s.replace(JavaTypes.C_DOLLAR, JavaTypes.C_DOT))
        .collect(toSet());
  }

  @Override
  public String getQualifier() {
    return m_qualifier;
  }

  @Override
  public String checkCurrentScope(TypeReferenceDescriptor candidate) {
    if (m_qualifier.equals(candidate.getQualifiedName())) {
      return candidate.getSimpleName(); // myself
    }
    if (m_innerTypeSimpleNames.contains(candidate.getSimpleName())) {
      // a type with the same simple name as an inner type.
      if (m_qualifier.equals(candidate.getQualifier())) {
        return candidate.getSimpleName(); // it is the inner type. simple qualification
      }
      return candidate.getQualifiedName(); // it is another type with the same simple name as an inner type: full qualification necessary
    }
    if (m_visibleInnerTypeInSuperHierarchyNames.contains(candidate.getQualifiedName())) {
      return candidate.getSimpleName(); // reference to a type visible because it is an inner type in the super hierarchy
    }

    return super.checkCurrentScope(candidate);
  }
}
