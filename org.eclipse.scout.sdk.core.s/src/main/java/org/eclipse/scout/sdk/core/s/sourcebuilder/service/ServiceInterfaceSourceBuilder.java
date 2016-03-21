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
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.model.ScoutAnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;

/**
 * <h3>{@link ServiceInterfaceSourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ServiceInterfaceSourceBuilder extends CompilationUnitSourceBuilder {

  private final String m_elementName;

  public ServiceInterfaceSourceBuilder(String elementName, String packageName) {
    super(elementName + SuffixConstants.SUFFIX_STRING_java, packageName);
    m_elementName = elementName;
  }

  public ServiceInterfaceSourceBuilder(ICompilationUnit existingInterface) {
    super(existingInterface);
    m_elementName = existingInterface.mainType().elementName();
  }

  public void setup() {
    if (getMainType() == null) {
      // CU comment
      setComment(CommentSourceBuilderFactory.createDefaultCompilationUnitComment(this));

      // new interface type
      ITypeSourceBuilder interfaceBuilder = new TypeSourceBuilder(m_elementName);

      interfaceBuilder.setFlags(Flags.AccPublic | Flags.AccInterface);
      interfaceBuilder.setComment(CommentSourceBuilderFactory.createDefaultTypeComment(interfaceBuilder));
      interfaceBuilder.addInterfaceSignature(Signature.createTypeSignature(IScoutRuntimeTypes.IService));

      // @TunnelToServer
      interfaceBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createTunnelToServer());

      addType(interfaceBuilder);
    }
  }
}
