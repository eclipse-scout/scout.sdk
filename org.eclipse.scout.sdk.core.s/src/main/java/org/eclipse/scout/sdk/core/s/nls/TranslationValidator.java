/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.nls;

import static java.util.Collections.singleton;

import java.util.Collection;

import org.eclipse.scout.sdk.core.s.nls.manager.IStackedTranslation;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * Contains methods to validate translations.
 */
public final class TranslationValidator {

  public static final int OK = 0;

  public static final int KEY_OVERRIDES_OTHER_STORE_WARNING = 100;
  public static final int KEY_IS_OVERRIDDEN_BY_OTHER_STORE_WARNING = 200;
  public static final int KEY_OVERRIDES_AND_IS_OVERRIDDEN_WARNING = 300;
  public static final int TEXT_INHERITED_BECOMES_ACTIVE_IF_REMOVED_WARNING = 400;

  public static final int KEY_ALREADY_EXISTS_ERROR = 40000;
  public static final int DEFAULT_TRANSLATION_MISSING_ERROR = 50000;
  public static final int KEY_EMPTY_ERROR = 70000;
  public static final int KEY_INVALID_ERROR = 80000;

  private TranslationValidator() {
  }

  /**
   * Checks if the given {@link ITranslation} contains a valid value for the default language.
   *
   * @param newTranslation
   *          The {@link ITranslation} to validate. Must not be {@code null}.
   * @param existing
   *          An optional currently existing {@link IStackedTranslation} to which the given translation belongs. If
   *          provided it is used to detect if there is another store with higher order available, that already provides
   *          a default language text. In that case it is accepted that the current value is empty.
   * @param target
   *          An optional target store in which the default text would be saved. Used to detect, if there is another
   *          store with higher order available, that already provides a default language text. In that case it is
   *          accepted that the current value is empty. May be {@code null}. Then the
   *          {@link IStackedTranslation#primaryEditableStore()} is used. Requires an existing
   *          {@link IStackedTranslation} to be passed (former parameter).
   * @return {@link #DEFAULT_TRANSLATION_MISSING_ERROR} if there is no default language or
   *         {@link #TEXT_INHERITED_BECOMES_ACTIVE_IF_REMOVED_WARNING} if there is an inherited one that will become
   *         active or {@link #OK}.
   */
  public static int validateDefaultText(ITranslation newTranslation, IStackedTranslation existing, ITranslationStore target) {
    var defaultLangText = newTranslation.text(Language.LANGUAGE_DEFAULT).orElse(null);
    return validateDefaultText(defaultLangText, existing, target);
  }

  /**
   * Checks if the given text contains a valid value for the default language.
   * 
   * @param newDefaultText
   *          The new text for the default language
   * @param key
   *          The key of the translation to which the new default text belongs
   * @param manager
   *          The {@link TranslationManager} to use for the validation. Must not be {@code null}.
   * @param target
   *          An optional target store in which the default text would be saved. Used to detect, if there is another
   *          store with higher order available, that already provides a default language text. In that case it is
   *          accepted that the current value is empty. May be {@code null}. Then the
   *          {@link IStackedTranslation#primaryEditableStore()} is used.
   * @return {@link #DEFAULT_TRANSLATION_MISSING_ERROR} if there is no default language or
   *         {@link #TEXT_INHERITED_BECOMES_ACTIVE_IF_REMOVED_WARNING} if there is an inherited one that will become
   *         active or {@link #OK}.
   */
  public static int validateDefaultText(CharSequence newDefaultText, String key, TranslationManager manager, ITranslationStore target) {
    var translation = manager.translation(key).orElse(null);
    return validateDefaultText(newDefaultText, translation, target);
  }

  /**
   * Checks if the given text contains a valid value for the default language.
   *
   * @param newDefaultText
   *          The new text for the default language
   * @param existing
   *          An optional currently existing {@link IStackedTranslation} to which the new default text belongs. If
   *          provided it is used to detect if there is another store with higher order available, that already provides
   *          a default language text. In that case it is accepted that the current value is empty.
   * @param target
   *          An optional target store in which the default text would be saved. Used to detect, if there is another
   *          store with higher order available, that already provides a default language text. In that case it is
   *          accepted that the current value is empty. May be {@code null}. Then the
   *          {@link IStackedTranslation#primaryEditableStore()} is used. Requires an existing
   *          {@link IStackedTranslation} to be passed (former parameter).
   * @return {@link #DEFAULT_TRANSLATION_MISSING_ERROR} if there is no default language or
   *         {@link #TEXT_INHERITED_BECOMES_ACTIVE_IF_REMOVED_WARNING} if there is an inherited one that will become
   *         active or {@link #OK}.
   */
  public static int validateDefaultText(CharSequence newDefaultText, IStackedTranslation existing, ITranslationStore target) {
    if (Strings.hasText(newDefaultText)) {
      return OK; // if there is a new text it is always ok
    }

    // if the new text is empty its only allowed if there is another store providing a default text
    if (isOverriding(existing, Language.LANGUAGE_DEFAULT, target)) {
      return TEXT_INHERITED_BECOMES_ACTIVE_IF_REMOVED_WARNING;
    }
    return DEFAULT_TRANSLATION_MISSING_ERROR;
  }

  /**
   * Checks if the text given would be replaced with a currently overridden one when removed.
   * 
   * @param newText
   *          The new text.
   * @param current
   *          An optional currently existing {@link IStackedTranslation}. If provided it is used to detect if there is
   *          another store with higher order available that provides a text for the language given.
   * @param language
   *          The language for which to check
   * @return {@link #TEXT_INHERITED_BECOMES_ACTIVE_IF_REMOVED_WARNING} or {@link #OK}.
   */
  public static int validateText(CharSequence newText, IStackedTranslation current, Language language) {
    if (Strings.hasText(newText)) {
      return OK; // if there is a new text it is always ok
    }

    if (isOverriding(current, language, null)) {
      return TEXT_INHERITED_BECOMES_ACTIVE_IF_REMOVED_WARNING;
    }
    return OK;
  }

  private static boolean isOverriding(IStackedTranslation current, Language language, ITranslationStore target) {
    if (current == null || language == null) {
      return false;
    }
    var targetStore = target;
    if (targetStore == null) {
      targetStore = current.primaryEditableStore().orElseThrow();
    }
    return current.isOverriding(language, targetStore);
  }

  /**
   * @param result
   *          The validation result code to check.
   * @return {@code true} if the given validation result describes a forbidden or invalid state.
   */
  @SuppressWarnings("squid:S1067") // number of conditional operators
  public static boolean isForbidden(int result) {
    return result != OK
        && result != KEY_OVERRIDES_OTHER_STORE_WARNING
        && result != KEY_IS_OVERRIDDEN_BY_OTHER_STORE_WARNING
        && result != KEY_OVERRIDES_AND_IS_OVERRIDDEN_WARNING
        && result != TEXT_INHERITED_BECOMES_ACTIVE_IF_REMOVED_WARNING;
  }

  /**
   * Checks if the given translation key is valid.
   * 
   * @param keyToValidate
   *          The key to validate
   * @return {@link #KEY_EMPTY_ERROR}, {@link #KEY_INVALID_ERROR} or {@link #OK}.
   */
  public static int validateKey(String keyToValidate) {
    return validateKey(null, null, keyToValidate, singleton(keyToValidate));
  }

  /**
   * Checks if the given key is valid in the context of the {@link TranslationManager manager} and
   * {@link ITranslationStore store} specified.
   *
   * @param manager
   *          The {@link TranslationManager manager} in which the key would be stored. Must not be {@code null}.
   * @param target
   *          The target {@link ITranslationStore store} in which the key would be stored. Must not be {@code null}.
   * @param keyToValidate
   *          The key to validate
   * @return {@link #KEY_EMPTY_ERROR}, {@link #KEY_ALREADY_EXISTS_ERROR},
   *         {@link #KEY_OVERRIDES_AND_IS_OVERRIDDEN_WARNING}, {@link #KEY_OVERRIDES_OTHER_STORE_WARNING},
   *         {@link #KEY_IS_OVERRIDDEN_BY_OTHER_STORE_WARNING}, {@link #KEY_INVALID_ERROR} or {@link #OK}.
   */
  public static int validateKey(TranslationManager manager, ITranslationStore target, String keyToValidate) {
    return validateKey(manager, target, keyToValidate, null);
  }

  /**
   * Checks if the given key is valid in the context of the {@link TranslationManager manager} and
   * {@link ITranslationStore store} specified.
   *
   * @param manager
   *          The {@link TranslationManager manager} in which the key would be stored.
   * @param target
   *          The target {@link ITranslationStore store} in which the key would be stored.
   * @param keyToValidate
   *          The key to validate
   * @param acceptedKeys
   *          An optional {@link Collection} of keys for which the given manager and store should be ignored in
   *          validation.
   * @return {@link #KEY_EMPTY_ERROR}, {@link #KEY_ALREADY_EXISTS_ERROR},
   *         {@link #KEY_OVERRIDES_AND_IS_OVERRIDDEN_WARNING}, {@link #KEY_OVERRIDES_OTHER_STORE_WARNING},
   *         {@link #KEY_IS_OVERRIDDEN_BY_OTHER_STORE_WARNING}, {@link #KEY_INVALID_ERROR} or {@link #OK}.
   */
  public static int validateKey(TranslationManager manager, ITranslationStore target, String keyToValidate, Collection<String> acceptedKeys) {
    if (Strings.isBlank(keyToValidate)) {
      return KEY_EMPTY_ERROR;
    }

    if (!ITranslation.KEY_REGEX.matcher(keyToValidate).matches()) {
      return KEY_INVALID_ERROR;
    }

    if (acceptedKeys != null && acceptedKeys.contains(keyToValidate)) {
      return OK;
    }

    if (target == null) {
      return OK; // no more validation possible without target store
    }

    if (target.containsKey(keyToValidate)) {
      return KEY_ALREADY_EXISTS_ERROR;
    }

    if (manager == null) {
      return OK; // no more validation possible without manager
    }

    var hasStoresWithKeyOverridden = manager.allStores()
        .filter(store -> store.containsKey(keyToValidate))
        .anyMatch(store -> store.service().order() > target.service().order());

    // the stores that override me
    var hasStoresOverridingKey = manager.allStores()
        .filter(store -> store.containsKey(keyToValidate))
        .anyMatch(store -> store.service().order() < target.service().order());

    if (hasStoresWithKeyOverridden && hasStoresOverridingKey) {
      return KEY_OVERRIDES_AND_IS_OVERRIDDEN_WARNING;
    }
    if (hasStoresWithKeyOverridden) {
      return KEY_OVERRIDES_OTHER_STORE_WARNING;
    }
    if (hasStoresOverridingKey) {
      return KEY_IS_OVERRIDDEN_BY_OTHER_STORE_WARNING;
    }

    return OK;
  }
}
