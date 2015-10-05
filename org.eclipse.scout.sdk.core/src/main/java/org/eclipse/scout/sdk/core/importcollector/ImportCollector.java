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
package org.eclipse.scout.sdk.core.importcollector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IImport;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureDescriptor;

public class ImportCollector implements IImportCollector {
  private final IJavaEnvironment m_env;
  private final Map<String/* simpleName */, ImportElement> m_imports = new HashMap<>();
  private final Map<String/* simpleName */, ImportElement> m_staticImports = new HashMap<>();

  public ImportCollector() {
    this((IJavaEnvironment) null);
  }

  public ImportCollector(ICompilationUnit icu) {
    this(icu.javaEnvironment());
    for (IImport imp : icu.imports()) {
      if (imp.isStatic()) {
        addStaticImport(imp.name());
      }
      else {
        addImport(imp.name());
      }
    }
  }

  public ImportCollector(IJavaEnvironment env) {
    m_env = env;
  }

  @Override
  public IJavaEnvironment getJavaEnvironment() {
    return m_env;
  }

  @Override
  public String getQualifier() {
    return null;
  }

  @Override
  public void reserveElement(SignatureDescriptor cand) {
    registerElementInternal(cand, false);
  }

  @Override
  public String registerElement(SignatureDescriptor cand) {
    return registerElementInternal(cand, true);
  }

  protected String registerElementInternal(SignatureDescriptor cand, boolean markAsUsed) {
    ImportElement elem = m_imports.get(cand.getSimpleName());
    if (elem == null) {
      m_imports.put(cand.getSimpleName(), new ImportElement(false, cand.getQualifier(), cand.getSimpleName(), markAsUsed));
    }
    else {
      m_imports.put(cand.getSimpleName(), new ImportElement(false, cand.getQualifier(), cand.getSimpleName(), markAsUsed || elem.m_used));
    }
    return cand.getSimpleName();
  }

  @Override
  public String checkExistingImports(SignatureDescriptor cand) {
    // handle primitive and void types
    if (cand.isBaseType() || cand.isUnresolved() || StringUtils.isBlank(cand.getQualifier())) {
      return cand.getSimpleName();
    }

    ImportElement existingElem = m_imports.get(cand.getSimpleName());
    if (existingElem != null && Objects.equals(existingElem.m_packageName, cand.getQualifier())) {
      // already used with same package -> simple name possible
      if (existingElem.m_used) {
        return cand.getSimpleName();
      }
      return null;//must register to use simple name
    }
    if (existingElem != null) {
      // already used with different package -> must qualify
      return cand.getQualifiedName();
    }

    //may register the import
    return null;
  }

  @Override
  public String checkCurrentScope(SignatureDescriptor cand) {
    return null;
  }

  @Override
  public void addImport(String fqn) {
    String packageName = Signature.getQualifier(fqn);
    String simpleName = Signature.getSimpleName(fqn);
    m_imports.put(simpleName, new ImportElement(false, packageName, simpleName, true));
  }

  @Override
  public void addStaticImport(String fqn) {
    String packageName = Signature.getQualifier(fqn);
    String simpleName = Signature.getSimpleName(fqn);
    m_staticImports.put(simpleName, new ImportElement(true, packageName, simpleName, true));
  }

  @Override
  public Collection<String> createImportDeclarations() {
    return organizeImports(m_staticImports.values(), m_imports.values());
  }

  protected List<String> organizeImports(Collection<ImportElement> unsortedList1, Collection<ImportElement> unsortedList2) {
    LinkedList<ImportElement> workList = new LinkedList<>();
    workList.addAll(unsortedList1);
    workList.addAll(unsortedList2);

    //filter
    for (Iterator<ImportElement> it = workList.iterator(); it.hasNext();) {
      ImportElement e = it.next();
      if (e.m_static) {
        //keep
        continue;
      }
      if (!e.m_used) {
        it.remove();
        continue;
      }
      // don't create imports for java.lang.* classes and classes in the same package.
      if ("java.lang".equals(e.m_packageName)) {
        it.remove();
        continue;
      }
    }

    //sort
    Collections.sort(workList, new ImportComparator());

    //add empty lines for import grouping
    String curGroup = null;
    for (int i = 0; i < workList.size(); i++) {
      ImportElement e = workList.get(i);
      if (curGroup == null) {
        curGroup = e.m_group;
        continue;
      }
      if (curGroup.equals(e.m_group)) {
        continue;
      }
      curGroup = e.m_group;
      workList.add(i, null);
      i++;
    }

    ArrayList<String> result = new ArrayList<>(workList.size());
    for (ImportElement e : workList) {
      result.add(e != null ? e.createImportDeclaration() : "");
    }
    return result;
  }

  private static final class ImportElement {
    private final boolean m_static;
    private final String m_packageName;
    private final String m_simpleName;
    private String m_group;
    private boolean m_used;

    private ImportElement(boolean _static, String packageName, String simpleName, boolean used) {
      m_static = _static;
      m_packageName = packageName;
      m_simpleName = simpleName;
      m_used = used;
      calculateGroup();
    }

    private void calculateGroup() {
      String pfx = (m_static ? "a." : "b.");
      if (m_packageName.startsWith("java.")) {
        m_group = pfx + "a";
      }
      else if (m_packageName.startsWith("javax.")) {
        m_group = pfx + "b";
      }
      else if (m_packageName.startsWith("org.")) {
        m_group = pfx + "c";
      }
      else {
        m_group = pfx + "d";
      }
    }

    public String createImportDeclaration() {
      return "import " + (m_static ? "static " : "") + m_packageName + '.' + m_simpleName + ';';
    }
  }

  /**
   * Sort in the following order: java, javax, org, other
   */
  private static final class ImportComparator implements Comparator<ImportElement> {
    @Override
    public int compare(ImportElement e1, ImportElement e2) {
      return prefix(e1).compareTo(prefix(e2));
    }

    private static String prefix(ImportElement e) {
      StringBuilder buf = new StringBuilder();
      buf.append(e.m_group);
      buf.append('.');
      buf.append(e.m_packageName);
      buf.append('.');
      buf.append(e.m_simpleName);
      return buf.toString();
    }
  }
}
