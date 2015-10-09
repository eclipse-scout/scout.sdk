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
package org.eclipse.scout.sdk.core.sourcebuilder.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importcollector.IImportCollector;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.AbstractMemberSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.field.FieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.typeparameter.ITypeParameterSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.typeparameter.TypeParameterSourceBuilder;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link TypeSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public class TypeSourceBuilder extends AbstractMemberSourceBuilder implements ITypeSourceBuilder {
  private String m_superTypeSignature;
  private ISourceBuilder m_declaringElement;
  private final List<ITypeParameterSourceBuilder> m_typeParameters = new ArrayList<>();
  private final List<String> m_interfaceSignatures = new ArrayList<>();
  private final List<IFieldSourceBuilder> m_fields = new ArrayList<>();
  private final Map<CompositeObject, IFieldSourceBuilder> m_sortedFields = new TreeMap<>();
  private final List<IMethodSourceBuilder> m_methods = new ArrayList<>();
  private final Map<CompositeObject, IMethodSourceBuilder> m_sortedMethods = new TreeMap<>();
  private final List<ITypeSourceBuilder> m_types = new ArrayList<>();
  private final Map<CompositeObject, ITypeSourceBuilder> m_sortedTypes = new TreeMap<>();

  public TypeSourceBuilder(IType element) {
    super(element);
    for (ITypeParameter p : element.typeParameters()) {
      addTypeParameter(new TypeParameterSourceBuilder(p));
    }
    if (element.superClass() != null && !"java.lang.Object".equals(element.superClass().name())) {
      setSuperTypeSignature(SignatureUtils.getTypeSignature(element.superClass()));
    }
    for (IType i : element.superInterfaces()) {
      addInterfaceSignature(SignatureUtils.getTypeSignature(i));
    }
    for (IField field : element.fields().list()) {
      addField(new FieldSourceBuilder(field));
    }
    for (IMethod method : element.methods().list()) {
      addMethod(new MethodSourceBuilder(method));
    }
    for (IType type : element.innerTypes().list()) {
      addType(new TypeSourceBuilder(type));
    }
  }

  /**
   * @param elementName
   * @param parentBuilder
   */
  public TypeSourceBuilder(String elementName) {
    super(elementName);
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    IImportCollector collector = new EnclosingTypeScopedImportCollector(validator.getImportCollector(), this);
    validator.setImportCollector(collector);

    super.createSource(source, lineDelimiter, context, validator);

    if (Flags.isInterface(getFlags()) && getSuperTypeSignature() != null) {
      throw new IllegalArgumentException("An interface can not have a superclass.");
    }

    // type definition
    source.append(Flags.toString(getFlags())).append(' ');
    source.append(((getFlags() & Flags.AccInterface) != 0) ? ("interface ") : ("class "));
    source.append(getElementName());

    // type parameters
    if (!m_typeParameters.isEmpty()) {
      source.append(ISignatureConstants.C_GENERIC_START);
      for (ITypeParameterSourceBuilder p : m_typeParameters) {
        p.createSource(source, lineDelimiter, context, validator);
        source.append(", ");
      }
      source.setLength(source.length() - 2);
      source.append(ISignatureConstants.C_GENERIC_END);
    }

    // super type (extends)
    if (!StringUtils.isEmpty(getSuperTypeSignature())) {
      String superTypeRefName = validator.useSignature(getSuperTypeSignature());
      source.append(" extends ").append(superTypeRefName);
    }

    // interfaces
    Iterator<String> interfaceSigIterator = getInterfaceSignatures().iterator();
    if (interfaceSigIterator.hasNext()) {
      source.append(((getFlags() & Flags.AccInterface) != 0) ? (" extends ") : (" implements "));
      source.append(validator.useSignature(interfaceSigIterator.next()));
      while (interfaceSigIterator.hasNext()) {
        source.append(", ").append(validator.useSignature(interfaceSigIterator.next()));
      }
    }
    source.append('{');
    createTypeContent(source, lineDelimiter, context, validator);
    source.append(lineDelimiter);
    source.append('}');
  }

  /**
   * @param sourceBuilder
   * @param icu
   * @param lineDelimiter
   * @param validator
   * @
   */
  protected void createTypeContent(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    // fields
    List<IFieldSourceBuilder> fieldSourceBuilders = getFields();
    if (!fieldSourceBuilders.isEmpty()) {
      source.append(lineDelimiter);
      for (IFieldSourceBuilder builder : fieldSourceBuilders) {
        if (builder != null) {
          source.append(lineDelimiter);
          builder.createSource(source, lineDelimiter, context, validator);
        }
      }
    }
    // methods
    List<IMethodSourceBuilder> methodSourceBuilders = getMethods();
    if (!methodSourceBuilders.isEmpty()) {
      source.append(lineDelimiter);
      for (IMethodSourceBuilder op : methodSourceBuilders) {
        if (op != null) {
          source.append(lineDelimiter);
          op.createSource(source, lineDelimiter, context, validator);
        }
      }
    }
    // inner types
    List<ITypeSourceBuilder> innerTypes = getTypes();
    if (!innerTypes.isEmpty()) {
      source.append(lineDelimiter);
      for (ITypeSourceBuilder op : innerTypes) {
        if (op != null) {
          source.append(lineDelimiter);
          op.createSource(source, lineDelimiter, context, validator);
        }
      }
    }
  }

  @Override
  public void addTypeParameter(ITypeParameterSourceBuilder typeParameter) {
    m_typeParameters.add(typeParameter);
  }

  @Override
  public boolean removeTypeParameter(String elementName) {
    for (Iterator<ITypeParameterSourceBuilder> it = m_typeParameters.iterator(); it.hasNext();) {
      if (elementName.equals(it.next().getElementName())) {
        it.remove();
        return true;
      }
    }
    return false;
  }

  @Override
  public List<ITypeParameterSourceBuilder> getTypeParameters() {
    return Collections.unmodifiableList(m_typeParameters);
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
  public void addField(IFieldSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("Source builder can not be null.");
    }
    if (!m_sortedFields.isEmpty()) {
      throw new IllegalStateException("This builder has already sorted field builder. A mix between sorted and unsorted field builders is not supported.");
    }
    m_fields.add(builder);
  }

  @Override
  public void addSortedField(CompositeObject sortKey, IFieldSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("Source builder can not be null.");
    }
    if (!m_fields.isEmpty()) {
      throw new IllegalStateException("This builder has already unsorted field builder. A mix between sorted and unsorted field builders is not supported.");
    }
    m_sortedFields.put(sortKey, builder);
  }

  @Override
  public boolean removeField(String elementName) {
    for (Iterator<IFieldSourceBuilder> it = m_fields.iterator(); it.hasNext();) {
      if (elementName.equals(it.next().getElementName())) {
        it.remove();
        return true;
      }
    }
    for (Iterator<IFieldSourceBuilder> it = m_sortedFields.values().iterator(); it.hasNext();) {
      if (elementName.equals(it.next().getElementName())) {
        it.remove();
        return true;
      }
    }
    return false;
  }

  @Override
  public List<IFieldSourceBuilder> getFields() {
    List<IFieldSourceBuilder> ops = new ArrayList<>(m_fields.size() + m_sortedFields.size());
    ops.addAll(m_fields);
    ops.addAll(m_sortedFields.values());
    return ops;
  }

  @Override
  public void addMethod(IMethodSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("Source builder can not be null.");
    }
    if (!m_sortedMethods.isEmpty()) {
      throw new IllegalStateException("This source builder has already sorted method builders. A mix between sorted and unsorted method builders is not supported.");
    }
    m_methods.add(builder);
  }

  @Override
  public void addSortedMethod(CompositeObject sortKey, IMethodSourceBuilder builder) {
    if (builder == null) {
      throw new IllegalArgumentException("Source builder can not be null.");
    }
    if (!m_methods.isEmpty()) {
      throw new IllegalStateException("This source builder has already unsorted method builders. A mix between sorted and unsorted method builders is not supported.");
    }
    m_sortedMethods.put(sortKey, builder);
  }

  @Override
  public boolean removeMethod(String elementName) {
    for (Iterator<IMethodSourceBuilder> it = m_methods.iterator(); it.hasNext();) {
      if (elementName.equals(it.next().getElementName())) {
        it.remove();
        return true;
      }
    }
    for (Iterator<IMethodSourceBuilder> it = m_sortedMethods.values().iterator(); it.hasNext();) {
      if (elementName.equals(it.next().getElementName())) {
        it.remove();
        return true;
      }
    }
    return false;
  }

  @Override
  public List<IMethodSourceBuilder> getMethods() {
    List<IMethodSourceBuilder> builders = new ArrayList<>(m_methods.size() + m_sortedMethods.size());
    builders.addAll(m_methods);
    builders.addAll(m_sortedMethods.values());
    return builders;
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
  public List<ITypeSourceBuilder> getTypes() {
    List<ITypeSourceBuilder> typeBuilders = new ArrayList<>(m_types.size() + m_sortedTypes.size());
    typeBuilders.addAll(m_types);
    typeBuilders.addAll(m_sortedTypes.values());
    return typeBuilders;
  }

  @Override
  public ISourceBuilder getDeclaringElement() {
    return m_declaringElement;
  }

  @Override
  public void setDeclaringElement(ISourceBuilder declaringElement) {
    m_declaringElement = declaringElement;
  }

  @Override
  public String getFullyQualifiedName() {
    ISourceBuilder parent = getDeclaringElement();
    if (parent instanceof ITypeSourceBuilder) {
      return ((ITypeSourceBuilder) parent).getFullyQualifiedName() + "$" + getElementName();
    }
    if (parent instanceof ICompilationUnitSourceBuilder) {
      return ((ICompilationUnitSourceBuilder) parent).getPackageName() + "." + getElementName();
    }
    return null;
  }

}
