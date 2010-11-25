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
package org.eclipse.scout.sdk.operation.form.formdata;

import java.util.Collection;
import java.util.TreeMap;

import org.eclipse.jdt.core.Flags;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;

/**
 *
 */
public class TypeSourceBuilder implements ITypeSourceBuilder {
  public static final int CATEGORY_METHOD_PROPERTY = 0;
  public static final int CATEGORY_METHOD_FIELD_GETTER = 2;
  public static final int CATEGORY_MEHTOD = 3;
  public static final int CATEGORY_TYPE_FIELD = 4;
  public static final int CATEGORY_TYPE_TABLE_COLUMN = 5;
  public static final int CATEGORY_TYPE_COMPOSER_ATTRIBUTE = 6;
  public static final int CATEGORY_TYPE_COMPOSER_ENTITY = 7;

  private TreeMap<CompositeObject, ISourceBuilder> m_children;
  private TreeMap<String, ITypeSourceBuilder> m_externalBuilder;
  private String m_superTypeSignature;
  private String m_elementName;
  private int m_flags;
  private boolean m_createDefaultConstructor;
  private boolean m_createDefaultSerialVersionUid;

  public TypeSourceBuilder() {
    m_children = new TreeMap<CompositeObject, ISourceBuilder>();
    // default
    m_flags = Flags.AccPublic;
    m_createDefaultConstructor = true;
    m_createDefaultSerialVersionUid = true;
  }

  public String createSource(IImportValidator validator) {
    StringBuilder builder = new StringBuilder();
    if (Flags.isPublic(getFlags())) {
      builder.append("public ");
    }
    if (Flags.isPrivate(getFlags())) {
      builder.append("private ");
    }
    if (Flags.isProtected(getFlags())) {
      builder.append("protected ");
    }
    if (Flags.isStatic(getFlags())) {
      builder.append("static ");
    }
    if (Flags.isFinal(getFlags())) {
      builder.append("final ");
    }
    if (Flags.isAbstract(getFlags())) {
      builder.append("abstract ");
    }
    builder.append("class ");
    builder.append(getElementName() + " ");
    if (!StringUtility.isNullOrEmpty(getSuperTypeSignature())) {
      builder.append("extends " + ScoutSdkUtility.getSimpleTypeRefName(getSuperTypeSignature(), validator) + " ");
    }
    builder.append("{\n");
    if (isCreateDefaultSerialVersionUid()) {
      builder.append("private static final long serialVersionUID=1L;\n\n");
    }
    if (isCreateDefaultConstructor()) {
      builder.append("public " + getElementName() + "() {\n}\n\n");
    }
    for (ISourceBuilder childBuilder : m_children.values()) {
      builder.append(childBuilder.createSource(validator));
    }
    builder.append("}");
    return builder.toString();
  }

  public void addBuilder(ISourceBuilder builder, int category) {
    addBuilder(builder, new CompositeObject(category, builder.getElementName(), builder));
  }

  public void addBuilder(ISourceBuilder builder, CompositeObject key) {
    m_children.put(key, builder);
  }

  public void addExternalBuilder(ITypeSourceBuilder builder) {
    m_externalBuilder.put(builder.getElementName(), builder);
  }

  public ITypeSourceBuilder[] getExternalBuilders() {
    Collection<ITypeSourceBuilder> values = m_externalBuilder.values();
    return values.toArray(new ITypeSourceBuilder[values.size()]);
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public void setElementName(String elementName) {
    m_elementName = elementName;
  }

  public String getElementName() {
    return m_elementName;
  }

  public void setFlags(int flags) {
    m_flags = flags;
  }

  public int getFlags() {
    return m_flags;
  }

  public void setCreateDefaultConstructor(boolean createDefaultConstructor) {
    m_createDefaultConstructor = createDefaultConstructor;
  }

  public boolean isCreateDefaultConstructor() {
    return m_createDefaultConstructor;
  }

  public void setCreateDefaultSerialVersionUid(boolean createDefaultSerialVersionUid) {
    m_createDefaultSerialVersionUid = createDefaultSerialVersionUid;
  }

  public boolean isCreateDefaultSerialVersionUid() {
    return m_createDefaultSerialVersionUid;
  }

}
