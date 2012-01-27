/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.nls.sdk.model;

import java.util.Map;

import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;

/**
 * <h4>INlsEntry</h4> The representation of a key with all available translations. Used for
 * modifications and creation of new entrsies.
 * Use the copy constructor of NlsEntry to change values and modify the
 * translations.
 * 
 * @see NlsEntry
 */
public interface INlsEntry {
  public static final int TYPE_LOCAL = 1;
  public static final int TYPE_INHERITED = 2;

  INlsProject getProject();

  int getType();

  /**
   * @return the key of the entry
   */
  public String getKey();

  /**
   * @param language
   * @return the translation in the given language
   */
  public String getTranslation(Language language);

  /**
   * @param language
   * @param defaultIfNotExist
   *          true to return the default translation if the desired language is not
   *          available.
   * @return the translation in the given language
   */
  public String getTranslation(Language language, boolean defaultIfNotExist);

  public int getReferenceCount();

  /**
   * @return
   */
  Map<Language, String> getAllTranslations();

}
