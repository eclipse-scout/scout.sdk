/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.derived;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.dto.DtoGeneratorFactory;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.java.annotation.DataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.java.annotation.FormDataAnnotationDescriptor;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutInterfaceApi;

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
    var scoutApi = modelType.javaEnvironment().requireApi(IScoutApi.class);
    var formDataAnnotation = findDataAnnotationForFormData(modelType);
    if (formDataAnnotation.isPresent()) {
      return getInput()
          .getSourceFolderOf(formDataAnnotation.orElseThrow().getFormDataType(), env)
          .flatMap(derivedSourceFolder -> DtoGeneratorFactory
              .createFormDataGenerator(modelType, derivedSourceFolder.javaEnvironment(), formDataAnnotation.orElseThrow())
              .map(g -> env.writeCompilationUnitAsync(g, derivedSourceFolder, progress)));
    }

    var pageDataAnnotation = findDataAnnotationForPageData(modelType, scoutApi);
    if (pageDataAnnotation.isPresent()) {
      return getInput()
          .getSourceFolderOf(pageDataAnnotation.orElseThrow().getDataType(), env)
          .flatMap(derivedSourceFolder -> DtoGeneratorFactory
              .createPageDataGenerator(modelType, derivedSourceFolder.javaEnvironment(), pageDataAnnotation.orElseThrow())
              .map(g -> env.writeCompilationUnitAsync(g, derivedSourceFolder, progress)));
    }

    var dataAnnotation = findDataAnnotationForRowData(modelType, scoutApi);
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

  protected static Optional<DataAnnotationDescriptor> findDataAnnotationForPageData(IType model, IScoutInterfaceApi api) {
    return Optional.of(model)
        .filter(m -> m.isInstanceOf(api.IPageWithTable()))
        .flatMap(DataAnnotationDescriptor::of);
  }

  protected static Optional<DataAnnotationDescriptor> findDataAnnotationForRowData(IType model, IScoutInterfaceApi api) {
    // direct column or table extension
    if (model.isInstanceOf(api.IColumn()) || model.isInstanceOf(api.ITableExtension())) {
      return DataAnnotationDescriptor.of(model);
    }

    // check for table extension in IPageWithTableExtension
    return Optional.of(model)
        .filter(m -> m.isInstanceOf(api.IPageWithTableExtension()))
        .filter(m -> containsTableExtension(m, api))
        .flatMap(DataAnnotationDescriptor::of);
  }

  protected static boolean containsTableExtension(IType model, IScoutInterfaceApi api) {
    return model.innerTypes().withInstanceOf(api.ITableExtension()).existsAny();
  }

  @Override
  public String toString() {
    return "Update DTO for '" + getInput() + "'.";
  }
}
