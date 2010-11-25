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
package org.eclipse.scout.sdk.operation.form.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.operation.form.util.FormDataUtility.FormDataAnnotationDesc;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;

public class ComposerFieldDataSourceBuilder extends AbstractSourceBuilder {
  private final IType iComposerAttribute = ScoutSdk.getType(RuntimeClasses.IComposerAttribute);
  private final IType iComposerEntity = ScoutSdk.getType(RuntimeClasses.IComposerEntity);

  private final IType m_uiFieldType;
  private final boolean m_isImplementation;

  public ComposerFieldDataSourceBuilder(String typeSimpleName, IType uiFieldType, ITypeHierarchy formFieldHierarchy, ISourceBuilder parentBuilder, boolean isImplementation) {
    this(typeSimpleName, uiFieldType, formFieldHierarchy, parentBuilder.getImportValidator(), isImplementation);
  }

  public ComposerFieldDataSourceBuilder(String typeSimpleName, IType uiFieldType, ITypeHierarchy formFieldHierarchy, IImportValidator importValidator, boolean isImplementation) {
    super(typeSimpleName, Signature.createTypeSignature(RuntimeClasses.AbstractComposerData, true), Flags.AccPublic, importValidator);
    m_uiFieldType = uiFieldType;
    m_isImplementation = isImplementation;
  }

  @Override
  public void build() throws CoreException {
    addBeanPropertiesFrom(m_uiFieldType, m_isImplementation);
    if (m_isImplementation) {
      StringBuilder sb = new StringBuilder();
      ITypeHierarchy attributeHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iComposerAttribute).combinedTypeHierarchy(m_uiFieldType);
      ITypeHierarchy entityHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iComposerEntity).combinedTypeHierarchy(m_uiFieldType);
      appendType(m_uiFieldType, sb, entityHierarchy, attributeHierarchy);
      addInnerClass(sb.toString());

    }
  }

  private void appendType(IType t, StringBuilder builder, ITypeHierarchy entityHierarchy, ITypeHierarchy attributeHierarchy) throws JavaModelException {
    // attributes
    for (IType attribute : TypeUtility.getInnerTypes(t, TypeFilters.getSubtypeFilter(iComposerAttribute, attributeHierarchy), TypeComparators.getTypeNameComparator())) {
      boolean implementation = true;
      FormDataAnnotationDesc annotation = FormDataUtility.parseFormDataAnnotation(t, entityHierarchy);
      if (annotation != null) {
        String superTypeSig = null;
        if (annotation.externalFlag) {
          // create sub field data
          // XXX TODO
        }
        if (annotation.usingFlag && annotation.fullyQualifiedName != null) {
          superTypeSig = Signature.createTypeSignature(annotation.fullyQualifiedName, true);
          implementation = false;
        }
        if (StringUtility.isNullOrEmpty(superTypeSig)) {
          superTypeSig = Signature.createTypeSignature(RuntimeClasses.AbstractComposerAttributeData, true);
        }
        builder.append("public class " + attribute.getElementName() + " ");
        builder.append("extends ");
        builder.append(getImportValidator().getSimpleTypeRef(superTypeSig));
        builder.append("{\n");
        builder.append("private static final long serialVersionUID = 1L;\n");
        if (implementation) {
          appendType(attribute, builder, entityHierarchy, attributeHierarchy);
        }
        builder.append("}\n");
      }
    }
    // entities
    for (IType entity : TypeUtility.getInnerTypes(t, TypeFilters.getSubtypeFilter(iComposerEntity, entityHierarchy), TypeComparators.getTypeNameComparator())) {
      boolean implementation = true;
      FormDataAnnotationDesc annotation = FormDataUtility.parseFormDataAnnotation(t, entityHierarchy);
      if (annotation != null) {
        String superTypeSig = null;
        if (annotation.externalFlag) {
          // create sub field data
          // XXX TODO
        }
        if (annotation.usingFlag && annotation.fullyQualifiedName != null) {
          superTypeSig = Signature.createTypeSignature(annotation.fullyQualifiedName, true);
          implementation = false;
        }
        if (StringUtility.isNullOrEmpty(superTypeSig)) {
          superTypeSig = Signature.createTypeSignature(RuntimeClasses.AbstractComposerEntityData, true);
        }
        builder.append("public class " + entity.getElementName() + " ");
        builder.append("extends ");
        builder.append(getImportValidator().getSimpleTypeRef(superTypeSig));
        builder.append("{\n");
        builder.append("private static final long serialVersionUID = 1L;\n");
        if (implementation) {
          appendType(entity, builder, entityHierarchy, attributeHierarchy);
        }
        builder.append("}\n");
      }
    }
  }

  private void buildEntity(IType entity, StringBuilder builder, ITypeHierarchy typeHierarchy) throws JavaModelException {
    boolean implementation = true;
    FormDataAnnotationDesc annotation = FormDataUtility.parseFormDataAnnotation(entity, typeHierarchy);
    if (annotation != null) {
      String superTypeSig = null;
      if (annotation.externalFlag) {
        // create sub field data
        // XXX TODO
      }
      if (annotation.usingFlag && annotation.fullyQualifiedName != null) {
        superTypeSig = Signature.createTypeSignature(annotation.fullyQualifiedName, true);
        implementation = false;
      }
      if (StringUtility.isNullOrEmpty(superTypeSig)) {
        superTypeSig = Signature.createTypeSignature(RuntimeClasses.AbstractComposerEntityData, true);
      }
      builder.append("public class " + entity.getElementName() + " ");
      builder.append("extends ");
      builder.append(getImportValidator().getSimpleTypeRef(superTypeSig));
      builder.append("{\n");
      // TODO
      builder.append("}\n");
    }

  }

  public void appendToParent(ISourceBuilder parentBuilder) throws CoreException {
    //add type
    parentBuilder.addInnerClass(createDocumentText());
    //add type getter
    parentBuilder.addFieldGetterMethod("public " + getSimpleTypeName() + " get" + getSimpleTypeName() + "(){\n" + ScoutIdeProperties.TAB + "return getFieldByClass(" + getSimpleTypeName() + ".class);\n}");
  }

  private String createFormDataSignatureFor(IType type, ITypeHierarchy columnHierarchy) throws JavaModelException {
    if (type == null || type.getFullyQualifiedName().equals(Object.class.getName())) {
      return null;
    }
    IType superType = columnHierarchy.getSuperclass(type);
    if (TypeUtility.exists(superType)) {
      if (TypeUtility.isGenericType(superType)) {
        String superTypeSig = type.getSuperclassTypeSignature();
        return ScoutSdkUtility.getResolvedSignature(Signature.getTypeArguments(superTypeSig)[0], type);
      }
      else {
        return createFormDataSignatureFor(superType, columnHierarchy);
      }
    }
    else {
      return null;
    }
  }

  private class Entity {
    private FormDataAnnotationDesc m_annotation;
    private IType m_type;

    public Entity(IType type, FormDataAnnotationDesc annotation) {
      m_type = type;
      m_annotation = annotation;
    }

    public FormDataAnnotationDesc getAnnotation() {
      return m_annotation;
    }

    public void setAnnotation(FormDataAnnotationDesc annotation) {
      m_annotation = annotation;
    }

    public IType getType() {
      return m_type;
    }

    public void setType(IType type) {
      m_type = type;
    }

  }
}
