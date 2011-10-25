package org.eclipse.scout.nls.sdk.services.operation;

import java.util.HashSet;

import org.eclipse.scout.nls.sdk.internal.model.PropertyBasedModel;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class NewNlsServiceModel extends PropertyBasedModel {

  private static final String PROP_TRANSLATION_FOLDER = "translationFolder";
  private static final String PROP_TRANSLATION_FILE = "translationFile";
  private static final String PROP_CLASS_NAME = "className";

  private final HashSet<String> m_languagesToCreate;
  private final IScoutBundle m_bundle;

  public NewNlsServiceModel(IScoutBundle b) {
    m_languagesToCreate = new HashSet<String>();
    m_languagesToCreate.add(null); // default language
    m_bundle = b;
  }

  public void addLanguage(String langName) {
    m_languagesToCreate.add(langName);
  }

  public void removeLanguage(String langName) {
    m_languagesToCreate.remove(langName);
  }

  public String[] getLanguages() {
    return m_languagesToCreate.toArray(new String[m_languagesToCreate.size()]);
  }

  public void setClassName(String input) {
    if (input == null || input.equals("")) {
      input = null;
    }
    setPropertyString(PROP_CLASS_NAME, input);
  }

  public String getClassName() {
    return getPropertyString(PROP_CLASS_NAME);
  }

  public void setTranslationFolder(String folder) {
    setPropertyString(PROP_TRANSLATION_FOLDER, folder);
  }

  public String getTranslationFolder() {
    return getPropertyString(PROP_TRANSLATION_FOLDER);
  }

  public void setTranlationFileName(String filename) {
    setPropertyString(PROP_TRANSLATION_FILE, filename);
  }

  public String getTranlationFileName() {
    return getPropertyString(PROP_TRANSLATION_FILE);
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }
}
