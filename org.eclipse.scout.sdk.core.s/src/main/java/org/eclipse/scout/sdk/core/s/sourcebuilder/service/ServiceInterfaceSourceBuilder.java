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
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.model.ScoutAnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.methodparameter.MethodParameterSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;

/**
 * <h3>{@link ServiceInterfaceSourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ServiceInterfaceSourceBuilder extends CompilationUnitSourceBuilder {

  public static final String SERVICE_LOAD_METHOD_NAME = "load";
  public static final String SERVICE_STORE_METHOD_NAME = "store";

  private final String m_elementName;

  private String m_dtoSignature;
  private ITypeSourceBuilder m_interfaceBuilder;

  public ServiceInterfaceSourceBuilder(String elementName, String packageName) {
    super(elementName + SuffixConstants.SUFFIX_STRING_java, packageName);
    m_elementName = elementName;
  }

  public void setup() {
    // CU comment
    setComment(CommentSourceBuilderFactory.createDefaultCompilationUnitComment(this));

    // interface type
    m_interfaceBuilder = new TypeSourceBuilder(m_elementName);
    m_interfaceBuilder.setFlags(Flags.AccPublic | Flags.AccInterface);
    m_interfaceBuilder.setComment(CommentSourceBuilderFactory.createDefaultTypeComment(m_interfaceBuilder));
    m_interfaceBuilder.addInterfaceSignature(Signature.createTypeSignature(IScoutRuntimeTypes.IService));
    addType(m_interfaceBuilder);

    // @TunnelToServer
    m_interfaceBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createTunnelToServer());

    // load method
    m_interfaceBuilder.addMethod(createInterfaceMethod(SERVICE_LOAD_METHOD_NAME));

    // store method
    m_interfaceBuilder.addMethod(createInterfaceMethod(SERVICE_STORE_METHOD_NAME));
  }

  protected IMethodSourceBuilder createInterfaceMethod(String name) {
    IMethodSourceBuilder methodBuilder = new MethodSourceBuilder(name);
    methodBuilder.setFlags(Flags.AccInterface);
    methodBuilder.setComment(CommentSourceBuilderFactory.createDefaultMethodComment(methodBuilder));
    if (getDtoSignature() != null) {
      methodBuilder.setReturnTypeSignature(getDtoSignature());
      methodBuilder.addParameter(new MethodParameterSourceBuilder("input", getDtoSignature()));
    }
    else {
      methodBuilder.setReturnTypeSignature(ISignatureConstants.SIG_VOID);
    }
    return methodBuilder;
  }

  public String getDtoSignature() {
    return m_dtoSignature;
  }

  public void setDtoSignature(String dtoSignature) {
    m_dtoSignature = dtoSignature;
  }

}
