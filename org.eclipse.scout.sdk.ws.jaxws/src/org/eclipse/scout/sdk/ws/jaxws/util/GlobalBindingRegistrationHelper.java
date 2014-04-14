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
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.SelectionDialog;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtifact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtifact.TypeEnum;
import org.eclipse.swt.widgets.Shell;

public class GlobalBindingRegistrationHelper {

  public static SchemaCandidate popupForSchema(IFile wsdlFile) throws CoreException {
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
      P_SelectionDialog dialog = new P_SelectionDialog(ScoutSdkUi.getShell());
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
    final List<SchemaCandidate> schemaCandidates = new ArrayList<SchemaCandidate>();
    SchemaUtility.visitArtifacts(wsdlFile, new SchemaArtifactVisitor<IFile>() {

      @Override
      protected void onWsdlArtifact(WsdlArtifact<IFile> wsdlArtifact) {
        for (Schema inlineSchema : wsdlArtifact.getInlineSchemas()) {
          schemaCandidates.add(new SchemaCandidate(inlineSchema, wsdlArtifact));
        }
      }
    });

    return schemaCandidates.toArray(new SchemaCandidate[schemaCandidates.size()]);
  }

  private static class P_SelectionDialog extends SelectionDialog<SchemaCandidate> {

    public P_SelectionDialog(Shell shell) {
      super(shell, "XML schema selection", "Which XML schema should be customized?");
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
        IFileHandle<IFile> fileHandle = candidate.getWsdlArtifact().getFileHandle();
        if (fileHandle != null) {
          text = fileHandle.getName();
        }
        else {
          text = "?";
        }

        if (candidate.getWsdlArtifact().getTypeEnum() == TypeEnum.RootWsdl) {
          text += " (Root WSDL file)";
        }
        cell.setText(text);
      }
    }
  }

  public static class SchemaCandidate {
    private Schema m_schema;
    private WsdlArtifact<IFile> m_wsdlArtifact;

    public SchemaCandidate(Schema schema, WsdlArtifact<IFile> wsdlArtifact) {
      m_schema = schema;
      m_wsdlArtifact = wsdlArtifact;
    }

    public Schema getSchema() {
      return m_schema;
    }

    public void setSchema(Schema schema) {
      m_schema = schema;
    }

    public WsdlArtifact<IFile> getWsdlArtifact() {
      return m_wsdlArtifact;
    }

    public void setWsdlArtifact(WsdlArtifact<IFile> wsdlArtifact) {
      m_wsdlArtifact = wsdlArtifact;
    }
  }
}
