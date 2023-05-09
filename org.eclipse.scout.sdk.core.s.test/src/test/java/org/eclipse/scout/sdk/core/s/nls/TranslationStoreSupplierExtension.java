/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls;

import static org.eclipse.scout.sdk.core.s.nls.Translations.registerStoreSupplier;
import static org.eclipse.scout.sdk.core.s.nls.Translations.registerUiTextContributor;
import static org.eclipse.scout.sdk.core.s.nls.Translations.removeStoreSupplier;
import static org.eclipse.scout.sdk.core.s.nls.Translations.removeUiTextContributor;
import static org.eclipse.scout.sdk.core.s.nls.properties.AbstractTranslationPropertiesFile.parseLanguageFromFileName;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import org.eclipse.scout.rt.security.ScoutSecurityTextProviderService;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.environment.NullProgress;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutVariousApi;
import org.eclipse.scout.sdk.core.s.nls.Translations.DependencyScope;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.scout.sdk.core.s.nls.properties.AbstractTranslationPropertiesFile;
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

  public static final String PROPERTIES_FILE_NAME_PREFIX = "Prefix";
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

  public static TranslationManager testingManager(IEnvironment env) {
    return Translations.createManager(Paths.get(""), env, new NullProgress(), DependencyScope.ALL)
        .orElseThrow(TranslationStoreSupplierExtension::createExtensionNotRegisteredError);
  }

  public static PropertiesTranslationStore testingStore(IEnvironment env) {
    return (PropertiesTranslationStore) testingManager(env)
        .primaryEditableStore()
        .orElseThrow(TranslationStoreSupplierExtension::createExtensionNotRegisteredError);
  }

  public static PropertiesTranslationStore createReadOnlyStore(IEnvironment env) {
    return createTestingStore(env, true, getScoutTextProviderServiceName(env), null);
  }

  public static PropertiesTranslationStore createEmptyStore(IJavaEnvironment env) {
    return new PropertiesTranslationStore(PropertiesTextProviderService.create(env.requireTypeFrom(IScoutApi.class, IScoutVariousApi::ScoutTextProviderService)).orElseThrow());
  }

  private static String getScoutTextProviderServiceName(IEnvironment env) {
    return ((TestingEnvironment) env).primaryEnvironment().requireApi(IScoutApi.class).ScoutTextProviderService().fqn();
  }

  private static IllegalArgumentException createExtensionNotRegisteredError() {
    return newFail("No translation store supplier available. Ensure the '{}' extension is registered in the unit test.", TranslationStoreSupplierExtension.class.getName());
  }

  protected static PropertiesTranslationStore createTestingStore(IEnvironment env, boolean readOnly, String svcFqn, Path dir) {
    var txtSvcType = ((TestingEnvironment) env).primaryEnvironment().requireType(svcFqn);
    return createTestingStore(txtSvcType, readOnly, dir);
  }

  protected static PropertiesTranslationStore createTestingStore(IType textService, boolean readOnly, Path directory) {
    var originalSvc = PropertiesTextProviderService.create(textService).orElseThrow();
    var txtSvc = spy(originalSvc);
    var store = new PropertiesTranslationStore(txtSvc);

    var def = new Properties();
    def.setProperty(TRANSLATION_KEY_1, KEY_1_VAL_DEFAULT);
    def.setProperty(TRANSLATION_KEY_2, KEY_2_VAL_DEFAULT);
    def.setProperty(TRANSLATION_KEY_3, KEY_3_VAL_DEFAULT);

    var en = new Properties();
    en.setProperty(TRANSLATION_KEY_1, KEY_1_VAL_EN);
    en.setProperty(TRANSLATION_KEY_3, KEY_3_VAL_EN);

    Collection<ITranslationPropertiesFile> translationFiles = new ArrayList<>();
    try {
      if (readOnly) {
        var readOnlyProps = new Properties();
        readOnlyProps.setProperty(TRANSLATION_KEY_1, KEY_1_VAL_OVERRIDDEN);
        var out = new ByteArrayOutputStream();
        readOnlyProps.store(out, "");
        translationFiles.add(new ReadOnlyTranslationFile(() -> new ByteArrayInputStream(out.toByteArray()), Language.LANGUAGE_DEFAULT));
        when(txtSvc.order()).thenReturn(10000.0);
      }
      else {
        translationFiles.add(createTranslationFile(directory.resolve(PROPERTIES_FILE_NAME_PREFIX + AbstractTranslationPropertiesFile.FILE_SUFFIX), def));
        translationFiles.add(createTranslationFile(directory.resolve(PROPERTIES_FILE_NAME_PREFIX + "_en_US" + AbstractTranslationPropertiesFile.FILE_SUFFIX), en));
        translationFiles.add(createTranslationFile(directory.resolve(PROPERTIES_FILE_NAME_PREFIX + "_es" + AbstractTranslationPropertiesFile.FILE_SUFFIX), null));
      }
    }
    catch (IOException e) {
      throw new SdkException(e);
    }

    store.load(translationFiles, new NullProgress());
    return store;
  }

  protected static EditableTranslationFile createTranslationFile(Path file, Properties content) throws IOException {
    if (content != null && !Files.exists(file)) { // reuse file if already exists
      try (var out = Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
        content.store(out, null);
      }
    }

    var language = parseLanguageFromFileName(file.getFileName().toString(), PROPERTIES_FILE_NAME_PREFIX).orElseThrow();
    var props = new EditableTranslationFile(file, language);
    assertTrue(props.load(new NullProgress()));
    return props;
  }

  public static class TestingTranslationStoreSupplier implements ITranslationStoreSupplier {

    private final Path m_dir;

    protected TestingTranslationStoreSupplier() throws IOException {
      m_dir = Files.createTempDirectory("SdkNlsTest");
    }

    @Override
    public Stream<ITranslationStore> visibleStoresForJavaModule(Path modulePath, IEnvironment env, IProgress progress) {
      return Stream.of(
          createTestingStore(env, false, getScoutTextProviderServiceName(env), m_dir),
          createTestingStore(env, true, ScoutSecurityTextProviderService.class.getName(), m_dir));
    }

    @Override
    public Stream<IType> visibleTextContributorsForJavaModule(Path modulePath, IEnvironment env, IProgress progress) {
      return Stream.empty();
    }

    @Override
    public Optional<ITranslationStore> createStoreForService(IType textService, IProgress progress) {
      return Optional.of(createTestingStore(textService, false, m_dir));
    }
  }
}
