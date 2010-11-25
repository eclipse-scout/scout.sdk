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
package org.eclipse.scout.nls.sdk.internal.ui.dialog.importexport;

import org.eclipse.scout.nls.sdk.internal.jdt.INlsFolder;
import org.eclipse.scout.nls.sdk.internal.model.PropertyBasedModel;

public class NlsImportDialogModel extends PropertyBasedModel {

  public static final String PROP_IMPORT_FILE = "importFile";
  public static final String PROP_CREATE_NEW_KEYS = "createNewKeys";
  public static final String PROP_NEW_FILE_LOCATION = "newFileLocation";
  public static final String PROP_REMEMBER_NEW_FILE_LOCATION = "rememberNewFileLocation";

  public void setImportFile(String fileName) {
    setPropertyString(PROP_IMPORT_FILE, fileName);
  }

  public String getImportFile() {
    return getPropertyString(PROP_IMPORT_FILE);
  }

  public void setCreateNewKeys(boolean selection) {

  }

  public void setNewFileLocation(INlsFolder folder) {
    setProperty(PROP_NEW_FILE_LOCATION, folder);
  }

  public INlsFolder getNewFileLocation() {
    return (INlsFolder) getProperty(PROP_NEW_FILE_LOCATION);
  }

  public void setRememberNewFileLocation(boolean remember) {
    setPropertyBool(PROP_REMEMBER_NEW_FILE_LOCATION, remember);
  }

  public boolean getRememberNewFileLocation() {
    return getPropertyBool(PROP_REMEMBER_NEW_FILE_LOCATION);
  }
}
