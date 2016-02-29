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
package org.eclipse.scout.sdk.core.s.sourcebuilder.lookupcall;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link LookupCallSourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class LookupCallSourceBuilder extends CompilationUnitSourceBuilder {

  private final String m_elementName;
  private String m_keyTypeSignature;
  private String m_superTypeSignature;
  private String m_lookupServiceIfcSignature;

  public LookupCallSourceBuilder(String elementName, String packageName) {
    super(elementName + SuffixConstants.SUFFIX_STRING_java, packageName);
    m_elementName = elementName;
  }

  public void setup() {
    TypeSourceBuilder lookupCallBuilder = new TypeSourceBuilder(m_elementName);
    lookupCallBuilder.setFlags(Flags.AccPublic);
    StringBuilder superTypeBuilder = new StringBuilder(SignatureUtils.toFullyQualifiedName(getSuperTypeSignature()));
    superTypeBuilder.append(ISignatureConstants.C_GENERIC_START);
    superTypeBuilder.append(SignatureUtils.toFullyQualifiedName(getKeyTypeSignature()));
    superTypeBuilder.append(ISignatureConstants.C_GENERIC_END);
    lookupCallBuilder.setSuperTypeSignature(Signature.createTypeSignature(superTypeBuilder.toString()));
    addType(lookupCallBuilder);

    // serialVersionUID
    lookupCallBuilder.addField(FieldSourceBuilderFactory.createSerialVersionUidBuilder());

    if (StringUtils.isNotBlank(getLookupServiceIfcSignature())) {
      IMethodSourceBuilder getConfiguredService = createGetConfiguredService();
      lookupCallBuilder.addSortedMethod(SortedMemberKeyFactory.createMethodGetConfiguredKey(getConfiguredService), getConfiguredService);
    }
  }

  protected IMethodSourceBuilder createGetConfiguredService() {
    IMethodSourceBuilder getConfiguredService = new MethodSourceBuilder("getConfiguredService");
    getConfiguredService.setFlags(Flags.AccProtected);

    StringBuilder superTypeBuilder = new StringBuilder(IJavaRuntimeTypes.java_lang_Class);
    superTypeBuilder.append(ISignatureConstants.C_GENERIC_START);
    superTypeBuilder.append("? extends ");
    superTypeBuilder.append(IScoutRuntimeTypes.ILookupService);
    superTypeBuilder.append(ISignatureConstants.C_GENERIC_START);
    superTypeBuilder.append(SignatureUtils.toFullyQualifiedName(getKeyTypeSignature()));
    superTypeBuilder.append(ISignatureConstants.C_GENERIC_END);
    superTypeBuilder.append(ISignatureConstants.C_GENERIC_END);
    getConfiguredService.setReturnTypeSignature(Signature.createTypeSignature(superTypeBuilder.toString()));

    getConfiguredService.setBody(new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append("return ").append(validator.useSignature(getLookupServiceIfcSignature())).append(SuffixConstants.SUFFIX_STRING_class).append(';');
      }
    });

    getConfiguredService.addAnnotation(AnnotationSourceBuilderFactory.createOverride());
    return getConfiguredService;
  }

  public String getKeyTypeSignature() {
    return m_keyTypeSignature;
  }

  public void setKeyTypeSignature(String keyTypeSignature) {
    m_keyTypeSignature = keyTypeSignature;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public String getLookupServiceIfcSignature() {
    return m_lookupServiceIfcSignature;
  }

  public void setLookupServiceIfcSignature(String lookupServiceIfcSignature) {
    m_lookupServiceIfcSignature = lookupServiceIfcSignature;
  }
}
