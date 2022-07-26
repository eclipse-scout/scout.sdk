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
package org.eclipse.scout.sdk.core.s.js.element;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IFuture;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.environment.SdkFuture;
import org.eclipse.scout.sdk.core.s.js.element.gen.IJsModelGenerator;
import org.eclipse.scout.sdk.core.s.js.element.gen.IJsSourceBuilder;
import org.eclipse.scout.sdk.core.s.js.element.gen.IJsSourceGenerator;
import org.eclipse.scout.sdk.core.s.js.element.gen.PrimaryJsModelGenerator;
import org.eclipse.scout.sdk.core.util.Strings;

public class JsElementsCreateOperation implements BiConsumer<IEnvironment, IProgress> {

  private final List<IType> m_elements = new ArrayList<>();
  private Function<IType, Path> m_getTargetJsFolderPathStrategy;
  private Function<IType, String> m_getModuleNameStrategy;

  @Override
  public void accept(IEnvironment env, IProgress progress) {
    validateOperation();
    prepareProgress(progress);
    executeOperation(env, progress);
  }

  protected void validateOperation() {
    // nop
  }

  protected void prepareProgress(IProgress progress) {
    progress.init(getTotalWork(), toString());
  }

  protected int getTotalWork() {
    return elements().size() * 2;
  }

  protected void executeOperation(IEnvironment env, IProgress progress) {
    var elements = elements();
    var writeOperations = elements.stream()
        .map(element -> createJsElements(element, env, progress.newChild(2)))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    SdkFuture.awaitAll(writeOperations);
  }

  protected Collection<IFuture<?>> createJsElements(IType element, IEnvironment env, IProgress progress) {
    var fileNameJsSourceGeneratorMap = new HashMap<String, IJsSourceGenerator<IJsSourceBuilder<?>>>();
    collectJsElementGenerators(element, fileNameJsSourceGeneratorMap);

    return fileNameJsSourceGeneratorMap.entrySet()
        .stream()
        .map(entry -> write(entry.getValue(), entry.getKey(), element, env, progress))
        .collect(Collectors.toList());
  }

  protected void collectJsElementGenerators(IType element, Map<String, IJsSourceGenerator<IJsSourceBuilder<?>>> fileNameJsSourceGeneratorMap) {
    var jsElementModel = JsElementModel.wrap(element);

//    fileNameJsSourceGeneratorMap.put(element.elementName(), createJsElementGenerator(jsElementModel));
    fileNameJsSourceGeneratorMap.put(element.elementName() + "Model", createJsElementModelGenerator(jsElementModel));
  }

  @SuppressWarnings("MethodMayBeStatic")
  protected CharSequence createJsElementGenerator(JsElementModel jsElementModel) {
    // TODO fsh
    return null;
  }

  @SuppressWarnings("MethodMayBeStatic")
  protected IJsModelGenerator<?> createJsElementModelGenerator(JsElementModel jsElementModel) {
    return PrimaryJsModelGenerator.create()
        .withJsElementModel(jsElementModel);
  }

  protected IFuture<?> write(IJsSourceGenerator<IJsSourceBuilder<?>> generator, String fileName, IType element, IEnvironment env, IProgress progress) {
    var path = getTargetJsFolderPath(element);
    var pck = element.containingPackage().elementName();
    if (pck != null) {
      pck = Strings.removePrefix(pck, getModuleName(element));
      pck = Strings.removePrefix(pck, ".");
      path = path.resolve(Strings.replace(pck, ".", "/").toString());
    }
    path = path.resolve(fileName + ".js");

    return env.writeJsSourceAsync(generator, path, progress);
  }

  protected Path getTargetJsFolderPath(IType element) {
    return getTargetJsFolderPathStrategy().apply(element);
  }

  protected String getModuleName(IType element) {
    return getModuleNameStrategy().apply(element);
  }

  protected List<IType> elements() {
    return Collections.unmodifiableList(m_elements);
  }

  public JsElementsCreateOperation withElements(Collection<IType> elements) {
    m_elements.clear();
    m_elements.addAll(elements);
    return this;
  }

  protected Function<IType, Path> getTargetJsFolderPathStrategy() {
    return m_getTargetJsFolderPathStrategy;
  }

  public JsElementsCreateOperation withGetTargetJsFolderPathStrategy(Function<IType, Path> getTargetJsFolderPathStrategy) {
    m_getTargetJsFolderPathStrategy = getTargetJsFolderPathStrategy;
    return this;
  }

  protected Function<IType, String> getModuleNameStrategy() {
    return m_getModuleNameStrategy;
  }

  public JsElementsCreateOperation withGetModuleNameStrategy(Function<IType, String> getModuleNameStrategy) {
    m_getModuleNameStrategy = getModuleNameStrategy;
    return this;
  }

  @Override
  public String toString() {
    return "Create JS elements";
  }
}
