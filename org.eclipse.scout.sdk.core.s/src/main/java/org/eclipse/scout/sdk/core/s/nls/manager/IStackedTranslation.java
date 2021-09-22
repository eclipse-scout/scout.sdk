/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.nls.manager;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.TextProviderService;

/**
 * Represents a stack of {@link ITranslationEntry} instances having the same key. The stack is build based on the @Order
 * annotation value of the text provider service the entry belongs to (see {@link ITranslationEntry#store()}.<br>
 * The texts of this translation consist of all entries merged according to the stack order (first entry for a language
 * defines the text for that language).
 */
public interface IStackedTranslation extends ITranslation {

  /**
   * @return All {@link ITranslationStore} instances in which the translation with this key exists. The stores are
   *         returned in no particular order.
   */
  Stream<ITranslationStore> stores();

  /**
   * @return All languages currently existing in all {@link ITranslationStore stores} of this stacked translation. While
   *         {@link #languages()} only returns languages for which a text exists for this instance,
   *         {@link #languagesOfAllStores()} also returns languages for which this translation currently has no text.
   */
  Stream<Language> languagesOfAllStores();

  /**
   * Checks if the text for the {@link Language} given within the {@link ITranslationStore} given overrides another text
   * from a service with higher order.
   * 
   * @param language
   *          The language to check
   * @param store
   *          The store to check if it overrides the text of another store for the given language.
   * @return {@code true} if there is another {@link ITranslationStore} with higher order than the given one that
   *         contains a text for the given language too.
   */
  boolean isOverriding(Language language, ITranslationStore store);

  /**
   * @return The editable {@link ITranslationStore} that exists in this translation whose {@link TextProviderService}
   *         has the lowest @Order value.
   */
  Optional<ITranslationStore> primaryEditableStore();

  /**
   * @return {@code true} if at least one {@link ITranslationStore} of this translation is editable.
   */
  boolean hasEditableStores();

  /**
   * @return {@code true} if all {@link ITranslationStore} of this translation are editable. Only such translation may
   *         be deleted or are allowed to change the key.
   * @see TranslationManager#changeKey(String, String)
   * @see TranslationManager#removeTranslations(Stream)
   */
  boolean hasOnlyEditableStores();

  /**
   * @param language
   *          The {@link Language} for which the {@link ITranslationEntry} should be returned.
   * @return The {@link ITranslationEntry} from which this translation contains the the text for the given language.
   *         This method respects overwritten texts and only returns the entry of the "winner text".
   */
  Optional<ITranslationEntry> entry(Language language);

}
