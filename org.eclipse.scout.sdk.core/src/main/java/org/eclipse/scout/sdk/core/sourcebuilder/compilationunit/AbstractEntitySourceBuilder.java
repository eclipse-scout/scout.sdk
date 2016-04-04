/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.sourcebuilder.compilationunit;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;

/**
 * <h3>{@link AbstractEntitySourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public abstract class AbstractEntitySourceBuilder extends CompilationUnitSourceBuilder {

  private final IJavaEnvironment m_javaEnvironment;
  private String m_entityName;

  /**
   * @param entityName
   *          The name of compilation unit and the nested primary class without any java suffixes like '.java'. Must not
   *          be <code>null</code>.
   * @param packageName
   *          The target package name of the compilation unit.
   * @param env
   *          The {@link IJavaEnvironment} of the target module. Must not be <code>null</code>.
   */
  protected AbstractEntitySourceBuilder(String entityName, String packageName, IJavaEnvironment env) {
    super(Validate.notNull(entityName) + SuffixConstants.SUFFIX_STRING_java, packageName);
    m_entityName = entityName;
    m_javaEnvironment = Validate.notNull(env);
  }

  protected AbstractEntitySourceBuilder(ICompilationUnit icu) {
    super(icu);
    m_javaEnvironment = icu.javaEnvironment();
    IType mainType = icu.mainType();
    if (mainType != null) {
      m_entityName = mainType.elementName();
    }
  }

  public abstract void setup();

  public IJavaEnvironment getJavaEnvironment() {
    return m_javaEnvironment;
  }

  public String getEntityName() {
    return m_entityName;
  }

  /**
   * @param newName
   *          The new entity name without any compilation unit suffixes like '.java'.
   */
  public void setEntityName(String newName) {
    m_entityName = newName;
    setElementName(newName + SuffixConstants.SUFFIX_STRING_java);
  }
}
