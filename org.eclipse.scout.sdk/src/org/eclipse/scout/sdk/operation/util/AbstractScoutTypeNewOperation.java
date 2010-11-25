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
package org.eclipse.scout.sdk.operation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.util.ScoutSignature;

/**
 * <h3>AbstractBcTypeNewOperation</h3> ...
 */
public abstract class AbstractScoutTypeNewOperation implements IOperation {

  private String m_typeName;
  private String m_javaDoc;
  private String m_superTypeSignature;
  private List<String> m_interfaceSignatures;
  private int m_typeModifiers;
  private List<AnnotationCreateOperation> m_annotations = new ArrayList<AnnotationCreateOperation>();

  public AbstractScoutTypeNewOperation(String name) {
    m_typeName = name;
    m_interfaceSignatures = new ArrayList<String>();
    m_typeModifiers = Flags.AccPublic;
  }

  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getTypeName())) {
      throw new IllegalArgumentException("type name can not be null or empty");
    }
  }

  public String createSource(IImportValidator validator) throws JavaModelException {
    StringBuilder buf = new StringBuilder();
    // javadoc
    if (!StringUtility.isNullOrEmpty(getJavaDoc())) {
      buf.append(getJavaDoc());
    }
    // annotations
    AnnotationCreateOperation[] annotationOps = getAnnotations();
    if (annotationOps != null && annotationOps.length > 0) {
      for (int i = 0; i < annotationOps.length; i++) {
        buf.append(annotationOps[i].createSource(validator) + "\n");
      }
    }

    buf.append(Flags.toString(getTypeModifiers()) + " ");
    buf.append(((getTypeModifiers() & Flags.AccInterface) != 0) ? ("interface ") : ("class "));
    buf.append(getTypeName());
    // super type (extends)
    if (!StringUtility.isNullOrEmpty(getSuperTypeSignature())) {
      String superTypeRefName = ScoutSignature.getTypeReference(getSuperTypeSignature(), validator);
      buf.append(" extends " + superTypeRefName);
    }
    // interfaces
    String[] interfaceSignatures = getInterfaceSignatures();
    if (interfaceSignatures.length > 0) {
      buf.append((hasModifier(Flags.AccInterface)) ? (" extends ") : (" implements "));
      for (int i = 0; i < interfaceSignatures.length; i++) {
        String interfaceTypeRefName = ScoutSdkUtility.getSimpleTypeRefName(interfaceSignatures[i], validator);
        buf.append(interfaceTypeRefName + ((interfaceSignatures.length > (i + 1)) ? (", ") : ("")));
      }
    }
    buf.append("{\n");
    StringBuilder contentBuilder = new StringBuilder();
    createContent(contentBuilder, validator);
    buf.append(contentBuilder.toString());
    buf.append("}");
    return buf.toString();
  }

  protected void createContent(@SuppressWarnings("unused") StringBuilder source, @SuppressWarnings("unused") IImportValidator validator) {

  }

  public abstract IType getCreatedType();

  public void setJavaDoc(String javaDoc) {
    m_javaDoc = javaDoc;
  }

  public String getJavaDoc() {
    return m_javaDoc;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    m_typeName = typeName;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public String[] getInterfaceSignatures() {
    ArrayList<String> sigs = new ArrayList<String>();
    for (String s : m_interfaceSignatures) {
      if (!StringUtility.isNullOrEmpty(s)) {
        sigs.add(s);
      }
    }
    return sigs.toArray(new String[sigs.size()]);
  }

  public void setInterfaceSignatures(String[] interfaceSignatures) {
    if (interfaceSignatures == null) {
      interfaceSignatures = new String[0];
    }
    m_interfaceSignatures = new ArrayList<String>(Arrays.asList(interfaceSignatures));
  }

  public void addInterfaceSignature(String interfaceSignature) {
    m_interfaceSignatures.add(interfaceSignature);
  }

  public AnnotationCreateOperation[] getAnnotations() {
    return m_annotations.toArray(new AnnotationCreateOperation[m_annotations.size()]);
  }

  public void addAnnotation(AnnotationCreateOperation annotation) {
    m_annotations.add(annotation);
  }

  /**
   * e.g. {@link Flags#AccAbstract} | {@link Flags#AccProtected}
   * 
   * @return {@link Flags} Acc constants.
   */
  public int getTypeModifiers() {
    return m_typeModifiers;
  }

  /**
   * e.g. {@link Flags#AccAbstract} | {@link Flags#AccProtected}
   */
  public void setTypeModifiers(int typeModifiers) {
    m_typeModifiers = typeModifiers;
  }

  /**
   * e.g. {@link Flags#AccAbstract} | {@link Flags#AccProtected}
   */
  public void addTypeModifier(int modifier) {
    m_typeModifiers |= modifier;
  }

  /**
   * @param modifier
   *          the flag to check {@link Flags}
   * @return
   */
  public boolean hasModifier(int modifier) {
    return (m_typeModifiers & modifier) != 0;
  }

}
