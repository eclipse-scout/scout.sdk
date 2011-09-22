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
package org.eclipse.scout.sdk.ui.view.properties.part;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.extensions.view.property.IPropertyViewPart;
import org.eclipse.scout.sdk.ui.util.TableWrapLayoutEx;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * <h3>AbstractSectionBasedPart</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 20.07.2010
 */
public abstract class AbstractSectionBasedPart implements IPropertyViewPart {

  private FormToolkit m_formToolkit;
  private ScrolledForm m_form;
  private HashMap<String, Section> m_sections;
  private Composite m_sectionContainer;
  private ArrayList<IStatus> m_stati;
  private Label m_statusIcon;
  private Label m_statusText;
  private Composite m_statusArea;

  public AbstractSectionBasedPart() {
    m_stati = new ArrayList<IStatus>();
  }

  @Override
  public final void createPart(Composite parent) {
    init();
    m_formToolkit = new FormToolkit(parent.getDisplay());
    m_form = m_formToolkit.createScrolledForm(parent);
    m_form.setDelayedReflow(true);
    m_form.setText(Texts.get("Properties"));
    createHeadInternal(m_form);
    // sections
    Composite formBody = m_form.getBody();

    // root pane is used in order of layout errors in TableWrapLayout with
    // scrollbars.
    m_sectionContainer = getFormToolkit().createComposite(formBody);
    formBody.setLayout(new FillLayout());
    TableWrapLayoutEx layout = new TableWrapLayoutEx();
    layout.makeColumnsEqualWidth = true;
    layout.numColumns = 1;
    layout.horizontalSpacing = 3;
    layout.verticalSpacing = 3;
    layout.rightMargin = 3;
    layout.leftMargin = 3;
    layout.topMargin = 3;
    layout.bottomMargin = 3;
    m_sectionContainer.setLayout(layout);
    m_sections = new HashMap<String, Section>();
    createSections();
    m_form.updateToolBar();
    m_form.reflow(true);
  }

  @Override
  public void init(IMemento memento) {

  }

  @Override
  public void save(IMemento memento) {

  }

  @Override
  public final void dispose() {
    cleanup();
    getFormToolkit().dispose();
    getForm().dispose();
  }

  private void createHeadInternal(ScrolledForm form) {
    Composite headComposite = getFormToolkit().createComposite(m_form.getForm().getHead());
    headComposite.setBackground(headComposite.getDisplay().getSystemColor(SWT.COLOR_BLUE));
    Control head = createHead(headComposite);
    Control statusArea = createStatusControl(headComposite);
    m_form.setHeadClient(headComposite);

    // layout
    GridLayout layout = new GridLayout(1, true);
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    headComposite.setLayout(layout);
    head.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH));
    GridData statusData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
    statusArea.setLayoutData(statusData);
    updateStatus();
  }

  private Control createStatusControl(Composite parent) {
    m_statusArea = getFormToolkit().createComposite(parent);
    m_statusIcon = getFormToolkit().createLabel(m_statusArea, "");
    m_statusText = getFormToolkit().createLabel(m_statusArea, "", SWT.LEFT);

    // layout
    m_statusArea.setLayout(new GridLayout(2, false));
    m_statusIcon.setLayoutData(new GridData(GridData.FILL_VERTICAL));
    m_statusText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH));
    return m_statusArea;
  }

  protected Control createHead(Composite parent) {
    Composite headArea = getFormToolkit().createComposite(parent);
    Label title = getFormToolkit().createLabel(headArea, "", SWT.WRAP | SWT.READ_ONLY);
    // layout
    headArea.setLayout(new GridLayout(1, true));
    GridData titleData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
    titleData.widthHint = 100;
    title.setLayoutData(titleData);
    return headArea;
  }

  protected void createSections() {
  }

  public ISection getSection(String sectionId) {
    Section section = m_sections.get(sectionId);
    return section;
  }

  /**
   * @param sectionId
   *          an identifier to access the section with {@link AbstractSectionBasedPart#getSection(String)}
   * @param title
   *          the title of the section
   * @return the section - create controls with the sections client {@link ISection#getSectionClient()} as parent
   */
  protected final ISection createSection(String sectionId, String title) {
    return createSection(sectionId, title, null, true);
  }

  /**
   * @param sectionId
   *          an identifier to access the section with {@link AbstractSectionBasedPart#getSection(String)}
   * @param title
   *          the title of the section
   * @param description
   *          the description of the section. If null or empty the section will not have a description part.
   * @param twistle
   *          true to ensure the section is collapsable
   * @return the section - create controls with the sections client {@link ISection#getSectionClient()} as parent
   */
  protected final ISection createSection(String sectionId, String title, String description, boolean twistle) {
    return createSection(sectionId, title, description, twistle, null);
  }

  /**
   * @param sectionId
   *          an identifier to access the section with {@link AbstractSectionBasedPart#getSection(String)}
   * @param title
   *          the title of the section.
   * @param description
   *          the description of the section. If null or empty the section will not have a description part.
   * @param twistle
   *          true to ensure the section is collapsable.
   * @param siblingSectionId
   *          an section id to insert the new section before the sibling section. null to append at the end.
   * @return the section - create controls with the sections client {@link ISection#getSectionClient()} as parent
   */
  protected final ISection createSection(String sectionId, String title, String description, boolean twistle, String siblingSectionId) {

    Section section = m_sections.get(sectionId);
    if (section == null) {
      int style = org.eclipse.ui.forms.widgets.Section.TITLE_BAR | org.eclipse.ui.forms.widgets.Section.EXPANDED;
      if (twistle) {
        style |= org.eclipse.ui.forms.widgets.Section.TWISTIE;
      }
      boolean hasDescription = !StringUtility.isNullOrEmpty(description);
      if (hasDescription) {
        style = style | org.eclipse.ui.forms.widgets.Section.DESCRIPTION;
      }
      Section sibling = null;
      // sibling
      if (siblingSectionId != null) {
        sibling = m_sections.get(siblingSectionId);
      }
      section = new Section(sectionId, getForm());
      section.createSection(getFormToolkit(), m_sectionContainer, title, description, style, sibling);
//      section.addExpansionListener(new ExpansionAdapter() {
//        @Override
//        public void expansionStateChanged(ExpansionEvent e) {
//          getForm().reflow(true);
//        }
//      });
//      section.setText(title);
//      if (hasDescription) {
//        section.setDescription(description);
//      }
//
//      // layout
//      TableWrapDataEx data = new TableWrapDataEx(TableWrapData.FILL_GRAB);
//      section.setLayoutData(data);
//      Composite sectionClient = getFormToolkit().createComposite(section);
//      section.setClient(sectionClient);
//      GridLayout layout = new GridLayout(1, true);
//      layout.horizontalSpacing = 3;
//      layout.verticalSpacing = 4;
//      layout.marginHeight = 0;
//      layout.marginWidth = 0;
//      sectionClient.setLayout(layout);
      m_sections.put(sectionId, section);
    }
    return section;
  }

  public void addStatus(IStatus status) {
    m_stati.add(status);
    updateStatus();
  }

  public boolean removeStatus(IStatus status) {
    boolean removed = m_stati.remove(status);
    updateStatus();
    return removed;
  }

  private void updateStatus() {
    if (getForm() != null && !getForm().isDisposed()) {
      MultiStatus multiStatus = new MultiStatus(ScoutSdkUi.PLUGIN_ID, -1, null, null);
      for (IStatus s : m_stati) {
        multiStatus.add(s);
      }
      IStatus status = getHighestSeverityStatus(multiStatus, Status.OK_STATUS);
      if (status == Status.OK_STATUS) {
        status = null;
      }

      GridData ld = (GridData) m_statusArea.getLayoutData();
      if (status == null) {
        ld.exclude = true;
      }
      else {
        ld.exclude = false;
        Image img = null;
        switch (status.getSeverity()) {
          case IStatus.ERROR:
            img = ScoutSdkUi.getImage(ScoutSdkUi.StatusError);
            break;
          case IStatus.WARNING:
            img = ScoutSdkUi.getImage(ScoutSdkUi.StatusWarning);
            break;
          case IStatus.INFO:
            img = ScoutSdkUi.getImage(ScoutSdkUi.StatusInfo);
            break;
        }
        m_statusIcon.setImage(img);
        m_statusText.setText(status.getMessage());
      }
      getForm().reflow(true);
//      m_sectionContainer.layout(true, true);
    }

  }

  private IStatus getHighestSeverityStatus(IStatus status, IStatus highestSeverity) {
    if (status.isMultiStatus()) {
      for (IStatus child : status.getChildren()) {
        highestSeverity = getHighestSeverityStatus(child, highestSeverity);
      }
      return highestSeverity;
    }
    else {
      if (highestSeverity.getSeverity() < status.getSeverity()) {
        highestSeverity = status;
      }
      return highestSeverity;
    }
  }

  /**
   * @return the form
   */
  public ScrolledForm getForm() {
    return m_form;
  }

  protected FormToolkit getFormToolkit() {
    return m_formToolkit;
  }

  /**
   * Is called before the sections are created to do some initialization work.
   */
  protected void init() {
  }

  /**
   * Is called to release resources or unregister from listeners.
   */
  protected void cleanup() {
  }
}
