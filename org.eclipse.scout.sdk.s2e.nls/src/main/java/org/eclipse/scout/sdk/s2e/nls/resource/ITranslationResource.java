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
package org.eclipse.scout.sdk.s2e.nls.resource;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.sdk.s2e.nls.model.Language;

/** <h4>ITranslationResource</h4> */
public interface ITranslationResource {

  void addTranslationResourceListener(ITranslationResourceListener listener);

  void removeTranslationResourceListener(ITranslationResourceListener listener);

  boolean isReadOnly();

  /**
   * @return
   */
  Language getLanguage();

  /**
   * @param monitor
   */
  void reload(IProgressMonitor monitor);

  /**
   * @return
   */
  Set<String> getAllKeys();

  /**
   * @param key
   * @return
   */
  String getTranslation(String key);

  /**
   * @param key
   * @param newText
   * @param nullProgressMonitor
   */
  void updateText(String key, String newText, boolean fireEvent, IProgressMonitor monitor);

  /**
   * @param key
   * @param monitor
   * @return
   */
  IStatus remove(String key, IProgressMonitor monitor);

  /**
   * @param oldKey
   * @param newKey
   * @param monitor
   * @return
   */
  IStatus updateKey(String oldKey, String newKey, IProgressMonitor monitor);

  /**
   * @return
   */
  boolean isDefaultLanguage();

  void commitChanges(IProgressMonitor monitor);

}
