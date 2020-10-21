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
package org.eclipse.scout.sdk.core.apidef;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.fixture.apidef.ICustomApi;
import org.eclipse.scout.sdk.core.fixture.apidef.IJavaApi;
import org.eclipse.scout.sdk.core.fixture.apidef.Java11Api;
import org.eclipse.scout.sdk.core.fixture.apidef.Java13Api;
import org.eclipse.scout.sdk.core.fixture.apidef.Java8Api;
import org.junit.jupiter.api.Test;

public class ApiSpecificationTest {

  @Test
  public void testApiComposition() {
    assertEquals("13", createFixtureApiDefinition(13).level().asString());
    assertEquals("11", createFixtureApiDefinition(11, 2, 14).level().asString());
    assertEquals("11", createFixtureApiDefinition(11, 0, 12).level().asString());
    assertNull(createFixtureApiDefinition(4, 0, 12));
  }

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsWithItself"})
  public void testHashCodeEqualsToString() {
    var apiDefinition1 = createFixtureApiDefinition(11, 4, 44);
    var apiDefinition2 = createFixtureApiDefinition(11, 4, 44);
    assertEquals(ApiSpecification.class.getSimpleName() + " [version=11, class=" + Java11Api.class.getName() + "]", apiDefinition1.toString());
    assertNotEquals(apiDefinition1.hashCode(), apiDefinition2.hashCode());
    assertTrue(apiDefinition1.equals(apiDefinition1));
    assertFalse(apiDefinition1.equals(apiDefinition2));
  }

  @Test
  public void testMissingMethodImplementation() {
    assertThrows(IllegalArgumentException.class, () -> createFixtureApiDefinition(11, 9, 0).requireApi(IJavaApi.class).unimplemented());
    assertThrows(IllegalArgumentException.class, () -> createFixtureApiDefinition(8).requireApi(ICustomApi.class));
  }

  @Test
  public void testOptionalApi() {
    var api8 = createFixtureApiDefinition(10);
    assertEquals("8", api8.level().asString());
    assertFalse(api8.api(ICustomApi.class).isPresent());

    var api11 = createFixtureApiDefinition(11, 9, 0).requireApi(ICustomApi.class);
    assertEquals(Java11Api.INT, api11.customMethod());

    var api13 = createFixtureApiDefinition(14).requireApi(ICustomApi.class);
    assertEquals("13", api13.level().asString());
    assertEquals(Java13Api.INT, api13.customMethod());
  }

  @Test
  public void testVersionOverwrite() {
    var api13 = createFixtureApiDefinition(13).requireApi(IJavaApi.class);
    assertEquals("13", api13.level().asString());

    var api11 = createFixtureApiDefinition(11, 4, 3).requireApi(IJavaApi.class);
    assertEquals("11", api11.level().asString());

    assertEquals(Java11Api.VALUE, api13.method());
    assertEquals(Java11Api.VALUE, api11.method());
  }

  protected static IApiSpecification createFixtureApiDefinition(int... version) {
    return ApiSpecification.create(asList(Java8Api.class, Java11Api.class, Java13Api.class), new ApiVersion(version));
  }
}
