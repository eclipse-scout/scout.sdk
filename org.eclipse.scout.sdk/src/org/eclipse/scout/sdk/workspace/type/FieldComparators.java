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
package org.eclipse.scout.sdk.workspace.type;

import java.util.Comparator;

import org.eclipse.jdt.core.IField;

/**
 *
 */
public final class FieldComparators {
  private FieldComparators() {
  }

  public static Comparator<IField> getNameComparator() {
    return new Comparator<IField>() {
      @Override
      public int compare(IField f1, IField f2) {
        if (f1 == null && f2 == null) {
          return 0;
        }
        else if (f1 == null) {
          return -1;
        }
        else if (f2 == null) {
          return 1;
        }
        else {
          return f1.getElementName().compareTo(f2.getElementName());
        }
      }
    };
  }

}
