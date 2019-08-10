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
  public void testpackageToNamespace() {
    assertEquals("test.scout.eclipse.org", EmptyWsdlGenerator.packageToNamespace("Whatever", "org.eclipse.scout.test"));
    assertEquals("scout.eclipse.org", EmptyWsdlGenerator.packageToNamespace("test", "org.eclipse.scout.test"));
    assertEquals("", EmptyWsdlGenerator.packageToNamespace("test", ""));
    assertEquals("", EmptyWsdlGenerator.packageToNamespace("test", "test"));
    assertEquals("org", EmptyWsdlGenerator.packageToNamespace("test", "org"));
  }
}
