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
package org.eclipse.scout.sdk.core.model.spi.internal;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.batch.ClasspathJar;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.util.SdkException;

public class WorkspaceFileSystem implements INameEnvironment {
  private static final char SEPARATOR = '/';

  private final Set<Classpath> m_classpaths;
  private final Map<String, ICompilationUnit> m_overrideCompilationUnits;
  private final Set<String> m_additionalPackages;

  /**
   * @param paths
   * @param overrideCompilationUnits
   *          is a map of "a/b/c/JavaClass.class" to its content
   */
  public WorkspaceFileSystem(Set<Classpath> paths) {
    m_overrideCompilationUnits = new HashMap<>();
    m_additionalPackages = new HashSet<>();
    m_classpaths = paths;
    try {
      for (Classpath cp : m_classpaths) {
        cp.initialize();
      }
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
  }

  /**
   * adds a compilation unit override
   *
   * @param cu
   *          The override
   * @return <code>true</code> if there was a previous mapping which has been updated with this call. <code>false</code>
   *         if this is the first override for this compilation unit.
   */
  public boolean addOverrideCompilationUnit(ICompilationUnit cu) {
    char[][] packageName0 = cu.getPackageName();

    // build key
    StringBuilder keyBuilder = new StringBuilder();
    if (packageName0 != null && packageName0.length > 0) {
      for (char[] segment : packageName0) {
        keyBuilder.append(segment);
        keyBuilder.append(SEPARATOR);
      }
    }
    keyBuilder.append(cu.getMainTypeName());

    boolean updatedExistingEntry = false;
    ICompilationUnit existingIcu = m_overrideCompilationUnits.put(keyBuilder.toString(), cu);
    if (existingIcu != null) {
      updatedExistingEntry = !Arrays.equals(existingIcu.getContents(), cu.getContents());
    }

    //register additional packages
    if (packageName0 != null && packageName0.length > 0) {
      for (int i = 1; i < packageName0.length; i++) {
        m_additionalPackages.add(new String(CharOperation.concatWith(CharOperation.subarray(packageName0, 0, i), SEPARATOR)));
      }
    }
    return updatedExistingEntry;
  }

  public Collection<ICompilationUnit> getOverrideCompilationUnits() {
    return Collections.unmodifiableCollection(m_overrideCompilationUnits.values());
  }

  @Override
  public void cleanup() {
    for (Classpath cp : m_classpaths) {
      cp.reset();
    }
    m_additionalPackages.clear();
    m_overrideCompilationUnits.clear();
  }

  @Override
  public boolean isPackage(char[][] compoundName, char[] packageName) {
    String packageFqnSlash = new String(CharOperation.concatWith(compoundName, packageName, SEPARATOR));
    if (m_additionalPackages.contains(packageFqnSlash)) {
      return true;
    }

    if (File.separatorChar == SEPARATOR) {
      // current platform uses slash: no need to handle different for directories
      for (Classpath cp : m_classpaths) {
        if (cp.isPackage(packageFqnSlash)) {
          return true;
        }
      }
    }
    else {
      String packageFqnPlatform = packageFqnSlash.replace(SEPARATOR, File.separatorChar);
      for (Classpath cp : m_classpaths) {
        String pathToUse = null;
        if (cp instanceof ClasspathJar) {
          // always use '/' if it is a jar
          pathToUse = packageFqnSlash;
        }
        else {
          pathToUse = packageFqnPlatform;
        }
        if (cp.isPackage(pathToUse)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public NameEnvironmentAnswer findType(char[][] compoundName) {
    if (compoundName == null) {
      return null;
    }
    return findTypeInternal(compoundName, null, null);
  }

  @Override
  public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
    if (typeName == null) {
      return null;
    }
    return findTypeInternal(null, typeName, packageName);
  }

  /**
   * @return best answer where source types takes precedence over binary types
   */
  protected NameEnvironmentAnswer findTypeInternal(char[][] compoundName, char[] typeName, char[][] packageName) {
    if (compoundName == null) {
      compoundName = CharOperation.arrayConcat(packageName, typeName);
    }
    else {
      packageName = CharOperation.subarray(compoundName, 0, compoundName.length - 1);
      typeName = compoundName[compoundName.length - 1];
    }

    String fqnUnix = new String(CharOperation.concatWith(compoundName, SEPARATOR));
    ICompilationUnit overrideCu = m_overrideCompilationUnits.get(fqnUnix);
    if (overrideCu != null) {
      return new NameEnvironmentAnswer(overrideCu, null);
    }

    String fileNameUnix = fqnUnix + SuffixConstants.SUFFIX_STRING_class;
    String pckUnix = new String(CharOperation.concatWith(packageName, SEPARATOR));
    String pckPlatform = pckUnix;
    String fileNamePlatform = fileNameUnix;
    if (SEPARATOR != File.separatorChar) {
      pckPlatform = new String(CharOperation.concatWith(packageName, File.separatorChar));
      fileNamePlatform = new String(CharOperation.concat(CharOperation.concatWith(compoundName, File.separatorChar), SuffixConstants.SUFFIX_class));
    }
    return findTypeInternal(compoundName, typeName, pckUnix, fileNameUnix, pckPlatform, fileNamePlatform);
  }

  protected NameEnvironmentAnswer findTypeInternal(char[][] compoundName, char[] typeName, String pckUnix, String fileNameUnix, String pckPlatform, String fileNamePlatform) {
    for (Classpath cp : m_classpaths) {
      NameEnvironmentAnswer answer = null;
      try {
        if (cp instanceof ClasspathJar) {
          answer = cp.findClass(typeName, pckUnix, fileNameUnix, false);
        }
        else {
          answer = cp.findClass(typeName, pckPlatform, fileNamePlatform, false);
        }
      }
      catch (RuntimeException e) {
        throw new SdkException("Error searching for '" + CharOperation.toString(compoundName) + "' in " + cp, e);
      }

      if (answer != null) {
        //prio 1: source type
        if (answer.isSourceType()) {
          return answer;
        }
        //prio 2: compilation unit
        if (answer.isCompilationUnit()) {
          return answer;
        }
        //prio 3: binary type
        if (answer.isBinaryType()) {
          return answer;
        }
      }
    }
    return null;
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      cleanup();
    }
    finally {
      super.finalize();
    }
  }

  public static Classpath createClasspath(File f, boolean source, String encoding) {
    if (f == null || !f.canRead()) {
      return null;
    }

    if (encoding == null) {
      encoding = StandardCharsets.UTF_8.name();
    }

    return FileSystem.getClasspath(f.getAbsolutePath(), encoding, source, null, null, AstCompiler.optsMap);
  }
}
