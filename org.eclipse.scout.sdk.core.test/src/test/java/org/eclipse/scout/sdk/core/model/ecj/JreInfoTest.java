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
package org.eclipse.scout.sdk.core.model.ecj;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link JreInfoTest}</h3>
 *
 * @since 9.0.0
 */
public class JreInfoTest {

  @Test
  public void testParseVersion() {
    var java11Content = "IMPLEMENTOR=\"Oracle Corporation\"\n" +
        "IMPLEMENTOR_VERSION=\"18.9\"\n" +
        "JAVA_VERSION=\"11.0.1\"\n" +
        "JAVA_VERSION_DATE=\"2018-10-16\"\n" +
        "MODULES=\"java.base java.compiler\"\n" +
        "OS_ARCH=\"x86_64\"\n" +
        "OS_NAME=\"Windows\"\n" +
        "SOURCE=\".:8513ac27b651\"\n";
    var java10Content = "IMPLEMENTOR=\"Oracle Corporation\"\n" +
        "IMPLEMENTOR_VERSION=\"18.3\"\n" +
        "JAVA_VERSION=\"10.0.2\"\n" +
        "JAVA_VERSION_DATE=\"2018-07-17\"\n" +
        "MODULES=\"java.base java.datatransfer\"\n" +
        "OS_ARCH=\"x86_64\"\n" +
        "OS_NAME=\"Windows\"\n" +
        "SOURCE=\".:45b1d041a4ef\"\n";
    var java9Content = "IMPLEMENTOR=\"N/A\"\n" +
        "JAVA_VERSION=\"9\"\n" +
        "MODULES=\"java.base java.datatransfer\"\n" +
        "OS_ARCH=\"x86_64\"\n" +
        "OS_NAME=\"Windows\"\n" +
        "SOURCE=\"\"\n";
    var java8Content = "JAVA_VERSION=\"1.8.0_45\"\n" +
        "OS_NAME=\"Windows\"\n" +
        "OS_VERSION=\"5.1\"\n" +
        "OS_ARCH=\"i586\"\n" +
        "SOURCE=\" .:15b679d327da corba:50fb9bed64c9\"\n" +
        "BUILD_TYPE=\"commercial\"\n";
    var java122Content = "JAVA_VERSION=\"12.2.0_45\"\n" +
        "OS_NAME=\"Windows\"\n" +
        "OS_VERSION=\"5.1\"\n" +
        "OS_ARCH=\"i586\"\n" +
        "SOURCE=\" .:15b679d327da corba:50fb9bed64c9\"\n" +
        "BUILD_TYPE=\"commercial\"\n";

    assertIsJavaVersion("11", java11Content);
    assertIsJavaVersion("10", java10Content);
    assertIsJavaVersion("9", java9Content);
    assertIsJavaVersion("1.8", java8Content);
    assertIsJavaVersion("12.2", java122Content);
  }

  protected static void assertIsJavaVersion(String expectedJavaVersion, String fileContent) {
    var version = JreInfo.parseVersion(asList(Pattern.compile("\\n").split(fileContent)));
    assertEquals(expectedJavaVersion, version);
  }
}
