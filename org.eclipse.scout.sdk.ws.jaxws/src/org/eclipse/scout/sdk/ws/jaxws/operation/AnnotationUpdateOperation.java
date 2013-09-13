/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
/**
 *
 */
package org.eclipse.scout.sdk.ws.jaxws.operation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementFormatOperation;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class AnnotationUpdateOperation implements IOperation {

  private IType m_declaringType;
  private IType m_annotationType;

  private Map<String, IType> m_typePropertyMap;
  private Map<String, String> m_stringPropertyMap;
  private Set<String> m_propertyRemoveMap;

  public AnnotationUpdateOperation() {
    m_typePropertyMap = new HashMap<String, IType>();
    m_stringPropertyMap = new HashMap<String, String>();
    m_propertyRemoveMap = new HashSet<String>();
  }

  public void addTypeProperty(String propertyName, IType type) {
    m_typePropertyMap.put(propertyName, type);
  }

  public void addStringProperty(String propertyName, String value) {
    m_stringPropertyMap.put(propertyName, value);
  }

  public void removeProperty(String propertyName) {
    m_propertyRemoveMap.add(propertyName);
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (m_declaringType == null) {
      throw new IllegalArgumentException("No declaring type set.");
    }
    if (m_annotationType == null) {
      throw new IllegalArgumentException("no Annotation Type set.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    AnnotationCreateOperation op = new AnnotationCreateOperation(m_declaringType, SignatureCache.createTypeSignature(m_annotationType.getFullyQualifiedName()));
    IAnnotation annotation = JaxWsSdkUtility.getAnnotation(m_declaringType, m_annotationType.getFullyQualifiedName(), false);

    Map<String, String> resolvedTypePropertyMap = new HashMap<String, String>();

    // if annotation is already installed, get properties already set
    if (TypeUtility.exists(annotation)) {
      for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
        String propertyName = pair.getMemberName();
        Object propertyValue = pair.getValue();

        if (pair.getValueKind() == IMemberValuePair.K_CLASS) {
          resolvedTypePropertyMap.put(propertyName, (String) propertyValue);
        }
        else if (pair.getValueKind() == IMemberValuePair.K_STRING) {
          if (!m_stringPropertyMap.containsKey(propertyName)) {
            m_stringPropertyMap.put(propertyName, (String) propertyValue);
          }
        }
      }
    }

    // resolve qualified name of 'type properties' regarding to the imports directives installed
    for (String propertyName : m_typePropertyMap.keySet()) {
      IType type = m_typePropertyMap.get(propertyName);
      String resolvedTypeName = JaxWsSdkUtility.resolveTypeName(m_declaringType, type);
      resolvedTypePropertyMap.put(propertyName, resolvedTypeName);
    }

    // prepare creation of properties
    // type properties
    for (Entry<String, String> property : resolvedTypePropertyMap.entrySet()) {
      if (!m_propertyRemoveMap.contains(property.getKey())) {
        op.addParameter(property.getKey() + " = " + property.getValue() + ".class");
      }
    }
    // string properties
    for (String propertyName : m_stringPropertyMap.keySet()) {
      if (!m_propertyRemoveMap.contains(propertyName)) {
        op.addParameter(propertyName + " = \"" + m_stringPropertyMap.get(propertyName) + "\"");
      }
    }

    op.validate();
    op.run(monitor, workingCopyManager);

    // create import directives
    JaxWsSdkUtility.createImportDirective(m_declaringType, m_annotationType);
    for (IType type : m_typePropertyMap.values()) {
      JaxWsSdkUtility.createImportDirective(m_declaringType, type);
    }
    JaxWsSdkUtility.organizeImports(m_declaringType);

    JavaElementFormatOperation wellFormOp = new JavaElementFormatOperation(m_declaringType, true);
    wellFormOp.validate();
    try {
      wellFormOp.run(monitor, workingCopyManager);
    }
    catch (Exception e) {
      //nop
    }
  }

  @Override
  public String getOperationName() {
    return AnnotationUpdateOperation.class.getName();
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public void setDeclaringType(IType declaringType) {
    m_declaringType = declaringType;
  }

  public IType getAnnotationType() {
    return m_annotationType;
  }

  public void setAnnotationType(IType annotationType) {
    m_annotationType = annotationType;
  }
}
