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
package org.eclipse.scout.sdk.core.s.nls;

import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.createFullStack;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.registerStoreSupplier;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.registerUiTextContributor;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.removeStoreSupplier;
import static org.eclipse.scout.sdk.core.s.nls.TranslationStores.removeUiTextContributor;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import org.eclipse.scout.rt.shared.services.common.text.ScoutTextProviderService;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.nls.properties.EditableTranslationFile;
import org.eclipse.scout.sdk.core.s.nls.properties.ITranslationPropertiesFile;
import org.eclipse.scout.sdk.core.s.nls.properties.PropertiesTextProviderService;
import org.eclipse.scout.sdk.core.s.nls.properties.PropertiesTranslationStore;
import org.eclipse.scout.sdk.core.s.nls.properties.ReadOnlyTranslationFile;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * This extension is in the test source folder because it is no general extension which may be used by other clients as
 * well (because it is not parameterizable). It is only designed for the tests in this package and subpackages.
 */
public class TranslationStoreSupplierExtension implements BeforeEachCallback, AfterEachCallback {

  public static final String TEST_DEPENDENCY_NAME = "@bsi-sdk/testdependency";
  public static final String TEST_UI_CONTRIBUTOR_FQN = "nls.TestUiTextContributor";

  public static final Language EN = Language.parseThrowingOnError("en_US");
  public static final Language ES = Language.parseThrowingOnError("es");

  public static final String TRANSLATION_KEY_1 = "key1";
  public static final String TRANSLATION_KEY_2 = "key2";
  public static final String TRANSLATION_KEY_3 = "key3";

  public static final String KEY_1_VAL_OVERRIDDEN = "1_def_overridden";
  public static final String KEY_1_VAL_DEFAULT = "1_def";
  public static final String KEY_2_VAL_DEFAULT = "2_def";
  public static final String KEY_3_VAL_DEFAULT = "3_def";
  public static final String KEY_1_VAL_EN = "1_en";
  public static final String KEY_3_VAL_EN = "3_en";

  private TestingTranslationStoreSupplier m_supplier;

  @Override
  public void beforeEach(ExtensionContext context) throws IOException {
    m_supplier = new TestingTranslationStoreSupplier();
    registerStoreSupplier(m_supplier);
    registerUiTextContributor(TEST_DEPENDENCY_NAME, TEST_UI_CONTRIBUTOR_FQN);
  }

  @Override
  public void afterEach(ExtensionContext context) {
    removeUiTextContributor(TEST_DEPENDENCY_NAME);
    removeStoreSupplier(m_supplier);
    try {
      CoreUtils.deleteDirectory(m_supplier.m_dir);
    }
    catch (IOException e) {
      SdkLog.warning("Unable to delete temp directory '{}'.", m_supplier.m_dir, e);
    }
    m_supplier = null;
  }

  public static TranslationStoreStack testingStack(IEnvironment env) {
    return createFullStack(Paths.get(""), env, new NullProgress())
        .orElseThrow(TranslationStoreSupplierExtension::createExtensionNotRegisteredError);
  }

  public static PropertiesTranslationStore testingStore(IEnvironment env) {
    return (PropertiesTranslationStore) testingStack(env)
        .primaryEditableStore()
        .orElseThrow(TranslationStoreSupplierExtension::createExtensionNotRegisteredError);
  }

  public static PropertiesTranslationStore createReadOnlyStore(IEnvironment env) {
    return createTestingStore(env, true, null);
  }

  public static PropertiesTranslationStore createEmptyStore(IJavaEnvironment env) {
    return new PropertiesTranslationStore(PropertiesTextProviderService.create(env.requireType(ScoutTextProviderService.class.getName())).get());
  }

  private static IllegalArgumentException createExtensionNotRegisteredError() {
    return newFail("No translation store supplier available. Ensure the '{}' extension is registered in the unit test.", TranslationStoreSupplierExtension.class.getName());
  }

  protected static PropertiesTranslationStore createTestingStore(IEnvironment env, boolean readOnly, Path dir) {
    IType txtSvcType = ((TestingEnvironment) env).primaryEnvironment().requireType(ScoutTextProviderService.class.getName());
    return createTestingStore(txtSvcType, readOnly, dir);
  }

  protected static PropertiesTranslationStore createTestingStore(IType textService, boolean readOnly, Path directory) {
    PropertiesTextProviderService originalSvc = PropertiesTextProviderService.create(textService).get();
    PropertiesTextProviderService txtSvc = spy(originalSvc);
    PropertiesTranslationStore store = new PropertiesTranslationStore(txtSvc);

    Properties def = new Properties();
    def.setProperty(TRANSLATION_KEY_1, KEY_1_VAL_DEFAULT);
    def.setProperty(TRANSLATION_KEY_2, KEY_2_VAL_DEFAULT);
    def.setProperty(TRANSLATION_KEY_3, KEY_3_VAL_DEFAULT);

    Properties en = new Properties();
    en.setProperty(TRANSLATION_KEY_1, KEY_1_VAL_EN);
    en.setProperty(TRANSLATION_KEY_3, KEY_3_VAL_EN);

    Collection<ITranslationPropertiesFile> translationFiles = new ArrayList<>();
    try {
      if (readOnly) {
        Properties readOnlyProps = new Properties();
        readOnlyProps.setProperty(TRANSLATION_KEY_1, KEY_1_VAL_OVERRIDDEN);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        readOnlyProps.store(out, "");
        //noinspection resource
        translationFiles.add(new ReadOnlyTranslationFile(() -> new ByteArrayInputStream(out.toByteArray()), Language.LANGUAGE_DEFAULT));
        when(txtSvc.order()).thenReturn(10000.0);
      }
      else {
        translationFiles.add(createTranslationFile(directory.resolve("Prefix.properties"), def));
        translationFiles.add(createTranslationFile(directory.resolve("Prefix_en_US.properties"), en));
        translationFiles.add(createTranslationFile(directory.resolve("Prefix_es.properties"), null));
      }
    }
    catch (IOException e) {
      throw new SdkException(e);
    }

    store.load(translationFiles, new NullProgress());
    return store;
  }

  protected static EditableTranslationFile createTranslationFile(Path file, Properties content) throws IOException {
    if (content != null) {
      try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
        content.store(out, null);
      }
    }

    EditableTranslationFile props = new EditableTranslationFile(file);
    assertTrue(props.load(new NullProgress()));
    return props;
  }

  public static class TestingTranslationStoreSupplier implements ITranslationStoreSupplier {

    private final Path m_dir;

    protected TestingTranslationStoreSupplier() throws IOException {
      m_dir = Files.createTempDirectory("SdkNlsTest");
    }

    @Override
    public Stream<? extends ITranslationStore> all(Path modulePath, IEnvironment env, IProgress progress) {
      return Stream.of(
          createTestingStore(env, false, m_dir),
          createTestingStore(env, true, m_dir));
    }

    @Override
    public Optional<? extends ITranslationStore> single(IType textService, IProgress progress) {
      return Optional.of(createTestingStore(textService, false, m_dir));
    }
  }
}
