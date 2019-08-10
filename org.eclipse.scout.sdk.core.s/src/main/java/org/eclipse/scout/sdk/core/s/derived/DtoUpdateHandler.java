/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.derived;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.annotation.DataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.dto.DtoGeneratorFactory;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * <h3>{@link DtoUpdateHandler}</h3>
 *
 * @since 7.0.0
 */
public class DtoUpdateHandler extends AbstractDerivedResourceHandler {

  public DtoUpdateHandler(IDerivedResourceInput input) {
    super(input);
  }

  @Override
  protected Collection<? extends IFuture<?>> execute(IEnvironment env, IProgress progress) {
    return getInput().getSourceType(env)
        .flatMap(sourceType -> writeDerivedTypeOf(sourceType, env, progress))
        .map(Collections::singleton)
        .orElseGet(Collections::emptySet);
  }

  protected Optional<IFuture<IType>> writeDerivedTypeOf(IType modelType, IEnvironment env, IProgress progress) {
    Optional<FormDataAnnotationDescriptor> formDataAnnotation = findDataAnnotationForFormData(modelType);
    if (formDataAnnotation.isPresent()) {
      return getInput()
          .getSourceFolderOf(formDataAnnotation.get().getFormDataType(), env)
          .flatMap(derivedSourceFolder -> DtoGeneratorFactory
              .createFormDataGenerator(modelType, derivedSourceFolder.javaEnvironment(), formDataAnnotation.get())
              .map(g -> env.writeCompilationUnitAsync(g, derivedSourceFolder, progress)));
    }

    Optional<DataAnnotationDescriptor> pageDataAnnotation = findDataAnnotationForPageData(modelType);
    if (pageDataAnnotation.isPresent()) {
      return getInput()
          .getSourceFolderOf(pageDataAnnotation.get().getDataType(), env)
          .flatMap(derivedSourceFolder -> DtoGeneratorFactory
              .createPageDataGenerator(modelType, derivedSourceFolder.javaEnvironment(), pageDataAnnotation.get())
              .map(g -> env.writeCompilationUnitAsync(g, derivedSourceFolder, progress)));
    }

    Optional<DataAnnotationDescriptor> dataAnnotation = findDataAnnotationForRowData(modelType);
    if (dataAnnotation.isPresent()) {
      return getInput()
          .getSourceFolderOf(dataAnnotation.get().getDataType(), env)
          .flatMap(derivedSourceFolder -> DtoGeneratorFactory
              .createTableRowDataGenerator(modelType, derivedSourceFolder.javaEnvironment(), dataAnnotation.get())
              .map(g -> env.writeCompilationUnitAsync(g, derivedSourceFolder, progress)));
    }

    return Optional.empty();
  }

  protected static Optional<FormDataAnnotationDescriptor> findDataAnnotationForFormData(IType modelType) {
    FormDataAnnotationDescriptor formDataAnnotation = FormDataAnnotationDescriptor.of(modelType);
    if (FormDataAnnotationDescriptor.isCreate(formDataAnnotation) && formDataAnnotation.getFormDataType() != null) {
      return Optional.of(formDataAnnotation);
    }
    return Optional.empty();
  }

  protected static Optional<DataAnnotationDescriptor> findDataAnnotationForPageData(IType model) {
    if (model.isInstanceOf(IScoutRuntimeTypes.IPageWithTable)) {
      return DataAnnotationDescriptor.of(model);
    }
    return Optional.empty();
  }

  protected static Optional<DataAnnotationDescriptor> findDataAnnotationForRowData(IType model) {
    // direct column or table extension
    if (model.isInstanceOf(IScoutRuntimeTypes.IColumn) || model.isInstanceOf(IScoutRuntimeTypes.ITableExtension)) {
      return DataAnnotationDescriptor.of(model);
    }
    // check for table extension in IPageWithTableExtension
    if (model.isInstanceOf(IScoutRuntimeTypes.IPageWithTableExtension)) {
      return model.innerTypes().withInstanceOf(IScoutRuntimeTypes.ITableExtension).first().flatMap(DataAnnotationDescriptor::of);
    }
    return Optional.empty();
  }

  @Override
  public String toString() {
    return "Update DTO for '" + getInput() + "'.";
  }
}
