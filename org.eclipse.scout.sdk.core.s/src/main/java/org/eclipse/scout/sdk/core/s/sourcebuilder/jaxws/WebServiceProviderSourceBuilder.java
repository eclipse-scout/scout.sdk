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
package org.eclipse.scout.sdk.core.s.sourcebuilder.jaxws;

import java.util.List;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.model.ScoutAnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.AbstractEntitySourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.SdkLog;

/**
 * <h3>{@link WebServiceProviderSourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class WebServiceProviderSourceBuilder extends AbstractEntitySourceBuilder {

  private String m_portTypeSignature;

  public WebServiceProviderSourceBuilder(String entityName, String packageName, IJavaEnvironment env) {
    super(entityName, packageName, env);
  }

  @Override
  public void setup() {
    setComment(CommentSourceBuilderFactory.createDefaultCompilationUnitComment(this));

    TypeSourceBuilder typeBuilder = new TypeSourceBuilder(getEntityName());
    typeBuilder.setFlags(Flags.AccPublic);
    typeBuilder.addInterfaceSignature(getPortTypeSignature());
    typeBuilder.addAnnotation(ScoutAnnotationSourceBuilderFactory.createApplicationScoped());
    addType(typeBuilder);

    if (getJavaEnvironment().findType(SignatureUtils.toFullyQualifiedName(getPortTypeSignature())) == null) {
      // stub generation failed and no port type is available. we cannot implement the required methods as we don't know them -> skip
      SdkLog.warning("Web Service implementation could not be filled with all methods because the port type could not be found.");
      return;
    }

    List<IMethodSourceBuilder> unimplementedMethods = MethodSourceBuilderFactory.createUnimplementedMethods(typeBuilder, getPackageName(), getJavaEnvironment());
    for (IMethodSourceBuilder unimplemented : unimplementedMethods) {
      typeBuilder.addMethod(unimplemented);
    }
  }

  public String getPortTypeSignature() {
    return m_portTypeSignature;
  }

  public void setPortTypeSignature(String portTypeSignature) {
    m_portTypeSignature = portTypeSignature;
  }

}
