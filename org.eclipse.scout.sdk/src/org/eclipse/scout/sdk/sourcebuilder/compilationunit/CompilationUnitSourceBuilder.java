/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.sourcebuilder.compilationunit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.sourcebuilder.AbstractJavaElementSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.signature.IImportValidator;

/**
 * <h3>{@link CompilationUnitSourceBuilder}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public class CompilationUnitSourceBuilder extends AbstractJavaElementSourceBuilder implements ICompilationUnitSourceBuilder {

  private final String m_packageName;

  private final List<ITypeSourceBuilder> m_typeSourceBuilders;
  private final Map<CompositeObject, ITypeSourceBuilder> m_sortedTypeSourceBuilders;

  /**
   * @param elementName
   */
  public CompilationUnitSourceBuilder(String elementName, String packageName) {
    super(elementName);
    m_packageName = packageName;
    m_typeSourceBuilders = new ArrayList<ITypeSourceBuilder>();
    m_sortedTypeSourceBuilders = new TreeMap<CompositeObject, ITypeSourceBuilder>();
  }

  @Override
  public void validate() throws IllegalArgumentException {
    super.validate();
    // empty package name is default package
    if (getPackageFragmentName() == null) {
      throw new IllegalArgumentException("package name is null!");
    }
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    // header
    StringBuilder headerSourceBuilder = new StringBuilder();
    super.createSource(headerSourceBuilder, lineDelimiter, ownerProject, validator);
    // package declaration
    if (!StringUtility.isNullOrEmpty(getPackageFragmentName())) {
      headerSourceBuilder.append("package ").append(getPackageFragmentName()).append(";").append(lineDelimiter).append(lineDelimiter);
    }

    StringBuilder typeSourceBuilder = new StringBuilder();
    // type sources
    for (ITypeSourceBuilder typeBuilder : getTypeSourceBuilder()) {
      if (typeBuilder != null) {
        typeBuilder.createSource(typeSourceBuilder, lineDelimiter, ownerProject, validator);
      }
    }

    // imports
    Set<String> importsToCreate = validator.getImportsToCreate();
    if (importsToCreate.size() > 0) {
      for (String imp : importsToCreate) {
        headerSourceBuilder.append("import ").append(imp).append(";").append(lineDelimiter);
      }
      headerSourceBuilder.append(lineDelimiter);
    }
    source.append(headerSourceBuilder);
    source.append(typeSourceBuilder);
  }

  @Override
  public String getPackageFragmentName() {
    return m_packageName;
  }

  @Override
  public void addTypeSourceBuilder(ITypeSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("Source builder can not be null.");
    }
    if (!m_sortedTypeSourceBuilders.isEmpty()) {
      throw new IllegalStateException("This builder has already sorted inner type builders. A mix between sorted and unsorted inner type builders is not supported.");
    }
    m_typeSourceBuilders.add(builder);
    builder.setParentFullyQualifiedName(getPackageFragmentName());
  }

  @Override
  public void addSortedTypeSourceBuilder(CompositeObject sortKey, ITypeSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("Source builder can not be null.");
    }
    if (!m_typeSourceBuilders.isEmpty()) {
      throw new IllegalStateException("This builder has already unsorted inner type builders. A mix between sorted and unsorted inner type builders is not supported.");
    }
    m_sortedTypeSourceBuilders.put(sortKey, builder);
    builder.setParentFullyQualifiedName(getPackageFragmentName());
  }

  @Override
  public boolean removeTypeSourceBuilder(ITypeSourceBuilder builder) {
    boolean removed = m_typeSourceBuilders.remove(builder);
    if (!removed) {
      Iterator<Entry<CompositeObject, ITypeSourceBuilder>> it = m_sortedTypeSourceBuilders.entrySet().iterator();
      while (it.hasNext()) {
        if (it.next().getValue().equals(builder)) {
          it.remove();
          return true;
        }
      }
      return false;
    }
    return removed;
  }

  @Override
  public List<ITypeSourceBuilder> getTypeSourceBuilder() {
    List<ITypeSourceBuilder> builders = new ArrayList<ITypeSourceBuilder>();
    builders.addAll(m_typeSourceBuilders);
    builders.addAll(m_sortedTypeSourceBuilders.values());
    return Collections.unmodifiableList(builders);
  }

}
