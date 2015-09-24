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
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.importvalidator.ImportElementCandidate;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IImport;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.AbstractJavaElementSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.RawSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.PropertyMap;
import org.eclipse.scout.sdk.core.util.SdkConsole;

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
  private final Map<CompositeObject, ITypeSourceBuilder> m_sortedTypes = new TreeMap<>();
  private final StringBuilder m_errors = new StringBuilder();

  /**
   * @param elementName
   */
  public CompilationUnitSourceBuilder(ICompilationUnit element) {
    super(element);
    m_packageName = element.getPackage().getName();
    for (IImport imp : element.getImports()) {
      if (imp.isStatic()) {
        addDeclaredStaticImport(imp.getName());
      }
      else {
        addDeclaredImport(imp.getName());
      }
    }
    if (element.getJavaDoc() != null) {
      setComment(new RawSourceBuilder(element.getJavaDoc().toString()));
    }
    for (IType type : element.getTypes()) {
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
  public final void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator0) {
    IImportValidator validator = new CompilationUnitScopedImportValidator(validator0, getPackageName());

    // loop through all types recursively to ensure all simple names that will be created are "consumed" in the import validator
    consumeAllTypeNamesRec(m_types, validator);

    // empty package name is default package
    if (getPackageName() == null) {
      throw new IllegalArgumentException("package name is null!");
    }

    //declared imports
    for (String s : m_declaredImports) {
      validator.addImport(s);
    }
    for (String s : m_declaredStaticImports) {
      validator.addStaticImport(s);
    }

    // header
    StringBuilder headerSourceBuilder = new StringBuilder();
    super.createSource(headerSourceBuilder, lineDelimiter, context, validator);
    // package declaration
    if (!StringUtils.isEmpty(getPackageName())) {
      headerSourceBuilder.append("package ").append(getPackageName()).append(";").append(lineDelimiter).append(lineDelimiter);
    }

    StringBuilder typeSourceBuilder = new StringBuilder();
    // type sources
    for (ITypeSourceBuilder typeBuilder : getTypes()) {
      if (typeBuilder != null) {
        typeBuilder.createSource(typeSourceBuilder, lineDelimiter, context, validator);
      }
    }

    // imports
    Collection<String> importsToCreate = validator.createImportDeclarations();
    if (importsToCreate.size() > 0) {
      for (String imp : importsToCreate) {
        headerSourceBuilder.append(imp).append(lineDelimiter);
      }
      headerSourceBuilder.append(lineDelimiter);
    }
    source.append(headerSourceBuilder);
    source.append(typeSourceBuilder);
    source.append(lineDelimiter);
    appendErrorMessages(source, lineDelimiter, context, validator);
  }

  private static void consumeAllTypeNamesRec(Collection<ITypeSourceBuilder> typeBuilders, IImportValidator validator) {
    for (ITypeSourceBuilder typeSrc : typeBuilders) {
      String fqn = typeSrc.getFullyQualifiedName();
      validator.reserveElement(new ImportElementCandidate(Signature.createTypeSignature(fqn)));
      consumeAllTypeNamesRec(typeSrc.getTypes(), validator);
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
  public void addErrorMessage(String taskType, String msg, Throwable... exceptions) {
    SdkConsole.println(getPackageName() + " " + getElementName() + " " + msg, exceptions);
    if (msg != null) {
      m_errors.append(taskType + " [generator] " + msg);
      m_errors.append("\n");
    }
    if (exceptions != null) {
      for (Throwable t : exceptions) {
        m_errors.append(SdkConsole.formatException(t));
        m_errors.append("\n");
      }
    }
  }

  protected void appendErrorMessages(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    if (m_errors.length() > 0) {
      source.append("/*");
      source.append(lineDelimiter);
      source.append(m_errors.toString().replace("\n", lineDelimiter));
      source.append(lineDelimiter);
      source.append("*/");
    }
  }
}