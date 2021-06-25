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
package org.eclipse.scout.sdk.core.apidef;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * Represents an API version consisting of up to three integers followed by an optional {@link String} suffix.
 */
public class ApiVersion implements Comparable<ApiVersion> {

  /**
   * {@link ApiVersion} indicating the latest (newest) version of an API.
   */
  public static final ApiVersion LATEST = new ApiVersion(Integer.MAX_VALUE);

  /**
   * Regex of a valid API pattern.
   */
  public static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)(\\.(\\d*))?(\\.(\\d*))?(-.*)?");

  private final int[] m_segments;
  private final String m_suffix;

  public ApiVersion(int... version) {
    this(version, null);
  }

  public ApiVersion(String suffix, int... version) {
    this(version, suffix);
  }

  public ApiVersion(int[] version, String suffix) {
    Ensure.isTrue(Ensure.notNull(version).length > 0);
    m_segments = Arrays.copyOf(version, version.length);
    m_suffix = suffix;
  }

  /**
   * Parses the value of an {@link MaxApiLevel} annotation on the given class.
   * 
   * @param clazz
   *          The class whose {@link MaxApiLevel} annotation should be parsed.
   * @return An {@link Optional} with the {@link ApiVersion} of the given class or an empty {@link Optional} if the
   *         given class is {@code null} or the annotation does not exist.
   * @see #requireMaxApiLevelOf(Class)
   */
  public static Optional<ApiVersion> maxApiLevelOf(Class<?> clazz) {
    return Optional.ofNullable(clazz)
        .map(c -> c.getAnnotation(MaxApiLevel.class))
        .map(MaxApiLevel::value)
        .map(ApiVersion::new);
  }

  /**
   * Parses the value of an {@link MaxApiLevel} annotation on the given class. This method fails if the given class does
   * not have an {@link MaxApiLevel} annotation.
   *
   * @param clazz
   *          The class whose {@link MaxApiLevel} annotation should be parsed.
   * @return The {@link ApiVersion} value of the annotation.
   * @throws IllegalArgumentException
   *           if the clazz is {@code null} or the clazz does not have the {@link MaxApiLevel} annotation.
   * @see #maxApiLevelOf(Class)
   */
  public static ApiVersion requireMaxApiLevelOf(Class<?> clazz) {
    return maxApiLevelOf(clazz)
        .orElseThrow(() -> Ensure.newFail("{} is missing required annotation '{}'.", clazz, MaxApiLevel.class.getName()));
  }

  /**
   * Parses the given {@link CharSequence} into an {@link ApiVersion}.
   * 
   * @param version
   *          The {@link CharSequence} to parse. It must fulfill {@link #VERSION_PATTERN} to be successfully parsed.
   * @return The {@link ApiVersion} or an empty {@link Optional} if the given {@link CharSequence} cannot be parsed.
   */
  public static Optional<ApiVersion> parse(CharSequence version) {
    if (Strings.isBlank(version)) {
      return Optional.empty();
    }

    var matcher = VERSION_PATTERN.matcher(version);
    if (!matcher.find()) {
      return Optional.empty();
    }

    var segments = Stream.of(1, 3, 5)
        .map(matcher::group)
        .filter(Strings::hasText)
        .mapToInt(Integer::parseInt)
        .toArray();
    var suffix = matcher.group(6);
    return Optional.of(new ApiVersion(segments, suffix));
  }

  /**
   * @return The suffix {@link String} or {@code null} if this {@link ApiVersion} has no suffix. The suffix is the whole
   *         part after the maximal three number sections (e.g. {@code "-SNAPSHOT"}).
   */
  public String suffix() {
    return m_suffix;
  }

  /**
   * @return A copy of all number segments of the version. Never returns {@code null}.
   */
  public int[] segments() {
    return Arrays.copyOf(m_segments, m_segments.length);
  }

  /**
   * @return A {@link String} representation of the version. Never returns {@code null}.
   */
  public String asString() {
    var numberPart = Arrays.stream(m_segments)
        .mapToObj(Integer::toString)
        .collect(joining("."));
    var suffix = suffix();
    if (Strings.isEmpty(suffix)) {
      return numberPart;
    }
    return numberPart + suffix;
  }

  @Override
  public String toString() {
    return ApiVersion.class.getSimpleName() + " " + asString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    var that = (ApiVersion) o;
    return Arrays.equals(m_segments, that.m_segments)
        && Objects.equals(m_suffix, that.m_suffix);
  }

  @Override
  public int hashCode() {
    return 31 * Arrays.hashCode(m_segments)
        + (m_suffix != null ? m_suffix.hashCode() : 0);
  }

  @Override
  public int compareTo(ApiVersion o) {
    if (o == null) {
      return 1;
    }
    if (this == o) {
      return 0;
    }

    // also compare missing segments. missing segments are treated as zero
    var numSegments = Math.max(o.m_segments.length, m_segments.length);
    for (var i = 0; i < numSegments; i++) {
      var me = positionValue(m_segments, i);
      var other = positionValue(o.m_segments, i);
      var dif = Integer.compare(me, other);
      if (dif != 0) {
        return dif;
      }
    }
    return Comparator.nullsLast(String::compareTo).compare(suffix(), o.suffix());
  }

  private static int positionValue(int[] arr, int index) {
    if (index >= arr.length) {
      return Integer.MAX_VALUE;
    }
    return arr[index];
  }
}
