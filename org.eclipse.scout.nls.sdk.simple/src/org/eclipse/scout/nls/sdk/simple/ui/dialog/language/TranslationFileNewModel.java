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
package org.eclipse.scout.nls.sdk.simple.ui.dialog.language;

import java.util.Locale;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.nls.sdk.internal.jdt.INlsFolder;
import org.eclipse.scout.nls.sdk.internal.model.PropertyBasedModel;
import org.eclipse.scout.nls.sdk.internal.ui.dialog.ITranslationLocationChooserModel;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.simple.model.ws.project.SimpleNlsProject;
import org.eclipse.scout.nls.sdk.ui.action.TranslationNewAction;

/**
 * The description to generate a new translation file.
 * 
 * @see TranslationNewAction
 * @see TranslationFileNewDialog
 */
public class TranslationFileNewModel extends PropertyBasedModel implements ITranslationLocationChooserModel {

  public static final String PROP_LANGUAGE_ISO = "language";
  public static final String PROP_LANGUAGE_COUNTRY_ISO = "languageCountry";
  public static final String PROP_LANGUAGE_VARIANT = "languageVariant";
  public static final String PROP_FOLDER = "location";
  public static final String PROP_PATH = "path";

  private SimpleNlsProject m_nlsProject;

  public TranslationFileNewModel(SimpleNlsProject project) {
    m_nlsProject = project;
    setPath(new Path(m_nlsProject.getNlsType().getTranslationsFolderName()));
  }

  @Override
  public SimpleNlsProject getNlsProject() {
    return m_nlsProject;
  }

  @Override
  public IProject getProject() {
    return m_nlsProject.getNlsType().getType().getJavaProject().getProject();
  }

  public void setLanguageIso(String isoLanguage) {
    setPropertyString(PROP_LANGUAGE_ISO, isoLanguage);
  }

  public String getLanguageIso() {
    return getPropertyString(PROP_LANGUAGE_ISO);
  }

  public void setCountryIso(String isoCountry) {
    setPropertyString(PROP_LANGUAGE_COUNTRY_ISO, isoCountry);
  }

  public String getCountryIso() {
    return getPropertyString(PROP_LANGUAGE_COUNTRY_ISO);
  }

  public void setLanguageVariant(String languageVariant) {
    setPropertyString(PROP_LANGUAGE_VARIANT, languageVariant);
  }

  public String getLanguageVariant() {
    return getPropertyString(PROP_LANGUAGE_VARIANT);
  }

  // commodity
  public Language getLanguage() {
    String langIso = getLanguageIso();
    if (langIso == null) {
      return null;
    }
    String countryIso = getCountryIso();
    if (countryIso == null) countryIso = "";
    String variant = getLanguageVariant();
    if (variant == null) variant = "";
    return new Language(new Locale(langIso, countryIso, variant));
  }

  @Override
  public INlsFolder getFolder() {
    return (INlsFolder) getProperty(PROP_FOLDER);
  }

  public void setFolder(INlsFolder folder) {
    setProperty(PROP_FOLDER, folder);
  }

  public void setPath(IPath path) {
    setProperty(PROP_PATH, path);
  }

  @Override
  public IPath getPath() {
    return (IPath) getProperty(PROP_PATH);
  }
}
