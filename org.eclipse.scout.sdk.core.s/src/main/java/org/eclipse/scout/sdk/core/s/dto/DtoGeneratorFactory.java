/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dto;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.java.comment.IJavaElementCommentBuilder;
import org.eclipse.scout.sdk.core.generator.annotation.AnnotationGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.CompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.compilationunit.ICompilationUnitGenerator;
import org.eclipse.scout.sdk.core.generator.type.ITypeGenerator;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.annotation.DataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.dto.table.PageDataGenerator;
import org.eclipse.scout.sdk.core.s.dto.table.TableFieldDataGenerator;
import org.eclipse.scout.sdk.core.s.dto.table.TableRowDataGenerator;

/**
 * Contains utilities for DTO creation
 */
public final class DtoGeneratorFactory {

  private static final String GENERATED_MSG = "This class is auto generated by the Scout SDK. No manual modifications recommended.";
  @SuppressWarnings("HardcodedLineSeparator")
  private static final String GENERATED_JAVADOC = "<b>NOTE:</b><br>\n" + GENERATED_MSG;

  private DtoGeneratorFactory() {
  }

  public static Optional<ICompilationUnitGenerator<?>> createTableRowDataGenerator(IType modelType, IJavaEnvironment sharedEnv) {
    return DataAnnotationDescriptor.of(modelType)
        .flatMap(dataAnnotation -> createTableRowDataGenerator(modelType, sharedEnv, dataAnnotation));
  }

  public static Optional<ICompilationUnitGenerator<?>> createTableRowDataGenerator(IType modelType, IJavaEnvironment sharedEnv, DataAnnotationDescriptor dataAnnotation) {
    if (modelType == null || dataAnnotation == null || sharedEnv == null) {
      return Optional.empty();
    }

    var rowDataTypeSrc = new TableRowDataGenerator<>(modelType, modelType, sharedEnv)
        .withExtendsAnnotationIfNecessary(dataAnnotation.getAnnotationHolder());

    var targetPackage = dataAnnotation.getDataType().requireCompilationUnit().containingPackage().elementName();
    var targetName = dataAnnotation.getDataType().elementName();
    return Optional.of(createDtoCuGenerator(modelType, targetName, targetPackage, rowDataTypeSrc));
  }

  public static Optional<ICompilationUnitGenerator<?>> createPageDataGenerator(IType modelType, IJavaEnvironment sharedEnv) {
    return DataAnnotationDescriptor.of(modelType)
        .flatMap(dataAnnotation -> createPageDataGenerator(modelType, sharedEnv, dataAnnotation));
  }

  public static Optional<ICompilationUnitGenerator<?>> createPageDataGenerator(IType modelType, IJavaEnvironment sharedEnv, DataAnnotationDescriptor dataAnnotation) {
    if (modelType == null || dataAnnotation == null || sharedEnv == null) {
      return Optional.empty();
    }
    ITypeGenerator<?> pageDataTypeSrc = new PageDataGenerator<>(modelType, dataAnnotation, sharedEnv);
    var targetPackage = dataAnnotation.getDataType().requireCompilationUnit().containingPackage().elementName();
    var targetName = dataAnnotation.getDataType().elementName();
    return Optional.of(createDtoCuGenerator(modelType, targetName, targetPackage, pageDataTypeSrc));
  }

  public static Optional<ICompilationUnitGenerator<?>> createFormDataGenerator(IType modelType, IJavaEnvironment sharedEnv) {
    return createFormDataGenerator(modelType, sharedEnv, FormDataAnnotationDescriptor.of(modelType));
  }

  public static Optional<ICompilationUnitGenerator<?>> createFormDataGenerator(IType modelType, IJavaEnvironment sharedEnv, FormDataAnnotationDescriptor formDataAnnotation) {
    if (!FormDataAnnotationDescriptor.isCreate(formDataAnnotation) || modelType == null || sharedEnv == null) {
      return Optional.empty();
    }
    var superType = formDataAnnotation.getSuperType();
    if (superType == null) {
      return Optional.empty();
    }

    AbstractDtoGenerator<?> formDataTypeSrc;
    var scoutApi = modelType.javaEnvironment().requireApi(IScoutApi.class);
    if (superType.isInstanceOf(scoutApi.AbstractTableFieldBeanData())) {
      formDataTypeSrc = new TableFieldDataGenerator<>(modelType, formDataAnnotation, sharedEnv);
    }
    else {
      formDataTypeSrc = new CompositeFormDataGenerator<>(modelType, formDataAnnotation, sharedEnv);
    }

    var targetPackage = formDataAnnotation.getFormDataType().requireCompilationUnit().containingPackage().elementName();
    var targetName = formDataAnnotation.getFormDataType().elementName();
    return Optional.of(
        createDtoCuGenerator(modelType, targetName, targetPackage,
            formDataTypeSrc
                .withExtendsAnnotationIfNecessary(formDataAnnotation.getAnnotationOwnerAsType())));
  }

  private static ICompilationUnitGenerator<?> createDtoCuGenerator(IType modelType, String targetPrimaryTypeName, String targetPackage, ITypeGenerator<?> dtoGeneratorForModel) {
    return CompilationUnitGenerator.create()
        .withPackageName(targetPackage)
        .withElementName(targetPrimaryTypeName)
        .withComment(IJavaElementCommentBuilder::appendDefaultElementComment)
        .withType(dtoGeneratorForModel
            .withElementName(targetPrimaryTypeName)
            .withComment(b -> b.appendJavaDocComment(GENERATED_JAVADOC))
            .withAnnotation(AnnotationGenerator.createGenerated(modelType.name(), GENERATED_MSG)));

  }
}
