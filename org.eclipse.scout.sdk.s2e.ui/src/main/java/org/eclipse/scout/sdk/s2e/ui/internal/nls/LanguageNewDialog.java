/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls;

import static java.util.function.Predicate.isEqual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.s.nls.ITranslationStore;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.fields.FieldToolkit;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.AbstractContentProviderAdapter;
import org.eclipse.scout.sdk.s2e.ui.fields.text.TextField;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class LanguageNewDialog extends TitleAreaDialog {

  private final String m_title;
  private final TranslationStoreStack m_stack;

  private String m_languageIso;
  private String m_countryIso;
  private ITranslationStore m_store;

  public LanguageNewDialog(Shell parentShell, TranslationStoreStack stack) {
    super(parentShell);
    m_stack = Ensure.notNull(stack);
    m_title = "Add a new language";
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(m_title);
  }

  @Override
  protected Control createContents(Composite parent) {
    Control contents = super.createContents(parent);
    setTitle(m_title);

    revalidate();

    setTitleImage(S2ESdkUiActivator.getImage(ISdkIcons.ScoutProjectNewWizBanner));
    setHelpAvailable(true);
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IScoutHelpContextIds.SCOUT_LANGUAGE_NEW_WIZARD_PAGE);
    return contents;
  }

  @Override
  protected Control createDialogArea(Composite p) {
    Composite rootArea = FieldToolkit.createGroupBox(p, "");

    // Language
    P_LanguageContentProvider langContentProvider = new P_LanguageContentProvider();
    ProposalTextField languageChooser = FieldToolkit.createProposalField(rootArea, "Language", TextField.TYPE_LABEL, 60);
    languageChooser.setContentProvider(langContentProvider);
    languageChooser.setLabelProvider(langContentProvider);
    languageChooser.addProposalListener(item -> setLanguageIso(Optional.ofNullable((Locale) item).map(Locale::getLanguage).orElse(null)));

    // Country
    P_CountryContentProvider countryContentProvider = new P_CountryContentProvider();
    ProposalTextField countryChooser = FieldToolkit.createProposalField(rootArea, "Country", TextField.TYPE_LABEL, 60);
    countryChooser.setContentProvider(countryContentProvider);
    countryChooser.setLabelProvider(countryContentProvider);
    countryChooser.addProposalListener(item -> setCountryIso(Optional.ofNullable((Locale) item).map(Locale::getCountry).orElse(null)));

    // Store
    ProposalTextField storeChooser = FieldToolkit.createTranslationStoreProposalField(rootArea, "Create in", stack(), 60);
    storeChooser.addProposalListener(item -> setStore((ITranslationStore) item));
    storeChooser.acceptProposal(stack().primaryEditableStore().orElse(null));

    attachGridData(storeChooser);
    attachGridData(countryChooser);
    attachGridData(languageChooser);

    // layout
    GridDataFactory
        .defaultsFor(rootArea)
        .align(SWT.FILL, SWT.FILL)
        .grab(true, true)
        .applyTo(rootArea);
    GridLayoutFactory
        .swtDefaults()
        .applyTo(rootArea);
    return rootArea;
  }

  private void revalidate() {
    Button okButton = getButton(IDialogConstants.OK_ID);
    if (okButton != null) {
      okButton.setEnabled(false);
    }

    // check that language is specified
    if (Strings.isBlank(getLanguageIso())) {
      setMessage("Please choose a language to add.", IMessageProvider.WARNING);
      return;
    }

    // check if folder is valid
    if (getStore() == null) {
      setMessage("Please choose a service in which the new language should be created.", IMessageProvider.WARNING);
      return;
    }

    // check if language already exists
    Language lang = new Language(new Locale(getLanguageIso(), Strings.notBlank(getCountryIso()).orElse("")));
    if (getStore().languages().anyMatch(isEqual(lang))) {
      setMessage("This language already exists.", IMessageProvider.WARNING);
      return;
    }

    setMessage("Adds a new Language.");
    if (okButton != null) {
      okButton.setEnabled(true);
    }
  }

  private static void attachGridData(Control c) {
    GridDataFactory
        .defaultsFor(c)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(c);
  }

  public TranslationStoreStack stack() {
    return m_stack;
  }

  public String getLanguageIso() {
    return m_languageIso;
  }

  protected void setLanguageIso(String languageIso) {
    m_languageIso = languageIso;
    revalidate();
  }

  public String getCountryIso() {
    return m_countryIso;
  }

  protected void setCountryIso(String countryIso) {
    m_countryIso = countryIso;
    revalidate();
  }

  public ITranslationStore getStore() {
    return m_store;
  }

  protected void setStore(ITranslationStore store) {
    m_store = store;
    revalidate();
  }

  private static final class P_LanguageContentProvider extends AbstractContentProviderAdapter {

    private final Image m_image;

    private P_LanguageContentProvider() {
      m_image = S2ESdkUiActivator.getImage(ISdkIcons.Comment);
    }

    @Override
    public String getText(Object element) {
      Locale loc = (Locale) element;
      String ds = loc.getDisplayLanguage();
      String l = loc.getLanguage();

      StringBuilder b = new StringBuilder(ds.length() + l.length() + 3);
      b.append(ds);
      b.append(" (");
      b.append(l);
      b.append(')');
      return b.toString();
    }

    @Override
    public Image getImage(Object element) {
      return m_image;
    }

    @Override
    protected Collection<?> loadProposals(IProgressMonitor monitor) {
      String[] isoLanguages = Locale.getISOLanguages();
      Collection<Locale> locs = new ArrayList<>(isoLanguages.length);
      for (String isoLang : isoLanguages) {
        locs.add(new Locale(isoLang));
      }
      return locs;
    }
  }

  private static final class P_CountryContentProvider extends AbstractContentProviderAdapter {
    private final Image m_image;

    private P_CountryContentProvider() {
      m_image = S2ESdkUiActivator.getImage(ISdkIcons.Comment);
    }

    @Override
    public String getText(Object element) {
      Locale loc = (Locale) element;
      String dc = loc.getDisplayCountry();
      String c = loc.getCountry();

      StringBuilder b = new StringBuilder(dc.length() + c.length() + 3);
      b.append(dc);
      b.append(" (");
      b.append(c);
      b.append(')');
      return b.toString();
    }

    @Override
    public Image getImage(Object element) {
      return m_image;
    }

    @Override
    protected Collection<?> loadProposals(IProgressMonitor monitor) {
      String[] isoCountries = Locale.getISOCountries();
      Collection<Locale> countries = new ArrayList<>(isoCountries.length);
      for (String isoCountry : isoCountries) {
        countries.add(new Locale("", isoCountry));
      }
      return countries;
    }
  }
}
