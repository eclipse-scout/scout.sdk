/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.nls.properties;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.nls.TextProviderService;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link PropertiesTextProviderService}</h3>
 * <p>
 * Text provider service that extends {@link IScoutRuntimeTypes#AbstractDynamicNlsTextProviderService}.
 *
 * @since 7.0.0
 */
public class PropertiesTextProviderService extends TextProviderService {
  protected static final Pattern REGEX_RESOURCE_BUNDLE_GETTER = Pattern.compile("return\\s*\"([^\"]*)\"\\s*;", Pattern.DOTALL);
  protected static final Pattern REGEX_DOT = Pattern.compile("\\.");
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
    return Ensure.notNull(txtSvc)
        .methods()
        .withMethodIdentifier(JavaTypes.createMethodIdentifier("getDynamicNlsBaseName", null))
        .withSuperClasses(true)
        .first()
        .flatMap(IMethod::sourceOfBody)
        .map(ISourceRange::asCharSequence)
        .map(CoreUtils::removeComments)
        .map(REGEX_RESOURCE_BUNDLE_GETTER::matcher)
        .filter(Matcher::find)
        .map(m -> m.group(1))
        .map(REGEX_DOT::split)
        .flatMap(segments -> fromSegments(segments, txtSvc));
  }

  protected static Optional<PropertiesTextProviderService> fromSegments(String[] segments, IType txtSvc) {
    if (segments.length < 1) {
      return Optional.empty();
    }
    String filePrefix = segments[segments.length - 1]; // last segment

    StringBuilder folderName = new StringBuilder();
    if (segments.length > 1) {
      folderName.append(segments[0]);
      for (int i = 1; i < segments.length - 1; i++) {
        folderName.append(FOLDER_SEGMENT_DELIMITER).append(segments[i]);
      }
    }
    return Optional.of(new PropertiesTextProviderService(txtSvc, folderName.toString(), filePrefix));
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
    int prime = 31;
    int result = super.hashCode();
    result = prime * result + m_filePrefix.hashCode();
    result = prime * result + m_folder.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }

    PropertiesTextProviderService other = (PropertiesTextProviderService) obj;
    return m_folder.equals(other.m_folder)
        && m_filePrefix.equals(other.m_filePrefix);
  }

  @Override
  public String toString() {
    return super.toString() + " for " + m_filePrefix + "*.properties files in " + m_folder;
  }
}