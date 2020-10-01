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

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;

public final class Api {

  private static final Map<Class<? extends IApiSpecification>, IApiProvider> REGISTRY = new ConcurrentHashMap<>();
  private static final Map<Entry<Class<? extends IApiSpecification>, ApiVersion>, IApiSpecification> API_CACHE = new ConcurrentHashMap<>();

  private Api() {
  }

  public enum ChildElementType {

    METHOD_NAME("MethodName"), // Methods in the type
    TYPE_PARAM_INDEX("TypeParamIndex"), // Type Parameter indices
    ANNOTATION_ELEMENT_NAME("ElementName"), // Annotation elements
    OTHER("other"); // all other methods

    private final String m_suffix;

    ChildElementType(String suffix) {
      m_suffix = Ensure.notBlank(suffix);
    }

    @Override
    public String toString() {
      return m_suffix;
    }
  }

  public static IApiProvider getProvider(Class<? extends IApiSpecification> apiDefinition) {
    Ensure.notNull(apiDefinition);
    ensureInitialized(apiDefinition);
    return Ensure.notNull(REGISTRY.get(apiDefinition), "No provider for API class '{}' found.", apiDefinition);
  }

  public static boolean registerProvider(Class<? extends IApiSpecification> apiDefinition, IApiProvider provider) {
    return REGISTRY.put(Ensure.notNull(apiDefinition), Ensure.notNull(provider)) != null;
  }

  public static boolean unregisterProvider(Class<? extends IApiSpecification> apiDefinition) {
    return REGISTRY.remove(apiDefinition) != null;
  }

  static void ensureInitialized(Class<?> classToInit) {
    try {
      Class.forName(classToInit.getName(), true, classToInit.getClassLoader());
    }
    catch (ClassNotFoundException e) {
      SdkLog.debug("Unable to locate class '{}'.", classToInit, e); // cannot happen
    }
  }

  public static int latestMajorVersion(Class<? extends IApiSpecification> api) {
    return latest(api).level().segments()[0];
  }

  public static <API extends IApiSpecification> API latest(Class<API> api) {
    return create(api, ApiVersion.LATEST);
  }

  public static Optional<ApiVersion> version(Class<? extends IApiSpecification> api, IJavaElement context) {
    return Optional.ofNullable(context)
        .map(IJavaElement::javaEnvironment)
        .flatMap(env -> version(api, env));
  }

  public static Optional<ApiVersion> version(Class<? extends IApiSpecification> api, IJavaEnvironment context) {
    return getProvider(api).version(context);
  }

  public static <API extends IApiSpecification> Optional<API> create(Class<API> api, IJavaElement context) {
    return create(api, context.javaEnvironment());
  }

  public static <API extends IApiSpecification> Optional<API> create(Class<API> api, IJavaEnvironment context) {
    return version(api, context)
        .map(version -> create(api, version));
  }

  public static <API extends IApiSpecification> API create(Class<API> api, ApiVersion version) {
    Entry<Class<? extends IApiSpecification>, ApiVersion> key = new SimpleImmutableEntry<>(api, version);
    IApiSpecification definition = API_CACHE.computeIfAbsent(key, Api::doCreateApi);
    return api.cast(definition);
  }

  static IApiSpecification doCreateApi(Entry<Class<? extends IApiSpecification>, ApiVersion> entry) {
    Class<? extends IApiSpecification> api = entry.getKey();
    ApiVersion version = entry.getValue();
    Collection<Class<? extends IApiSpecification>> apiDefinitions = getProvider(api).knownApis();
    IApiSpecification definition = Ensure.notNull(ApiSpecification.create(apiDefinitions, version), "No known API supports {}. Available APIs: {}", version, apiDefinitions);
    if (SdkLog.isDebugEnabled()) {
      SdkLog.debug("Creating API definition. Parsed version: {}. API level: {}.", definition.version().map(ApiVersion::asString).orElse("latest"), definition.level().asString());
    }
    return definition;
  }

  public static <API extends IApiSpecification> Stream<API> allKnown(Class<API> api) {
    return getProvider(api)
        .knownApis().stream()
        .map(ApiVersion::requireApiLevelOf)
        .sorted(Comparator.reverseOrder()) // newest version first
        .map(version -> create(api, version));
  }

  public static Map<String, Map<ChildElementType, Map<String, String>>> dump(IApiSpecification api) {
    return Stream.of(Ensure.notNull(api).getClass().getMethods())
        .filter(m -> m.getParameterCount() == 0)
        .filter(m -> IClassNameSupplier.class.isAssignableFrom(m.getReturnType()))
        .map(m -> (IClassNameSupplier) invoke(m, api))
        .map(Api::dump)
        .collect(toMap(Entry::getKey, Entry::getValue));
  }

  public static Entry<String, Map<ChildElementType, Map<String, String>>> dump(IClassNameSupplier cns) {
    Class<? extends IClassNameSupplier> supplierClass = Ensure.notNull(cns).getClass();
    List<Method> methods = Stream.of(supplierClass.getMethods())
        .filter(m -> m.getDeclaringClass() == supplierClass)
        .filter(m -> m.getParameterCount() == 0)
        .filter(m -> m.getReturnType() != void.class)
        .filter(m -> !"fqn".equals(m.getName()) && !"simpleName".equals(m.getName())) // methods defined in IClassNameSupplier
        .collect(toList());
    Map<ChildElementType, Map<String, String>> entries = new EnumMap<>(ChildElementType.class);
    for (ChildElementType type : ChildElementType.values()) {
      if (type == ChildElementType.OTHER) {
        continue;
      }
      entries.put(type, consumeMethods(methods, cns, m -> m.getName().endsWith(type.toString())));
    }
    entries.put(ChildElementType.OTHER, consumeMethods(methods, cns, null)); // assign remaining methods not matching one of the previous types
    return new SimpleImmutableEntry<>(Ensure.notNull(cns).fqn(), entries);
  }

  static Map<String, String> consumeMethods(Collection<Method> methods, IClassNameSupplier owner, Predicate<Method> filter) {
    Map<String, String> methodResultMapping = methods.stream()
        .filter(m -> filter == null || filter.test(m))
        .collect(toMap(Method::getName, m -> String.valueOf(invoke(m, owner))));
    if (methodResultMapping.isEmpty()) {
      return emptyMap();
    }
    methods.removeIf(m -> methodResultMapping.containsKey(m.getName())); // consume methods (only assign to one type)
    return methodResultMapping;
  }

  static Object invoke(Method m, Object instance) {
    try {
      return m.invoke(instance);
    }
    catch (ReflectiveOperationException e) {
      throw new SdkException("Cannot invoke method '{}' on '{}'.", m.getName(), instance, e);
    }
  }
}
