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
package org.eclipse.scout.sdk.ws.jaxws.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.wsdl.extensions.schema.Schema;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.SelectionDialog;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.Artefact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtefact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtefact.TypeEnum;
import org.eclipse.swt.widgets.Shell;

public class GlobalBindingRegistrationHelper {

  public static SchemaCandidate popupForSchema(IScoutBundle bundle, IFile wsdlFile) throws CoreException {
    // determine schema to use for global binding registration
    SchemaCandidate[] schemaCandidates = getSchemaCandidates(wsdlFile);
    if (schemaCandidates.length == 0) {
      JaxWsSdk.logWarning("No XML schema found to apply customized binding");
      return null;
    }
    else if (schemaCandidates.length == 1) {
      return schemaCandidates[0];
    }
    else {
      // let user choose which schema to apply global binding to
      P_SelectionDialog dialog = new P_SelectionDialog(ScoutSdkUi.getShell(), bundle);
      dialog.setElements(Arrays.asList(schemaCandidates));
      if (dialog.open() == Window.OK) {
        return dialog.getElement();
      }
      else {
        throw new CoreException(Status.CANCEL_STATUS);
      }
    }
  }

  public static SchemaCandidate[] getSchemaCandidates(IFile wsdlFile) {
    Artefact[] artefacts = SchemaUtility.getArtefacts(wsdlFile, true);

    List<SchemaCandidate> candidates = new ArrayList<SchemaCandidate>();
    for (Artefact artefact : artefacts) {
      if (artefact instanceof WsdlArtefact) {
        for (Schema inlineSchema : ((WsdlArtefact) artefact).getInlineSchemas()) {
          candidates.add(new SchemaCandidate(inlineSchema, (WsdlArtefact) artefact));
        }
      }
    }
    return candidates.toArray(new SchemaCandidate[candidates.size()]);
  }

  private static class P_SelectionDialog extends SelectionDialog<SchemaCandidate> {

    private IScoutBundle m_bundle;

    public P_SelectionDialog(Shell shell, IScoutBundle bundle) {
      super(shell, "XML schema selection", "Which XML schema should be customized?");
      m_bundle = bundle;
    }

    @Override
    protected String getConfiguredNameColumnText() {
      return "TargetNamespace";
    }

    @Override
    protected String getConfiguredDescriptionColumnText() {
      return "Schema defining WSDL file";
    }

    @Override
    protected boolean getConfiguredIsDescriptionColumnVisible() {
      return true;
    }

    @Override
    protected void execDecorateElement(SchemaCandidate candidate, ViewerCell cell) {
      if (cell.getColumnIndex() == 0) {
        cell.setText(StringUtility.nvl(SchemaUtility.getSchemaTargetNamespace(candidate.getSchema()), "?"));
        cell.setImage(JaxWsSdk.getImage(JaxWsIcons.WsdlFile));
      }
      else {
        String text;
        IFile wsdlLocationFile = JaxWsSdkUtility.toFile(m_bundle, candidate.getWsdlArtefact().getFile());
        if (wsdlLocationFile != null) {
          text = wsdlLocationFile.getName();
        }
        else {
          text = "?";
        }

        if (candidate.getWsdlArtefact().getTypeEnum() == TypeEnum.RootWsdl) {
          text += " (Root WSDL file)";
        }
        cell.setText(text);
      }
    }
  }

  public static class SchemaCandidate {
    private Schema m_schema;
    private WsdlArtefact m_wsdlArtefact;

    public SchemaCandidate(Schema schema, WsdlArtefact wsdlArtefact) {
      m_schema = schema;
      m_wsdlArtefact = wsdlArtefact;
    }

    public Schema getSchema() {
      return m_schema;
    }

    public void setSchema(Schema schema) {
      m_schema = schema;
    }

    public WsdlArtefact getWsdlArtefact() {
      return m_wsdlArtefact;
    }

    public void setWsdlArtefact(WsdlArtefact wsdlArtefact) {
      m_wsdlArtefact = wsdlArtefact;
    }
  }
}
