/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.apidef;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * Main access class for obtaining {@link IApiSpecification} instances.
 */
@SuppressWarnings("SynchronizationOnStaticField")
public final class Api {

  @SuppressWarnings("StaticCollection")
  private static final Map<Class<? extends IApiSpecification>, IApiProvider> REGISTRY = new HashMap<>();
  @SuppressWarnings("StaticCollection")
  private static final Map<Entry<Class<? extends IApiSpecification>, ApiVersion>, IApiSpecification> API_CACHE = new HashMap<>();

  private Api() {
  }

  /**
   * Type of API elements and its suffix conventions.
   */
  public enum ChildElementType {

    METHOD_NAME("MethodName"), // Methods in the type
    FIELD_NAME("FieldName"), // Fields in the type
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

  /**
   * @param apiDefinition
   *          The API definition class for which the {@link IApiProvider} should be returned. Must not be {@code null}.
   * @return The {@link IApiProvider} in this registry for the API definition given.
   * @throws IllegalArgumentException
   *           if the API definition is {@code null} or no provider for the given definition could be found in the
   *           registry.
   */
  public static IApiProvider getProvider(Class<? extends IApiSpecification> apiDefinition) {
    Ensure.notNull(apiDefinition);
    ensureInitialized(apiDefinition);
    synchronized (REGISTRY) {
      return Ensure.notNull(REGISTRY.get(apiDefinition), "No provider for API class '{}' found.", apiDefinition);
    }
  }

  /**
   * Registers the given {@link IApiProvider} in this registry.
   * 
   * @param apiDefinition
   *          The API definition class the given provider can handle. Must not be {@code null}.
   * @param provider
   *          The provider. Must not be {@code null}.
   * @return {@code false} if this was the first provider for this class. {@code true} if there was already a provider
   *         for that class which was replaced.
   */
  public static boolean registerProvider(Class<? extends IApiSpecification> apiDefinition, IApiProvider provider) {
    synchronized (REGISTRY) {
      removeCachedApisOf(apiDefinition);
      return REGISTRY.put(Ensure.notNull(apiDefinition), Ensure.notNull(provider)) != null;
    }
  }

  /**
   * Removes the {@link IApiProvider} registration for the given API definition class.
   * 
   * @param apiDefinition
   *          The API definition class to remove.
   * @return {@code true} if a mapping was removed, {@code false} if there was already no mapping for this class.
   */
  public static boolean unregisterProvider(Class<? extends IApiSpecification> apiDefinition) {
    synchronized (REGISTRY) {
      removeCachedApisOf(apiDefinition);
      return REGISTRY.remove(apiDefinition) != null;
    }
  }

  static void removeCachedApisOf(Class<? extends IApiSpecification> apiDefinition) {
    API_CACHE.keySet().removeIf(e -> e.getKey() == apiDefinition);
  }

  static void ensureInitialized(Class<?> classToInit) {
    try {
      Class.forName(classToInit.getName(), true, classToInit.getClassLoader());
    }
    catch (ClassNotFoundException e) {
      SdkLog.debug("Unable to locate class '{}'.", classToInit, e); // cannot happen
    }
  }

  /**
   * Gets the latest (newest) major version that is supported for the given API definition class.
   * 
   * @param api
   *          The API definition to check. Must not be {@code null}.
   * @return The newest supported major version for the given API.
   */
  public static int latestMajorVersion(Class<? extends IApiSpecification> api) {
    return latest(api).maxLevel().major();
  }

  /**
   * Gets the latest (newest) API version that is supported for the given API definition class.
   *
   * @param api
   *          The API definition for which the newest version should be returned. Must not be {@code null}.
   * @return The newest supported version for the given API.
   */
  public static <API extends IApiSpecification> API latest(Class<API> api) {
    return create(api, ApiVersion.LATEST);
  }

  /**
   * Gets the {@link ApiVersion version} of the given API in the context of the given {@link IJavaElement}.
   * 
   * @param api
   *          The API definition for which the version should be returned. Must not be {@code null}.
   * @param context
   *          The {@link IJavaElement} in which the API should be searched.
   * @return The {@link ApiVersion} of the given API in the context of the given {@link IJavaElement} or an empty
   *         {@link Optional} if the given context is {@code null} or the API could not be found in the context.
   * @throws IllegalArgumentException
   *           if the API type is {@code null} or no {@link IApiProvider} could be found for that type
   */
  public static Optional<ApiVersion> version(Class<? extends IApiSpecification> api, IJavaElement context) {
    return Optional.ofNullable(context)
        .map(IJavaElement::javaEnvironment)
        .flatMap(env -> version(api, env));
  }

  /**
   * Gets the {@link ApiVersion version} of the given API in the context of the given {@link IJavaEnvironment}.
   *
   * @param api
   *          The API definition for which the version should be returned. Must not be {@code null}.
   * @param context
   *          The {@link IJavaEnvironment} in which the API should be searched.
   * @return The {@link ApiVersion} of the given API in the context of the given {@link IJavaEnvironment} or an empty
   *         {@link Optional} if the given context is {@code null} or the API could not be found in the context.
   * @throws IllegalArgumentException
   *           if the API type is {@code null} or no {@link IApiProvider} could be found for that type
   */
  public static Optional<ApiVersion> version(Class<? extends IApiSpecification> api, IJavaEnvironment context) {
    return Optional.ofNullable(context).flatMap(ctx -> getProvider(api).version(ctx));
  }

  /**
   * Creates an API instance of the given type having the version as found in the context of the given
   * {@link IJavaElement}.
   * 
   * @param api
   *          The API definition type to return. Must not be {@code null}.
   * @param context
   *          The {@link IJavaElement} in which the API should be searched.
   * @param <API>
   *          The API type.
   * @return The API instance of the given type having the version found in the given context or an empty
   *         {@link Optional} if the given context is {@code null} or the given API could not be found in the context.
   * @throws IllegalArgumentException
   *           if the API type is {@code null}, no {@link IApiProvider} could be found for that type or the API version
   *           found in the context is not supported (version found in the context is too old).
   */
  public static <API extends IApiSpecification> Optional<API> create(Class<API> api, IJavaElement context) {
    return create(api, context == null ? null : context.javaEnvironment());
  }

  /**
   * Creates an API instance of the given type having the version as found in the context of the given
   * {@link IJavaEnvironment}.
   *
   * @param api
   *          The API definition type to return. Must not be {@code null}.
   * @param context
   *          The {@link IJavaEnvironment} in which the API should be searched.
   * @param <API>
   *          The API type.
   * @return The API instance of the given type having the version found in the given context or an empty
   *         {@link Optional} if the given context is {@code null} or the given API could not be found in the context.
   * @throws IllegalArgumentException
   *           if the API type is {@code null}, no {@link IApiProvider} could be found for that type or the API version
   *           found in the context is not supported (version found in the context is too old).
   */
  public static <API extends IApiSpecification> Optional<API> create(Class<API> api, IJavaEnvironment context) {
    return version(api, context)
        .map(version -> create(api, version));
  }

  /**
   * Creates an API instance of the given type having the given version.
   *
   * @param api
   *          The API definition type to return. Must not be {@code null}.
   * @param version
   *          The {@link ApiVersion} for which the compatible API should be retrieved. If {@code null} or
   *          {@link ApiVersion#LATEST} the latest (newest) API of that type is returned.
   * @param <API>
   *          The API type.
   * @return The API instance of the given type supporting the given version
   * @throws IllegalArgumentException
   *           if the API type is {@code null}, no {@link IApiProvider} could be found for that type or the API version
   *           is not supported (version found in the context is too old).
   */
  public static <API extends IApiSpecification> API create(Class<API> api, ApiVersion version) {
    Entry<Class<? extends IApiSpecification>, ApiVersion> key = new SimpleImmutableEntry<>(api, version);
    synchronized (REGISTRY) {
      var definition = API_CACHE.computeIfAbsent(key, Api::doCreateApi);
      return api.cast(definition);
    }
  }

  static IApiSpecification doCreateApi(Entry<Class<? extends IApiSpecification>, ApiVersion> entry) {
    var api = entry.getKey();
    var version = entry.getValue();
    var apiDefinitions = getProvider(api).knownApis();
    var definition = Ensure.notNull(ApiSpecification.create(apiDefinitions, version), "No known API supports version {}. Available APIs: {}", version, apiDefinitions);
    if (SdkLog.isDebugEnabled()) {
      SdkLog.debug("Creating API definition '{}' supporting up to level '{}' (including).", api.getSimpleName(), definition.maxLevel().asString());
    }
    return definition;
  }

  /**
   * Gets all known (supported) API version for the given API type.
   * 
   * @param api
   *          The API definition type to return. Must not be {@code null}.
   * @param <API>
   *          The API type.
   * @return A {@link Stream} with all known (supported) versions of the given API. The {@link Stream} is sorted having
   *         the latest (newest) API first.
   */
  public static <API extends IApiSpecification> Stream<API> allKnown(Class<API> api) {
    return getProvider(api)
        .knownApis().stream()
        .map(ApiVersion::requireMaxApiLevelOf)
        .sorted(Comparator.reverseOrder()) // newest version first
        .map(version -> create(api, version));
  }

  /**
   * Dumps the given {@link IApiSpecification} into a {@link Map} data structure.
   * 
   * @param api
   *          The {@link IApiSpecification} to dump. Must not be {@code null}.
   * @return The dump
   * @see #dump(ITypeNameSupplier)
   */
  public static Map<String /* fqn of all ITypeNameSupplier in the API */, Map<ChildElementType, Map<String /* method name in the ITypeNameSupplier */, String /* method value */>>> dump(IApiSpecification api) {
    return Arrays.stream(Ensure.notNull(api).getClass().getMethods())
        .filter(m -> m.getParameterCount() == 0)
        .filter(m -> ITypeNameSupplier.class.isAssignableFrom(m.getReturnType()))
        .map(m -> (ITypeNameSupplier) invoke(m, api))
        .map(Api::dump)
        .collect(toMap(Entry::getKey, Entry::getValue, (a, b) -> a /* it does not matter which one to keep */));
  }

  /**
   * Dumps the given {@link ITypeNameSupplier} into an {@link Entry} data structure.
   *
   * @param cns
   *          The {@link ITypeNameSupplier} to dump. Must not be {@code null}.
   * @return An {@link Entry} for the given {@link ITypeNameSupplier} having the fully qualified name as key and a
   *         {@link Map} of all methods in the given {@link ITypeNameSupplier} and its values grouped by
   *         {@link ChildElementType}.
   */
  public static Entry<String /* fqn of the ITypeNameSupplier */, Map<ChildElementType, Map<String /* method name in the ITypeNameSupplier */, String /* method value */>>> dump(ITypeNameSupplier cns) {
    var supplierClass = Ensure.notNull(cns).getClass();
    var methods = Arrays.stream(supplierClass.getMethods())
        .filter(m -> m.getDeclaringClass() != Object.class)
        .filter(m -> m.getParameterCount() == 0)
        .filter(m -> m.getReturnType() != void.class)
        .filter(m -> !"fqn".equals(m.getName()) && !"simpleName".equals(m.getName())) // methods defined in ITypeNameSupplier
        .collect(toList());

    Map<ChildElementType, Map<String, String>> entries = new EnumMap<>(ChildElementType.class);
    //noinspection Convert2streamapi
    for (var type : ChildElementType.values()) {
      if (type == ChildElementType.OTHER) {
        continue;
      }
      entries.put(type, consumeMethods(methods, cns, m -> m.getName().endsWith(type.toString())));
    }
    entries.put(ChildElementType.OTHER, consumeMethods(methods, cns, null)); // assign remaining methods not matching one of the previous types
    return new SimpleImmutableEntry<>(Ensure.notNull(cns).fqn(), entries);
  }

  static Map<String, String> consumeMethods(Collection<Method> methods, ITypeNameSupplier owner, Predicate<Method> filter) {
    var methodResultMapping = methods.stream()
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
