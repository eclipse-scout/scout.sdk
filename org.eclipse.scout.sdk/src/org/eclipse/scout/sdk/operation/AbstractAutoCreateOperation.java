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
package org.eclipse.scout.sdk.operation;

import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractAutoCreateOperation implements IOperation {

  protected String createColumnName(String columnName, TreeSet<String> columnSet) {
    Matcher m;
    columnName = columnName.trim();
    if (columnName.startsWith(":")) {
      // bind var name
      m = Pattern.compile("[\\w]+").matcher(columnName);
      m.find();
      columnName = m.group().substring(0, 1).toUpperCase() + m.group().substring(1);
      return columnName;
    }
    else if (columnName.indexOf(" ") > 0) {
      columnName = columnName.substring(columnName.indexOf(" ") + 1);
    }
    else if (columnName.indexOf(".") > 0) {
      columnName = columnName.substring(columnName.indexOf(".") + 1);
    }
    m = Pattern.compile("\\W").matcher(columnName);
    if (m.find()) {
      // not a simple column name or synonym but something complex
      columnName = "Undefined";
    }
    else {
      columnName = "_" + columnName.toLowerCase();

      StringBuilder b = new StringBuilder();
      for (int i = 0; i < columnName.length(); i++) {
        char ch = columnName.charAt(i);
        switch (ch) {
          case '_': {
            i++;
            b.append(String.valueOf(columnName.charAt(i)).toUpperCase());
            break;
          }
          default: {
            b.append(ch);
            break;
          }
        }
      }
      columnName = b.toString();
    }
    if (columnSet != null) return makeUniqueText(columnName, columnSet);
    else return columnName;
  }

  private String makeUniqueText(String name, TreeSet<String> columnSet) {
    int index = 1;
    String key = name + (index == 1 ? "" : "" + index);
    while (columnSet.contains(key)) {
      index++;
      key = name + (index == 1 ? "" : "" + index);
    }
    columnSet.add(key);
    return key;
  }
}
