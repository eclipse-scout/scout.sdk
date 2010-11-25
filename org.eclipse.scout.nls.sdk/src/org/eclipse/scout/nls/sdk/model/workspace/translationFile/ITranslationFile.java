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
package org.eclipse.scout.nls.sdk.model.workspace.translationFile;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.nls.sdk.model.util.Language;

/** <h4>ITranslationFile</h4> */
public interface ITranslationFile {

  void addTranslationFileListener(ITranslationFileListener listener);

  void removeTranslationFileListener(ITranslationFileListener listener);

  /**
   * @return
   */
  Language getLanguage();

  /**
   * @return
   */
  String getName();

  /**
   * @param monitor
   */
  void reload(IProgressMonitor monitor);

  /**
   * @return
   */
  String[] getAllKeys();

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
  void updateText(String key, String newText, IProgressMonitor monitor);

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
