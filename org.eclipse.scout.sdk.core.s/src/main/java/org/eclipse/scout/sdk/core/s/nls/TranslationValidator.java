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

import java.util.Collection;
import java.util.Collections;

import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * Contains methods to validate translations.
 */
public final class TranslationValidator {

  public static final int OK = 0;

  public static final int DEFAULT_TRANSLATION_MISSING_ERROR = 1;
  public static final int DEFAULT_TRANSLATION_EMPTY_ERROR = 2;

  public static final int KEY_EMPTY_ERROR = 3;
  public static final int KEY_ALREADY_EXISTS_ERROR = 4;
  public static final int KEY_OVERRIDES_OTHER_STORE_WARNING = 5;
  public static final int KEY_IS_OVERRIDDEN_BY_OTHER_STORE_WARNING = 6;
  public static final int KEY_OVERRIDES_AND_IS_OVERRIDDEN_WARNING = 7;
  public static final int KEY_INVALID_ERROR = 8;

  private TranslationValidator() {
  }

  public static int validateTranslation(ITranslation toValidate) {
    Ensure.notNull(toValidate, "A translation must be specified.");
    int result = validateKey(null, null, toValidate.key(), Collections.singleton(toValidate.key()));
    if (result != OK) {
      return result;
    }
    return validateDefaultText(toValidate);
  }

  /**
   * Checks if the given {@link ITranslation} contains a valid value for the default language.
   *
   * @param translation
   *          The {@link ITranslation} to validate
   * @return {@link #DEFAULT_TRANSLATION_MISSING_ERROR} if there is no default language,
   *         {@link #DEFAULT_TRANSLATION_EMPTY_ERROR} if the default text is empty or {@link #OK}.
   */
  public static int validateDefaultText(ITranslation translation) {
    return Ensure.notNull(translation).text(Language.LANGUAGE_DEFAULT)
        .map(TranslationValidator::validateDefaultText)
        .orElse(DEFAULT_TRANSLATION_MISSING_ERROR);
  }

  /**
   * Checks if the given {@link CharSequence} is a valid value for a default language text entry.
   *
   * @param defaultTranslation
   *          The text for the default language.
   * @return {@link #DEFAULT_TRANSLATION_EMPTY_ERROR} if the default text is empty or {@link #OK}.
   */
  public static int validateDefaultText(CharSequence defaultTranslation) {
    if (Strings.isBlank(defaultTranslation)) {
      return DEFAULT_TRANSLATION_EMPTY_ERROR;
    }
    return OK;
  }

  /**
   * @param result
   *          The validation result code to check.
   * @return {@code true} if the given validation result describes a forbidden or invalid state.
   */
  public static boolean isForbidden(int result) {
    return result != OK
        && result != KEY_OVERRIDES_OTHER_STORE_WARNING
        && result != KEY_IS_OVERRIDDEN_BY_OTHER_STORE_WARNING
        && result != KEY_OVERRIDES_AND_IS_OVERRIDDEN_WARNING;
  }

  /**
   * Checks if the given key is valid in the context of the {@link TranslationStoreStack stack} and
   * {@link ITranslationStore store} specified.
   *
   * @param stack
   *          The {@link TranslationStoreStack stack} in which the key would be stored. Must not be {@code null}.
   * @param target
   *          The target {@link ITranslationStore store} in which the key would be stored. Must not be {@code null}.
   * @param keyToValidate
   *          The key to validate
   * @return {@link #KEY_EMPTY_ERROR}, {@link #KEY_ALREADY_EXISTS_ERROR},
   *         {@link #KEY_OVERRIDES_AND_IS_OVERRIDDEN_WARNING}, {@link #KEY_OVERRIDES_OTHER_STORE_WARNING},
   *         {@link #KEY_IS_OVERRIDDEN_BY_OTHER_STORE_WARNING}, {@link #KEY_INVALID_ERROR} or {@link #OK}.
   */
  public static int validateKey(TranslationStoreStack stack, ITranslationStore target, String keyToValidate) {
    return validateKey(stack, target, keyToValidate, null);
  }

  /**
   * Checks if the given key is valid in the context of the {@link TranslationStoreStack stack} and
   * {@link ITranslationStore store} specified.
   *
   * @param stack
   *          The {@link TranslationStoreStack stack} in which the key would be stored.
   * @param target
   *          The target {@link ITranslationStore store} in which the key would be stored.
   * @param keyToValidate
   *          The key to validate
   * @param acceptedKeys
   *          An optional {@link Collection} of keys for which the given stack and store should be ignored in
   *          validation.
   * @return {@link #KEY_EMPTY_ERROR}, {@link #KEY_ALREADY_EXISTS_ERROR},
   *         {@link #KEY_OVERRIDES_AND_IS_OVERRIDDEN_WARNING}, {@link #KEY_OVERRIDES_OTHER_STORE_WARNING},
   *         {@link #KEY_IS_OVERRIDDEN_BY_OTHER_STORE_WARNING}, {@link #KEY_INVALID_ERROR} or {@link #OK}.
   */
  public static int validateKey(TranslationStoreStack stack, ITranslationStore target, String keyToValidate, Collection<String> acceptedKeys) {
    if (Strings.isBlank(keyToValidate)) {
      return KEY_EMPTY_ERROR;
    }

    if (acceptedKeys == null || !acceptedKeys.contains(keyToValidate)) {
      if (target.containsKey(keyToValidate)) {
        return KEY_ALREADY_EXISTS_ERROR;
      }

      // the stores that are overridden by me
      long numStoresWithKeyOverridden = stack.allStores()
          .filter(store -> store.containsKey(keyToValidate))
          .filter(store -> store.service().order() > target.service().order())
          .count();

      // the stores that override me
      long numStoresOverridingKey = stack.allStores()
          .filter(store -> store.containsKey(keyToValidate))
          .filter(store -> store.service().order() < target.service().order())
          .count();

      if (numStoresWithKeyOverridden > 0 && numStoresOverridingKey > 0) {
        return KEY_OVERRIDES_AND_IS_OVERRIDDEN_WARNING;
      }
      if (numStoresWithKeyOverridden > 0) {
        return KEY_OVERRIDES_OTHER_STORE_WARNING;
      }
      if (numStoresOverridingKey > 0) {
        return KEY_IS_OVERRIDDEN_BY_OTHER_STORE_WARNING;
      }
    }

    if (!ITranslation.KEY_REGEX.matcher(keyToValidate).matches()) {
      return KEY_INVALID_ERROR;
    }

    return OK;
  }
}
