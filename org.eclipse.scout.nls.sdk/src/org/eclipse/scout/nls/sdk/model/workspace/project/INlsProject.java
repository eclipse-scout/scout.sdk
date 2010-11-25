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
package org.eclipse.scout.nls.sdk.model.workspace.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.osgi.util.NLS;
import org.eclipse.scout.nls.sdk.internal.jdt.INlsFolder;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.INlsConstants;
import org.eclipse.scout.nls.sdk.model.workspace.INlsType;
import org.eclipse.scout.nls.sdk.model.workspace.translationFile.ITranslationFile;

/**
 * The <b>INlsProject</b> is the basis class of an nls support in a plugin. Each Plugin may have more than one
 * NlsProject (*.nls file). In case of dynamic NLS support a NlsProject may have a parent and each translated text is
 * inherited by the child project or may be overwritten. In addition a NlsProject defines additional translations. A
 * translation is defined in a class extends {@link AbstractDynamicNls} or {@link NLS} as a static variable. All
 * translations are kept in resource bundles. Use the NLS support creation wizard to generate a NLS support in a plugin.
 * Furthermore all translations should be edited in the NLS Editor (edit the .nls file).
 *
 * @see INlsConstants
 */
public interface INlsProject extends INlsConstants {

  void addProjectListener(INlsProjectListener projectListener);

  void removeProjectListener(INlsProjectListener projectListener);

  IProject getProject();

  // /** returns all supported languages of the nls project.
  // * @return a list of all supported iso codes (e.g. en, de, it) or INlsConstants.LANGUGE_DEFAULT
  // */
  // List<String> getAllSupportedIsoCodes();

  /**
   * returns all keys of the current nls project. The keys are defined in the
   * nls class.
   *
   * @return
   */
  String[] getAllKeys();

  String getFullyQuallifiedNlsClassName();

  INlsType getNlsType();

  INlsEntry getEntry(String key);

  INlsEntry[] getEntries(String prefix, boolean caseSensitive);

  INlsEntry[] getAllEntries();

  /**
   * @return
   */
  INlsProject getParent();

  /**
   * @return
   */
  Language[] getAllLanguages();

  boolean containsLanguage(Language languge);

  /**
   * @return
   */
  public String getName();

  /**
   * @param row
   */
  void updateRow(INlsEntry row);

  /**
   * @param row
   * @param createLanguageFiles
   * @param monitor
   */
  void updateRow(INlsEntry row, IProgressMonitor monitor);

  void updateKey(INlsEntry row, String newKey, IProgressMonitor monitor);

  void createTranslationFile(Language language, INlsFolder folder, IProgressMonitor monitor) throws CoreException;

  /**
   * To find the best matching language supported of the project:
   * <p>
   *
   * <pre>
   * e.g. Supported languages = [default, en, de , de_ch, fr]
   * INPUT: de_ch OUTPUT: de_ch
   * INPUT: de_de OUTPUT: de
   * INPUT: it OUTPUT: default
   * </pre>
   *
   * @param language
   * @return the project supporting language best matching to the given language
   */
  Language getBestMatchingProjectLanguage(Language language);

  /**
   * @return
   */
  ITranslationFile[] getTranslationFiles();

  /**
   * @return
   */
  Language getDevelopmentLanguage();

  /**
   * @return
   */
  IType[] getReferenceTypes();
}
