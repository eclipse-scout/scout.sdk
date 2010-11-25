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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.ScoutSdk;

/**
 * representation of the build.properties file
 * XXX check if IPluginModelBase.getBuildModel().getBuild() would do the same job
 */
public class BuildProperties extends AbstractConfigFile {

  public static final String PROP_BIN_INCLUDES = "bin.includes";
  public static final String PROP_SRC_INCLUDES = "src.includes";
  public static final String PROP_OUTPUT = "output..";
  public static final String PROP_SOURCE = "source..";
  public static final String PROP_JARS_EXTRA_CLASSPATH = "jars.extra.classpath";

  private static final String PREFIX_TEMPLATE = "                                                ";
  private HashMap<String, ArrayList<String>> m_content;

  public BuildProperties(IProject p) throws CoreException {
    super(p, "build.properties");
    m_content = new HashMap<String, ArrayList<String>>();
    String[] lines = loadTextInternal("UTF-8").split("[\\n\\r]+");
    ArrayList<String> entries = new ArrayList<String>();
    StringBuffer entry = new StringBuffer();
    for (String line : lines) {
      boolean isTerminal;
      String part = line.trim();
      if (part.endsWith("\\")) {
        part = part.substring(0, part.length() - 1);
        isTerminal = false;
      }
      else {
        isTerminal = true;
      }
      entry.append(part);
      if (isTerminal) {
        if (entry.length() > 0) {
          entries.add(entry.toString());
        }
        entry.setLength(0);
      }
    }
    if (entry.length() > 0) {
      entries.add(entry.toString());
      entry.setLength(0);
    }
    //
    for (String s : entries) {
      String[] nvSplit = s.split("[=]", 2);
      if (nvSplit.length == 2) {
        String name = nvSplit[0].trim();
        String value = nvSplit[1].trim();
        if (m_content.containsKey(name)) {
          ScoutSdk.logWarning("duplicate entry: " + name + " in " + getFile().getFullPath().toOSString());
        }
        m_content.put(name, new ArrayList<String>(Arrays.asList(value.split("[,]"))));
      }
      else {
        ScoutSdk.logWarning("unexpected entry: " + s + " in " + getFile().getFullPath().toOSString());
      }
    }
  }

  /**
   * @return read-only list of paths (directories, files)
   */
  public List<String> getPaths(String propertyName) {
    ArrayList<String> list = getPathsInternal(propertyName, false);
    if (list != null) {
      return Collections.unmodifiableList(list);
    }
    else {
      return Collections.emptyList();
    }
  }

  private ArrayList<String> getPathsInternal(String propertyName, boolean autoCreate) {
    ArrayList<String> list = m_content.get(propertyName);
    if (list == null && autoCreate) {
      list = new ArrayList<String>();
      m_content.put(propertyName, list);
    }
    return list;
  }

  /**
   * add new directories, directory paths end with a /
   */
  public void addDirectories(String propertyName, String[] paths) {
    if (paths != null) {
      for (String path : paths) {
        path = normalizeDirectoryPath(path);
        if (path != null) {
          addPathInternal(propertyName, path);
        }
      }
    }
  }

  /**
   * remove directories
   */
  public void removeDirectories(String propertyName, String[] paths) {
    if (paths != null) {
      for (String path : paths) {
        path = normalizeDirectoryPath(path);
        if (path != null) {
          removePathInternal(propertyName, path);
        }
      }
    }
  }

  /**
   * add new files, file paths don't end with a /
   */
  public void addFiles(String propertyName, String[] paths) {
    if (paths != null) {
      for (String path : paths) {
        path = normalizeFilePath(path);
        if (path != null) {
          addPathInternal(propertyName, path);
        }
      }
    }
  }

  /**
   * remove files
   */
  public void removeFiles(String propertyName, String[] paths) {
    if (paths != null) {
      for (String path : paths) {
        path = normalizeFilePath(path);
        if (path != null) {
          removePathInternal(propertyName, path);
        }
      }
    }
  }

  public static String normalizeDirectoryPath(String path) {
    if (path != null) {
      path = path.trim();
      if (path.length() > 0) {
        if (!path.endsWith("/")) {
          path = path + "/";
        }
        if (path.equals("./")) {
          path = ".";
        }
        return path;
      }
    }
    return null;
  }

  public static String normalizeFilePath(String path) {
    if (path != null) {
      path = path.trim();
      if (path.endsWith("/")) {
        path = path.substring(0, path.length() - 1);
      }
      if (path.length() > 0) {
        return path;
      }
    }
    return null;
  }

  private void addPathInternal(String propertyName, String path) {
    ArrayList<String> list = getPathsInternal(propertyName, true);
    if (!list.contains(path)) {
      list.add(path);
    }
  }

  private void removePathInternal(String propertyName, String path) {
    ArrayList<String> list = getPathsInternal(propertyName, false);
    if (list != null) {
      list.remove(path);
    }
  }

  @Override
  public boolean store(IProgressMonitor p) throws CoreException {
    StringWriter sw = new StringWriter();
    PrintWriter out = new PrintWriter(sw);
    for (Map.Entry<String, ArrayList<String>> e : m_content.entrySet()) {
      String propertyName = e.getKey();
      ArrayList<String> list = e.getValue();
      if (list.size() > 0) {
        out.print(propertyName);
        out.print(" = ");
        if (list.size() == 1) {
          out.print(list.get(0));
          out.println();
        }
        else {
          String prefix = PREFIX_TEMPLATE.substring(0, propertyName.length() + 3);
          int n = list.size();
          for (int i = 0; i < n; i++) {
            out.print(list.get(i));
            if (i + 1 < n) {
              out.print(",\\");
              out.println();
              out.print(prefix);
            }
          }
          out.println();
        }
      }
    }
    out.close();
    return storeTextInternal(sw.getBuffer().toString(), p);
  }

}
