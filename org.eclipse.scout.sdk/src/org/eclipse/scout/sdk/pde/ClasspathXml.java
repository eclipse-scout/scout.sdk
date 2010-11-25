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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;

/**
 * representation of the .classpath xml file
 */
public class ClasspathXml extends AbstractConfigFile {
  private SimpleXmlElement m_xml;

  public ClasspathXml(IProject p) throws CoreException {
    super(p, ".classpath");
    m_xml = new SimpleXmlElement("classpath");
    loadXmlInternal(m_xml);
  }

  public Collection<ClasspathEntry> getEntries() {
    ArrayList<ClasspathEntry> entries = new ArrayList<ClasspathEntry>();
    for (SimpleXmlElement e : m_xml.getChildren("classpathentry")) {
      entries.add(new ClasspathEntry(e));
    }
    return entries;
  }

  public ClasspathEntry addEntry(int kind, String path) {
    return addEntry(kind, path, null);
  }

  public ClasspathEntry addEntry(int kind, String path, String sourcepath) {
    return addEntry(kind, path, sourcepath, false);
  }

  public ClasspathEntry addEntry(int kind, String path, String sourcepath, boolean exported) {
    return addEntry(kind, path, sourcepath, exported, false);
  }

  public ClasspathEntry addEntry(int kind, String path, String sourcepath, boolean exported, boolean combineaccessrule) {
    ClasspathEntry entry = getEntry(kind, path);
    if (entry == null) {
      SimpleXmlElement e = new SimpleXmlElement("classpathentry");
      entry = new ClasspathEntry(e);
      entry.setKind(kind);
      entry.setPath(path);
      entry.setSourcepath(sourcepath);
      entry.setExported(exported);
      entry.setCombineaccessrules(combineaccessrule);
      entry.applyChanges();
      m_xml.addChild(e);
    }
    return entry;
  }

  public ClasspathEntry getEntry(int kind, String path) {
    String kindText = ClasspathEntry.formatKind(kind);
    if (path == null) path = "";
    for (SimpleXmlElement e : m_xml.getChildren("classpathentry")) {
      if (kindText.equals(e.getStringAttribute("kind", "")) && path.equals(e.getStringAttribute("path", ""))) {
        return new ClasspathEntry(e);
      }
    }
    return null;
  }

  public ClasspathEntry getEntryByPathPrefix(int kind, String pathPrefix) {
    String kindText = ClasspathEntry.formatKind(kind);
    if (pathPrefix == null) pathPrefix = "";
    for (SimpleXmlElement e : m_xml.getChildren("classpathentry")) {
      if (kindText.equals(e.getStringAttribute("kind", "")) && e.getStringAttribute("path", "").startsWith(pathPrefix)) {
        return new ClasspathEntry(e);
      }
    }
    return null;
  }

  @Override
  public boolean store(IProgressMonitor p) throws CoreException {
    return storeXmlInternal(m_xml, p);
  }

}
