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

import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.SchemaImportArtifact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.SchemaIncludeArtifact;
import org.eclipse.scout.sdk.ws.jaxws.util.SchemaUtility.WsdlArtifact;

public interface ISchemaArtifactVisitor<T> {

  void onRootWsdlArtifact(WsdlArtifact<T> wsdlArtifact);

  void onReferencedWsdlArtifact(WsdlArtifact<T> wsdlArtifact);

  void onSchemaImportArtifact(SchemaImportArtifact<T> schemaImportArtifact);

  void onSchemaIncludeArtifact(SchemaIncludeArtifact<T> schemaIncludeArtifact);
}
