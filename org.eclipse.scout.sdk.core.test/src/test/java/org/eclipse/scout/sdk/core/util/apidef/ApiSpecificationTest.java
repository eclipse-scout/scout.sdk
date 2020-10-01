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
package org.eclipse.scout.sdk.core.util.apidef;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.fixture.apidef.ICustomApi;
import org.eclipse.scout.sdk.core.fixture.apidef.IJavaApi;
import org.eclipse.scout.sdk.core.fixture.apidef.JavaApi11;
import org.eclipse.scout.sdk.core.fixture.apidef.JavaApi13;
import org.eclipse.scout.sdk.core.fixture.apidef.JavaApi8;
import org.junit.jupiter.api.Test;

public class ApiSpecificationTest {

  @Test
  public void testApiComposition() {
    assertEquals("13", createFixtureApiDefinition(13).level().asString());
    assertEquals("11", createFixtureApiDefinition(11, 2, 14).level().asString());
    assertEquals("11", createFixtureApiDefinition(11, 0, 12).level().asString());
    assertEquals(new ApiVersion(11, 4, 5), createFixtureApiDefinition(11, 4, 5).version().get());
    assertNull(createFixtureApiDefinition(4, 0, 12));
  }

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsWithItself"})
  public void testHashCodeEqualsToString() {
    IApiSpecification apiDefinition1 = createFixtureApiDefinition(11, 4, 44);
    IApiSpecification apiDefinition2 = createFixtureApiDefinition(11, 4, 44);
    assertEquals(ApiSpecification.class.getSimpleName() + " [version=11, class=" + JavaApi11.class.getName() + "]", apiDefinition1.toString());
    assertNotEquals(apiDefinition1.hashCode(), apiDefinition2.hashCode());
    assertTrue(apiDefinition1.equals(apiDefinition1));
    assertFalse(apiDefinition1.equals(apiDefinition2));
  }

  @Test
  public void testMissingMethodImplementation() {
    assertThrows(IllegalArgumentException.class, () -> createFixtureApiDefinition(11, 9, 0).api(IJavaApi.class).unimplemented());
    assertThrows(IllegalArgumentException.class, () -> createFixtureApiDefinition(8).api(ICustomApi.class));
  }

  @Test
  public void testOptionalApi() {
    IApiSpecification api8 = createFixtureApiDefinition(10);
    assertEquals("8", api8.level().asString());
    assertFalse(api8.optApi(ICustomApi.class).isPresent());

    ICustomApi api11 = createFixtureApiDefinition(11, 9, 0).api(ICustomApi.class);
    assertEquals(JavaApi11.INT, api11.customMethod());

    ICustomApi api13 = createFixtureApiDefinition(14).api(ICustomApi.class);
    assertEquals("13", api13.level().asString());
    assertEquals(JavaApi13.INT, api13.customMethod());
  }

  @Test
  public void testVersionOverwrite() {
    IJavaApi api13 = createFixtureApiDefinition(13).api(IJavaApi.class);
    assertEquals("13", api13.level().asString());

    IJavaApi api11 = createFixtureApiDefinition(11, 4, 3).api(IJavaApi.class);
    assertEquals("11", api11.level().asString());

    assertEquals(JavaApi11.VALUE, api13.method());
    assertEquals(JavaApi11.VALUE, api11.method());
  }

  protected static IApiSpecification createFixtureApiDefinition(int... version) {
    return ApiSpecification.create(asList(JavaApi8.class, JavaApi11.class, JavaApi13.class), new ApiVersion(version));
  }
}
