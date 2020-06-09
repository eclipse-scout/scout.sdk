/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.derived;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.annotation.DataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.annotation.FormDataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.dto.DtoGeneratorFactory;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;

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
    return dataAnnotation.flatMap(dataAnnotationDescriptor -> getInput()
        .getSourceFolderOf(dataAnnotationDescriptor.getDataType(), env)
        .flatMap(derivedSourceFolder -> DtoGeneratorFactory
            .createTableRowDataGenerator(modelType, derivedSourceFolder.javaEnvironment(), dataAnnotationDescriptor)
            .map(g -> env.writeCompilationUnitAsync(g, derivedSourceFolder, progress))));

  }

  protected static Optional<FormDataAnnotationDescriptor> findDataAnnotationForFormData(IType model) {
    return Optional.of(model)
        .map(FormDataAnnotationDescriptor::of)
        .filter(FormDataAnnotationDescriptor::isCreate)
        .filter(d -> d.getFormDataType() != null);
  }

  protected static Optional<DataAnnotationDescriptor> findDataAnnotationForPageData(IType model) {
    return Optional.of(model)
        .filter(m -> m.isInstanceOf(IScoutRuntimeTypes.IPageWithTable))
        .flatMap(DataAnnotationDescriptor::of);
  }

  protected static Optional<DataAnnotationDescriptor> findDataAnnotationForRowData(IType model) {
    // direct column or table extension
    if (model.isInstanceOf(IScoutRuntimeTypes.IColumn) || model.isInstanceOf(IScoutRuntimeTypes.ITableExtension)) {
      return DataAnnotationDescriptor.of(model);
    }

    // check for table extension in IPageWithTableExtension
    return Optional.of(model)
        .filter(m -> m.isInstanceOf(IScoutRuntimeTypes.IPageWithTableExtension))
        .filter(DtoUpdateHandler::containsTableExtension)
        .flatMap(DataAnnotationDescriptor::of);
  }

  protected static boolean containsTableExtension(IType model) {
    return model.innerTypes()
        .withInstanceOf(IScoutRuntimeTypes.ITableExtension)
        .existsAny();
  }

  @Override
  public String toString() {
    return "Update DTO for '" + getInput() + "'.";
  }
}
