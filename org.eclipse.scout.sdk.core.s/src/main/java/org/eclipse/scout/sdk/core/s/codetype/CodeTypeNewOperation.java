/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.codetype;

import java.util.function.BiConsumer;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.classid.ClassIds;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.uniqueid.UniqueIds;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link CodeTypeNewOperation}</h3>
 *
 * @since 7.0.0
 */
public class CodeTypeNewOperation implements BiConsumer<IEnvironment, IProgress> {

  // in
  private String m_codeTypeName;
  private IClasspathEntry m_sharedSourceFolder;
  private String m_package;
  private String m_superType;
  private String m_codeTypeIdDataType;

  //out
  private IType m_createdCodeType;

  @Override
  public void accept(IEnvironment env, IProgress progress) {
    progress.init(toString(), 1);
    IType createCodeType = createCodeType(env, progress.newChild(1));
    setCreatedCodeType(createCodeType);
  }

  protected IType createCodeType(IEnvironment env, IProgress progress) {
    Ensure.notBlank(getCodeTypeName(), "No codetype name provided");
    Ensure.notNull(getSharedSourceFolder(), "No source folder provided");
    Ensure.notBlank(getPackage(), "No package name provided");
    Ensure.notBlank(getSuperType(), "No supertype provided");
    Ensure.notBlank(getCodeTypeIdDataType(), "No codetype id datatype provided");

    CodeTypeGenerator<?> codeTypeBuilder = new CodeTypeGenerator<>()
        .withElementName(getCodeTypeName())
        .withPackageName(getPackage())
        .withSuperClass(getSuperType())
        .withCodeTypeIdDataType(getCodeTypeIdDataType())
        .withClassIdValue(ClassIds.nextIfEnabled(getPackage() + JavaTypes.C_DOT + getCodeTypeName()));

    String idValue = UniqueIds.next(getCodeTypeIdDataType());
    if (Strings.isBlank(idValue)) {
      idValue = JavaTypes.defaultValueOf(getCodeTypeIdDataType());
    }
    if (Strings.hasText(idValue) && !"null".equals(idValue)) {
      codeTypeBuilder.withIdValueBuilder(ISourceGenerator.raw(idValue));
    }

    return env.writeCompilationUnit(codeTypeBuilder, getSharedSourceFolder(), progress);
  }

  public String getCodeTypeName() {
    return m_codeTypeName;
  }

  public void setCodeTypeName(String codeTypeName) {
    m_codeTypeName = codeTypeName;
  }

  public IClasspathEntry getSharedSourceFolder() {
    return m_sharedSourceFolder;
  }

  public void setSharedSourceFolder(IClasspathEntry sharedSourceFolder) {
    m_sharedSourceFolder = sharedSourceFolder;
  }

  protected void setCreatedCodeType(IType createdCodeType) {
    m_createdCodeType = createdCodeType;
  }

  public IType getCreatedCodeType() {
    return m_createdCodeType;
  }

  public String getSuperType() {
    return m_superType;
  }

  public void setSuperType(String superType) {
    m_superType = superType;
  }

  public String getPackage() {
    return m_package;
  }

  public void setPackage(String package1) {
    m_package = package1;
  }

  public String getCodeTypeIdDataType() {
    return m_codeTypeIdDataType;
  }

  public void setCodeTypeIdDataType(String codeTypeIdDataType) {
    m_codeTypeIdDataType = codeTypeIdDataType;
  }

  @Override
  public String toString() {
    return "Create new CodeType";
  }
}