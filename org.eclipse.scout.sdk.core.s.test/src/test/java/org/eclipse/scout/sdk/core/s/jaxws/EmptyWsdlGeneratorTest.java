/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.jaxws;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link EmptyWsdlGeneratorTest}</h3>
 *
 * @since 7.0.0
 */
public class EmptyWsdlGeneratorTest {
  @Test
  public void testPackageToNamespace() {
    assertEquals("test.scout.eclipse.org", EmptyWsdlGenerator.packageToNamespace("Whatever", "org.eclipse.scout.test"));
    assertEquals("scout.eclipse.org", EmptyWsdlGenerator.packageToNamespace("test", "org.eclipse.scout.test"));
    assertEquals("", EmptyWsdlGenerator.packageToNamespace("test", ""));
    assertEquals("", EmptyWsdlGenerator.packageToNamespace("test", "test"));
    assertEquals("org", EmptyWsdlGenerator.packageToNamespace("test", "org"));
  }
}
