/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.model;

import java.util.Map;

import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;

/**
 * <h4>INlsEntry</h4> The representation of a key with all available translations. Used for modifications and creation
 * of new entries. Use the copy constructor of NlsEntry to change values and modify the translations.
 *
 * @see NlsEntry
 */
public interface INlsEntry {

  /**
   * Describes an entry that belongs to the {@link INlsProject} itself.
   */
  int TYPE_LOCAL = 1;

  /**
   * Describes an entry that is inherited from a parent {@link INlsProject}.
   */
  int TYPE_INHERITED = 2;

  /**
   * @return The {@link INlsProject} this entry belongs to.
   */
  INlsProject getProject();

  /**
   * The type of entry.
   *
   * @return One of {@link #TYPE_LOCAL} or {@link #TYPE_INHERITED}
   */
  int getType();

  /**
   * @return the key of the entry
   */
  String getKey();

  /**
   * Gets the translation text for the given {@link Language}
   *
   * @param The
   *          {@link Language} for which the text should be returned.
   * @return the translation in the given {@link Language}
   */
  String getTranslation(Language language);

  /**
   * Gets the translation text for the given {@link Language} or the translation text of the default language if the
   * given {@link Language} cannot be found.
   *
   * @param language
   *          The {@link Language} for which the text should be returned.
   * @param defaultIfNotExist
   *          true if the translation text of the {@link Language#LANGUAGE_DEFAULT} should be returned if no translation
   *          for the given {@link Language} can be found. False otherwise.
   * @return The translation text or null.
   */
  String getTranslation(Language language, boolean defaultIfNotExist);

  /**
   * @return A copy of all translation mappings for this entry.
   */
  Map<Language, String> getAllTranslations();

}
