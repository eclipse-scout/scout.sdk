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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents the result of a {@link ITranslation} import on a {@link TranslationStoreStack}.
 * 
 * @see TranslationStoreStack#importTranslations(List, String, ITranslationStore)
 */
public interface ITranslationImportInfo {
  /**
   * Result constant indicating that no data was available to import.
   */
  int NO_DATA = 0;

  /**
   * Result constant indicating that no key column and/or no default language column could be found in the import table
   * data.
   */
  int NO_KEY_OR_DEFAULT_LANG_COLUMN = -1;

  /**
   * The result of the import operation.
   * 
   * @return A number > 0 indicates the number of translations that have been imported successfully. If the number is <=
   *         0 it is one of the result constants: {@link #NO_DATA} or {@link #NO_KEY_OR_DEFAULT_LANG_COLUMN} indicating
   *         that the import failed.
   */
  int result();

  /**
   * @return An unmodifiable {@link Map} holding all imported (updated or newly created) {@link ITranslationEntry
   *         entries} grouped by key.
   */
  Map<String, ITranslationEntry> importedTranslations();

  /**
   * @return All columns of the header row that could not be mapped to a language or the key column. These columns have
   *         been ignored in the import. It contains column-index to cell content mappings.
   */
  Map<Integer, String> ignoredColumns();

  /**
   * @return All keys that existed multiple times in the import data. The import only uses the last occurrence of a key
   *         within the data set.
   */
  Set<String> duplicateKeys();

  /**
   * @return Row indices which do not contain any valid {@link ITranslation translations}. These are rows not having a
   *         valid key or not having a text for the default language. These rows are skipped in the import.
   */
  List<Integer> invalidRowIndices();

  /**
   * @return The zero based column index that contains the default language.
   */
  int defaultLanguageColumnIndex();

  /**
   * @return The zero based column index that contains the key.
   */
  int keyColumnIndex();
}
