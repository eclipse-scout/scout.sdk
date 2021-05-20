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
package org.eclipse.scout.sdk.core.imports;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.query.HierarchyInnerTypeQuery;
import org.eclipse.scout.sdk.core.model.api.query.SuperTypeQuery;
import org.eclipse.scout.sdk.core.util.JavaTypes;

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
        .map(ITypeGenerator::elementName)
        .flatMap(Optional::stream)
        .collect(toSet());

    var env = getJavaEnvironment();
    if (env == null) {
      m_visibleInnerTypeInSuperHierarchyNames = emptySet();
      return;
    }

    m_visibleInnerTypeInSuperHierarchyNames = Stream.concat(enclosingTypeGenerator.superClass().stream(), enclosingTypeGenerator.interfaces())
        .flatMap(af -> af.apply(env).stream())
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
  public String checkCurrentScope(TypeReferenceDescriptor descriptor) {
    if (m_qualifier.equals(descriptor.getQualifiedName())) {
      return descriptor.getSimpleName(); // myself
    }
    if (m_innerTypeSimpleNames.contains(descriptor.getSimpleName())) {
      // a type with the same simple name as an inner type.
      if (m_qualifier.equals(descriptor.getQualifier())) {
        return descriptor.getSimpleName(); // it is the inner type. simple qualification
      }
      return descriptor.getQualifiedName(); // it is another type with the same simple name as an inner type: full qualification necessary
    }
    if (m_visibleInnerTypeInSuperHierarchyNames.contains(descriptor.getQualifiedName())) {
      return descriptor.getSimpleName(); // reference to a type visible because its an inner type in the super hierarchy
    }

    return super.checkCurrentScope(descriptor);
  }
}
