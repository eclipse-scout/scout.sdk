/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.testing.context;

import java.lang.reflect.AnnotatedElement;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.dataobject.DoContextResolvers;
import org.eclipse.scout.sdk.core.s.dataobject.DoContextResolvers.IDoContextResolver;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

public class DoContextExtension implements BeforeEachCallback, AfterEachCallback {

  @Override
  public void beforeEach(ExtensionContext context) {
    //noinspection resource
    context.getElement()
        .map(e -> new TestingEnvironmentExtension().getOrCreateContextFor(e, context))
        .map(te -> te.primarySourceFolder().javaEnvironment())
        .or(() -> context.getElement().map(e -> new JavaEnvironmentExtension().getOrCreateContextFor(e, context)))
        .ifPresent(je -> getOrCreateContext(context, context.getElement().orElseThrow(), je));
  }

  protected Store getStore(ExtensionContext context) {
    return context.getStore(Namespace.create(getClass(), context));
  }

  protected void getOrCreateContext(ExtensionContext context, AnnotatedElement element, IJavaEnvironment env) {
    getStore(context).getOrComputeIfAbsent(contextKey(), k -> createContext(element, env));
  }

  protected static IDoContextResolver createContext(AnnotatedElement element, IJavaEnvironment env) {
    var annotation = Ensure.notNull(element.getAnnotation(ExtendWithDoContext.class), "Annotation '{}' is required.", ExtendWithDoContext.class.getName());
    var context = new TestingDoContextResolver(annotation, env);
    DoContextResolvers.set(context);
    return context;
  }

  protected static String contextKey() {
    return "dataObjectContextResolver";
  }

  @Override
  public void afterEach(ExtensionContext context) {
    DoContextResolvers.set(null);
    getStore(context).remove(contextKey());
  }

  private static final class TestingDoContextResolver implements IDoContextResolver {

    private final IType m_namespace;
    private final IType m_typeVersion;

    private TestingDoContextResolver(ExtendWithDoContext annotation, IJavaEnvironment env) {
      m_namespace = env.requireType(annotation.namespace().getName());
      m_typeVersion = env.requireType(annotation.typeVersion().getName());
    }

    @Override
    public Stream<IType> resolveNamespaceCandidates(IJavaEnvironment environment) {
      return Stream.of(m_namespace)
          .filter(it -> !it.isInterface() && !Flags.isAbstract(it.flags()));
    }

    @Override
    public Stream<IType> resolvePrimaryTypesInPackageOf(IType namespace) {
      return Stream.of(m_typeVersion)
          .filter(it -> !it.isInterface() && !Flags.isAbstract(it.flags()));
    }
  }
}
