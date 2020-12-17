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
package org.eclipse.scout.sdk.core.s.util.maven;

import java.util.Optional;

import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <h3>{@link Pom}</h3>
 *
 * @since 6.1.0
 */
public final class Pom {
  private Pom() {
  }

  /**
   * Gets the artifactId of the given pom {@link Document}.
   *
   * @param pom
   *          The pom {@link Document} to search the artifactId for.
   * @return The artifactId or {@code null} if no artifactId exists in the given {@link Document}.
   */
  public static Optional<String> artifactId(Document pom) {
    if (pom == null) {
      return Optional.empty();
    }
    return Xml.firstChildElement(pom.getDocumentElement(), IMavenConstants.ARTIFACT_ID)
        .map(Element::getTextContent)
        .flatMap(Strings::notBlank);
  }

  /**
   * Gets the groupId of the given pom {@link Document}.
   *
   * @param pom
   *          The pom {@link Document} to search the groupId for.
   * @return The groupId or an empty {@link Optional} if no groupId exists in the given {@link Document}.
   */
  public static Optional<String> groupId(Document pom) {
    return getInheritedValueOfPom(pom, IMavenConstants.GROUP_ID);
  }

  /**
   * Gets the version of the given pom {@link Document}.
   *
   * @param pom
   *          The pom {@link Document} to search the version for.
   * @return The version or an empty {@link Optional} if no version exists in the given {@link Document}.
   */
  public static Optional<String> version(Document pom) {
    return getInheritedValueOfPom(pom, IMavenConstants.VERSION);
  }

  /**
   * Gets the artifactId of the parent of the given pom {@link Document}.
   *
   * @param pom
   *          The pom {@link Document} to search the parent artifactId for.
   * @return The artifactId name or an empty {@link Optional} if it does not exist.
   */
  public static Optional<String> parentArtifactId(Document pom) {
    if (pom == null) {
      return Optional.empty();
    }

    return Xml.firstChildElement(pom.getDocumentElement(), IMavenConstants.PARENT)
        .flatMap(parent -> Xml.firstChildElement(parent, IMavenConstants.ARTIFACT_ID))
        .map(Element::getTextContent)
        .flatMap(Strings::notBlank);
  }

  static Optional<String> getInheritedValueOfPom(Document pom, String tagName) {
    if (pom == null) {
      return Optional.empty();
    }
    var documentElement = pom.getDocumentElement();
    var directlySpecified = Xml.firstChildElement(documentElement, tagName)
        .map(Element::getTextContent)
        .filter(Strings::hasText);
    if (directlySpecified.isPresent()) {
      return directlySpecified;
    }

    return Xml.firstChildElement(documentElement, IMavenConstants.PARENT)
        .flatMap(parent -> Xml.firstChildElement(parent, tagName))
        .map(Element::getTextContent)
        .flatMap(Strings::notBlank);
  }
}
