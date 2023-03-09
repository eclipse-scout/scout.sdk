/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.testing;

import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.model.api.NodeModulesProvider;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

public class NodeModulesExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

  private static final Path WORKING_DIR = Paths.get("").toAbsolutePath();

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    if (!isAnnotated(extensionContext.getRequiredTestMethod(), ExtendWithNodeModules.class) && !isAnnotated(extensionContext.getRequiredTestClass(), ExtendWithNodeModules.class)) {
      return false;
    }
    return INodeModule.class.equals(parameterContext.getParameter().getType());
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return findAnnotation(extensionContext)
        .map(ExtendWithNodeModules::value)
        .map(name -> pathToFixtureConfig(name, extensionContext.getRequiredTestClass()))
        .map(path -> NodeModulesProvider.createNodeModule(path, extensionContext).orElseThrow().api())
        .orElse(null);
  }

  protected static Path pathToFixtureConfig(String name, Class<?> testClass) {
    var fullName = Ensure.notBlank(name);
    var suffix = ".xml";
    if (!fullName.endsWith(suffix)) {
      fullName += suffix;
    }
    var file = WORKING_DIR
        .resolve("src/test/resources")
        .resolve(testClass.getPackageName().replace('.', '/'))
        .resolve(fullName);
    return Ensure.isFile(file);
  }

  public static Optional<ExtendWithNodeModules> findAnnotation(ExtensionContext extensionContext) {
    var currentContext = extensionContext;
    while (currentContext != null) {
      var annotation = getAnnotation(currentContext);
      if (annotation.isPresent()) {
        return annotation;
      }
      currentContext = currentContext.getParent().orElse(null);
    }
    return Optional.empty();
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    NodeModulesProvider.registerProvider(context, new TestingNodeModulesProvider());
  }

  @Override
  public void afterEach(ExtensionContext context) {
    NodeModulesProvider.clearNodeModules();
    NodeModulesProvider.removeProvider(context);
  }

  protected static Optional<ExtendWithNodeModules> getAnnotation(ExtensionContext context) {
    return AnnotationSupport.findAnnotation(context.getElement(), ExtendWithNodeModules.class);
  }
}
