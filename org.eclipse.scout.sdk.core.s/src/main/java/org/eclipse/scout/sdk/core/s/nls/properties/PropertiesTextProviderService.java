/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls.properties;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.JavaUtils;
import org.eclipse.scout.sdk.core.java.model.api.IMethod;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutAbstractApi.AbstractDynamicNlsTextProviderService;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.nls.TextProviderService;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SourceRange;

/**
 * <h3>{@link PropertiesTextProviderService}</h3>
 * <p>
 * Text provider service that extends {@link IScoutApi#AbstractDynamicNlsTextProviderService()}.
 *
 * @since 7.0.0
 */
public class PropertiesTextProviderService extends TextProviderService {
  protected static final Pattern REGEX_RESOURCE_BUNDLE_GETTER = Pattern.compile("return\\s*\"([^\"]*)\"\\s*;", Pattern.DOTALL);
  public static final char FOLDER_SEGMENT_DELIMITER = '/';

  private final String m_folder;
  private final String m_filePrefix;

  protected PropertiesTextProviderService(IType txtSvc, String folder, String filePrefix) {
    super(txtSvc);
    m_folder = Ensure.notNull(folder);
    m_filePrefix = Ensure.notNull(filePrefix);
  }

  /**
   * Tries to create a {@link PropertiesTextProviderService} from the specified {@link IType}.
   *
   * @param txtSvc
   *          The {@link IType} to parse. Must not be {@code null}.
   * @return The parsed {@link PropertiesTextProviderService} or an empty {@link Optional} if it cannot be parsed.
   */
  public static Optional<PropertiesTextProviderService> create(IType txtSvc) {
    return Ensure.notNull(txtSvc).javaEnvironment().api(IScoutApi.class)
        .map(IScoutApi::AbstractDynamicNlsTextProviderService)
        .map(AbstractDynamicNlsTextProviderService::getDynamicNlsBaseNameMethodName)
        .flatMap(getDynamicNlsBaseName -> create(txtSvc, getDynamicNlsBaseName));
  }

  protected static Optional<PropertiesTextProviderService> create(IType txtSvc, CharSequence getDynamicNlsBaseName) {
    return txtSvc
        .methods()
        .withMethodIdentifier(JavaTypes.createMethodIdentifier(getDynamicNlsBaseName, null))
        .withSuperClasses(true)
        .first()
        .flatMap(IMethod::sourceOfBody)
        .map(SourceRange::asCharSequence)
        .map(JavaUtils::removeComments)
        .map(REGEX_RESOURCE_BUNDLE_GETTER::matcher)
        .filter(Matcher::find)
        .map(m -> m.group(1))
        .map(ISdkConstants.REGEX_DOT::split)
        .flatMap(segments -> fromSegments(segments, txtSvc));
  }

  protected static Optional<PropertiesTextProviderService> fromSegments(String[] segments, IType txtSvc) {
    if (segments.length < 1) {
      return Optional.empty();
    }
    var filePrefix = segments[segments.length - 1]; // last segment

    var folderName = "";
    if (segments.length > 1) {
      folderName = Arrays.stream(segments, 0, segments.length - 1)
          .collect(joining(String.valueOf(FOLDER_SEGMENT_DELIMITER)));
    }
    return Optional.of(new PropertiesTextProviderService(txtSvc, folderName, filePrefix));
  }

  /**
   * @return The module relative folder name without leading and ending folder delimiter.
   */
  public String folder() {
    return m_folder;
  }

  /**
   * @return The {@code .properties} file name prefix.
   */
  public String filePrefix() {
    return m_filePrefix;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), m_folder, m_filePrefix);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    var that = (PropertiesTextProviderService) o;
    return m_folder.equals(that.m_folder) &&
        m_filePrefix.equals(that.m_filePrefix);
  }

  @Override
  public String toString() {
    return super.toString() + " for " + m_filePrefix + "*.properties files in " + m_folder;
  }
}
