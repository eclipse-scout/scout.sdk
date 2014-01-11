package org.eclipse.scout.sdk.util.typecache;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 * <h3>{@link ITypeCache}</h3> Type cache handling access to {@link IType} instances.<br>
 * <br>
 * Use the {@link TypeUtility} to search for types hold by the type cache.
 * 
 * @author Matthias Villiger
 * @since 3.4
 * @see TypeUtility
 * @see IType
 * @see IJavaProject
 */
public interface ITypeCache {
  /**
   * Returns the first {@link IType} having the given name that is on all classpaths in the
   * workspace (all {@link IJavaProject}s).
   * 
   * @param typeName
   *          The fully qualified or simple name of the {@link IType} to return.
   * @return The first {@link IType} having the given name that is on all classpaths in the
   *         workspace (all {@link IJavaProject}s).
   */
  IType getType(String typeName);

  /**
   * Returns all {@link IType}s with the given name that are on all classpaths in the
   * workspace (all {@link IJavaProject}s).
   * 
   * @param typeName
   *          The fully qualified or simple name of the {@link IType}s to return.
   * @return All {@link IType}s with the given name that are on all classpaths in the
   *         workspace (all {@link IJavaProject}s).
   */
  IType[] getTypes(String typeName);

  /**
   * Returns all {@link IType}s with the given name that are on the classpath of the given reference
   * project.
   * 
   * @param typeName
   *          The fully qualified or simple name to search.
   * @param classpath
   *          The {@link IJavaProject} defining the classpath the returned {@link IType}s must be in
   * @return an array holding all {@link IType}s with the given name that are on the classpath of the
   *         given reference project. Never returns null.
   */
  IType[] getTypes(String typeName, IJavaProject classpath);

  /**
   * Returns the first {@link IType} found on the classpath of the given {@link IJavaProject} that has the given name.
   * 
   * @param typeName
   *          The fully qualified or simple name of the type to search.d
   * @param classpath
   *          The {@link IJavaProject} defining the classpath the returned {@link IType} must be in
   * @return The first {@link IType} found on the classpath of the given {@link IJavaProject} that has the given name.
   */
  IType getType(String typeName, IJavaProject classpath);

  /**
   * Disposes the cache. Releases all cached instances and stops listening for workspace changes.
   */
  void dispose();
}
