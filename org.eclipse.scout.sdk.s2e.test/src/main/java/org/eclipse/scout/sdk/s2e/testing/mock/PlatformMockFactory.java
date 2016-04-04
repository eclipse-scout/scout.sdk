/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.testing.mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * <h3>{@link PlatformMockFactory}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class PlatformMockFactory {

  private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("\\s+(?:class|interface)\\s+([A-Z]{1}[A-Za-z0-9_]*)");

  private final IJavaProject m_javaProject;
  private final IJavaEnvironment m_javaEnv;
  private final Map<IType, IBuffer> m_bufferMap;

  protected PlatformMockFactory() {
    m_javaProject = createMock(IJavaProject.class);
    m_javaEnv = CoreTestingUtils.createJavaEnvironment();
    m_bufferMap = new HashMap<>();
  }

  public <T> T createMock(Class<T> type) {
    T mock = mock(type);
    initMock(mock, type);
    return mock;
  }

  protected void initMock(IJavaProject element) {
    initJavaElementMock(element);
  }

  protected void initMock(final IType element) {
    ITypeHierarchy hierarchy = createMock(ITypeHierarchy.class);
    try {
      when(element.newSupertypeHierarchy(any(IProgressMonitor.class))).thenReturn(hierarchy);
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }

    final IFile r = createMock(IFile.class);
    try {
      when(r.getCharset()).thenReturn(StandardCharsets.UTF_8.name());
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
    try {
      when(r.getContents()).then(new Answer<InputStream>() {
        @Override
        public InputStream answer(InvocationOnMock invocation) throws Throwable {
          ICompilationUnit icu = element.getCompilationUnit();
          if (icu == null) {
            return null;
          }
          return new ByteArrayInputStream(icu.getBuffer().getContents().getBytes(r.getCharset()));
        }
      });
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
    try {
      doAnswer(new Answer<Void>() {
        @Override
        @SuppressWarnings("resource")
        public Void answer(InvocationOnMock invocation) throws Throwable {
          ICompilationUnit compilationUnit = element.getCompilationUnit();
          if (compilationUnit == null) {
            return null;
          }
          InputStream inputStream = invocation.getArgumentAt(0, InputStream.class);
          compilationUnit.getBuffer().setContents(CoreUtils.inputStreamToString(inputStream, r.getCharset()).toString());
          return null;
        }
      }).when(r).setContents(any(InputStream.class), Matchers.anyInt(), any(IProgressMonitor.class));
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }

    when(r.exists()).thenReturn(Boolean.TRUE);
    when(element.getResource()).thenReturn(r);
    IPath p = createMock(IPath.class);
    when(r.getLocation()).thenReturn(p);
    when(p.toString()).then(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        return element.getFullyQualifiedName();
      }
    });

    initJavaElementMock(element);
  }

  protected void initMock(IFile element) {
    when(element.getType()).thenReturn(IResource.FILE);
  }

  protected void initMock(ITypeHierarchy element) {
    when(element.getAllTypes()).thenReturn(new IType[]{}); // empty by default
  }

  protected void initMock(IJavaEnvironmentProvider element) {
    Mockito.when(element.get(any(IJavaProject.class))).thenReturn(getJavaEnvironment());
  }

  protected void initMock(IPackageFragmentRoot element) {
    IPath sourceFolderPath = createMock(IPath.class);
    IPackageFragment pck = createMock(IPackageFragment.class);
    when(element.getPath()).thenReturn(sourceFolderPath);
    when(element.getPackageFragment(Matchers.anyString())).thenReturn(pck);
    initJavaElementMock(element);
  }

  protected void initMock(IPath element) {
    when(element.toString()).thenReturn(new File(".").getAbsoluteFile().getParent());
  }

  protected void initMock(IPackageFragment element) {
    when(element.getCompilationUnit(Matchers.anyString())).then(new Answer<ICompilationUnit>() {
      @Override
      public ICompilationUnit answer(InvocationOnMock invocation) throws Throwable {
        ICompilationUnit icu = createMock(ICompilationUnit.class);
        when(icu.getElementName()).thenReturn(invocation.getArgumentAt(0, String.class));
        return icu;
      }
    });
    initJavaElementMock(element);
  }

  protected void initMock(final ICompilationUnit element) {
    IBuffer buffer = createMock(IBuffer.class);
    final IType primaryType = createMock(IType.class);
    m_bufferMap.put(primaryType, buffer);
    try {
      when(element.getBuffer()).thenReturn(buffer);
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
    try {
      when(element.getSource()).thenAnswer(new Answer<String>() {
        @Override
        public String answer(InvocationOnMock invocation) throws Throwable {
          return element.getBuffer().getContents();
        }
      });
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
    when(primaryType.getFullyQualifiedName()).then(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        return parseFqn(element.getBuffer().getContents());
      }
    });
    when(primaryType.getElementName()).then(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        return Signature.getSimpleName(primaryType.getFullyQualifiedName());
      }
    });
    when(primaryType.getCompilationUnit()).thenReturn(element);
    when(element.getType(Matchers.anyString())).thenReturn(primaryType);
    initJavaElementMock(element);
  }

  protected void initMock(IBuffer element) {
    final StringBuilder builder = new StringBuilder();
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        String content = invocation.getArgumentAt(0, String.class);
        builder.delete(0, builder.length());
        builder.append(content);

        // update new content in environment
        String typeFqn = parseFqn(content);
        IJavaEnvironment environment = getJavaEnvironment();
        boolean reloadRequired = environment.registerCompilationUnitOverride(Signature.getQualifier(typeFqn), Signature.getSimpleName(typeFqn) + SuffixConstants.SUFFIX_STRING_java, builder);
        if (reloadRequired) {
          environment.reload();
        }
        return null;
      }
    }).when(element).setContents(Matchers.anyString());
    doAnswer(new Answer<Character>() {
      @Override
      public Character answer(InvocationOnMock invocation) throws Throwable {
        int index = invocation.getArgumentAt(0, int.class);
        return builder.charAt(index);
      }
    }).when(element).getChar(Matchers.anyInt());
    doAnswer(new Answer<char[]>() {
      @Override
      public char[] answer(InvocationOnMock invocation) throws Throwable {
        char[] result = new char[builder.length()];
        builder.getChars(0, builder.length(), result, 0);
        return result;
      }
    }).when(element).getCharacters();
    doAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        return builder.toString();
      }
    }).when(element).getContents();
    doAnswer(new Answer<Integer>() {
      @Override
      public Integer answer(InvocationOnMock invocation) throws Throwable {
        return Integer.valueOf(builder.length());
      }
    }).when(element).getLength();
  }

  protected void initMock(Object element, Class<?> type) {
    try {
      Method method = getClass().getDeclaredMethod("initMock", type);
      invokeMethod(element, method);
    }
    catch (NoSuchMethodException | SecurityException e1) {
      // nop
    }
  }

  protected void invokeMethod(Object element, Method method) {
    try {
      method.invoke(this, element);
    }
    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new SdkException(e);
    }
  }

  protected String parseFqn(String icuSource) {
    icuSource = CoreUtils.removeComments(icuSource);
    StringBuilder fqn = new StringBuilder();
    final String packageIdentifier = "package ";
    final Pattern classIdentifier = CLASS_NAME_PATTERN;
    int packagePos = icuSource.indexOf(packageIdentifier);
    if (packagePos >= 0) {
      int semiPos = icuSource.indexOf(';', packagePos + packageIdentifier.length());
      if (semiPos > packagePos) {
        String pck = icuSource.substring(packagePos + packageIdentifier.length(), semiPos);
        fqn.append(pck).append('.');
      }
    }

    Matcher matcher = classIdentifier.matcher(icuSource);
    if (matcher.find()) {
      fqn.append(matcher.group(1));
    }
    else {
      throw new SdkException("Unable to parse class fqn");
    }
    return fqn.toString();
  }

  protected void initJavaElementMock(IJavaElement element) {
    when(element.exists()).thenReturn(Boolean.TRUE);
    when(element.getJavaProject()).thenReturn(getJavaProject());
  }

  public IBuffer getBufferFor(String typeFqn) {
    for (Entry<IType, IBuffer> entry : m_bufferMap.entrySet()) {
      if (StringUtils.isNotBlank(entry.getValue().getContents()) && typeFqn.equals(entry.getKey().getFullyQualifiedName())) {
        return entry.getValue();
      }
    }
    return null;
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  public IJavaEnvironment getJavaEnvironment() {
    return m_javaEnv;
  }
}
