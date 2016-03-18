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
package org.eclipse.scout.sdk.core.s.sourcebuilder.service;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.AbstractEntitySourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;

/**
 * <h3>{@link ServiceImplSourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ServiceImplSourceBuilder extends AbstractEntitySourceBuilder {

  private final ITypeSourceBuilder m_interfaceBuilder;
  private final IType m_existingServiceType;

  public ServiceImplSourceBuilder(String elementName, String packageName, IJavaEnvironment env, ITypeSourceBuilder ifcBuilder) {
    super(elementName, packageName, env);
    m_interfaceBuilder = ifcBuilder;
    m_existingServiceType = null;
  }

  public ServiceImplSourceBuilder(ICompilationUnit existingIcu, ITypeSourceBuilder ifcBuilder) {
    super(existingIcu);
    m_existingServiceType = existingIcu.mainType();
    m_interfaceBuilder = ifcBuilder;
  }

  @Override
  public void setup() {
    if (m_existingServiceType == null) {
      // new service type
      setComment(CommentSourceBuilderFactory.createDefaultCompilationUnitComment(this));

      ITypeSourceBuilder implBuilder = new TypeSourceBuilder(getEntityName());
      implBuilder.setFlags(Flags.AccPublic);
      implBuilder.addInterfaceSignature(Signature.createTypeSignature(m_interfaceBuilder.getFullyQualifiedName()));
      addType(implBuilder);
    }
  }
}
