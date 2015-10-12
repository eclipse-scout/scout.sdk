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
package org.eclipse.scout.sdk.core.sourcebuilder.compilationunit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importcollector.IImportCollector;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IImport;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureDescriptor;
import org.eclipse.scout.sdk.core.sourcebuilder.AbstractJavaElementSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link CompilationUnitSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public class CompilationUnitSourceBuilder extends AbstractJavaElementSourceBuilder implements ICompilationUnitSourceBuilder {
  private final String m_packageName;
  private final List<String> m_declaredImports = new ArrayList<>();
  private final List<String> m_declaredStaticImports = new ArrayList<>();
  private final List<ITypeSourceBuilder> m_types = new ArrayList<>();
  private final List<ISourceBuilder> m_footerSourceBuilders = new ArrayList<>();
  private final Map<CompositeObject, ITypeSourceBuilder> m_sortedTypes = new TreeMap<>();

  /**
   * @param elementName
   */
  public CompilationUnitSourceBuilder(ICompilationUnit element) {
    super(element);
    m_packageName = element.containingPackage().name();
    for (IImport imp : element.imports()) {
      if (imp.isStatic()) {
        addDeclaredStaticImport(imp.elementName());
      }
      else {
        addDeclaredImport(imp.elementName());
      }
    }
    if (element.javaDoc() != null) {
      setComment(new RawSourceBuilder(element.javaDoc().toString()));
    }
    for (IType type : element.types().list()) {
      addType(new TypeSourceBuilder(type));
    }
  }

  /**
   * @param elementName
   */
  public CompilationUnitSourceBuilder(String elementName, String packageName) {
    super(elementName);
    m_packageName = packageName;
  }

  @Override
  public final void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    // add CU scope to import validator chain
    IImportCollector collector = new CompilationUnitScopedImportCollector(validator.getImportCollector(), getPackageName());
    validator.setImportCollector(collector);

    // loop through all types recursively to ensure all simple names that will be created are "consumed" in the import validator
    consumeAllTypeNamesRec(m_types, collector);

    // empty package name is default package
    if (getPackageName() == null) {
      throw new IllegalArgumentException("package name is null!");
    }

    //declared imports
    for (String s : m_declaredImports) {
      collector.addImport(s);
    }
    for (String s : m_declaredStaticImports) {
      collector.addStaticImport(s);
    }

    // header
    StringBuilder headerSourceBuilder = new StringBuilder();
    super.createSource(headerSourceBuilder, lineDelimiter, context, validator);

    // package declaration
    if (!StringUtils.isEmpty(getPackageName())) {
      headerSourceBuilder.append("package ").append(getPackageName()).append(";").append(lineDelimiter).append(lineDelimiter);
    }

    // type sources
    StringBuilder typeSourceBuilder = new StringBuilder();
    for (ITypeSourceBuilder typeBuilder : getTypes()) {
      if (typeBuilder != null) {
        typeBuilder.createSource(typeSourceBuilder, lineDelimiter, context, validator);
      }
    }

    // imports
    Collection<String> importsToCreate = collector.createImportDeclarations();
    if (importsToCreate.size() > 0) {
      for (String imp : importsToCreate) {
        headerSourceBuilder.append(imp).append(lineDelimiter);
      }
      headerSourceBuilder.append(lineDelimiter);
    }

    source.append(headerSourceBuilder);
    source.append(typeSourceBuilder);
    source.append(lineDelimiter);

    // footer
    for (ISourceBuilder f : m_footerSourceBuilders) {
      f.createSource(source, lineDelimiter, context, validator);
    }
  }

  private static void consumeAllTypeNamesRec(Collection<ITypeSourceBuilder> typeBuilders, IImportCollector collector) {
    for (ITypeSourceBuilder typeSrc : typeBuilders) {
      String fqn = typeSrc.getFullyQualifiedName();
      collector.reserveElement(new SignatureDescriptor(Signature.createTypeSignature(fqn)));
      consumeAllTypeNamesRec(typeSrc.getTypes(), collector);
    }
  }

  @Override
  public String getPackageName() {
    return m_packageName;
  }

  @Override
  public void addDeclaredImport(String name) {
    m_declaredImports.add(name);
  }

  @Override
  public void addDeclaredStaticImport(String name) {
    m_declaredStaticImports.add(name);
  }

  @Override
  public List<String> getDeclaredImports() {
    return m_declaredImports;
  }

  @Override
  public List<String> getDeclaredStaticImports() {
    return m_declaredStaticImports;
  }

  @Override
  public void addType(ITypeSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("Source builder can not be null.");
    }
    if (!m_sortedTypes.isEmpty()) {
      throw new IllegalStateException("This builder has already sorted inner type builders. A mix between sorted and unsorted inner type builders is not supported.");
    }
    m_types.add(builder);
    builder.setDeclaringElement(this);
  }

  @Override
  public void addSortedType(CompositeObject sortKey, ITypeSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("Source builder can not be null.");
    }
    if (!m_types.isEmpty()) {
      throw new IllegalStateException("This builder has already unsorted inner type builders. A mix between sorted and unsorted inner type builders is not supported.");
    }
    m_sortedTypes.put(sortKey, builder);
    builder.setDeclaringElement(this);
  }

  @Override
  public boolean removeType(String elementName) {
    ITypeSourceBuilder builder = null;
    for (Iterator<ITypeSourceBuilder> it = m_types.iterator(); it.hasNext();) {
      builder = it.next();
      if (elementName.equals(builder.getElementName())) {
        builder.setDeclaringElement(null);
        it.remove();
        return true;
      }
    }
    for (Iterator<ITypeSourceBuilder> it = m_sortedTypes.values().iterator(); it.hasNext();) {
      builder = it.next();
      if (elementName.equals(builder.getElementName())) {
        builder.setDeclaringElement(null);
        it.remove();
        return true;
      }
    }
    return false;
  }

  @Override
  public ITypeSourceBuilder getMainType() {
    List<ITypeSourceBuilder> list = getTypes();
    return list.isEmpty() ? null : list.get(0);
  }

  @Override
  public List<ITypeSourceBuilder> getTypes() {
    List<ITypeSourceBuilder> builders = new ArrayList<>(m_types.size() + m_sortedTypes.size());
    builders.addAll(m_types);
    builders.addAll(m_sortedTypes.values());
    return builders;
  }

  @Override
  public void addFooter(ISourceBuilder footerSourceBuilder) {
    m_footerSourceBuilders.add(footerSourceBuilder);
  }

  @Override
  public List<ISourceBuilder> getFooters() {
    return new ArrayList<>(m_footerSourceBuilders);
  }
}
