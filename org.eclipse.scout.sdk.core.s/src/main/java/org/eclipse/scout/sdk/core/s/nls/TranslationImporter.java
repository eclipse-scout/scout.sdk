/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.validateDefaultText;
import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.validateKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

public class TranslationImporter implements ITranslationImportInfo {

  // input
  private final TranslationStoreStack m_stack;
  private final List<List<String>> m_rawTableData;
  private final ITranslationStore m_storeForNewTranslations;
  private final String m_keyColumnName;

  // column mappings
  private final Map<Integer/* column index */, Language> m_columnMapping;
  private int m_keyColumnIndex;
  private int m_defaultLanguageColumnIndex;

  // result
  private final Map<Integer, String> m_unmappedColumns;
  private final Set<String> m_duplicateKeys;
  private final List<Integer> m_invalidRows;
  private final Map<String, ITranslationEntry> m_importedTranslations;
  private int m_result;

  protected TranslationImporter(TranslationStoreStack stackToImportTo, List<List<String>> rawTableData, String keyColumnName, ITranslationStore storeForNewTranslations) {
    m_stack = Ensure.notNull(stackToImportTo);
    m_rawTableData = Ensure.notNull(rawTableData);
    m_storeForNewTranslations = storeForNewTranslations;
    m_keyColumnName = Ensure.notBlank(keyColumnName);

    m_columnMapping = new HashMap<>();
    m_keyColumnIndex = -1;
    m_defaultLanguageColumnIndex = -1;

    m_unmappedColumns = new LinkedHashMap<>();
    m_duplicateKeys = new HashSet<>();
    m_invalidRows = new ArrayList<>();
    m_importedTranslations = new HashMap<>();
  }

  public void tryImport() {
    m_result = doImport();
  }

  protected int doImport() {
    var rawData = rawTableData();
    if (rawData.size() < 2) {
      return NO_DATA; // no data to import
    }

    Map<String, ITranslation> toImport = new HashMap<>(rawData.size() - 1);
    m_duplicateKeys.addAll(IntStream.range(0, rawData.size())
        .mapToObj(row -> parseRow(rawData.get(row), row))
        .filter(Objects::nonNull)
        .map(translation -> toImport.put(translation.key(), translation))
        .filter(Objects::nonNull)
        .map(ITranslation::key)
        .collect(toSet()));

    if (!isValidHeader(keyColumnIndex(), defaultLanguageColumnIndex())) {
      return NO_KEY_OR_DEFAULT_LANG_COLUMN;
    }
    if (toImport.isEmpty()) {
      return NO_DATA;
    }

    var stack = stack();
    var targetForNewTranslations = storeForNewTranslations();
    stack.setChanging(true);
    try {
      m_importedTranslations.putAll(toImport.values().stream()
          .map(translation -> stack.mergeTranslation(translation, targetForNewTranslations))
          .collect(toMap(ITranslation::key, Function.identity(), Ensure::failOnDuplicates)));
    }
    finally {
      stack.setChanging(false);
    }
    return importedTranslations().size();
  }

  protected ITranslation parseRow(List<String> row, int rowIndex) {
    if (!columnMappingDone()) {
      // parse header first
      parseHeader(row);
      return null;
    }

    // skip empty rows
    if (isEmptyRow(row)) {
      return null;
    }

    // validate key column
    if (row.size() <= keyColumnIndex()) {
      m_invalidRows.add(rowIndex);
      return null;
    }
    var key = row.get(keyColumnIndex());
    if (validateKey(key) != TranslationValidator.OK) {
      m_invalidRows.add(rowIndex);
      return null;
    }

    // validate default language column
    if (row.size() <= defaultLanguageColumnIndex()) {
      m_invalidRows.add(rowIndex);
      return null;
    }
    var defaultLangText = row.get(defaultLanguageColumnIndex());
    if (validateDefaultText(defaultLangText) != TranslationValidator.OK) {
      m_invalidRows.add(rowIndex);
      return null;
    }

    // create translation for row
    var t = new Translation(key);
    t.putText(Language.LANGUAGE_DEFAULT, defaultLangText);
    m_columnMapping.forEach((index, language) -> appendText(t, language, row, index));
    return t;
  }

  protected static boolean isEmptyRow(Collection<String> row) {
    return row.stream().noneMatch(Strings::hasText);
  }

  protected static void appendText(Translation toFill, Language lang, List<String> row, int index) {
    if (row.size() <= index) {
      return;
    }
    var text = row.get(index);
    if (Strings.isEmpty(text)) {
      return;
    }
    toFill.putText(lang, text);
  }

  protected boolean isEmptyColumn(int columnIndex) {
    return rawTableData().stream().allMatch(row -> {
      if (columnIndex >= row.size()) {
        return true;
      }
      return Strings.isBlank(row.get(columnIndex));
    });
  }

  protected void parseHeader(List<String> headerRow) {
    Map<Integer, Language> columnMapping = new HashMap<>(headerRow.size() - 1);
    Map<Integer, String> unmappedColumns = new LinkedHashMap<>();
    var keyColumnIndex = -1;
    var defaultLanguageColumnIndex = -1;

    for (var i = 0; i < headerRow.size(); i++) {
      var cell = headerRow.get(i);
      if (Strings.isBlank(cell)) {
        if (!isEmptyColumn(i)) {
          unmappedColumns.put(i, cell);
        }
        continue;
      }

      if (keyColumnName().equalsIgnoreCase(cell)) {
        keyColumnIndex = i;
      }
      else if (Language.LANGUAGE_DEFAULT.locale().toString().equalsIgnoreCase(cell)) {
        defaultLanguageColumnIndex = i;
      }
      else {
        var language = Language.parse(cell);
        if (language.isPresent()) {
          columnMapping.put(i, language.get());
        }
        else {
          unmappedColumns.put(i, cell);
        }
      }
    }
    if (isValidHeader(keyColumnIndex, defaultLanguageColumnIndex)) {
      // valid header row: at least a key column and default language could be found: accept it as header
      m_columnMapping.putAll(columnMapping);
      m_unmappedColumns.putAll(unmappedColumns);
      m_keyColumnIndex = keyColumnIndex;
      m_defaultLanguageColumnIndex = defaultLanguageColumnIndex;
    }
  }

  @Override
  public Map<String, ITranslationEntry> importedTranslations() {
    return unmodifiableMap(m_importedTranslations);
  }

  @Override
  public int result() {
    return m_result;
  }

  @Override
  public int defaultLanguageColumnIndex() {
    return m_defaultLanguageColumnIndex;
  }

  @Override
  public int keyColumnIndex() {
    return m_keyColumnIndex;
  }

  @Override
  public Map<Integer, String> ignoredColumns() {
    return unmodifiableMap(m_unmappedColumns);
  }

  @Override
  public Set<String> duplicateKeys() {
    return unmodifiableSet(m_duplicateKeys);
  }

  @Override
  public List<Integer> invalidRowIndices() {
    return unmodifiableList(m_invalidRows);
  }

  protected boolean columnMappingDone() {
    return isValidHeader(keyColumnIndex(), defaultLanguageColumnIndex());
  }

  public String keyColumnName() {
    return m_keyColumnName;
  }

  public TranslationStoreStack stack() {
    return m_stack;
  }

  public List<List<String>> rawTableData() {
    return m_rawTableData;
  }

  public ITranslationStore storeForNewTranslations() {
    return m_storeForNewTranslations;
  }

  protected static boolean isValidHeader(int keyColumnIndex, int defaultLanguageColumnIndex) {
    return isValidKeyColumnIndex(keyColumnIndex) && isValidDefaultLanguageColumnIndex(defaultLanguageColumnIndex);
  }

  protected static boolean isValidDefaultLanguageColumnIndex(int defaultLanguageColumnIndex) {
    return defaultLanguageColumnIndex >= 0;
  }

  protected static boolean isValidKeyColumnIndex(int keyColumnIndex) {
    return keyColumnIndex >= 0;
  }
}
