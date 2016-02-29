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
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.methodparameter.IMethodParameterSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link ServiceImplSourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ServiceImplSourceBuilder extends CompilationUnitSourceBuilder {

  private static final String TEXT_AUTHORIZATION_FAILED = "AuthorizationFailed";

  private final String m_elementName;
  private final ITypeSourceBuilder m_interfaceBuilder;

  private String m_readPermissionSignature;
  private String m_updatePermissionSignature;
  private TypeSourceBuilder m_implBuilder;

  public ServiceImplSourceBuilder(String elementName, String packageName, ITypeSourceBuilder ifcBuilder) {
    super(elementName + SuffixConstants.SUFFIX_STRING_java, packageName);
    m_elementName = elementName;
    m_interfaceBuilder = ifcBuilder;
  }

  public void setup() {
    // service type
    m_implBuilder = new TypeSourceBuilder(m_elementName);
    m_implBuilder.setFlags(Flags.AccPublic);
    m_implBuilder.addInterfaceSignature(Signature.createTypeSignature(m_interfaceBuilder.getFullyQualifiedName()));
    addType(m_implBuilder);

    addAllInterfaceMethods();
  }

  protected void addAllInterfaceMethods() {
    for (IMethodSourceBuilder ifcMethod : m_interfaceBuilder.getMethods()) {
      final String methodName = ifcMethod.getElementName();
      final IMethodSourceBuilder currentMethodBuilder = ifcMethod;
      currentMethodBuilder.setComment(null);
      ifcMethod.setFlags(Flags.AccPublic);
      ifcMethod.setBody(new ISourceBuilder() {
        @Override
        public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
          // permission check
          String permissionSig = null;
          if (ServiceInterfaceSourceBuilder.SERVICE_LOAD_METHOD_NAME.equals(methodName)) {
            permissionSig = getReadPermissionSignature();
          }
          else if (ServiceInterfaceSourceBuilder.SERVICE_STORE_METHOD_NAME.equals(methodName)) {
            permissionSig = getUpdatePermissionSignature();
          }

          if (permissionSig != null) {
            createPermissionCheckSource(source, lineDelimiter, validator, permissionSig);
          }

          createMethodContentSource(source, lineDelimiter, validator, currentMethodBuilder);
        }
      });
      ifcMethod.addAnnotation(AnnotationSourceBuilderFactory.createOverride());
      m_implBuilder.addMethod(ifcMethod);
    }
  }

  /**
   * @param source
   * @param lineDelimiter
   * @param validator
   * @param parentMethod
   */
  protected void createMethodContentSource(StringBuilder source, String lineDelimiter, IImportValidator validator, IMethodSourceBuilder parentMethod) {
    // add todo
    source.append(CoreUtils.getCommentBlock("add business logic here.")).append(lineDelimiter);

    // return clause
    String paramToReturn = getParamNameOfReturnType(parentMethod);
    String returnSig = parentMethod.getReturnTypeSignature();

    if (paramToReturn == null) {
      String returnValue = CoreUtils.getDefaultValueOf(returnSig);
      if (returnValue != null) {
        source.append("return ").append(returnValue).append(';');
      }
    }
    else {
      source.append("return ").append(paramToReturn).append(';');
    }
  }

  protected void createPermissionCheckSource(StringBuilder source, String lineDelimiter, IImportValidator validator, String permissionSig) {
    source.append("if(!").append(validator.useName(IScoutRuntimeTypes.ACCESS));
    source.append(".check(new ").append(validator.useSignature(permissionSig)).append("())) {").append(lineDelimiter);

    source.append("  throw new ").append(validator.useName(IScoutRuntimeTypes.VetoException)).append('(');
    source.append(validator.useName(IScoutRuntimeTypes.TEXTS));
    source.append(".get(\"").append(TEXT_AUTHORIZATION_FAILED).append("\")");
    source.append(");").append(lineDelimiter);

    source.append('}').append(lineDelimiter);
  }

  protected String getParamNameOfReturnType(IMethodSourceBuilder msb) {
    if (msb.getReturnTypeSignature() == null || ISignatureConstants.SIG_VOID.equals(msb.getReturnTypeSignature())) {
      return null;
    }
    for (IMethodParameterSourceBuilder mpsb : msb.getParameters()) {
      if (msb.getReturnTypeSignature().equals(mpsb.getDataTypeSignature())) {
        return mpsb.getElementName();
      }
    }
    return null;
  }

  public String getReadPermissionSignature() {
    return m_readPermissionSignature;
  }

  public void setReadPermissionSignature(String readPermissionSignature) {
    m_readPermissionSignature = readPermissionSignature;
  }

  public String getUpdatePermissionSignature() {
    return m_updatePermissionSignature;
  }

  public void setUpdatePermissionSignature(String updatePermissionSignature) {
    m_updatePermissionSignature = updatePermissionSignature;
  }
}
