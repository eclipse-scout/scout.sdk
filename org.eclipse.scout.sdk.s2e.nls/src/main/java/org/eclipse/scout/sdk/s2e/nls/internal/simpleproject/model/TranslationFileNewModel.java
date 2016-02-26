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
package org.eclipse.scout.sdk.s2e.nls.internal.simpleproject.model;

import java.beans.PropertyChangeListener;
import java.util.Locale;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.sdk.core.util.BasicPropertySupport;
import org.eclipse.scout.sdk.s2e.nls.internal.simpleproject.SimpleNlsProject;
import org.eclipse.scout.sdk.s2e.nls.internal.simpleproject.ui.TranslationFileNewDialog;
import org.eclipse.scout.sdk.s2e.nls.model.Language;

/**
 * The description to generate a new translation file.
 *
 * @see TranslationFileNewDialog
 */
public class TranslationFileNewModel implements ITranslationLocationChooserModel {

  public static final String PROP_LANGUAGE_ISO = "language";
  public static final String PROP_LANGUAGE_COUNTRY_ISO = "languageCountry";
  public static final String PROP_LANGUAGE_VARIANT = "languageVariant";
  public static final String PROP_FOLDER = "location";
  public static final String PROP_PATH = "path";

  private final SimpleNlsProject m_nlsProject;
  private final BasicPropertySupport m_propertySupport;

  public TranslationFileNewModel(SimpleNlsProject project) {
    m_nlsProject = project;
    m_propertySupport = new BasicPropertySupport(this);
    setPath(new Path(m_nlsProject.getNlsType().getTranslationsFolderName()));
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
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
    m_propertySupport.setPropertyString(PROP_LANGUAGE_ISO, isoLanguage);
  }

  public String getLanguageIso() {
    return m_propertySupport.getPropertyString(PROP_LANGUAGE_ISO);
  }

  public void setCountryIso(String isoCountry) {
    m_propertySupport.setPropertyString(PROP_LANGUAGE_COUNTRY_ISO, isoCountry);
  }

  public String getCountryIso() {
    return m_propertySupport.getPropertyString(PROP_LANGUAGE_COUNTRY_ISO);
  }

  public void setLanguageVariant(String languageVariant) {
    m_propertySupport.setPropertyString(PROP_LANGUAGE_VARIANT, languageVariant);
  }

  public String getLanguageVariant() {
    return m_propertySupport.getPropertyString(PROP_LANGUAGE_VARIANT);
  }

  // commodity
  public Language getLanguage() {
    String langIso = getLanguageIso();
    if (langIso == null) {
      return null;
    }
    String countryIso = getCountryIso();
    if (countryIso == null) {
      countryIso = "";
    }
    String variant = getLanguageVariant();
    if (variant == null) {
      variant = "";
    }
    return new Language(new Locale(langIso, countryIso, variant));
  }

  @Override
  public IFolder getFolder() {
    return (IFolder) m_propertySupport.getProperty(PROP_FOLDER);
  }

  public void setFolder(IFolder folder) {
    m_propertySupport.setProperty(PROP_FOLDER, folder);
  }

  public void setPath(IPath path) {
    m_propertySupport.setProperty(PROP_PATH, path);
  }

  @Override
  public IPath getPath() {
    return (IPath) m_propertySupport.getProperty(PROP_PATH);
  }
}
