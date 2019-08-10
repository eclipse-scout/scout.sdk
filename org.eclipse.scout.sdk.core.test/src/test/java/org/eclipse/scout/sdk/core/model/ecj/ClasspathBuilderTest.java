/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link ClasspathBuilderTest}</h3>
 *
 * @since 7.0.100
 */
public class ClasspathBuilderTest {

  @Test
  public void testWithRunningJre() {
    assertValid(new ClasspathBuilder(null /* use running */, createFixtureEntries()));
  }

  @Test
  public void testWithExplicitJre() {
    assertValid(new ClasspathBuilder(Util.getJavaHome().toPath(), createFixtureEntries()));
  }

  private static Collection<? extends ClasspathEntry> createFixtureEntries() {
    int numDuplicates = 2;
    Collection<ClasspathEntry> fixtureWithDuplicate = new ArrayList<>(numDuplicates);
    String tmpDir = System.getProperty("java.io.tmpdir");
    for (int i = 0; i < numDuplicates; i++) {
      fixtureWithDuplicate.add(new ClasspathEntry(Paths.get(tmpDir), ClasspathSpi.MODE_BINARY, null));
    }
    fixtureWithDuplicate.add(new ClasspathEntry(Paths.get(tmpDir).resolve("scoutSdkNotExistingDir__"), ClasspathSpi.MODE_SOURCE, null));
    return fixtureWithDuplicate;
  }

  private static void assertValid(ClasspathBuilder b) {
    assertEquals(1, b.userClasspathEntries().size());
    assertFalse(b.bootClasspath().isEmpty());
    assertEquals(b.fullClasspath().size(), b.bootClasspath().size() + 1);
    assertEquals(Util.getJavaHome().toPath(), b.jreInfo().jreHome());
    assertEquals(1, b.userClasspath().size());
  }
}
