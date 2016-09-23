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
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.testing.JavaEnvironmentBuilder;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.CachingJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * <h3>{@link FileSystemMockFactory}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class FileSystemMockFactory {

  public IJavaEnvironmentProvider createJavaEnvProvider() {
    return new CachingJavaEnvironmentProvider() {
      @Override
      protected IJavaEnvironment getOrCreateEnv(IJavaProject jdtProject) {
        File projectRoot = jdtProject.getProject().getLocation().toFile();
        return new JavaEnvironmentBuilder()
            .withAbsoluteSourcePath(new File(projectRoot, "src/main/java").getAbsolutePath())
            .withAbsoluteSourcePath(new File(projectRoot, "src/main/resources").getAbsolutePath())
            .withAbsoluteSourcePath(new File(projectRoot, "target/generated-sources/annotations").getAbsolutePath())
            .withAbsoluteSourcePath(new File(projectRoot, "target/generated-sources/wsimport").getAbsolutePath())
            .build();
      }
    };
  }

  public IType createIType(final File location, final IJavaProject owner) {
    final IType type = mock(IType.class);
    fillJavaElementMock(type, owner, location);
    when(type.getFullyQualifiedName()).then(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        File projectLoc = type.getResource().getProjectRelativePath().toFile();
        Path relPath = projectLoc.toPath();
        String fqn = relPath.subpath(3, relPath.getNameCount()).toString().replace(relPath.getFileSystem().getSeparator(), ".");
        if (fqn.endsWith(SuffixConstants.SUFFIX_STRING_java)) {
          fqn = fqn.substring(0, fqn.length() - SuffixConstants.SUFFIX_STRING_java.length());
        }
        return fqn;
      }
    });
    when(type.getAncestor(anyInt())).then(new Answer<IJavaElement>() {
      @Override
      public IJavaElement answer(InvocationOnMock invocation) throws Throwable {
        int kind = invocation.getArgumentAt(0, Integer.class).intValue();
        switch (kind) {
          case IJavaElement.PACKAGE_FRAGMENT_ROOT:
            int numPackageNames = StringUtils.countMatches(type.getFullyQualifiedName(), ".") + 1;
            Path path = location.toPath();
            return createPackageFragmentRoot(path.resolve(StringUtils.repeat("../", numPackageNames)).normalize().toFile(), type.getJavaProject());
          default:
            throw new UnsupportedOperationException();
        }
      }
    });
    when(type.getResource()).thenAnswer(new Answer<IResource>() {
      @Override
      public IResource answer(InvocationOnMock invocation) throws Throwable {
        return createFile(location, owner.getProject());
      }
    });
    return type;
  }

  public IJavaProject createJavaProject(final File location) {
    final IJavaProject jp = mock(IJavaProject.class);
    when(jp.getProject()).thenAnswer(new Answer<IProject>() {
      @Override
      public IProject answer(InvocationOnMock invocation) throws Throwable {
        return createProject(location);
      }
    });
    when(jp.getResource()).thenAnswer(new Answer<IResource>() {
      @Override
      public IResource answer(InvocationOnMock invocation) throws Throwable {
        return createFolder(location, jp.getProject());
      }
    });
    try {
      when(jp.getPackageFragmentRoots()).thenAnswer(new Answer<IPackageFragmentRoot[]>() {
        @Override
        public IPackageFragmentRoot[] answer(InvocationOnMock invocation) throws Throwable {
          String[] relPath = {"src/main/java", "src/main/resources", "src/test/java", "src/test/resources", "src/generated/java", "target/generated-sources/annotations", "target/generated-sources/wsimport"};
          List<IPackageFragmentRoot> roots = new ArrayList<>();
          for (String s : relPath) {
            File candidate = new File(location, s);
            if (candidate.exists()) {
              roots.add(createPackageFragmentRoot(candidate, jp));
            }
          }
          return roots.toArray(new IPackageFragmentRoot[roots.size()]);
        }
      });
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
    fillJavaElementMock(jp, jp, location);
    return jp;
  }

  public ICompilationUnit createCompilationUnit(final File location, final IJavaProject owner) {
    final ICompilationUnit p = mock(ICompilationUnit.class);
    fillJavaElementMock(p, owner, location);
    when(p.getResource()).thenAnswer(new Answer<IResource>() {
      @Override
      public IResource answer(InvocationOnMock invocation) throws Throwable {
        return createFile(location, owner.getProject());
      }
    });
    try {
      when(p.getBuffer()).then(new Answer<IBuffer>() {
        @Override
        public IBuffer answer(InvocationOnMock invocation) throws Throwable {
          return createBuffer(location);
        }
      });
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
    return p;
  }

  public IBuffer createBuffer(final File location) {
    final IBuffer p = mock(IBuffer.class);
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        String newContent = invocation.getArgumentAt(0, String.class);
        Files.write(location.toPath(), newContent.getBytes(StandardCharsets.UTF_8));
        return null;
      }
    }).when(p).setContents(anyString());
    return p;
  }

  public IPackageFragment createPackageFragment(final File location, final IJavaProject owner) {
    final IPackageFragment p = mock(IPackageFragment.class);
    fillJavaElementMock(p, owner, location);
    when(p.getCompilationUnit(anyString())).thenAnswer(new Answer<ICompilationUnit>() {
      @Override
      public ICompilationUnit answer(InvocationOnMock invocation) throws Throwable {
        String fileName = invocation.getArgumentAt(0, String.class);
        return createCompilationUnit(new File(location, fileName), owner);
      }
    });
    when(p.getResource()).thenAnswer(new Answer<IResource>() {
      @Override
      public IResource answer(InvocationOnMock invocation) throws Throwable {
        return createFolder(location, owner.getProject());
      }
    });
    try {
      when(p.createCompilationUnit(anyString(), anyString(), anyBoolean(), any(IProgressMonitor.class))).thenAnswer(new Answer<ICompilationUnit>() {
        @Override
        public ICompilationUnit answer(InvocationOnMock invocation) throws Throwable {
          String fileName = invocation.getArgumentAt(0, String.class);
          ICompilationUnit result = p.getCompilationUnit(fileName);
          String content = invocation.getArgumentAt(1, String.class);
          Files.write(result.getResource().getLocation().toFile().toPath(), content.getBytes(StandardCharsets.UTF_8));
          return result;
        }
      });
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }

    return p;
  }

  public IPackageFragmentRoot createPackageFragmentRoot(final File location, final IJavaProject owner) {
    final IPackageFragmentRoot p = mock(IPackageFragmentRoot.class);
    final IClasspathEntry cpEntry = mock(IClasspathEntry.class);
    fillJavaElementMock(p, owner, location);
    try {
      when(p.getKind()).thenReturn(IPackageFragmentRoot.K_SOURCE);
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
    when(p.getPackageFragment(anyString())).thenAnswer(new Answer<IPackageFragment>() {
      @Override
      public IPackageFragment answer(InvocationOnMock invocation) throws Throwable {
        String pck = invocation.getArgumentAt(0, String.class).replace('.', File.separatorChar);
        File pckLocation = new File(location, pck);
        return createPackageFragment(pckLocation, owner);
      }
    });
    try {
      when(p.getRawClasspathEntry()).thenAnswer(new Answer<IClasspathEntry>() {
        @Override
        public IClasspathEntry answer(InvocationOnMock invocation) throws Throwable {
          return cpEntry;
        }
      });
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
    try {
      doAnswer(new Answer<IPackageFragment>() {
        @Override
        public IPackageFragment answer(InvocationOnMock invocation) throws Throwable {
          IPackageFragment fragment = p.getPackageFragment(invocation.getArgumentAt(0, String.class));
          Files.createDirectories(fragment.getResource().getLocation().toFile().toPath());
          return fragment;
        }
      }).when(p).createPackageFragment(anyString(), anyBoolean(), any(IProgressMonitor.class));
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
    when(p.getResource()).thenAnswer(new Answer<IResource>() {
      @Override
      public IResource answer(InvocationOnMock invocation) throws Throwable {
        return createFolder(location, owner.getProject());
      }
    });
    return p;
  }

  public IProject createProject(final File location) {
    final IProject p = mock(IProject.class);
    fillIResourceMock(p, p, location, IResource.PROJECT);
    when(p.getFolder(anyString())).thenAnswer(new Answer<IFolder>() {
      @Override
      public IFolder answer(InvocationOnMock invocation) throws Throwable {
        return createFolder(new File(location, invocation.getArgumentAt(0, String.class)), p);
      }
    });
    when(p.getFile(anyString())).then(new Answer<IFile>() {
      @Override
      public IFile answer(InvocationOnMock invocation) throws Throwable {
        return createFile(new File(location, invocation.getArgumentAt(0, String.class)), p);
      }
    });
    return p;
  }

  public IFolder createFolder(final File location, final IProject p) {
    IFolder f = mock(IFolder.class);
    when(f.getFile(anyString())).then(new Answer<IFile>() {
      @Override
      public IFile answer(InvocationOnMock invocation) throws Throwable {
        return createFile(new File(location, invocation.getArgumentAt(0, String.class)), p);
      }
    });
    fillIResourceMock(f, p, location, IResource.FOLDER);
    when(f.getFolder(anyString())).then(new Answer<IFolder>() {
      @Override
      public IFolder answer(InvocationOnMock invocation) throws Throwable {
        return createFolder(new File(location, invocation.getArgumentAt(0, String.class)), p);
      }
    });

    try {
      doAnswer(new Answer<Void>() {
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
          Files.createDirectories(location.toPath());
          return null;
        }
      }).when(f).create(anyBoolean(), anyBoolean(), any(IProgressMonitor.class));
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
    return f;
  }

  public IFile createFile(final File location, final IProject p) {
    final IFile f = mock(IFile.class);
    fillIResourceMock(f, p, location, IResource.FILE);
    try {
      when(f.getCharset()).then(new Answer<String>() {
        @Override
        public String answer(InvocationOnMock invocation) throws Throwable {
          return StandardCharsets.UTF_8.name();
        }
      });
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
    try {
      when(f.getContents()).then(new Answer<InputStream>() {
        @Override
        public InputStream answer(InvocationOnMock invocation) throws Throwable {
          return new FileInputStream(location);
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
          InputStream in = invocation.getArgumentAt(0, InputStream.class);
          StringBuilder content = CoreUtils.inputStreamToString(in, StandardCharsets.UTF_8);
          Files.write(location.toPath(), content.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
          return null;
        }
      }).when(f).create(any(InputStream.class), anyBoolean(), any(IProgressMonitor.class));
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
    try {
      doAnswer(new Answer<Void>() {
        @Override
        @SuppressWarnings("resource")
        public Void answer(InvocationOnMock invocation) throws Throwable {
          InputStream in = invocation.getArgumentAt(0, InputStream.class);
          f.create(in, invocation.getArgumentAt(1, Boolean.class).booleanValue(), invocation.getArgumentAt(3, IProgressMonitor.class));
          return null;
        }
      }).when(f).setContents(any(InputStream.class), anyBoolean(), anyBoolean(), any(IProgressMonitor.class));
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }

    return f;
  }

  public IPath createPath(final File location) {
    IPath p = mock(IPath.class);
    when(p.toFile()).then(new Answer<File>() {
      @Override
      public File answer(InvocationOnMock invocation) throws Throwable {
        return location;
      }
    });
    when(p.removeFirstSegments(Matchers.anyInt())).then(new Answer<IPath>() {
      @Override
      public IPath answer(InvocationOnMock invocation) throws Throwable {
        int num = invocation.getArgumentAt(0, Integer.class).intValue();
        Path path = location.toPath();
        return createPath(path.subpath(num, path.getNameCount()).toFile());
      }
    });
    when(p.toString()).then(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        String result = location.toString();
        if (File.separatorChar == '/') {
          return result;
        }
        return result.replace(File.separatorChar, '/');
      }
    });
    doAnswer(new Answer<IPath>() {
      @Override
      public IPath answer(InvocationOnMock invocation) throws Throwable {
        File base = invocation.getArgumentAt(0, IPath.class).toFile();
        File relPath = base.toPath().relativize(location.toPath()).toFile();
        return createPath(relPath);
      }
    }).when(p).makeRelativeTo(any(IPath.class));

    return p;
  }

  protected void fillJavaElementMock(final IJavaElement e, final IJavaProject owner, final File location) {
    when(e.getPath()).thenAnswer(new Answer<IPath>() {
      @Override
      public IPath answer(InvocationOnMock invocation) throws Throwable {
        File projectParent = owner.getProject().getLocation().toFile().getParentFile();
        return createPath(projectParent.toPath().relativize(location.toPath()).toFile());
      }
    });
    when(e.exists()).thenAnswer(new Answer<Boolean>() {
      @Override
      public Boolean answer(InvocationOnMock invocation) throws Throwable {
        return location.exists();
      }
    });
    when(e.getJavaProject()).thenAnswer(new Answer<IJavaProject>() {
      @Override
      public IJavaProject answer(InvocationOnMock invocation) throws Throwable {
        return owner;
      }
    });
  }

  protected void fillIResourceMock(final IResource r, final IProject p, final File location, int type) {
    when(r.getType()).thenReturn(Integer.valueOf(type));
    when(r.getLocation()).then(new Answer<IPath>() {
      @Override
      public IPath answer(InvocationOnMock invocation) throws Throwable {
        return createPath(location);
      }
    });

    when(r.exists()).thenAnswer(new Answer<Boolean>() {
      @Override
      public Boolean answer(InvocationOnMock invocation) throws Throwable {
        return Boolean.valueOf(location.exists());
      }
    });
    when(r.getName()).thenAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        return location.getName();
      }
    });
    when(r.getProject()).thenAnswer(new Answer<IProject>() {
      @Override
      public IProject answer(InvocationOnMock invocation) throws Throwable {
        return p;
      }
    });
    when(r.getParent()).then(new Answer<IContainer>() {
      @Override
      public IContainer answer(InvocationOnMock invocation) throws Throwable {
        return createFolder(location.getParentFile(), p);
      }
    });
    doAnswer(new Answer<IPath>() {
      @Override
      public IPath answer(InvocationOnMock invocation) throws Throwable {
        File projectFile = r.getProject().getLocation().toFile();
        File projectRelFile = projectFile.toPath().relativize(location.toPath()).toFile();
        return createPath(projectRelFile);
      }
    }).when(r).getProjectRelativePath();
  }
}
