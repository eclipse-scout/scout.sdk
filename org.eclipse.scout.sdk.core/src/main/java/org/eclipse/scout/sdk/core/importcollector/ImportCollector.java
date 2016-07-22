/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.importcollector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

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
        addStaticImport(imp.elementName(), true);
      }
      else {
        addImport(imp.elementName(), true);
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
      m_imports.put(cand.getSimpleName(), new ImportElement(false, cand.getQualifier(), cand.getSimpleName(), markAsUsed, false));
    }
    else {
      elem.m_used = markAsUsed || elem.m_used;
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
    addImport(fqn, false);
  }

  public void addImport(String fqn, boolean fromExisting) {
    String packageName = Signature.getQualifier(fqn);
    String simpleName = Signature.getSimpleName(fqn);
    m_imports.put(simpleName, new ImportElement(false, packageName, simpleName, true, fromExisting));
  }

  @Override
  public void addStaticImport(String fqn) {
    addStaticImport(fqn, false);
  }

  protected void addStaticImport(String fqn, boolean fromExisting) {
    String packageName = Signature.getQualifier(fqn);
    String simpleName = Signature.getSimpleName(fqn);
    m_staticImports.put(simpleName, new ImportElement(true, packageName, simpleName, true, fromExisting));
  }

  @Override
  public Collection<String> getImports() {
    return getImports(m_imports.values());
  }

  @Override
  public Collection<String> getStaticImports() {
    return getImports(m_staticImports.values());
  }

  protected static Collection<String> getImports(Collection<ImportElement> imports) {
    List<String> result = new ArrayList<>(imports.size());
    for (ImportElement e : imports) {
      result.add(e.getImport());
    }
    return result;
  }

  @Override
  public List<String> createImportDeclarations() {
    return createImportDeclarations(true);
  }

  @Override
  public List<String> createImportDeclarations(boolean includeExisting) {
    return organizeImports(m_staticImports.values(), m_imports.values(), includeExisting);
  }

  protected void addFiltered(Collection<ImportElement> elements, boolean includeExisting, Collection<ImportElement> collector) {
    for (ImportElement e : elements) {
      if (!includeExisting && e.m_fromExisting) {
        continue;
      }

      if (!e.m_static) {
        if (!e.m_used) {
          continue;
        }
        // don't create imports for java.lang.* classes
        if ("java.lang".equals(e.m_packageName)) {
          continue;
        }
      }
      collector.add(e);
    }
  }

  protected List<String> organizeImports(Collection<ImportElement> unsortedList1, Collection<ImportElement> unsortedList2, boolean includeExisting) {
    Set<ImportElement> workList = new TreeSet<>(new ImportComparator());
    addFiltered(unsortedList1, includeExisting, workList);
    addFiltered(unsortedList2, includeExisting, workList);

    String lastGroup = null;
    List<String> result = new ArrayList<>(workList.size() + 7 /* max number of empty group lines */);
    for (ImportElement e : workList) {
      if (lastGroup != null && !lastGroup.equals(e.m_group)) {
        //add empty lines for import grouping
        result.add("");
      }
      result.add(e.createImportDeclaration());
      lastGroup = e.m_group;
    }
    return result;
  }

  private static final class ImportElement {
    private final boolean m_static;
    private final String m_packageName;
    private final String m_simpleName;
    private final boolean m_fromExisting;

    private String m_group;
    private boolean m_used;

    private ImportElement(boolean isStatic, String packageName, String simpleName, boolean used, boolean fromExisting) {
      m_static = isStatic;
      m_packageName = packageName;
      m_simpleName = simpleName;
      m_used = used;
      m_fromExisting = fromExisting;
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

    public String getImport() {
      return new StringBuilder().append(m_packageName).append('.').append(m_simpleName).toString();
    }

    public String createImportDeclaration() {
      return new StringBuilder().append("import ").append(m_static ? "static " : "").append(getImport()).append(';').toString();
    }
  }

  /**
   * Sort in the following order: java, javax, org, other
   */
  private static final class ImportComparator implements Comparator<ImportElement>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(ImportElement e1, ImportElement e2) {
      int result = e1.m_group.compareTo(e2.m_group);
      if (result != 0) {
        return result;
      }
      result = e1.m_packageName.compareTo(e2.m_packageName);
      if (result != 0) {
        return result;
      }
      return e1.m_simpleName.compareTo(e2.m_simpleName);
    }
  }
}
