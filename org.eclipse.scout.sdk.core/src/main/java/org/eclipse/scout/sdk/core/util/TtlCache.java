/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.log.SdkLog;

/**
 * Implements a simple {@link Map} like cache whose elements are removed after a certain timeout elapsed.
 * <p>
 * It optionally allows to asynchronously remove old items from the cache even if the cache is not accessed.
 * <p>
 * The cache allows {@code null} for keys and values.
 * <p>
 * This class is thread safe.
 * 
 * @param <K>
 *          The key type
 * @param <V>
 *          The value type
 */
public class TtlCache<K, V> {

  private final long m_ttl;
  private final TimeUnit m_timeUnit;
  private final ScheduledExecutorService m_executorService; // may be null
  private final Map<K, TtlCacheEntry<V>> m_cache;
  private ScheduledFuture<?> m_cleanupFuture; // may be null

  /**
   * Creates a new cache instance.
   *
   * @param ttl
   *          The ttl (time-to-live) for cached items. A ttl <= 0 means the items will stay in the cache for ever (no
   *          ttl).
   * @param timeUnit
   *          The {@link TimeUnit} of the ttl. Must not be {@code null}.
   */
  public TtlCache(long ttl, TimeUnit timeUnit) {
    this(ttl, timeUnit, null);
  }

  /**
   * Creates a new cache instance.
   * 
   * @param ttl
   *          The ttl (time-to-live) for cached items. A ttl <= 0 means the items will stay in the cache for ever (no
   *          ttl).
   * @param timeUnit
   *          The {@link TimeUnit} of the ttl. Must not be {@code null}.
   * @param executorService
   *          An optional {@link ScheduledExecutorService}. If provided it is used to asynchronously remove items whose
   *          ttl has elapsed. May be {@code null}. In that case old items are only removed on next cache access.
   */
  public TtlCache(long ttl, TimeUnit timeUnit, ScheduledExecutorService executorService) {
    m_timeUnit = Ensure.notNull(timeUnit);
    m_executorService = executorService;
    m_ttl = ttl;
    m_cache = new HashMap<>();
  }

  protected static void removeInvalidEntriesOf(Map<?, ? extends TtlCacheEntry<?>> cache) {
    cache.values().removeIf(TtlCacheEntry::elapsed);
  }

  /**
   * Gets the item with given key from the cache.
   *
   * @param key
   *          The key of the item to retrieve. May be {@code null}.
   * @return The cached item for the given key or {@code null} if there is no such item for which the ttl has not
   *         elapsed yet.
   */
  public V get(K key) {
    var element = withCacheExec(it -> it.get(key));
    if (element == null) {
      return null;
    }
    return element.m_element;
  }

  /**
   * If the specified key is not already associated with a value, computes its value using the given mapping function
   * and enters it into this cache.
   * <p>
   * If the mapping function throws an exception, the exception is rethrown, and no mapping is recorded.
   * <p>
   * The mapping function should not modify this cache during computation.
   *
   * @param key
   *          The key of the item to retrieve. May be {@code null}.
   * @param mappingFunction
   *          the mapping function to compute a value. Must not be {@code null}.
   * @return the current (existing or computed) value associated with the specified key, or {@code null} if the computed
   *         value is {@code null}.
   * @throws IllegalArgumentException
   *           if the mappingFunction is {@code null}.
   */
  public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
    Ensure.notNull(mappingFunction);
    return withCacheExec(cache -> cache.computeIfAbsent(key, k -> new TtlCacheEntry<>(mappingFunction.apply(k), getTtl(), getTimeUnit())).m_element);
  }

  /**
   * Associates the given value with the given key probably overwriting (replacing) an existing mapping for the same
   * key.
   *
   * @param key
   *          The key of the item to store. May be {@code null}.
   * @param value
   *          The value of the item to store. May be {@code null}.
   * @return the previous value associated with key, or {@code null} if there was no mapping for key.
   */
  public V put(K key, V value) {
    var previous = withCacheExec(it -> it.put(key, new TtlCacheEntry<>(value, getTtl(), getTimeUnit())));
    if (previous == null) {
      return null;
    }
    return previous.m_element;
  }

  /**
   * Removes all elements from the cache.
   */
  public void clear() {
    synchronized (m_cache) {
      m_cache.clear();
    }
  }

  /**
   * @return The time-to-live of this cache.
   */
  public long getTtl() {
    return m_ttl;
  }

  /**
   * @return The {@link TimeUnit} of the ttl (see {@link #getTtl()}).
   */
  public TimeUnit getTimeUnit() {
    return m_timeUnit;
  }

  protected <R> R withCacheExec(Function<Map<K, TtlCacheEntry<V>>, R> function) {
    synchronized (m_cache) {
      var cacheHasTtl = getTtl() > 0;
      if (cacheHasTtl) {
        removeInvalidEntriesOf(m_cache); // ensure cache is up-to-date
      }
      var result = function.apply(m_cache);
      if (cacheHasTtl && !m_cache.isEmpty()) {
        scheduleCacheCleanup();
      }
      return result;
    }
  }

  protected void scheduleCacheCleanup() {
    if (m_executorService == null) {
      return;
    }

    var cleanupFuture = m_cleanupFuture;
    if (cleanupFuture != null) {
      cleanupFuture.cancel(false);
    }
    m_cleanupFuture = m_executorService.schedule(() -> withCacheExec(this::afterScheduledCacheCleanup),
        getTimeUnit().toMillis(getTtl()) + 1, TimeUnit.MILLISECONDS);
  }

  protected Void afterScheduledCacheCleanup(Map<K, TtlCacheEntry<V>> cache) {
    SdkLog.debug("{} cleanup executed after {} {}. Remaining cached items: {}.",
        getClass().getSimpleName(), getTtl(), getTimeUnit().toString().toLowerCase(Locale.US), cache.size());
    return null;
  }

  protected static final class TtlCacheEntry<T> {
    private final T m_element;
    private final long m_validUntil;

    private TtlCacheEntry(T element, long ttl, TimeUnit timeUnit) {
      m_element = element;
      m_validUntil = System.currentTimeMillis() + timeUnit.toMillis(ttl);
    }

    boolean elapsed() {
      return System.currentTimeMillis() > m_validUntil;
    }
  }
}
