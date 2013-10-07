/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.test.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.FormatPreservingProperties;
import org.junit.Test;

/**
 * <h3>{@link FormatPreservingPropertiesTest}</h3>
 * 
 * @author mvi
 * @since 3.10.0 05.10.2013
 */
public class FormatPreservingPropertiesTest {

  @Test
  public void testFormatPreservingProperties() throws Exception {
    String content = "# You are reading the \".properties\" entry.\n" +
        "! The exclamation mark can also mark text as comments.\n" +
        "# The key and element characters $, #, !, =, and : are written with a preceding backslash to ensure that they are properly loaded.\n" +
        "website = http\\://en.wikipedia.org/\n" +
        "language = English\n" +
        "# The backslash below tells the application to continue reading\n" +
        "# the value onto the next line.\n" +
        "message = Welcome to \\\n" +
        "          Wikipedia!\n" +
        "# Add spaces to the key\n" +
        "key\\ with\\ spaces = This is the value that could be looked up with the key \"key with spaces\".\n\n" +
        "# Unicode\n" +
        "tab : \\u0009\n" +
        "keyWithEqualInValue=ThisIsTheEqualSign\\:\\=Ok\n" +
        "simplekey simplevalue\n";

    InputStream is = null;
    try {
      is = new ByteArrayInputStream(content.getBytes());
      FormatPreservingProperties props = new FormatPreservingProperties();
      props.load(is);
      props.setProperty("message", "new value");
      props.setProperty("nonExistentKey", "newValueWithSpecialChars:$üäè!à\"");

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      props.store(out);

      String fileContent = new String(out.toByteArray(), FormatPreservingProperties.ENCODING);
      SdkAssert.assertTrue(fileContent.contains("# The key and element characters $"));

      Properties finalProps = new Properties();
      finalProps.load(new ByteArrayInputStream(out.toByteArray()));
      SdkAssert.assertEquals("new value", finalProps.getProperty("message"));
      SdkAssert.assertEquals("\t", finalProps.getProperty("tab"));
      SdkAssert.assertEquals("simplevalue", finalProps.getProperty("simplekey"));
      SdkAssert.assertEquals("ThisIsTheEqualSign:=Ok", finalProps.getProperty("keyWithEqualInValue"));
      SdkAssert.assertEquals("newValueWithSpecialChars:$üäè!à\"", finalProps.getProperty("nonExistentKey"));
      SdkAssert.assertEquals("This is the value that could be looked up with the key \"key with spaces\".", finalProps.getProperty("key with spaces"));
    }
    finally {
      if (is != null) {
        is.close();
      }
    }
  }
}
