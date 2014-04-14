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

import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.AbstractSchemaArtifact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.SchemaImportArtifact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.SchemaIncludeArtifact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtifact;

public class SchemaArtifactVisitor<T> implements ISchemaArtifactVisitor<T> {

  @Override
  public void onRootWsdlArtifact(WsdlArtifact<T> wsdlArtifact) {
    onWsdlArtifact(wsdlArtifact);
  }

  @Override
  public void onReferencedWsdlArtifact(WsdlArtifact<T> wsdlArtifact) {
    onWsdlArtifact(wsdlArtifact);
  }

  @Override
  public void onSchemaImportArtifact(SchemaImportArtifact<T> schemaImportArtifact) {
    onSchemaArtifact(schemaImportArtifact);
  }

  @Override
  public void onSchemaIncludeArtifact(SchemaIncludeArtifact<T> schemaIncludeArtifact) {
    onSchemaArtifact(schemaIncludeArtifact);
  }

  protected void onWsdlArtifact(WsdlArtifact<T> wsdlArtifact) {
  }

  protected void onSchemaArtifact(AbstractSchemaArtifact<T> schemaArtifact) {
  }
}
