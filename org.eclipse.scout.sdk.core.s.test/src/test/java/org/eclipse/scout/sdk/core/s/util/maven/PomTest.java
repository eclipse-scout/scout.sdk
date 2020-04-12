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
package org.eclipse.scout.sdk.core.s.util.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.eclipse.scout.sdk.core.util.Xml;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link PomTest}</h3>
 *
 * @since 6.1.0
 */
public class PomTest {

  @Test
  public void testGetArtifactIdOfPom() throws IOException {
    assertNull(Pom.artifactId(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project></project>")).orElse(null));
    assertNull(Pom.artifactId(null).orElse(null));
    assertEquals("testle", Pom.artifactId(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><artifactId>testle</artifactId></project>")).orElse(null));
    assertNull(Pom.artifactId(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><artifactId></artifactId></project>")).orElse(null));
  }

  @Test
  public void testGroupId() throws IOException {
    assertNull(Pom.groupId(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project></project>")).orElse(null));
    assertNull(Pom.groupId(null).orElse(null));
    assertEquals("testle", Pom.groupId(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><groupId>testle</groupId></project>")).orElse(null));
    assertEquals("testle", Pom.groupId(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><groupId>whatever</groupId></parent><groupId>testle</groupId></project>")).orElse(null));
    assertEquals("testle", Pom.groupId(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><groupId>testle</groupId></parent><groupIdA>whatever</groupIdA></project>")).orElse(null));
    assertNull(Pom.groupId(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><groupIdA>testle</groupIdA></parent><groupIdA>whatever</groupIdA></project>")).orElse(null));
  }

  @Test
  public void testVersion() throws IOException {
    assertNull(Pom.version(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project></project>")).orElse(null));
    assertNull(Pom.version(null).orElse(null));
    assertEquals("testle", Pom.version(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><version>testle</version></project>")).orElse(null));
    assertEquals("testle", Pom.version(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><version>whatever</version></parent><version>testle</version></project>")).orElse(null));
    assertEquals("testle", Pom.version(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><version>testle</version></parent><versionA>whatever</versionA></project>")).orElse(null));
    assertNull(Pom.version(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><versionA>testle</versionA></parent><versionA>whatever</versionA></project>")).orElse(null));
  }

  @Test
  public void testParentArtifactId() throws IOException {
    assertNull(Pom.parentArtifactId(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project></project>")).orElse(null));
    assertNull(Pom.parentArtifactId(null).orElse(null));
    assertNull(Pom.parentArtifactId(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent></parent><version>testle</version></project>")).orElse(null));
    assertEquals("testle", Pom.parentArtifactId(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><artifactId>testle</artifactId></parent></project>")).orElse(null));
    assertNull(Pom.parentArtifactId(Xml.get("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><artifactIdA>testle</artifactIdA></parent><artifactId>testle</artifactId></project>")).orElse(null));
  }
}
