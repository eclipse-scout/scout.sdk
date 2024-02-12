/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls;

import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.isForbidden;
import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.validateDefaultText;
import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.validateKey;

import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore;
import org.eclipse.scout.sdk.core.s.nls.TranslationValidator;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;

/**
 * <h3>{@link TranslationInputValidator}</h3>
 *
 * @since 7.0.0
 */
public final class TranslationInputValidator {

  private TranslationInputValidator() {
  }

  public static IStatus validateDefaultTranslation(CharSequence defaultTranslation) {
    return toStatus(validateDefaultText(defaultTranslation, null, null));
  }

  public static IStatus validateNlsKey(TranslationManager manager, ITranslationStore target, String key) {
    return validateNlsKey(manager, target, key, null);
  }

  public static IStatus validateNlsKey(TranslationManager manager, ITranslationStore target, String key, Collection<String> acceptedKeys) {
    return toStatus(validateKey(manager, target, key, acceptedKeys));
  }

  public static IStatus validateTranslationStore(ITranslationStore store) {
    if (store == null) {
      return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, -1, "Please choose a service.", null);
    }
    if (!store.isEditable()) {
      return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, -1, "The selected service is read-only.", null);
    }
    return Status.OK_STATUS;
  }

  public static IStatus toStatus(int validationResult) {
    if (validationResult == TranslationValidator.OK) {
      return Status.OK_STATUS;
    }
    var severity = IStatus.ERROR;
    if (!isForbidden(validationResult)) {
      severity = IStatus.WARNING;
    }
    return new Status(severity, S2ESdkUiActivator.PLUGIN_ID, validationResult, getValidationText(validationResult), null);
  }

  private static String getValidationText(int validationResult) {
    return switch (validationResult) {
      case TranslationValidator.OK -> "";
      case TranslationValidator.DEFAULT_TRANSLATION_MISSING_ERROR -> "The default text must be set.";
      case TranslationValidator.KEY_EMPTY_ERROR -> "Please specify a key.";
      case TranslationValidator.KEY_ALREADY_EXISTS_ERROR -> "This key already exists!";
      case TranslationValidator.KEY_OVERRIDES_OTHER_STORE_WARNING -> "The key overrides an inherited entry.";
      case TranslationValidator.KEY_IS_OVERRIDDEN_BY_OTHER_STORE_WARNING -> "The key is overridden by another entry.";
      case TranslationValidator.KEY_OVERRIDES_AND_IS_OVERRIDDEN_WARNING -> "The key overrides an inherited entry and is itself overridden by another entry.";
      case TranslationValidator.TEXT_INHERITED_BECOMES_ACTIVE_IF_REMOVED_WARNING -> "This text will be removed and the inherited text becomes active.";
      default -> "The key is not valid.";
    };
  }
}
