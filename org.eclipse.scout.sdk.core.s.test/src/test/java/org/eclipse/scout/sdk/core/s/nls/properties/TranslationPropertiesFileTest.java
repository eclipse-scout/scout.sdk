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
package org.eclipse.scout.sdk.core.s.nls.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link TranslationPropertiesFileTest}</h3>
 *
 * @since 7.0.0
 */
public class TranslationPropertiesFileTest {

  @Test
  public void testTranslationPropertiesFile() throws IOException {

    Path tmpDir = Files.createTempDirectory("sdkTest");
    try {
      Path fileExisting = tmpDir.resolve("prefix_en_GB.properties");
      Path fileNotExisting = tmpDir.resolve("prefix.properties");

      Properties src = new Properties();
      src.setProperty("key1", "val1");
      src.setProperty("key2", "val2");
      EditableTranslationFile props1 = createTranslationFile(fileExisting, src);
      EditableTranslationFile props2 = createTranslationFile(fileNotExisting);

      // test editable files
      assertFalse(props1.load(new NullProgress())); // load a second time
      assertFalse(props2.load(new NullProgress())); // load a second time
      assertEquals(2, props1.allEntries().size());
      assertEquals(0, props2.allEntries().size());
      assertEquals(2, props1.allKeys().count());
      assertEquals(0, props2.allKeys().count());
      assertEquals("val1", props1.translation("key1").get());
      assertFalse(props2.translation("key1").isPresent());
      assertEquals(Language.parseThrowingOnError("en_GB"), props1.language());
      assertEquals(Language.LANGUAGE_DEFAULT, props2.language());
      assertTrue(props1.isEditable());
      assertTrue(props2.isEditable());
      assertSame(fileExisting, props1.file());
      assertSame(fileNotExisting, props2.file());

      // test read only file
      ReadOnlyTranslationFile readOnly = new ReadOnlyTranslationFile(() -> EditableTranslationFile.toStream(fileExisting), Language.LANGUAGE_DEFAULT);
      assertFalse(readOnly.isEditable());
      assertThrows(UnsupportedOperationException.class, () -> readOnly.setTranslation("a", "b"));
      assertThrows(UnsupportedOperationException.class, () -> readOnly.removeTranslation("a"));
      assertThrows(UnsupportedOperationException.class, () -> readOnly.flush(null, null));
    }
    finally {
      CoreUtils.deleteDirectory(tmpDir);
    }
  }

  @Test
  public void testParse() {
    assertSame(Language.LANGUAGE_DEFAULT, AbstractTranslationPropertiesFile.parseFromFileNameOrThrow("prefix.properties"));
    assertEquals(new Language(new Locale("test")), AbstractTranslationPropertiesFile.parseFromFileNameOrThrow("prefix_test.properties"));
    assertEquals(new Language(new Locale("de", "FR", "xx")), AbstractTranslationPropertiesFile.parseFromFileNameOrThrow("prefix_de_FR_xx.properties"));
    assertEquals(new Language(new Locale("de", "FR")), AbstractTranslationPropertiesFile.parseFromFileNameOrThrow("prefix_de_FR.properties"));
    assertEquals(new Language(new Locale("de")), AbstractTranslationPropertiesFile.parseFromFileNameOrThrow("prefix_de.properties"));
    assertThrows(IllegalArgumentException.class, () -> AbstractTranslationPropertiesFile.parseFromFileNameOrThrow("abc"));
  }

  static EditableTranslationFile createTranslationFile(Path file) throws IOException {
    return createTranslationFile(file, null);
  }

  static EditableTranslationFile createTranslationFile(Path file, Properties content) throws IOException {
    if (content != null) {
      try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
        content.store(out, null);
      }
    }

    EditableTranslationFile props = new EditableTranslationFile(file);
    assertTrue(props.load(new NullProgress()));
    return props;
  }
}
