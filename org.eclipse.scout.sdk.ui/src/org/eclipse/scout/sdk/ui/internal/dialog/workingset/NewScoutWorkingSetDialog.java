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
package org.eclipse.scout.sdk.ui.internal.dialog.workingset;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.TextField;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerSettingsSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link NewScoutWorkingSetDialog}</h3> ...
 * 
 * @author mvi
 * @since 3.9.0 04.04.2013
 */
public class NewScoutWorkingSetDialog extends Dialog {
  public static final String PROP_WORKING_SET_NAME = "scoutWorkingSetName";

  private final BasicPropertySupport m_propertySupport;
  private final String[] m_existingSets;

  public NewScoutWorkingSetDialog(Shell parentShell, String[] existing) {
    super(parentShell);
    m_propertySupport = new BasicPropertySupport(this);
    m_existingSets = existing;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Texts.get("NewScoutWorkingSet"));
  }

  public String getWorkingSetName() {
    return (String) m_propertySupport.getPropertyString(PROP_WORKING_SET_NAME);
  }

  @Override
  protected Control createButtonBar(Composite parent) {
    Control ret = super.createButtonBar(parent);
    getButton(OK).setEnabled(false);
    return ret;
  }

  private static boolean isValid(String txt, String[] usedNames) {
    if (!isValid(txt, (String) null)) {
      return false;
    }

    // working set must not exist yet
    if (usedNames != null && usedNames.length > 0) {
      for (String s : usedNames) {
        if (CompareUtility.equals(txt, s)) {
          return false;
        }
      }
    }

    return true;
  }

  public static boolean isValid(String txt, String oldName) {
    if (!StringUtility.hasText(txt)) {
      return false;
    }

    // working set must not exist yet
    if (!CompareUtility.equals(txt, oldName)) {
      if (PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(txt) != null) {
        return false;
      }
    }

    // name cannot contain the delimiter used to persist the sets
    if (txt.indexOf(ScoutExplorerSettingsSupport.DELIMITER) >= 0) {
      return false;
    }

    // other working set name is reserved
    if (ScoutExplorerSettingsSupport.OTHER_PROJECTS_WORKING_SET_NAME.equals(txt)) {
      return false;
    }
    return true;
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite rootArea = new Composite(parent, SWT.NONE);
    final TextField f = new TextField(rootArea, Texts.get("Name"));
    f.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        String newText = f.getText();
        boolean valid = isValid(newText, m_existingSets);
        if (valid) {
          m_propertySupport.setProperty(PROP_WORKING_SET_NAME, newText.trim());
        }
        getButton(OK).setEnabled(valid);
      }
    });

    // layout
    GridLayout gl = new GridLayout();
    gl.marginWidth = 15;
    gl.marginHeight = 15;
    rootArea.setLayout(gl);
    rootArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

    GridData gd = new GridData(SWT.FILL);
    gd.widthHint = 190;
    f.setLayoutData(gd);

    return rootArea;
  }
}
