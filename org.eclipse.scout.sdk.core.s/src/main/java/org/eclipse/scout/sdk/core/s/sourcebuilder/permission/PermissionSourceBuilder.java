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
package org.eclipse.scout.sdk.core.s.sourcebuilder.permission;

import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.importvalidator.IImportValidator;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.sourcebuilder.ISourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.field.FieldSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.core.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.core.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.core.util.PropertyMap;

/**
 * <h3>{@link PermissionSourceBuilder}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class PermissionSourceBuilder extends CompilationUnitSourceBuilder {

  private final String m_elementName;

  public PermissionSourceBuilder(String elementName, String packageName) {
    super(elementName + SuffixConstants.SUFFIX_STRING_java, packageName);
    m_elementName = elementName;
  }

  public void setup() {
    TypeSourceBuilder permissionBuilder = new TypeSourceBuilder(m_elementName);
    permissionBuilder.setFlags(Flags.AccPublic);
    permissionBuilder.setSuperTypeSignature(Signature.createTypeSignature(IJavaRuntimeTypes.java_security_BasicPermission));
    addType(permissionBuilder);

    // serialVersionUID
    permissionBuilder.addField(FieldSourceBuilderFactory.createSerialVersionUidBuilder());

    // constructor
    permissionBuilder.addMethod(createConstructor(permissionBuilder));
  }

  protected IMethodSourceBuilder createConstructor(final ITypeSourceBuilder constructorOwner) {
    IMethodSourceBuilder constructor = MethodSourceBuilderFactory.createConstructor(m_elementName);
    constructor.setBody(new ISourceBuilder() {
      @Override
      public void createSource(StringBuilder source, String lineDelimiter, PropertyMap context, IImportValidator validator) {
        source.append("super(").append(validator.useName(constructorOwner.getFullyQualifiedName())).append(SuffixConstants.SUFFIX_STRING_class).append(".getSimpleName());");
      }
    });
    return constructor;
  }
}
