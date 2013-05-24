/*******************************************************************************
 * Copyright (c) 2011,2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.workspace.type.validationrule;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ITypeHierarchy;

public class ValidationRuleMethod {
  private final IAnnotation m_annotation;
  private final IField m_ruleField;
  private final String m_ruleName;
  private final String m_ruleGeneratedSourceCode;
  private final IMethod m_annotatedMethod;
  private final IMethod m_implementedMethod;
  private final ITypeHierarchy m_superTypeHierarchy;
  private boolean m_skipRule;

  public ValidationRuleMethod(IAnnotation annotation, IField ruleField, String ruleName, String ruleGeneratedSourceCode, IMethod annotatedMethod, IMethod implementedMethod, ITypeHierarchy superTypeHierarchy, boolean skipRule) {
    m_annotation = annotation;
    m_ruleField = ruleField;
    m_ruleName = ruleName;
    m_ruleGeneratedSourceCode = ruleGeneratedSourceCode;
    m_annotatedMethod = annotatedMethod;
    m_implementedMethod = implementedMethod;
    m_superTypeHierarchy = superTypeHierarchy;
    m_skipRule = skipRule;
  }

  /**
   * @return the annotation
   */
  public IAnnotation getAnnotation() {
    return m_annotation;
  }

  /**
   * @return the ruleField
   */
  public IField getRuleField() {
    return m_ruleField;
  }

  /**
   * @return the ruleName
   */
  public String getRuleName() {
    return m_ruleName;
  }

  /**
   * @return the ruleGeneratedSourceCode
   */
  public String getRuleGeneratedSourceCode() {
    return m_ruleGeneratedSourceCode;
  }

  /**
   * @return the annotatedMethod
   */
  public IMethod getAnnotatedMethod() {
    return m_annotatedMethod;
  }

  /**
   * @return the implementedMethod
   */
  public IMethod getImplementedMethod() {
    return m_implementedMethod;
  }

  /**
   * @return the superTypeHierarchy
   */
  public ITypeHierarchy getSuperTypeHierarchy() {
    return m_superTypeHierarchy;
  }

  /**
   * @return Returns <code>true</code> if this rule should be skipped.
   */
  public boolean isSkipRule() {
    return m_skipRule;
  }

  public void setSkipRule(boolean skipRule) {
    m_skipRule = skipRule;
  }
}
