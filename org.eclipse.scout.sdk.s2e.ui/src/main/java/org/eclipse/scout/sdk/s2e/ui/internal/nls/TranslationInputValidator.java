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
package org.eclipse.scout.sdk.s2e.ui.internal.nls;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.core.util.Strings;
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
    if (Strings.isEmpty(defaultTranslation)) {
      return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, -1, "The default translation must be set.", null);
    }
    return Status.OK_STATUS;
  }

  public static IStatus validateNlsKey(TranslationStoreStack project, String key) {
    return validateNlsKey(project, key, null);
  }

  public static IStatus validateNlsKey(TranslationStoreStack project, String key, Collection<String> exceptions) {
    if (Strings.isBlank(key)) {
      return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, -1, "Please specify a key.", null);
    }

    if (exceptions == null || !exceptions.contains(key)) {
      Optional<? extends ITranslationEntry> e = project.translation(key);
      if (e.isPresent()) {
        if (e.get().store().isEditable()) {
          return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, -1, "A key '" + key + "' already exists!", null);
        }
        return new Status(IStatus.WARNING, S2ESdkUiActivator.PLUGIN_ID, -1, "The key '" + key + "' overrides an inherited entry.", null);
      }
    }

    if (!ITranslation.KEY_REGEX.matcher(key).matches()) {
      return new Status(IStatus.ERROR, S2ESdkUiActivator.PLUGIN_ID, -1, "The key name is not valid.", null);
    }

    return Status.OK_STATUS;
  }
}
