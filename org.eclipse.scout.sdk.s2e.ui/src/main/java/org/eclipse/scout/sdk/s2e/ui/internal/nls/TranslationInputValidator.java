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
package org.eclipse.scout.sdk.s2e.ui.internal.nls;

import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.isForbidden;
import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.validateDefaultText;
import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.validateKey;

import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.core.s.nls.TranslationValidator;
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
    return toStatus(validateDefaultText(defaultTranslation));
  }

  public static IStatus validateNlsKey(TranslationStoreStack project, ITranslationStore target, String key) {
    return validateNlsKey(project, target, key, null);
  }

  public static IStatus validateNlsKey(TranslationStoreStack project, ITranslationStore target, String key, Collection<String> acceptedKeys) {
    return toStatus(validateKey(project, target, key, acceptedKeys));
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

  private static IStatus toStatus(int validationResult) {
    if (validationResult == TranslationValidator.OK) {
      return Status.OK_STATUS;
    }
    int severity = IStatus.ERROR;
    if (!isForbidden(validationResult)) {
      severity = IStatus.WARNING;
    }
    return new Status(severity, S2ESdkUiActivator.PLUGIN_ID, validationResult, getValidationText(validationResult), null);
  }

  private static String getValidationText(int validationResult) {
    switch (validationResult) {
      case TranslationValidator.OK:
        return "";
      case TranslationValidator.DEFAULT_TRANSLATION_MISSING_ERROR:
      case TranslationValidator.DEFAULT_TRANSLATION_EMPTY_ERROR:
        return "The default text must be set.";
      case TranslationValidator.KEY_EMPTY_ERROR:
        return "Please specify a key.";
      case TranslationValidator.KEY_ALREADY_EXISTS_ERROR:
        return "This key already exists!";
      case TranslationValidator.KEY_OVERRIDES_OTHER_STORE_WARNING:
        return "The key overrides an inherited entry.";
      case TranslationValidator.KEY_IS_OVERRIDDEN_BY_OTHER_STORE_WARNING:
        return "The key is overridden by another entry.";
      case TranslationValidator.KEY_OVERRIDES_AND_IS_OVERRIDDEN_WARNING:
        return "The key overrides an inherited entry and is itself overridden by another entry.";
      default:
        return "The key name is not valid.";
    }
  }
}
