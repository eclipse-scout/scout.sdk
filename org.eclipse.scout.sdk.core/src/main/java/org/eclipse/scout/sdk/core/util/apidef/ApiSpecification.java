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
package org.eclipse.scout.sdk.core.util.apidef;

import static org.eclipse.scout.sdk.core.util.CoreUtils.invokeDefaultMethod;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.eclipse.scout.sdk.core.util.apidef.ApiVersion.requireApiLevelOf;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.AbstractMap.SimpleEntry;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.log.MessageFormatter;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.JavaTypes;

public class ApiSpecification implements InvocationHandler, IApiSpecification {

  private final ApiVersion m_version;
  private final FinalValue<ApiVersion> m_level;
  private final Map<Class<? extends IApiSpecification>, Optional<?>> m_apiCache;
  private final Map<String, Entry<Method, ApiSpecification>> m_methods; // flattened over the full chain of api specs (for performance reasons)

  private final Class<? extends IApiSpecification> m_interface; // api spec interface
  private final FinalValue<IApiSpecification> m_implementation; // api spec implementation (proxy)

  private final ApiSpecification m_nestedApi;

  protected ApiSpecification(ApiSpecification nestedApi, Class<? extends IApiSpecification> ifc, ApiVersion version) {
    m_interface = Ensure.notNull(ifc);
    m_nestedApi = nestedApi; // may be null
    m_version = version; // may be null indicating the "latest and greatest" version
    m_implementation = new FinalValue<>();
    m_level = new FinalValue<>();
    m_apiCache = new ConcurrentHashMap<>();
    m_methods = new HashMap<>();
  }

  static IApiSpecification create(Collection<Class<? extends IApiSpecification>> apiClasses, ApiVersion version) {
    Predicate<ApiVersion> versionFilter = version == null || version == ApiVersion.LATEST ? v -> true : v -> v.compareTo(version) <= 0;
    BiFunction<ApiSpecification, Class<? extends IApiSpecification>, ApiSpecification> wrapWith = (a, c) -> wrapWith(a, c, version);
    ApiSpecification root = apiClasses.stream()
        .map(ApiSpecification::associateWithLevel)
        .filter(e -> versionFilter.test(e.getKey()))
        .sorted(Entry.comparingByKey())
        .map(Entry::getValue)
        .reduce(null, wrapWith, Ensure::failOnDuplicates);
    if (root == null) {
      return null;
    }
    root.doInChain(root::mergeMethodsIntoCache);
    return root.apiImplementation();
  }

  static Entry<ApiVersion, Class<? extends IApiSpecification>> associateWithLevel(Class<? extends IApiSpecification> definition) {
    return new SimpleEntry<>(requireApiLevelOf(definition), definition);
  }

  static ApiSpecification wrapWith(ApiSpecification nested, Class<? extends IApiSpecification> wrapperInterface, ApiVersion version) {
    return new ApiSpecification(nested, wrapperInterface, version);
  }

  public IApiSpecification nestedApiImplementation() {
    return m_nestedApi;
  }

  public Class<? extends IApiSpecification> apiInterface() {
    return m_interface;
  }

  public IApiSpecification apiImplementation() {
    return m_implementation.computeIfAbsentAndGet(() -> apiInterface().cast(Proxy.newProxyInstance(apiInterface().getClassLoader(), new Class[]{apiInterface()}, this)));
  }

  @Override
  public ApiVersion level() {
    return m_level.computeIfAbsentAndGet(() -> requireApiLevelOf(apiInterface()));
  }

  @Override
  public Optional<ApiVersion> version() {
    return Optional.ofNullable(m_version);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object invoke(Object proxy, Method method, Object[] args) {
    if (method.getDeclaringClass() == Object.class) {
      String methodName = method.getName();
      switch (methodName) {
        case "hashCode":
          return System.identityHashCode(proxy);
        case "equals":
          return proxy == args[0];
        case "toString":
          return MessageFormatter.arrayFormat("ApiSpecification [version={}, class={}]", level().asString(), apiInterface().getName()).message();
      }
    }
    if (method.getDeclaringClass() == IApiSpecification.class) {
      String methodName = method.getName();
      switch (methodName) {
        case "version":
          return version();
        case "level":
          return level();
        case "optApi":
          return optApi((Class<? extends IApiSpecification>) args[0]);
        case "api":
          return api((Class<? extends IApiSpecification>) args[0]);
      }
    }
    return invokeIfcMethod(method, args);
  }

  protected Object invokeIfcMethod(Method methodTemplate, Object[] args) {
    String methodIdToFind = JavaTypes.createMethodIdentifier(methodTemplate);
    Entry<Method, ApiSpecification> methodSpec = m_methods.get(methodIdToFind);
    if (methodSpec == null) {
      throw newFail("Pure virtual function call: {}", methodTemplate);
    }
    ApiSpecification spec = methodSpec.getValue();
    return invokeDefaultMethod(spec.apiInterface(), spec.apiImplementation(), methodSpec.getKey(), args);
  }

  protected boolean mergeMethodsIntoCache(ApiSpecification spec) {
    Stream.of(spec.apiInterface().getMethods())
        .filter(Method::isDefault)
        .forEach(m -> m_methods.putIfAbsent(JavaTypes.createMethodIdentifier(m), new SimpleImmutableEntry<>(m, spec)));
    return true; // continue with the next spec
  }

  @Override
  public <A extends IApiSpecification> A api(Class<A> apiDefinition) {
    return optApi(apiDefinition).orElseThrow(() -> newFail("API {} is not supported.", apiDefinition.getSimpleName()));
  }

  @Override

  @SuppressWarnings("unchecked")
  public <A extends IApiSpecification> Optional<A> optApi(Class<A> apiDefinition) {
    Class<A> key = apiDefinition == null ? (Class<A>) IApiSpecification.class : apiDefinition;
    return (Optional<A>) m_apiCache.computeIfAbsent(key, this::computeApi);
  }

  @SuppressWarnings("unchecked")
  protected <A extends IApiSpecification> Optional<A> computeApi(Class<A> apiDefinition) {
    if (apiDefinition == IApiSpecification.class) {
      return Optional.empty();
    }

    FinalValue<A> result = new FinalValue<>();
    doInChain(invocationHandler -> {
      IApiSpecification candidate = invocationHandler.apiImplementation();
      if (apiDefinition.isInstance(candidate)) {
        result.set((A) candidate);
        return false;
      }
      return true;
    });
    return result.opt();
  }

  protected void doInChain(Predicate<ApiSpecification> chainFunction) {
    ApiSpecification invocationHandler = this;
    do {
      if (!chainFunction.test(invocationHandler)) {
        return;
      }
      invocationHandler = invocationHandler.m_nestedApi;
    }
    while (invocationHandler != null);
  }
}
