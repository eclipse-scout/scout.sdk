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

import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;

/**
 * <h3>{@link ServiceImplSourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ServiceImplSourceBuilder extends CompilationUnitSourceBuilder {

  private final String m_elementName;
  private final ITypeSourceBuilder m_interfaceBuilder;
  private final IType m_existingServiceType;

  public ServiceImplSourceBuilder(String elementName, String packageName, ITypeSourceBuilder ifcBuilder) {
    super(elementName + SuffixConstants.SUFFIX_STRING_java, packageName);
    m_elementName = elementName;
    m_interfaceBuilder = ifcBuilder;
    m_existingServiceType = null;
  }

  public ServiceImplSourceBuilder(ICompilationUnit existingIcu, ITypeSourceBuilder ifcBuilder) {
    super(existingIcu);
    m_existingServiceType = existingIcu.mainType();
    m_elementName = m_existingServiceType.elementName();
    m_interfaceBuilder = ifcBuilder;
  }

  public void setup() {
    if (m_existingServiceType == null) {
      // new service type
      ITypeSourceBuilder implBuilder = new TypeSourceBuilder(m_elementName);
      implBuilder.setFlags(Flags.AccPublic);
      implBuilder.addInterfaceSignature(Signature.createTypeSignature(m_interfaceBuilder.getFullyQualifiedName()));
      addType(implBuilder);
    }
  }
}
