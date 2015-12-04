/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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

    Assert.assertEquals(getStringWithoutTimeComment(w1), getStringWithoutTimeComment(w2));
  }

  private static String getStringWithoutTimeComment(StringWriter w) {
    StringBuffer buffer = w.getBuffer();
    int startOfBin = buffer.indexOf("bin");
    if (startOfBin >= 0) {
      buffer.replace(0, startOfBin, "");
    }
    return buffer.toString();
  }
}
