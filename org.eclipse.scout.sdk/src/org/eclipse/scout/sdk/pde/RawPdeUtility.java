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
package org.eclipse.scout.sdk.pde;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Pde handling using java.io.Files
 * inside osgi/eclipse preferrably use {@link PdeUtility}
 */
public final class RawPdeUtility {

  private RawPdeUtility() {
  }

  /**
   * @return the appended values
   */
  public static String[] exportPackage(RawManifest mf, String packageName) throws IOException {
    return exportPackages(mf, new String[]{packageName});
  }

  /**
   * @return the appended values
   */
  public static String[] exportPackages(RawManifest mf, String[] packageNames) throws IOException {
    return extendAttributeList(mf, "Export-Package", packageNames);
  }

  public static String[] getExportPackages(RawManifest mf) throws IOException {
    return getAttributeList(mf, "Export-Package");
  }

  public static String[] getImportPackages(RawManifest mf) throws IOException {
    return getAttributeList(mf, "Import-Package");
  }

  /**
   * @return the appended values
   */
  public static String[] setExportedPackages(RawManifest mf, String[] packageNames) throws IOException {
    return setAttributeList(mf, "Export-Package", packageNames);
  }

  /**
   * @return the appended values
   */
  public static String[] addDependency(RawManifest mf, String d) throws IOException {
    return addDependencies(mf, new String[]{d});
  }

  /**
   * @return the appended values
   */
  public static String[] addDependencies(RawManifest mf, String[] d) throws IOException {
    return extendAttributeList(mf, "Require-Bundle", d);
  }

  public static String[] getDependencies(RawManifest mf) throws IOException {
    return getAttributeList(mf, "Require-Bundle");
  }

  /**
   * @return the appended values
   */
  public static String[] setDependencies(RawManifest mf, String[] d) throws IOException {
    return setAttributeList(mf, "Require-Bundle", d);
  }

  /**
   * @return the appended values
   */
  public static String[] addClasspath(RawManifest mf, String path) throws IOException {
    return addClasspaths(mf, new String[]{path});
  }

  /**
   * @return the appended values
   */
  public static String[] addClasspaths(RawManifest mf, String[] paths) throws IOException {
    String[] changes = extendAttributeList(mf, "Bundle-ClassPath", paths);
    return changes;
  }

  public static String[] getClasspaths(RawManifest mf) throws IOException {
    return getAttributeList(mf, "Bundle-ClassPath");
  }

  /**
   * @return the appended values
   */
  public static String[] extendAttributeList(RawManifest mf, String attributeName, String additionalValue) throws IOException {
    return extendAttributeList(mf, attributeName, new String[]{additionalValue});
  }

  public static String[] reduceAttributList(RawManifest mf, String attributeName, String[] values) throws IOException {
    return reduceAttributeListImpl(mf, attributeName, values);
  }

  /**
   * @return the appended values
   */
  private static String[] reduceAttributeListImpl(RawManifest mf, String attributeName, String[] toReduceValues) throws IOException {
    String value = mf.getAttribute(attributeName);
    if (value != null && value.trim().length() == 0) {
      value = null;
    }
    HashSet<String> exportedNames = new HashSet<String>();
    if (value != null) {
      String[] a = value.split(",");
      for (int i = 0; i < a.length; i++) {
        exportedNames.add(a[i].trim());
      }
    }
    // collect all removed entries
    ArrayList<String> removedList = new ArrayList<String>();
    for (int i = 0; i < toReduceValues.length; i++) {
      String toReduce = toReduceValues[i].trim();
      if (exportedNames.remove(toReduce)) {
        removedList.add(toReduce);
      }
    }
    if (exportedNames.size() <= 0) {
      mf.removeAttribute(value);
      return removedList.toArray(new String[removedList.size()]);
    }
    else {
      StringBuilder newValue = new StringBuilder();
      boolean firstValue = true;
      for (String exportPackageName : exportedNames) {
        if (firstValue) {
          firstValue = false;
          newValue.append(exportPackageName);
        }
        else {
          newValue.append("," + exportPackageName);
        }
      }
      if (removedList.size() > 0) {
        mf.setAttribute(attributeName, newValue.toString());
      }
      return removedList.toArray(new String[removedList.size()]);
    }

  }

  /**
   * @return the appended values
   */
  public static String[] extendAttributeList(RawManifest mf, String attributeName, String[] additionalValues) throws IOException {
    String[] appendedValues = extendAttributeListImpl(mf, attributeName, additionalValues);
    return appendedValues;
  }

  public static String[] getAttributeList(RawManifest mf, String attributeName) throws IOException {
    return getAttributeListImpl(mf, attributeName);
  }

  /**
   * @return the appended values
   */
  public static String[] setAttributeList(RawManifest mf, String attributeName, String[] newValues) throws IOException {
    String[] appendedValues = setAttributeListImpl(mf, attributeName, newValues);
    return appendedValues;
  }

  /**
   * @return the appended values
   */
  private static String[] extendAttributeListImpl(RawManifest mf, String attributeName, String[] additionalValues) throws IOException {
    String value = mf.getAttribute(attributeName);
    if (value != null && value.trim().length() == 0) {
      value = null;
    }
    HashSet<String> exportedNames = new HashSet<String>();
    if (value != null) {
      String[] a = value.split(",");
      for (int i = 0; i < a.length; i++) {
        exportedNames.add(a[i].trim());
      }
    }
    StringBuilder newValue = new StringBuilder();
    if (value != null) {
      newValue.append(value);
    }
    ArrayList<String> appendedList = new ArrayList<String>();
    for (int i = 0; i < additionalValues.length; i++) {
      String s = additionalValues[i].trim();
      if (!exportedNames.contains(s)) {
        appendedList.add(s);
        if (newValue.length() <= 0) {
          newValue.append(s);
        }
        else {
          newValue.append("," + s);
        }
      }
    }
    if (appendedList.size() > 0) {
      mf.setAttribute(attributeName, newValue.toString());
    }
    return appendedList.toArray(new String[0]);
  }

  private static String[] getAttributeListImpl(RawManifest mf, String attributeName) throws IOException {
    String value = mf.getAttribute(attributeName);
    if (value != null && value.trim().length() == 0) {
      value = null;
    }
    HashSet<String> values = new HashSet<String>();
    if (value != null) {
      String[] a = value.split(",");
      for (int i = 0; i < a.length; i++) {
        values.add(a[i].trim());
      }
    }
    return values.toArray(new String[values.size()]);
  }

  private static String[] setAttributeListImpl(RawManifest mf, String attributeName, String[] newValues) throws IOException {
    HashSet<String> exportedNames = new HashSet<String>();
    StringBuilder newValue = new StringBuilder();
    ArrayList<String> appendedList = new ArrayList<String>();
    for (int i = 0; i < newValues.length; i++) {
      String s = newValues[i].trim();
      if (!exportedNames.contains(s)) {
        appendedList.add(s);
        if (newValue.length() <= 0) {
          newValue.append(s);
        }
        else {
          newValue.append("," + s);
        }
      }
    }
    if (appendedList.size() > 0) {
      mf.setAttribute(attributeName, newValue.toString());
    }
    return appendedList.toArray(new String[0]);
  }

  /**
   * called when the plugin closes
   */
  public static void dispose() {

  }
}
