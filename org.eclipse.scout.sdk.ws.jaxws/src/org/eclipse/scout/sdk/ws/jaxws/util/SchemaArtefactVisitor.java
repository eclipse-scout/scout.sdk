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

import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.SchemaArtefact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.SchemaImportArtefact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.SchemaIncludeArtefact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtefact;

public class SchemaArtefactVisitor<T> implements ISchemaArtefactVisitor<T> {

  @Override
  public void onRootWsdlArtefact(WsdlArtefact<T> wsdlArtefact) {
    onWsdlArtefact(wsdlArtefact);
  }

  @Override
  public void onReferencedWsdlArtefact(WsdlArtefact<T> wsdlArtefact) {
    onWsdlArtefact(wsdlArtefact);
  }

  @Override
  public void onSchemaImportArtefact(SchemaImportArtefact<T> schemaImportArtefact) {
    onSchemaArtefact(schemaImportArtefact);
  }

  @Override
  public void onSchemaIncludeArtefact(SchemaIncludeArtefact<T> schemaIncludeArtefact) {
    onSchemaArtefact(schemaIncludeArtefact);
  }

  protected void onWsdlArtefact(WsdlArtefact<T> wsdlArtefact) {
  }

  protected void onSchemaArtefact(SchemaArtefact<T> schemaArtefact) {
  }
}
