/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.scout.sdk.core.util.Xml;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

/**
 * Test to generate pom dependencies of a given groupId.
 * Note: Only the artifacts having the newest versions are printed. Artifacts of older releases (e.g. if there was no update in the newest Eclipse release) are skipped.
 *
 * @noinspection UseOfSystemOutOrSystemErr
 */
@Disabled("only executed manually for an Eclipse platform dependency update")
public class EclipsePlatformDependencyGeneratorTest {
  @Test
  public void testGenerateDependencies() throws URISyntaxException, IOException, XPathExpressionException {
    createDependenciesForGroupId("org.eclipse.platform");
    createDependenciesForGroupId("org.eclipse.jdt");
    createDependenciesForGroupId("com.ibm.icu");
    createDependenciesForGroupId("org.eclipse.emf");
  }

  private static void createDependenciesForGroupId(String groupId) throws URISyntaxException, IOException, XPathExpressionException {
    var uri = new URI("https://central.sonatype.com/solrsearch/select?q=g:" + groupId + "&core=gav&rows=500&wt=xml&sort=version+desc");
    var dom = Xml.get(uri);
    //    System.out.println(Xml.writeDocument(dom, true).toString());
    var newestRelease = Xml.evaluateXPath("response/docs/docs/timestamp", dom.getDocumentElement()).stream()
        .map(Node::getTextContent)
        .distinct()
        .mapToLong(Long::parseLong)
        .max().orElseThrow();
    var artifactVersions = Xml.evaluateXPath("response/docs/docs", dom.getDocumentElement()).stream()
        .filter(n -> isNewestRelease(n, newestRelease))
        .collect(Collectors.toMap(EclipsePlatformDependencyGeneratorTest::getArtifactId, EclipsePlatformDependencyGeneratorTest::getLatestVersion));
    artifactVersions.entrySet().stream()
        .sorted(Entry.comparingByKey())
        .forEach(e -> printAsDependency(groupId, e.getKey(), e.getValue()));
  }

  private static void printAsDependency(String groupId, String artifactId, String version) {
    var src = """
              <dependency>
                <groupId>%s</groupId>
                <artifactId>%s</artifactId>
                <version>%s</version>
              </dependency>
        """.formatted(groupId, artifactId, version);
    System.out.print(src);
  }

  private static String getArtifactId(Node node) {
    return Xml.childElementsWithTagName(node, "a").getFirst().getTextContent();
  }

  private static String getLatestVersion(Node node) {
    return Xml.childElementsWithTagName(node, "latestVersion").getFirst().getTextContent();
  }

  private static boolean isNewestRelease(Node node, long newestRelease) {
    var ts = Xml.childElementsWithTagName(node, "timestamp").getFirst().getTextContent();
    return Long.parseLong(ts) == newestRelease;
  }
}
