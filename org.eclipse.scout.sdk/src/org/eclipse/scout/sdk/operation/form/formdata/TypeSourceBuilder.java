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

import java.util.ArrayList;
import java.util.TreeMap;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;

/**
 *
 */
public class TypeSourceBuilder implements ITypeSourceBuilder {

  private TreeMap<CompositeObject, ISourceBuilder> m_children;
  private String m_superTypeSignature;
  private String m_elementName;
  private int m_flags;
  private boolean m_createDefaultConstructor;
  private boolean m_createDefaultSerialVersionUid;
  private ArrayList<AnnotationSourceBuilder> m_annotations;

  protected final String NL;

  public TypeSourceBuilder(String nl) {
    NL = nl;
    m_children = new TreeMap<CompositeObject, ISourceBuilder>();
    m_annotations = new ArrayList<AnnotationSourceBuilder>();
    // default
    m_flags = Flags.AccPublic;
    m_createDefaultConstructor = true;
    m_createDefaultSerialVersionUid = true;
  }

  @Override
  public int getType() {
    return TYPE_SOURCE_BUILDER;
  }

  @Override
  public String createSource(IImportValidator validator) throws JavaModelException {
    StringBuilder builder = new StringBuilder();
    for (AnnotationSourceBuilder as : getAnnotations()) {
      builder.append(as.createSource(validator) + NL);
    }
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
      builder.append("extends " + SignatureUtility.getTypeReference(getSuperTypeSignature(), validator) + " ");
    }
    builder.append("{" + NL);
    if (isCreateDefaultSerialVersionUid()) {
      builder.append("private static final long serialVersionUID=1L;" + NL + NL);
    }
    if (isCreateDefaultConstructor()) {
      builder.append("public " + getElementName() + "() {" + NL + "}" + NL);
    }
    ISourceBuilder[] childBuilders = m_children.values().toArray(new ISourceBuilder[m_children.size()]);
    if (childBuilders.length > 0) {
      builder.append(NL);
      for (int i = 0; i < childBuilders.length; i++) {
        builder.append(childBuilders[i].createSource(validator));
        if (i < childBuilders.length - 1) {
          builder.append(NL);
        }
      }
    }
    builder.append("}");
    return builder.toString();
  }

  @Override
  public AnnotationSourceBuilder[] getAnnotations() {
    return m_annotations.toArray(new AnnotationSourceBuilder[m_annotations.size()]);
  }

  @Override
  public void addAnnotation(AnnotationSourceBuilder annotation) {
    m_annotations.add(annotation);
  }

  @Override
  public void addBuilder(ISourceBuilder builder, int category) {
    addBuilder(builder, new CompositeObject(category, builder.getElementName(), builder));
  }

  @Override
  public void addBuilder(ISourceBuilder builder, CompositeObject key) {
    m_children.put(key, builder);
  }

  @Override
  public ISourceBuilder[] getSourceBuilders(int type) {
    ArrayList<ISourceBuilder> builders = new ArrayList<ISourceBuilder>();
    for (ISourceBuilder b : m_children.values()) {
      if (b.getType() == type) {
        builders.add(b);
      }
    }
    return builders.toArray(new ISourceBuilder[builders.size()]);
  }

  @Override
  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  @Override
  public void setElementName(String elementName) {
    m_elementName = elementName;
  }

  @Override
  public String getElementName() {
    return m_elementName;
  }

  @Override
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
