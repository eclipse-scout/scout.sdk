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
package org.eclipse.scout.sdk.ui.fields.javacode;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.validation.JavaElementValidator;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>CodeField</h3> ...
 */
public class EntityTextField extends Composite {

  private final int m_labelPercentage;
  private final IScoutProject m_project;

  private StyledText m_text;
  private Label m_label;

  private P_EntityContentProvider m_contentProvider;
  private ContentProposalAdapter m_proposalAdapter;

  public EntityTextField(Composite parent, String labelName, int labelPercentage, IScoutProject p) {
    super(parent, SWT.NONE);
    m_labelPercentage = labelPercentage;
    m_project = p;
    setLayout(new FormLayout());
    createContent(this);
    setLabelText(labelName);
  }

  protected void createContent(Composite parent) {
    m_label = new Label(parent, SWT.NONE);
    m_label.setAlignment(SWT.RIGHT);

    m_text = new StyledText(parent, SWT.SINGLE | SWT.BORDER);
    m_text.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        if (!getStatus().isOK()) {
          m_text.setForeground(m_text.getDisplay().getSystemColor(SWT.COLOR_RED));
        }
        else {
          m_text.setForeground(null);
        }
      }
    });

    ControlDecoration deco = new ControlDecoration(m_text, SWT.LEFT | SWT.TOP);
    deco.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ContentAssist));

    m_contentProvider = new P_EntityContentProvider();
    m_proposalAdapter = new ContentProposalAdapter(m_text, m_contentProvider, m_contentProvider, KeyStroke.getInstance(SWT.CONTROL, ' '), null);
    m_proposalAdapter.setLabelProvider(m_contentProvider);
    m_proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

    // layout
    FormData labelData = new FormData();
    labelData.top = new FormAttachment(0, 4);
    labelData.left = new FormAttachment(0, 0);
    labelData.right = new FormAttachment(getLabelPercentage(), 0);
    labelData.bottom = new FormAttachment(100, 0);
    m_label.setLayoutData(labelData);

    FormData textData = new FormData();
    textData.top = new FormAttachment(0, 0);
    textData.left = new FormAttachment(m_label, 5);
    textData.right = new FormAttachment(100, 0);
    textData.bottom = new FormAttachment(100, 0);
    m_text.setLayoutData(textData);
  }

  public void setLabelText(String text) {
    m_label.setText(text);
  }

  public String getLabelText() {
    return m_label.getText();
  }

  public String getText() {
    return m_text.getText();
  }

  public void setText(String s) {
    if (s == null) {
      s = "";
    }
    m_text.setText(s);
  }

  public IStatus getStatus() {
    return JavaElementValidator.validatePackageName(m_text.getText());
  }

  public void addModifyListener(ModifyListener listener) {
    m_text.addModifyListener(listener);
  }

  public void removeModifyListener(ModifyListener listener) {
    m_text.removeModifyListener(listener);
  }

  @Override
  public void addFocusListener(FocusListener listener) {
    m_text.addFocusListener(listener);
  }

  @Override
  public void removeFocusListener(FocusListener listener) {
    m_text.removeFocusListener(listener);
  }

  public int getLabelPercentage() {
    return m_labelPercentage;
  }

  private static class P_EntityProposal implements IContentProposal, Comparable<P_EntityProposal> {

    private P_EntityProposal(String c) {
      m_content = c;
    }

    private String m_content;

    @Override
    public String getContent() {
      return m_content;
    }

    @Override
    public int getCursorPosition() {
      return 0;
    }

    @Override
    public String getLabel() {
      return m_content;
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public int hashCode() {
      return m_content.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof P_EntityProposal) {
        return m_content.equals(((P_EntityProposal) obj).m_content);
      }
      return false;
    }

    @Override
    public int compareTo(P_EntityProposal o) {
      return m_content.compareTo(o.m_content);
    }
  }

  private class P_EntityContentProvider extends LabelProvider implements IContentProposalProvider, IControlContentAdapter {
    @Override
    public IContentProposal[] getProposals(String contents, int position) {
      String searchString = null;
      if (!StringUtility.hasText(contents)) {
        searchString = "*";
      }
      else {
        searchString = contents.substring(0, position).trim() + "*";
      }

      Set<P_EntityProposal> entities = getAllEntities();
      Iterator<P_EntityProposal> iterator = entities.iterator();
      while (iterator.hasNext()) {
        P_EntityProposal candidate = iterator.next();
        if (SearchPattern.getMatchingRegions(searchString, candidate.m_content, SearchPattern.R_PATTERN_MATCH) == null) {
          iterator.remove();
        }
      }
      return entities.toArray(new IContentProposal[entities.size()]);
    }

    private Set<P_EntityProposal> getAllEntities() {
      TreeSet<P_EntityProposal> ret = new TreeSet<P_EntityProposal>();
      String[] entities;
      try {
        entities = ScoutUtility.getEntities(m_project);
        for (String e : entities) {
          ret.add(new P_EntityProposal(e));
        }
      }
      catch (JavaModelException e1) {
        ScoutSdkUi.logError("Error while calculating the entity proposals", e1);
      }
      return ret;
    }

    @Override
    public Image getImage(Object element) {
      return ScoutSdkUi.getImage(ScoutSdkUi.Package);
    }

    @Override
    public String getText(Object element) {
      return ((IContentProposal) element).getLabel();
    }

    @Override
    public void setControlContents(Control control, String contents, int cursorPosition) {
      m_text.setText(contents);
    }

    @Override
    public void insertControlContents(Control control, String contents, int cursorPosition) {
      m_text.setText(contents);
    }

    @Override
    public String getControlContents(Control control) {
      return m_text.getText();
    }

    @Override
    public int getCursorPosition(Control control) {
      return m_text.getCaretOffset();
    }

    @Override
    public Rectangle getInsertionBounds(Control control) {
      Point caretOrigin = m_text.getCaret().getLocation();
      return new Rectangle(caretOrigin.x, caretOrigin.y, 1, m_text.getLineHeight());
    }

    @Override
    public void setCursorPosition(Control control, int index) {
      m_text.setCaretOffset(index);
    }
  }
}
