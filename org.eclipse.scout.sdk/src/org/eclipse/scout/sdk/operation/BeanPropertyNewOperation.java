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
package org.eclipse.scout.sdk.operation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.jdt.field.FieldNewOperation;
import org.eclipse.scout.sdk.operation.jdt.method.MethodNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.util.type.MethodParameter;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>BeanPropertyNewOperation</h3> ...
 */
public class BeanPropertyNewOperation implements IBeanPropertyNewOperation, IOperation {

  private final IType m_declaringType;
  // fields
  private String m_beanName;
  private int m_methodFlags;
  private IJavaElement m_siblingMethods;
  private IJavaElement m_siblingField;
  private String m_beanSignature;
  private boolean m_createGetterMethod;
  private boolean m_createSetterMethod;
  private boolean m_createFormDataAnnotation;
  private boolean m_useHungarianNotation;
  private boolean m_correctSpelling;

  public BeanPropertyNewOperation(IType declaringType) {
    m_declaringType = declaringType;
    m_createGetterMethod = true;
    m_createSetterMethod = true;
    m_createFormDataAnnotation = true;
    m_useHungarianNotation = true;
    m_correctSpelling = true;
  }

  /**
   * @param parentType
   * @param beanName
   * @param beanType
   *          used for return type of the getter
   * @param methodFlags
   *          a binary or combination of {@link Flags#AccAbstract}, {@link Flags#AccPrivate}, {@link Flags#AccProtected}
   *          , {@link Flags#AccDefault}, {@link Flags#AccPublic}, {@link Flags#AccFinal}, {@link Flags#AccStatic}
   */
  public BeanPropertyNewOperation(IType parentType, String beanName, String beanSignature, int methodFlags) {
    m_declaringType = parentType;
    m_beanSignature = beanSignature;
    m_beanName = Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
    m_methodFlags = methodFlags;
    m_createGetterMethod = true;
    m_createSetterMethod = true;
  }

  @Override
  public String getOperationName() {
    return Texts.get("Action_newTypeX", "Property");
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getBeanName())) {
      throw new IllegalArgumentException("bean name can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getBeanTypeSignature())) {
      throw new IllegalArgumentException("bean signature can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    workingCopyManager.register(m_declaringType.getCompilationUnit(), monitor);
//    ArrayList<String> imports = new ArrayList<String>();
//    String beanTypeString = ScoutSdkUtility.getSimpleTypeSignature(getBeanTypeSignature(), imports);
//    for (String imp : imports) {
//      m_declaringType.createImport(imp, monitor);
//    }

    // field
    String memberName;
    if (m_correctSpelling) {
      memberName = getBeanName(false); 
    }
    else {
      memberName = getBeanName();
    }

    if (memberName.startsWith("m_")) {
      memberName = memberName.substring(2);
    }
    if (isUseHungarianNotation()) {
      memberName = "m_" + memberName;
    }
    FieldNewOperation fieldOp = new FieldNewOperation(memberName, getDeclaringType(), false);
    fieldOp.setSibling(getSiblingField());
    fieldOp.setSignature(getBeanTypeSignature());
    fieldOp.setFlags(Flags.AccPrivate);
    fieldOp.validate();
    fieldOp.run(monitor, workingCopyManager);

    // getter
    if (isCreateGetterMethod()) {
      String prefix = "get";
      if (Signature.SIG_BOOLEAN.equals(getBeanTypeSignature())) {
        //    if (beanTypeString.equals("boolean")) {
        prefix = "is";
      }
      String getterName = prefix + getBeanName(true);

      MethodNewOperation getterOp = new MethodNewOperation(getterName, getDeclaringType(), false);
      getterOp.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return " + memberName + ";"));
      getterOp.setReturnTypeSignature(getBeanTypeSignature());
      getterOp.setFlags(Flags.AccPublic);
      if (m_createFormDataAnnotation) {
        getterOp.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createFormDataAnnotation());
      }
      getterOp.setSibling(getSiblingMethods());
      getterOp.setFormatSource(true);
      getterOp.validate();
      getterOp.run(monitor, workingCopyManager);
    }

    if (isCreateSetterMethod()) {
      // setter
      String setterName = "set" + getBeanName(true);

      String parameterName = null;
      if (m_correctSpelling) {
        parameterName = getBeanName(false);
      }
      else {
        parameterName = getBeanName();
      }

      String content;
      if (m_useHungarianNotation) {
        content = memberName + " = " + parameterName + ";";
      }
      else {
        content = "this." + memberName + " = " + parameterName + ";";
      }
      MethodNewOperation setterOp = new MethodNewOperation(setterName, getDeclaringType(), false);
      setterOp.setReturnTypeSignature(Signature.SIG_VOID);
      setterOp.setFlags(Flags.AccPublic);
      setterOp.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody(content));
      setterOp.addParameter(new MethodParameter(parameterName, getBeanTypeSignature()));
      if (m_createFormDataAnnotation) {
        setterOp.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createFormDataAnnotation());
      }
      setterOp.setSibling(getSiblingMethods());
      setterOp.setFormatSource(true);
      setterOp.validate();
      setterOp.run(monitor, workingCopyManager);
    }

  }

  @Override
  public String getBeanName(boolean startWithUpperCase) {
    if (StringUtility.isNullOrEmpty(getBeanName())) {
      return null;
    }
    if (startWithUpperCase) {
      return Character.toUpperCase(getBeanName().charAt(0)) + getBeanName().substring(1);
    }
    else {
      return Character.toLowerCase(getBeanName().charAt(0)) + getBeanName().substring(1);
    }
  }

  @Override
  public String getBeanName() {
    return m_beanName;
  }

  @Override
  public void setBeanName(String beanName) {
    m_beanName = beanName;
  }

  /**
   * @return a binary or combination of {@link Flags#AccAbstract}, {@link Flags#AccPrivate}, {@link Flags#AccProtected},
   *         {@link Flags#AccDefault}, {@link Flags#AccPublic}, {@link Flags#AccFinal}, {@link Flags#AccStatic}
   */
  @Override
  public int getMethodFlags() {
    return m_methodFlags;
  }

  /**
   * @param methodFlags
   *          a binary or combination of {@link Flags#AccAbstract}, {@link Flags#AccPrivate}, {@link Flags#AccProtected}
   *          , {@link Flags#AccDefault}, {@link Flags#AccPublic}, {@link Flags#AccFinal}, {@link Flags#AccStatic}
   */
  @Override
  public void setMethodFlags(int methodFlags) {
    m_methodFlags = methodFlags;
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public void setBeanTypeSignature(String beanTypeSignature) {
    m_beanSignature = beanTypeSignature;
  }

  @Override
  public String getBeanTypeSignature() {
    return m_beanSignature;
  }

  public void setSiblingMethods(IJavaElement siblingMethods) {
    m_siblingMethods = siblingMethods;
  }

  public IJavaElement getSiblingMethods() {
    return m_siblingMethods;
  }

  public void setSiblingField(IJavaElement siblingField) {
    m_siblingField = siblingField;
  }

  public IJavaElement getSiblingField() {
    return m_siblingField;
  }

  public boolean isCreateGetterMethod() {
    return m_createGetterMethod;
  }

  public void setCreateGetterMethod(boolean createGetterMethod) {
    m_createGetterMethod = createGetterMethod;
  }

  public boolean isCreateSetterMethod() {
    return m_createSetterMethod;
  }

  public void setCreateSetterMethod(boolean createSetterMethod) {
    m_createSetterMethod = createSetterMethod;
  }

  public boolean isCreateFormDataAnnotation() {
    return m_createFormDataAnnotation;
  }

  public void setCreateFormDataAnnotation(boolean createFormDataAnnotation) {
    m_createFormDataAnnotation = createFormDataAnnotation;
  }

  public boolean isUseHungarianNotation() {
    return m_useHungarianNotation;
  }

  public void setUseHungarianNotation(boolean useHungarianNotation) {
    m_useHungarianNotation = useHungarianNotation;
  }

  public boolean isCorrectSpelling() {
    return m_correctSpelling;
  }

  public void setCorrectSpelling(boolean correctSpelling) {
    m_correctSpelling = correctSpelling;
  }
}
