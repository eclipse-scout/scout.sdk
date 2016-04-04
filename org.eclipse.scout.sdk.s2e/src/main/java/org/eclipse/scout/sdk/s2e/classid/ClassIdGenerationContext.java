/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.classid;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;

/**
 * <h3>{@link ClassIdGenerationContext}</h3> Context for a ClassId generation. Holds information about the type that
 * will hold the annotation after it has been created.
 *
 * @author Matthias Villiger
 * @since 3.10.0 2014-01-05
 */
public class ClassIdGenerationContext {

  private final String m_declaringTypeFqn;

  private final ITypeSourceBuilder m_parentTypeSourceBuilder;
  private final IType m_declaringType;

  public ClassIdGenerationContext(ITypeSourceBuilder parentSourceBuilder) {
    m_declaringTypeFqn = parentSourceBuilder.getFullyQualifiedName();
    m_parentTypeSourceBuilder = parentSourceBuilder;
    m_declaringType = null;
  }

  public ClassIdGenerationContext(IType declaringType) {
    m_declaringTypeFqn = declaringType.getFullyQualifiedName();
    m_declaringType = declaringType;
    m_parentTypeSourceBuilder = null;
  }

  public ClassIdGenerationContext(String fqn) {
    m_declaringTypeFqn = Validate.notNull(fqn);
    m_declaringType = null;
    m_parentTypeSourceBuilder = null;
  }

  /**
   * @return the fully qualified name of the type that will hold the annotation after it has been created. Is never
   *         <code>null</code>.
   */
  public String getDeclaringTypeFqn() {
    return m_declaringTypeFqn;
  }

  /**
   * Gets the source builder to which the annotation will be added.
   *
   * @return The parent source builder that will hold the annotation or <code>null</code>.
   */
  public ITypeSourceBuilder getParentTypeSourceBuilder() {
    return m_parentTypeSourceBuilder;
  }

  /**
   * Gets the declaring type for which the ClassId should be generated.
   *
   * @return The declaring type or <code>null</code> if the type does not exist yet.
   */
  public IType getDeclaringType() {
    return m_declaringType;
  }
}
