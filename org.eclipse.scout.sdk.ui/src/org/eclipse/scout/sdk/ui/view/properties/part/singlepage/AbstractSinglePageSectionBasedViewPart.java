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
package org.eclipse.scout.sdk.ui.view.properties.part.singlepage;

import java.util.HashMap;

import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.ui.extensions.view.property.ISinglePropertyViewPart;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.properties.part.AbstractSectionBasedPart;
import org.eclipse.scout.sdk.ui.view.properties.part.Section;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>AbstractSinglePageSectionBasedViewPart</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 23.07.2010
 */
public abstract class AbstractSinglePageSectionBasedViewPart extends AbstractSectionBasedPart implements ISinglePropertyViewPart {

  private final static HashMap<String, HashMap<String, Boolean>> expansionSettings = new HashMap<String, HashMap<String, Boolean>>();

  private IPage m_page;

  @Override
  public IPage getPage() {
    return m_page;
  }

  @Override
  public void setPage(IPage page) {
    m_page = page;
  }

  /**
   * Gets a string that uniquely identifies this part for the current page.<br>
   * Override this method to use a more precise key.
   * 
   * @return The default implementation returns a string representing the page instance this part is attached to or null
   *         if no page is present.
   */
  protected String getPartKey() {
    IPage p = getPage();
    if (p == null) return null;
    return p.toString();
  }

  @Override
  protected void cleanup() {
    cacheSectionExpansionState();
    super.cleanup();
  }

  private void cacheSectionExpansionState() {
    final Section[] sections = getSections();
    HashMap<String, Boolean> pageCache = expansionSettings.get(getPartKey());
    if (pageCache == null) {
      pageCache = new HashMap<String, Boolean>(sections.length);
      expansionSettings.put(getPartKey(), pageCache);
    }
    for (Section s : sections) {
      pageCache.put(s.getSectionId(), s.isExpanded());
    }
  }

  /**
   * Gets if the section with given Id was expanded when used the last time for the page of this part.<br>
   * If no last state exist, the given default value is returned.
   * 
   * @param sectionId
   *          The id of the section.
   * @param defaultValue
   *          The default value to return when this section was never used before for the page of this part.
   * @return If the section was expanded the last time or the default value if not known.
   */
  protected boolean wasSectionExpanded(String sectionId, boolean defaultValue) {
    TriState state = wasSectionExpanded(sectionId);
    if (state == TriState.UNDEFINED) return defaultValue;
    else return state.getBooleanValue();
  }

  /**
   * Gets the expansion state of the section with the given id like it was lastly used for the page of this part.
   * 
   * @param sectionId
   *          The id of the section.
   * @return If the given section for the current page was expanded when used the last time. The <code>TriState</code>
   *         has the value <code>UNDEFINED</code> if no last usage for this section and page is stored so far.
   * @see TriState
   */
  protected TriState wasSectionExpanded(String sectionId) {
    HashMap<String, Boolean> pageCache = expansionSettings.get(getPartKey());
    if (pageCache == null) return TriState.UNDEFINED;

    Boolean expanded = pageCache.get(sectionId);
    if (expanded == null) return TriState.UNDEFINED;

    if (expanded) return TriState.TRUE;
    else return TriState.FALSE;
  }

  @Override
  protected Control createHead(Composite parent) {
    Composite headArea = getFormToolkit().createComposite(parent);
    Label title = getFormToolkit().createLabel(headArea, getPage().getName(), SWT.WRAP | SWT.READ_ONLY);
    // layout
    headArea.setLayout(new GridLayout(1, true));
    GridData titleData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
    titleData.widthHint = 100;
    title.setLayoutData(titleData);
    return headArea;
  }
}
