/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.apidef;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.java.fixture.apidef.ICustomApi;
import org.eclipse.scout.sdk.core.java.fixture.apidef.IJavaApi;
import org.eclipse.scout.sdk.core.java.fixture.apidef.Java11Api;
import org.eclipse.scout.sdk.core.java.fixture.apidef.Java13Api;
import org.eclipse.scout.sdk.core.java.fixture.apidef.Java8Api;
import org.junit.jupiter.api.Test;

public class ApiSpecificationTest {

  @Test
  public void testApiComposition() {
    assertEquals("8", createFixtureApiDefinition(8).method());
    assertEquals("8", createFixtureApiDefinition(8, 4).method());
    assertEquals("13", createFixtureApiDefinition(13).maxLevel().asString());
    assertEquals("11", createFixtureApiDefinition(11, 2, 14).method());
    assertEquals("11", createFixtureApiDefinition(11, 0, 12).method());

    // for old unsupported releases: use the oldest api available (better use the "best" match instead of throwing an exception about unsupported api but the result might even be correct).
    assertEquals("8", createFixtureApiDefinition(4, 0, 12).method());
  }

  @Test
  public void testApiCompositionWithSnapshots() {
    var snapshotSuffix = "-SNAPSHOT";
    assertEquals("3", createTestApiDefinition(new ApiVersion(null, 3)).myVersion());
    assertEquals("3", createTestApiDefinition(new ApiVersion(null, 3, 1)).myVersion());
    assertEquals("3", createTestApiDefinition(new ApiVersion(snapshotSuffix, 3, 1)).myVersion());

    assertEquals("4", createTestApiDefinition(new ApiVersion(null, 4, 4)).myVersion());
    assertEquals("401", createTestApiDefinition(new ApiVersion(null, 4, 3)).myVersion());
    assertEquals("401", createTestApiDefinition(new ApiVersion(null, 4, 2)).myVersion());
    assertEquals("4", createTestApiDefinition(new ApiVersion(snapshotSuffix, 4, 4)).myVersion());
    assertEquals("401", createTestApiDefinition(new ApiVersion(snapshotSuffix, 4, 3)).myVersion()); // important so that for an RT 4.3-SNAPSHOT the 4.3 API is used
    assertEquals("401", createTestApiDefinition(new ApiVersion(snapshotSuffix, 4, 2)).myVersion());

    assertEquals("4", createTestApiDefinition(new ApiVersion(null, 4)).myVersion());
    assertEquals("401", createTestApiDefinition(new ApiVersion(null, 4, 1)).myVersion());
    assertEquals("401", createTestApiDefinition(new ApiVersion(snapshotSuffix, 4, 1)).myVersion());
  }

  @Test
  @SuppressWarnings({"SimplifiableJUnitAssertion", "EqualsWithItself"})
  public void testHashCodeEqualsToString() {
    var apiDefinition1 = createFixtureApiDefinition(11, 4, 44);
    var apiDefinition2 = createFixtureApiDefinition(11, 4, 44);
    assertEquals(ApiSpecification.class.getSimpleName() + " [maxLevel=12, class=" + Java11Api.class.getName() + "]", apiDefinition1.toString());
    assertNotEquals(apiDefinition1.hashCode(), apiDefinition2.hashCode());
    assertTrue(apiDefinition1.equals(apiDefinition1));
    assertFalse(apiDefinition1.equals(apiDefinition2));
  }

  @Test
  public void testMissingMethodImplementation() {
    assertThrows(IllegalArgumentException.class, () -> createFixtureApiDefinition(11, 9, 0).unimplemented());
    assertThrows(IllegalArgumentException.class, () -> createFixtureApiDefinition(8).requireApi(ICustomApi.class));
  }

  @Test
  public void testOptionalApi() {
    var api8 = createFixtureApiDefinition(10);
    assertEquals("8", api8.method());
    assertFalse(api8.api(ICustomApi.class).isPresent());

    var api11 = createFixtureApiDefinition(11, 9, 0).requireApi(ICustomApi.class);
    assertEquals(Java11Api.INT, api11.customMethod());

    var api13 = createFixtureApiDefinition(14).requireApi(ICustomApi.class);
    assertEquals(Java13Api.INT, api13.customMethod());
  }

  @Test
  public void testVersionOverwrite() {
    var api8 = createFixtureApiDefinition(8);
    var api84 = createFixtureApiDefinition(8, 4);
    var api13 = createFixtureApiDefinition(13);
    var api11 = createFixtureApiDefinition(11, 4, 3);
    var api17 = createFixtureApiDefinition(17);

    assertEquals(Java8Api.VALUE, api8.method());
    assertEquals(Java8Api.VALUE, api84.method());
    assertEquals(Java11Api.VALUE, api13.method());
    assertEquals(Java11Api.VALUE, api11.method());
    assertEquals(Java11Api.VALUE, api17.method());
  }

  @Test
  public void testMethodsOnNestedApis() {
    var testApi = createTestApiDefinition(new ApiVersion(null, 4));

    assertEquals("someFunction4", testApi.someFunction());
    assertEquals("someOtherFunction4", testApi.someOtherFunction());

    assertEquals("someFunction3", testApi.requireApi(TestApi401.class).someFunction());
    assertEquals("someOtherFunction4", testApi.requireApi(TestApi401.class).someOtherFunction());

    assertEquals("someFunction3", testApi.requireApi(TestApi3.class).someFunction());
    assertThrows(IllegalArgumentException.class, () -> testApi.requireApi(TestApi3.class).someOtherFunction());
  }

  protected static IJavaApi createFixtureApiDefinition(int... version) {
    var spec = ApiSpecification.create(asList(Java8Api.class, Java11Api.class, Java13Api.class), new ApiVersion(version));
    assertNotNull(spec);
    return spec.requireApi(IJavaApi.class);
  }

  protected static ITestApi createTestApiDefinition(ApiVersion v) {
    var spec = ApiSpecification.create(asList(TestApi3.class, TestApi401.class, TestApi4.class), v);
    assertNotNull(spec);
    return spec.requireApi(ITestApi.class);
  }

  public interface ITestApi extends IApiSpecification {
    String myVersion();

    String someFunction();

    String someOtherFunction();
  }

  @MaxApiLevel(3)
  public interface TestApi3 extends ITestApi {
    @Override
    default String myVersion() {
      return "3";
    }

    @Override
    default String someFunction() {
      return "someFunction3";
    }
  }

  @MaxApiLevel({4, 3})
  public interface TestApi401 extends ITestApi {
    @Override
    default String myVersion() {
      return "401";
    }

    @Override
    default String someOtherFunction() {
      return "someOtherFunction4";
    }
  }

  @MaxApiLevel(4)
  public interface TestApi4 extends ITestApi {
    @Override
    default String myVersion() {
      return "4";
    }

    @Override
    default String someFunction() {
      return "someFunction4";
    }
  }
}
