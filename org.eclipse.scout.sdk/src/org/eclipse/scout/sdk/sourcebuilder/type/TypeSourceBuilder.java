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
package org.eclipse.scout.sdk.sourcebuilder.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.extensions.classidgenerators.ClassIdGenerators;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.sourcebuilder.AbstractAnnotatableSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 * <h3>{@link TypeSourceBuilder}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public class TypeSourceBuilder extends AbstractAnnotatableSourceBuilder implements ITypeSourceBuilder {

  private String m_superTypeSignature;
  private String m_parentFullyQualifiedName;
  private ITypeSourceBuilder m_parentSourceBuilder;
  private final List<String> m_interfaceSignatures;
  private final List<IFieldSourceBuilder> m_fieldSourceBuilders;
  private final Map<CompositeObject, IFieldSourceBuilder> m_sortedFieldSourceBuilders;
  private final List<IMethodSourceBuilder> m_methodSourceBuilders;
  private final Map<CompositeObject, IMethodSourceBuilder> m_sortedMethodSourceBuilders;
  private final List<ITypeSourceBuilder> m_typeSourceBuilders;
  private final Map<CompositeObject, ITypeSourceBuilder> m_sortedTypeSourceBuilders;

  /**
   * @param elementName
   * @param parentBuilder
   */
  public TypeSourceBuilder(String elementName) {
    super(elementName);
    m_interfaceSignatures = new ArrayList<String>();
    m_fieldSourceBuilders = new ArrayList<IFieldSourceBuilder>();
    m_sortedFieldSourceBuilders = new TreeMap<CompositeObject, IFieldSourceBuilder>();
    m_methodSourceBuilders = new ArrayList<IMethodSourceBuilder>();
    m_sortedMethodSourceBuilders = new TreeMap<CompositeObject, IMethodSourceBuilder>();
    m_typeSourceBuilders = new ArrayList<ITypeSourceBuilder>();
    m_sortedTypeSourceBuilders = new TreeMap<CompositeObject, ITypeSourceBuilder>();
  }

  @Override
  public void validate() throws IllegalArgumentException {
    super.validate();
    if (Flags.isInterface(getFlags()) && getSuperTypeSignature() != null) {
      throw new IllegalArgumentException("An interface can not have a superclass.");
    }
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    super.createSource(source, lineDelimiter, ownerProject, validator);
    createClassIdAnnotation(source, lineDelimiter, ownerProject, validator);
    // type definition
    source.append(Flags.toString(getFlags())).append(" ");
    source.append(((getFlags() & Flags.AccInterface) != 0) ? ("interface ") : ("class "));
    source.append(getElementName());

    // add our own type name to the validator so that it cannot interfere with other types (e.g. in the jre) with the same name.
    validator.getTypeName(SignatureCache.createTypeSignature(getFullyQualifiedName()));

    // super type (extends)
    if (!StringUtility.isNullOrEmpty(getSuperTypeSignature())) {
      String superTypeRefName = SignatureUtility.getTypeReference(getSuperTypeSignature(), validator);
      source.append(" extends ").append(superTypeRefName);
    }

    // interfaces
    Iterator<String> interfaceSigIterator = getInterfaceSignatures().iterator();
    if (interfaceSigIterator.hasNext()) {
      source.append(((getFlags() & Flags.AccInterface) != 0) ? (" extends ") : (" implements "));
      source.append(SignatureUtility.getTypeReference(interfaceSigIterator.next(), validator));
      while (interfaceSigIterator.hasNext()) {
        source.append(", ").append(SignatureUtility.getTypeReference(interfaceSigIterator.next(), validator));
      }
    }
    source.append("{");
    createTypeContent(source, lineDelimiter, ownerProject, validator);
    source.append(lineDelimiter);
    source.append("}");
  }

  protected void createClassIdAnnotation(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    if (ClassIdGenerators.isAutomaticallyCreateClassIdAnnotation() && !StringUtility.isNullOrEmpty(getSuperTypeSignature())) {
      IType iTypeWithClassId = TypeUtility.getType(IRuntimeClasses.ITypeWithClassId);
      if (TypeUtility.exists(iTypeWithClassId)) {
        IType superType = TypeUtility.getTypeBySignature(getSuperTypeSignature());
        if (TypeUtility.exists(superType)) {
          if (TypeUtility.getSuperTypeHierarchy(superType).contains(iTypeWithClassId)) {
            IAnnotationSourceBuilder createClassIdAnnotation = AnnotationSourceBuilderFactory.createClassIdAnnotation(this);
            createClassIdAnnotation.createSource(source, lineDelimiter, ownerProject, validator);
            source.append(lineDelimiter);
          }
        }
      }
    }
  }

  /**
   * @param sourceBuilder
   * @param icu
   * @param lineDelimiter
   * @param validator
   * @throws CoreException
   */
  protected void createTypeContent(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    // fields
    List<IFieldSourceBuilder> fieldSourceBuilders = getFieldSourceBuilders();
    if (!fieldSourceBuilders.isEmpty()) {
      source.append(lineDelimiter);
      for (IFieldSourceBuilder builder : fieldSourceBuilders) {
        if (builder != null) {
          source.append(lineDelimiter);
          builder.createSource(source, lineDelimiter, ownerProject, validator);
        }
      }
    }
    // methods
    List<IMethodSourceBuilder> methodSourceBuilders = getMethodSourceBuilders();
    if (!methodSourceBuilders.isEmpty()) {
      source.append(lineDelimiter);
      for (IMethodSourceBuilder op : methodSourceBuilders) {
        if (op != null) {
          source.append(lineDelimiter);
          op.createSource(source, lineDelimiter, ownerProject, validator);
        }
      }
    }
    // inner types
    List<ITypeSourceBuilder> innerTypes = getTypeSourceBuilder();
    if (!innerTypes.isEmpty()) {
      source.append(lineDelimiter);
      for (ITypeSourceBuilder op : innerTypes) {
        if (op != null) {
          source.append(lineDelimiter);
          op.createSource(source, lineDelimiter, ownerProject, validator);
        }
      }
    }
  }

  @Override
  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  @Override
  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  @Override
  public void addInterfaceSignature(String interfaceSignature) {
    m_interfaceSignatures.add(interfaceSignature);
  }

  @Override
  public boolean removeInterfaceSignature(String interfaceSignature) {
    return m_interfaceSignatures.remove(interfaceSignature);
  }

  @Override
  public void setInterfaceSignatures(String[] interfaceSignatures) {
    m_interfaceSignatures.clear();
    m_interfaceSignatures.addAll(Arrays.asList(interfaceSignatures));
  }

  @Override
  public List<String> getInterfaceSignatures() {
    return m_interfaceSignatures;
  }

  @Override
  public void addFieldSourceBuilder(IFieldSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("Source builder can not be null.");
    }
    if (!m_sortedFieldSourceBuilders.isEmpty()) {
      throw new IllegalStateException("This builder has already sorted field builder. A mix between sorted and unsorted field builders is not supported.");
    }
    m_fieldSourceBuilders.add(builder);
  }

  @Override
  public void addSortedFieldSourceBuilder(CompositeObject sortKey, IFieldSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("Source builder can not be null.");
    }
    if (!m_fieldSourceBuilders.isEmpty()) {
      throw new IllegalStateException("This builder has already unsorted field builder. A mix between sorted and unsorted field builders is not supported.");
    }
    m_sortedFieldSourceBuilders.put(sortKey, builder);
  }

  @Override
  public boolean removeFieldSourceBuilder(IFieldSourceBuilder builder) {
    boolean removed = m_fieldSourceBuilders.remove(builder);
    if (!removed) {
      Iterator<Entry<CompositeObject, IFieldSourceBuilder>> it = m_sortedFieldSourceBuilders.entrySet().iterator();
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
  public List<IFieldSourceBuilder> getFieldSourceBuilders() {
    List<IFieldSourceBuilder> ops = new ArrayList<IFieldSourceBuilder>(m_fieldSourceBuilders.size() + m_sortedFieldSourceBuilders.size());
    ops.addAll(m_fieldSourceBuilders);
    ops.addAll(m_sortedFieldSourceBuilders.values());
    return ops;
  }

  @Override
  public void addMethodSourceBuilder(IMethodSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("Source builder can not be null.");
    }
    if (!m_sortedMethodSourceBuilders.isEmpty()) {
      throw new IllegalStateException("This source builder has already sorted method builders. A mix between sorted and unsorted method builders is not supported.");
    }
    m_methodSourceBuilders.add(builder);
  }

  @Override
  public void addSortedMethodSourceBuilder(CompositeObject sortKey, IMethodSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("Source builder can not be null.");
    }
    if (!m_methodSourceBuilders.isEmpty()) {
      throw new IllegalStateException("This source builder has already unsorted method builders. A mix between sorted and unsorted method builders is not supported.");
    }
    m_sortedMethodSourceBuilders.put(sortKey, builder);
  }

  @Override
  public boolean removeMethodSourceBuilder(IMethodSourceBuilder builder) {
    boolean removed = m_methodSourceBuilders.remove(builder);
    if (!removed) {
      Iterator<Entry<CompositeObject, IMethodSourceBuilder>> it = m_sortedMethodSourceBuilders.entrySet().iterator();
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
  public List<IMethodSourceBuilder> getMethodSourceBuilders() {
    List<IMethodSourceBuilder> builders = new ArrayList<IMethodSourceBuilder>(m_methodSourceBuilders.size() + m_sortedMethodSourceBuilders.size());
    builders.addAll(m_methodSourceBuilders);
    builders.addAll(m_sortedMethodSourceBuilders.values());
    return builders;
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
    builder.setParentTypeSourceBuilder(this);
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
    builder.setParentTypeSourceBuilder(this);
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
    List<ITypeSourceBuilder> typeBuilders = new ArrayList<ITypeSourceBuilder>(m_typeSourceBuilders.size() + m_sortedTypeSourceBuilders.size());
    typeBuilders.addAll(m_typeSourceBuilders);
    typeBuilders.addAll(m_sortedTypeSourceBuilders.values());
    return typeBuilders;
  }

  @Override
  public String getFullyQualifiedName() {
    ITypeSourceBuilder parentSourceBuilder = getParentTypeSourceBuilder();
    StringBuilder sb = null;
    if (parentSourceBuilder != null) {
      sb = new StringBuilder(parentSourceBuilder.getFullyQualifiedName());
    }
    else {
      String pfqn = getParentFullyQualifiedName();
      if (pfqn != null) {
        sb = new StringBuilder(pfqn);
      }
    }
    if (sb == null) {
      return null;
    }
    return sb.append('.').append(getElementName()).toString();
  }

  @Override
  public ITypeSourceBuilder getParentTypeSourceBuilder() {
    return m_parentSourceBuilder;
  }

  @Override
  public void setParentTypeSourceBuilder(ITypeSourceBuilder parentBuilder) {
    m_parentSourceBuilder = parentBuilder;
  }

  @Override
  public String getParentFullyQualifiedName() {
    return m_parentFullyQualifiedName;
  }

  @Override
  public void setParentFullyQualifiedName(String parentFullyQualifiedName) {
    m_parentFullyQualifiedName = parentFullyQualifiedName;
  }
}
