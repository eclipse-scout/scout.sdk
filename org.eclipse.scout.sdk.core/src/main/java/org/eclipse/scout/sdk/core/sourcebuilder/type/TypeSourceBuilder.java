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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.Flags;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.model.ITypeParameter;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.AbstractAnnotatableSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link TypeSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public class TypeSourceBuilder extends AbstractAnnotatableSourceBuilder implements ITypeSourceBuilder {

  private String m_superTypeSignature;
  private List<ITypeParameter> m_typeParameters;
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
    m_typeParameters = new ArrayList<>();
    m_interfaceSignatures = new ArrayList<>();
    m_fieldSourceBuilders = new ArrayList<>();
    m_sortedFieldSourceBuilders = new TreeMap<>();
    m_methodSourceBuilders = new ArrayList<>();
    m_sortedMethodSourceBuilders = new TreeMap<>();
    m_typeSourceBuilders = new ArrayList<>();
    m_sortedTypeSourceBuilders = new TreeMap<>();
  }

  @Override
  public void validate() {
    super.validate();
    if (Flags.isInterface(getFlags()) && getSuperTypeSignature() != null) {
      throw new IllegalArgumentException("An interface can not have a superclass.");
    }
  }

  protected String typeParamToSource(ITypeParameter param, IImportValidator validator) {
    List<IType> boundsSignatures = param.getBounds();
    boolean hasBounds = !boundsSignatures.isEmpty();

    StringBuilder fqnBuilder = new StringBuilder();
    if (StringUtils.isNotBlank(param.getName())) {
      fqnBuilder.append(param.getName());
    }
    else if (hasBounds) {
      fqnBuilder.append('?');
    }

    if (hasBounds) {
      fqnBuilder.append(" extends ");
      Iterator<IType> iterator = boundsSignatures.iterator();
      String sig = SignatureUtils.getResolvedSignature(iterator.next());
      fqnBuilder.append(SignatureUtils.getTypeReference(sig, validator));
      while (iterator.hasNext()) {
        sig = SignatureUtils.getResolvedSignature(iterator.next());
        fqnBuilder.append(" & ").append(SignatureUtils.getTypeReference(sig, validator));
      }
    }
    return fqnBuilder.toString();
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
    super.createSource(source, lineDelimiter, context, validator);
    // type definition
    source.append(Flags.toString(getFlags())).append(" ");
    source.append(((getFlags() & Flags.AccInterface) != 0) ? ("interface ") : ("class "));
    source.append(getElementName());
    // parameter types
    if (getTypeParameters().size() > 0) {
      source.append(ISignatureConstants.C_GENERIC_START);
      Iterator<ITypeParameter> it = getTypeParameters().iterator();
      // first
      ITypeParameter tp = it.next();
      source.append(typeParamToSource(tp, validator));
      // rest
      while (it.hasNext()) {
        source.append(", ");
        tp = it.next();
        source.append(typeParamToSource(tp, validator));
      }
      source.append(ISignatureConstants.C_GENERIC_END);
    }
    validator.getTypeName(Signature.createTypeSignature(getFullyQualifiedName()));

    // super type (extends)
    if (!StringUtils.isEmpty(getSuperTypeSignature())) {
      String superTypeRefName = SignatureUtils.getTypeReference(getSuperTypeSignature(), validator);
      source.append(" extends ").append(superTypeRefName);
    }

    // interfaces
    Iterator<String> interfaceSigIterator = getInterfaceSignatures().iterator();
    if (interfaceSigIterator.hasNext()) {
      source.append(((getFlags() & Flags.AccInterface) != 0) ? (" extends ") : (" implements "));
      source.append(SignatureUtils.getTypeReference(interfaceSigIterator.next(), validator));
      while (interfaceSigIterator.hasNext()) {
        source.append(", ").append(SignatureUtils.getTypeReference(interfaceSigIterator.next(), validator));
      }
    }
    source.append("{");
    createTypeContent(source, lineDelimiter, context, validator);
    source.append(lineDelimiter);
    source.append("}");
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
    List<IFieldSourceBuilder> fieldSourceBuilders = getFieldSourceBuilders();
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
    List<IMethodSourceBuilder> methodSourceBuilders = getMethodSourceBuilders();
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
    List<ITypeSourceBuilder> innerTypes = getTypeSourceBuilder();
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
  public void addTypeParameter(ITypeParameter typeParameter) {
    m_typeParameters.add(typeParameter);
  }

  @Override
  public void setTypeParameters(List<? extends ITypeParameter> typeParameters) {
    m_typeParameters = new ArrayList<>(typeParameters);
  }

  @Override
  public List<ITypeParameter> getTypeParameters() {
    return m_typeParameters;
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
    List<IFieldSourceBuilder> ops = new ArrayList<>(m_fieldSourceBuilders.size() + m_sortedFieldSourceBuilders.size());
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
    List<IMethodSourceBuilder> builders = new ArrayList<>(m_methodSourceBuilders.size() + m_sortedMethodSourceBuilders.size());
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
    List<ITypeSourceBuilder> typeBuilders = new ArrayList<>(m_typeSourceBuilders.size() + m_sortedTypeSourceBuilders.size());
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
      sb.append('$');
    }
    else {
      String pfqn = getParentFullyQualifiedName();
      if (pfqn != null) {
        sb = new StringBuilder(pfqn);
        sb.append('.');
      }
    }
    if (sb == null) {
      return null;
    }
    return sb.append(getElementName()).toString();
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
