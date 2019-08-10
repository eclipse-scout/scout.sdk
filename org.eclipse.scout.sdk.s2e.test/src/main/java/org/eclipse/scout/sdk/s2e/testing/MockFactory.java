/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.testing;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcjBuilder;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.util.CharSequenceInputStream;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * <h3>{@link MockFactory}</h3>
 *
 * @since 7.0.0
 */
public final class MockFactory {

  private MockFactory() {
  }

  public static IType createJdtTypeMock(String fqn, IJavaProject jp) {
    IType type = mock(IType.class);
    when(type.getJavaProject()).thenReturn(jp);
    when(type.getFullyQualifiedName()).thenReturn(fqn);
    when(type.exists()).thenReturn(Boolean.TRUE);
    return type;
  }

  public static IJavaProject createJavaProjectMock() {
    IJavaProject jp = mock(IJavaProject.class);
    new JavaEnvironmentWithEcjBuilder<>()
        .withParseMethodBodies(true)
        .withoutScoutSdk()
        .accept(env -> {
          IPackageFragmentRoot[] roots = env.classpath()
              .map(cpe -> cpEntryToPackageRoot(cpe, jp))
              .collect(toList())
              .toArray(new IPackageFragmentRoot[]{});

          try {
            when(jp.getAllPackageFragmentRoots()).then(i -> roots);
          }
          catch (JavaModelException e) {
            throw new SdkException(e);
          }
        });
    return jp;
  }

  static ICompilationUnit createIcuMock(Map<String, String> icus, String name) {
    ICompilationUnit icu = mock(ICompilationUnit.class);
    when(icu.exists()).thenAnswer(i -> icus.containsKey(name));
    String typeName = name.substring(0, name.length() - JavaTypes.JAVA_FILE_SUFFIX.length());
    when(icu.getType(typeName)).then(invocation -> {
      IType type = mock(IType.class);
      when(type.getFullyQualifiedName()).thenReturn(typeName);
      return type;
    });
    IFile f = mock(IFile.class);
    when(f.exists()).thenAnswer(invocation -> icus.containsKey(name));
    try {
      when(f.getContents()).thenAnswer(invocation -> new CharSequenceInputStream(icus.get(name), StandardCharsets.UTF_8));
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
    try {
      when(icu.getSource()).thenAnswer(invocation -> icus.get(name));
    }
    catch(JavaModelException e) {
      throw new SdkException(e);
    }
    try {
      when(f.getCharset()).thenReturn(StandardCharsets.UTF_8.name());
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
    when(icu.getResource()).thenReturn(f);

    IBuffer buffer = mock(IBuffer.class);
    try {
      when(icu.getBuffer()).thenReturn(buffer);
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
    doAnswer(invocation -> {
      icus.put(name, invocation.getArgument(0));
      return null;
    }).when(buffer).setContents(any(String.class));

    return icu;
  }

  static IPackageFragmentRoot cpEntryToPackageRoot(IClasspathEntry entry, IJavaProject parent) {
    IPackageFragmentRoot root = mock(IPackageFragmentRoot.class);
    when(root.getJavaProject()).thenReturn(parent);
    when(root.exists()).thenReturn(Boolean.TRUE);
    try {
      when(root.getRawClasspathEntry()).thenReturn(mock(org.eclipse.jdt.core.IClasspathEntry.class));
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
    try {
      when(root.getKind()).thenReturn(entry.isSourceFolder() ? IPackageFragmentRoot.K_SOURCE : IPackageFragmentRoot.K_BINARY);
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }

    Map<String, String> icus = new HashMap<>();
    IPackageFragment fragment = mock(IPackageFragment.class);
    when(fragment.getCompilationUnit(any())).thenAnswer(invocation -> createIcuMock(icus, invocation.getArgument(0)));

    try {
      when(fragment.createCompilationUnit(any(), any(), anyBoolean(), any())).then(invocation -> {
        icus.put(invocation.getArgument(0), invocation.getArgument(1));
        return createIcuMock(icus, invocation.getArgument(0));
      });
    }
    catch (JavaModelException e1) {
      throw new SdkException(e1);
    }

    try {
      when(root.createPackageFragment(any(), anyBoolean(), any())).thenReturn(fragment);
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }

    IPath path = mock(IPath.class);
    when(path.toFile()).thenReturn(entry.path().toFile());
    when(root.getPath()).thenReturn(path);

    IResource r = mock(IResource.class);
    when(r.getLocation()).thenReturn(path);
    when(r.exists()).thenReturn(true);
    when(r.contains(r)).thenReturn(true);
    when(r.isConflicting(r)).thenReturn(true);

    when(root.getResource()).thenReturn(r);
    return root;
  }
}
