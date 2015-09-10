package org.eclipse.scout.sdk.core.model.spi;

import java.util.Collection;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;

/**
 * <h3>{@link JavaEnvironmentSpi}</h3> Represents a lookup environment (classpath) capable to resolve {@link TypeSpi}s
 * by name.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public interface JavaEnvironmentSpi {
  PackageSpi getPackage(String name);

  /**
   * Tries to find the {@link TypeSpi} with the given name in the receiver {@link JavaEnvironmentSpi} (classpath).
   * <p>
   * Also primitive types such as int, float, void, null etc. are supported
   *
   * @param fqn
   *          The fully qualified name of the {@link TypeSpi} to find. For inner {@link TypeSpi}s the inner part must be
   *          separated using '$': <code>org.eclipse.scout.hello.world.MainClass$InnerClass$AnotherInnerClass</code>.
   * @return The {@link Iype} matching the given fully qualified name or <code>null</code> if it could not be found.
   */
  TypeSpi findType(String fqn);

  /**
   * @return the new {@link JavaEnvironmentSpi}. This new environment is automatically published to all existing api
   *         classes that are wrapping an spi of it
   */
  JavaEnvironmentSpi reload();

  /**
   * Register an override for a (possibly) existing compilation unit. This only has an effect after a call to
   * {@link #reload()}
   *
   * @param packageName
   * @param fileName
   * @param buf
   */
  void registerCompilationUnitOverride(String packageName, String fileName, StringBuilder buf);

  /**
   * @param fqn
   *          type name
   * @return null if the type has no compilation errors
   */
  String getCompileErrors(String fqn);

  Collection<ClasspathSpi> getClasspath();

  IJavaEnvironment wrap();

}
