package org.eclipse.scout.sdk.core.generator.compilationunit;

import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Helper class to compute the path segments of a compilation unit
 */
public class CompilationUnitPath {

  private final String m_fileName;
  private final Path m_targetDirectory;
  private final Path m_targetFile;

  /**
   * @param packageName
   *          The package of the compilation unit or {@code null} if the default package.
   * @param classSimpleName
   *          The simple name of the main class of the compilation unit without any file suffixes. Must not be
   *          {@code null} or empty.
   * @param sourceFolder
   *          The target source folder in which the compilation unit resides. Must not be {@code null}.
   */
  public CompilationUnitPath(String packageName, String classSimpleName, Path sourceFolder) {
    m_fileName = Ensure.notBlank(classSimpleName) + JavaTypes.JAVA_FILE_SUFFIX;
    if (Strings.isBlank(packageName)) {
      m_targetDirectory = sourceFolder;
    }
    else {
      m_targetDirectory = sourceFolder.resolve(packageName.replace(JavaTypes.C_DOT, File.separatorChar));
    }
    m_targetFile = m_targetDirectory.resolve(m_fileName);
  }

  /**
   * @param generator
   *          The {@link ICompilationUnitGenerator} that defines name and package. Must not be {@code null}.
   * @param sourceFolder
   *          The source folder in which the compilation unit will be created. Must not be {@code null}.
   */
  public CompilationUnitPath(ICompilationUnitGenerator<?> generator, IClasspathEntry sourceFolder) {
    this(generator.packageName().orElse(""),
        generator.elementName().orElseThrow(() -> Ensure.newFail("File name missing in generator")),
        sourceFolder.path());
  }

  /**
   * @return The filename of the compilation unit. E.g. {@code MyClass.java}. Never returns {@code null}.
   */
  public String fileName() {
    return m_fileName;
  }

  /**
   * @return The absolute directory in which the compilation unit will be stored. Never returns {@code null}.
   */
  public Path targetDirectory() {
    return m_targetDirectory;
  }

  /**
   * @return The absolute path in which the compilation unit will be stored. Never returns {@code null}.
   */
  public Path targetFile() {
    return m_targetFile;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CompilationUnitPath that = (CompilationUnitPath) o;
    return m_fileName.equals(that.m_fileName) &&
        m_targetDirectory.equals(that.m_targetDirectory) &&
        m_targetFile.equals(that.m_targetFile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_fileName, m_targetDirectory, m_targetFile);
  }
}
