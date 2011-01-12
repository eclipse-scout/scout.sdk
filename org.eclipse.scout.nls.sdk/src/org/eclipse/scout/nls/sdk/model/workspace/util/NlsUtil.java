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
package org.eclipse.scout.nls.sdk.model.workspace.util;

import org.eclipse.scout.nls.sdk.model.INlsEntry;

/** <h4> NlsUtil </h4>
 *
 * @author Andreas Hoegger
 * @since 1.1.0 (12.01.2011)
 *
 */
public class NlsUtil {

  public static String getVerbose(INlsEntry[] entries){
    if(entries == null){
      return "[no entries]";
    }
    StringBuilder builder = new StringBuilder("[");
    for(int i = 0; i<  entries.length;i++){
      builder.append("'"+entries[i].getKey()+"'");
      if(i+1 != entries.length){
        builder.append(", ");
      }
    }
    builder.append("]");
    return builder.toString();
  }

}
