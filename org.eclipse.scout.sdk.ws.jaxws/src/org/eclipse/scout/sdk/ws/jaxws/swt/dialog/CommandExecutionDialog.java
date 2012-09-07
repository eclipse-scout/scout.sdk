/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.dialog;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.IMarkerCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class CommandExecutionDialog extends TitleAreaDialog {

  // fields
  private FormToolkit m_toolkit;
  private SelectionListener m_selectAllSelectionListener;
  private String m_dialogTitle;
  private String m_headerMessage;

  // process fields
  private IMarkerCommand[] m_commands;

  // ui fields
  private Button m_selectAllButton;
  private Set<Button> m_commandSelectorFields;

  public CommandExecutionDialog(String dialogTitle) {
    super(ScoutSdkUi.getShell());
    m_dialogTitle = dialogTitle;

    m_toolkit = new FormToolkit(ScoutSdkUi.getDisplay());
    m_commandSelectorFields = new HashSet<Button>();
    setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE | getDefaultOrientation());
  }

  @Override
  protected final void configureShell(Shell shell) {
    super.configureShell(shell);
    if (m_dialogTitle != null) {
      shell.setText(m_dialogTitle);
    }
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    // create the top level composite for the dialog area
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

    if (m_headerMessage != null) {
      setMessage(m_headerMessage, IMessageProvider.WARNING);
    }
    setTitle(m_dialogTitle);
    setHelpAvailable(false);

    GridLayout gridLayout = new GridLayout();
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    gridLayout.verticalSpacing = 0;
    gridLayout.horizontalSpacing = 0;
    composite.setLayout(gridLayout);

    // Build the separator line
    Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
    titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    // Build the form
    ScrolledForm form = m_toolkit.createScrolledForm(composite);
    form.setLayoutData(new GridData(GridData.FILL_BOTH));

    // Build the form body
    Composite formBody = form.getBody();
    TableWrapLayout tableWrapLayout = new TableWrapLayout();
    tableWrapLayout.makeColumnsEqualWidth = true;
    tableWrapLayout.numColumns = 1;
    formBody.setLayout(tableWrapLayout);

    createCommandSections(formBody);

    // Build the separator line
    Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
    separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    m_selectAllButton = new Button(composite, SWT.CHECK);
    m_selectAllButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    ((GridData) m_selectAllButton.getLayoutData()).verticalIndent = 5;
    ((GridData) m_selectAllButton.getLayoutData()).horizontalIndent = 16;
    m_selectAllButton.setText(Texts.get("SelectAll"));
    m_selectAllButton.setEnabled(hasExecutableCommands());
    m_selectAllButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        for (Button button : m_commandSelectorFields) {
          button.setSelection(m_selectAllButton.getSelection());
          ((IMarkerCommand) button.getData()).setDoExecute(m_selectAllButton.getSelection());
        }
      }
    });

    form.updateToolBar();
    form.reflow(true);
    // form.getShell().layout();

    return composite;
  }

  protected void createCommandSections(Composite parent) {
    for (IMarkerCommand command : m_commands) {
      Composite section = createSection(parent, command.getProblemName(), command.getMarker().getAttribute(IMarker.MESSAGE, ""));

      int numOfColumns = 1;
      if (command.isExecutable()) {
        numOfColumns = 2;
      }

      section.setLayout(new GridLayout(numOfColumns, false));

      if (command.isExecutable()) {
        Button commandButton = m_toolkit.createButton(section, null, SWT.CHECK);
        m_commandSelectorFields.add(commandButton);
        GridData data = new GridData();
        data.verticalAlignment = SWT.TOP;
        data.horizontalAlignment = SWT.LEFT;
        commandButton.setLayoutData(data);
        commandButton.setData(command);
        commandButton.addSelectionListener(new SelectionAdapter() {

          @Override
          public void widgetSelected(SelectionEvent e) {
            Button checkBox = (Button) e.getSource();
            IMarkerCommand cmd = (IMarkerCommand) checkBox.getData();
            cmd.setDoExecute(checkBox.getSelection());
            checkForSelectAll();
          }
        });
      }

      Text solutionField = new Text(section, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
      solutionField.setText(command.getSolutionDescription());
      GridData gridData = new GridData();
      gridData.widthHint = 550; // this hint is just because of a bug in SWT. If not provided the text does not wrap at all.
      solutionField.setLayoutData(gridData);
    }
  }

  private boolean hasExecutableCommands() {
    for (IMarkerCommand command : m_commands) {
      if (command.isExecutable()) {
        return true;
      }
    }

    return false;
  }

  public void setCommands(IMarkerCommand[] commands) {
    m_commands = commands;
  }

  public void setHeaderMessage(String headerMessage) {
    m_headerMessage = headerMessage;
  }

  private void checkForSelectAll() {
    boolean selectAll = true;
    for (Button commandButton : m_commandSelectorFields) {
      selectAll = commandButton.getSelection();
      if (!selectAll) {
        break;
      }
    }

    m_selectAllButton.setSelection(selectAll);
  }

  private Composite createSection(Composite parent, String problemName, String problemDescription) {
    int style = Section.DESCRIPTION | Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED;

    // Build the section header
    Section section = m_toolkit.createSection(parent, style);
    section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
    section.setText(problemName);
    section.setDescription(problemDescription);

    // Build the section client
    Composite sectionClient = m_toolkit.createComposite(section);
    section.setClient(sectionClient);

    GridLayout gridLayout = new GridLayout(1, false);
    gridLayout.marginWidth = 0;
    gridLayout.verticalSpacing = 30;
    sectionClient.setLayout(gridLayout);

    Composite commandComposite = m_toolkit.createComposite(sectionClient);
    commandComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
    return commandComposite;
  }
}
