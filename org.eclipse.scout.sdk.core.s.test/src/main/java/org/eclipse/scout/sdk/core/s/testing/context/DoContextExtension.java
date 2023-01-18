/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.testing.context;

import static org.eclipse.scout.sdk.core.java.testing.context.AbstractContextExtension.findAnnotationContext;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.s.dataobject.DoContextResolvers;
import org.eclipse.scout.sdk.core.s.dataobject.DoContextResolvers.IDoContextResolver;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.platform.commons.PreconditionViolationException;

public class DoContextExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

  @Override
  public void beforeAll(ExtensionContext context) {
    beforeEach(context);
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    var entry = findAnnotationContext(context, ExtendWithDoContext.class).orElseThrow(() -> new PreconditionViolationException("Annotation '" + ExtendWithDoContext.class.getSimpleName() + "' is required."));
    var annotation = entry.getKey();
    var annotationContext = entry.getValue();
    getStore(annotationContext).getOrComputeIfAbsent(contextKey(), k -> activateDoContextResolver(annotation, annotationContext, context));
  }

  protected Store getStore(ExtensionContext context) {
    return context.getStore(Namespace.create(getClass(), context));
  }

  protected static String contextKey() {
    return "dataObjectContextResolver";
  }

  protected static String oldResolverKey() {
    return "previousDataObjectContextResolver";
  }

  protected IDoContextResolver activateDoContextResolver(ExtendWithDoContext annotation, ExtensionContext contextOfExtendWithDo, ExtensionContext rootContext) {
    var javaEnvironment = Optional.ofNullable(new TestingEnvironmentExtension().getOrCreateContextFor(rootContext))
        .map(te -> te.primarySourceFolder().javaEnvironment())
        .or(() -> Optional.ofNullable(new JavaEnvironmentExtension().getOrCreateContextFor(rootContext)))
        .orElseThrow(() -> new PreconditionViolationException("A DoContext requires an '" + ExtendWithJavaEnvironmentFactory.class.getName() + "' or '" + ExtendWithTestingEnvironment.class.getName() + "' to be present."));

    var resolver = new TestingDoContextResolver(annotation, javaEnvironment);
    var previous = DoContextResolvers.set(resolver);
    if (previous != null) {
      getStore(contextOfExtendWithDo).put(oldResolverKey(), previous);
    }
    return resolver;
  }

  @Override
  public void afterEach(ExtensionContext context) {
    var store = getStore(context);
    var removed = store.remove(contextKey(), IDoContextResolver.class);
    if (removed != null) {
      // a testing resolver is active. Revert to the previous one
      DoContextResolvers.set(store.remove(oldResolverKey(), IDoContextResolver.class));
    }
  }

  @Override
  public void afterAll(ExtensionContext context) {
    afterEach(context);
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
