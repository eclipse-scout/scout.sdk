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

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

public class ApiVersion implements Comparable<ApiVersion> {

  public static final ApiVersion LATEST = new ApiVersion(Integer.MAX_VALUE);
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

  public static Optional<ApiVersion> apiLevelOf(Class<? extends IApiSpecification> definition) {
    return Optional.ofNullable(definition)
        .map(c -> c.getAnnotation(ApiLevel.class))
        .map(ApiLevel::value)
        .map(ApiVersion::new);
  }

  public static ApiVersion requireApiLevelOf(Class<? extends IApiSpecification> definition) {
    return apiLevelOf(definition)
        .orElseThrow(() -> Ensure.newFail("{} is missing required annotation '{}'.", definition, ApiLevel.class.getName()));
  }

  public static Optional<ApiVersion> parse(CharSequence version) {
    if (Strings.isBlank(version)) {
      return Optional.empty();
    }

    Matcher matcher = VERSION_PATTERN.matcher(version);
    if (!matcher.find()) {
      return Optional.empty();
    }

    int[] segments = Stream.of(1, 3, 5)
        .map(matcher::group)
        .filter(Strings::hasText)
        .mapToInt(Integer::parseInt)
        .toArray();
    String suffix = matcher.group(6);
    return Optional.of(new ApiVersion(segments, suffix));
  }

  public String suffix() {
    return m_suffix;
  }

  public int[] segments() {
    return Arrays.copyOf(m_segments, m_segments.length);
  }

  public String asString() {
    String numberPart = IntStream.of(m_segments)
        .mapToObj(Integer::toString)
        .collect(joining("."));
    String suffix = suffix();
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

    ApiVersion that = (ApiVersion) o;
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
    int numSegments = Math.max(o.m_segments.length, m_segments.length);
    for (int i = 0; i < numSegments; i++) {
      int me = positionValue(m_segments, i);
      int other = positionValue(o.m_segments, i);
      int dif = Integer.compare(me, other);
      if (dif != 0) {
        return dif;
      }
    }
    return Comparator.nullsFirst(String::compareTo).compare(suffix(), o.suffix());
  }

  private static int positionValue(int[] arr, int index) {
    if (index >= arr.length) {
      return 0;
    }
    return arr[index];
  }
}
