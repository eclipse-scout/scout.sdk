/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.sdk.core.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.testing.JavaEnvironmentBuilder;
import org.junit.Assert;
import org.junit.Test;

public class JavaEnvironmentImportExportTest {

  @Test
  public void testInMemory() throws IOException {
    IJavaEnvironment env = new JavaEnvironmentBuilder()
        .withoutScoutSdk()
        .without(".*" + Pattern.quote("/target/generated-sources/annotations") + ".*")
        .build();

    StringWriter w1 = new StringWriter();
    CoreTestingUtils.exportJavaEnvironment(env, w1);

    env = CoreTestingUtils.importJavaEnvironment(new StringReader(w1.toString()));

    StringWriter w2 = new StringWriter();
    CoreTestingUtils.exportJavaEnvironment(env, w2);

    Assert.assertEquals(w1.toString(), w2.toString());
  }

}
