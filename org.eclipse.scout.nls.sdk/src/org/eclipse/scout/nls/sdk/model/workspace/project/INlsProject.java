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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.translationResource.ITranslationResource;
import org.eclipse.scout.nls.sdk.ui.action.INewLanguageContext;

/**
 * The <b>INlsProject</b> is the basis class of an NLS support in a plugin. Each plugin may have more than one
 * NlsProject. A NlsProject may have a parent and each translated text is inherited by the child project or may
 * be overwritten.
 * Furthermore all translations should be edited in the NLS Editor.
 */
public interface INlsProject {

  /**
   * Adds an event listener to this project.
   * The last listener added is the first to be called.
   * If a listener is added that already exists in the list, the listener is added an additional time.
   *
   * @param projectListener
   *          The new listener.
   */
  void addProjectListener(INlsProjectListener projectListener);

  /**
   * Removes all registrations of the given listener.
   * the listeners are identified using reference equality (==).
   *
   * @param projectListener
   */
  void removeProjectListener(INlsProjectListener projectListener);

  /**
   * Clears the cache and completely reloads the translations from the providers.
   */
  void refresh();

  /**
   * returns all keys of the current NLS project.
   *
   * @return
   */
  Set<String> getAllKeys();

  /**
   * Gets the name of the nls project. This e.g. used in the editor to display which project is displayed.
   *
   * @return The project name.
   */
  String getName();

  /**
   * Gets the entry with the given key.
   *
   * @param key
   *          The key to search for.
   * @return The cached entry.
   */
  INlsEntry getEntry(String key);

  /**
   * Returns all entries with a key starting with the given prefix.
   *
   * @param prefix
   *          The prefix to search for
   * @param caseSensitive
   *          if true, the search is done case sensitive.
   * @return The entries found.
   */
  List<INlsEntry> getEntries(String prefix, boolean caseSensitive);

  /**
   * Get all entries in this project.
   *
   * @return The complete list.
   */
  List<INlsEntry> getAllEntries();

  /**
   * Gets the parent project or null if no parent exists.
   *
   * @return
   */
  INlsProject getParent();

  /**
   * Gets all languages of this project.
   *
   * @return The existing languages.
   */
  List<Language> getAllLanguages();

  /**
   * Checks whether the given language exists in this project.
   *
   * @param languge
   *          The language to search.
   * @return True if the given language exists.
   */
  boolean containsLanguage(Language languge);

  /**
   * Updates (or creates if not existing) the given row.
   * If the key is inherited, this method does nothing.
   *
   * @param row
   *          The row to update or create.
   * @param monitor
   */
  void updateRow(INlsEntry row, IProgressMonitor monitor);

  /**
   * Updates (or creates if not existing) the given row.
   * If the key is inherited, this method does nothing.
   *
   * @param row
   *          The row to update or create.
   * @param flush
   *          Specifis if the changes should directly be persisted to the underlying resource.
   * @param monitor
   */
  void updateRow(INlsEntry row, boolean flush, IProgressMonitor monitor);

  /**
   * Changes the key of the entry with the same key as the given row
   *
   * @param row
   *          The entry with the same key as in row is updated.
   * @param newKey
   *          The new key of the entry found.
   * @param monitor
   */
  void updateKey(INlsEntry row, String newKey, IProgressMonitor monitor);

  /**
   * Gets a fresh NewLanguageContext that can be used to create new languages in a project specific way.
   *
   * @return a newly created NewLanguageContext
   */
  INewLanguageContext getTranslationCreationContext();

  IStatus removeEntries(Collection<INlsEntry> entries, IProgressMonitor m);

  /**
   * To find the best matching language supported of the project:
   * <p>
   *
   * <pre>
   * Example:<br/>
   * Supported languages = [default, en, de , de_ch, fr]
   * INPUT: de_ch OUTPUT: de_ch
   * INPUT: de_de OUTPUT: de
   * INPUT: it OUTPUT: default
   * </pre>
   *
   * </p>
   *
   * @param language
   * @return the best matching language supported by this project
   */
  Language getBestMatchingProjectLanguage(Language language);

  /**
   * Gets the best matching language of the eclipse instance running.
   *
   * @return
   */
  Language getDevelopmentLanguage();

  /**
   * Gets the type that is used to access translations at runtime (e.g. Texts or TEXTS)
   *
   * @return
   */
  IType getNlsAccessorType();

  /**
   * gets the translation resource for the given language.
   *
   * @param language
   * @return
   */
  ITranslationResource getTranslationResource(Language language);

  /**
   * Generates a new key based on the given input text.
   *
   * @param baseText
   *          The input text a key should be generated for.
   * @return The key generated from the given input text. The result may already exist in the NlsProject.
   */
  String generateKey(String baseText);

  /**
   * Generates a new key based on the given input text.
   *
   * @param baseText
   *          The input text a key should be generated for.
   * @return a new key. it is guaranteed, that this key does not exist in this project at the time of key generation.
   */
  String generateNewKey(String baseText);

  /**
   * specifies if this project contains read-only resources or not.
   *
   * @return true if this project contains at least one read-only resource.
   */
  boolean isReadOnly();

  /**
   * Flushes all changes done to this NLS project to the underlying resources.
   *
   * @param monitor
   */
  void flush(IProgressMonitor monitor);
}
