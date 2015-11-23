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
package org.eclipse.scout.sdk.core.util.compat;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 * <h3>{@link CompatibilityLayer}</h3> Class that cares about all API differences in the supported ECJ versions
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public final class CompatibilityLayer {

  private CompatibilityLayer() {
  }

  /**
   * Wrapper for {@link FileSystem#getClasspath(String, String, boolean, AccessRuleSet, String)} that works with Mars
   * and Neon release version.
   *
   * @param f
   *          The {@link File} for which to create a {@link Classpath} instance.
   * @param source
   *          <code>true</code> if the given file denotes Java source. <code>false</code> otherwise.
   * @param encoding
   *          The encoding of the files found
   * @param optionProvider
   *          A {@link Callable} that will be executed on Neon JDT version only providing the compiler options to create
   *          the classpath for.
   * @return The created {@link Classpath}.
   */
  public static Classpath getFileSystemClasspath(File f, boolean source, String encoding, Callable<Map<?, ?>> optionProvider) {
    //  in Mars SR1
    //  public static Classpath getClasspath(String classpathName, String encoding, boolean isSourceOnly, AccessRuleSet accessRuleSet, String destinationPath)
    //
    //  in Neon M3
    //  public static Classpath getClasspath(String classpathName, String encoding, boolean isSourceOnly, AccessRuleSet accessRuleSet, String destinationPath, Map options)

    Classpath result = null;
    List<Object> args = new ArrayList<>(6);
    args.add(f.getAbsolutePath()); // classpathName
    args.add(encoding); // encoding
    args.add(source); // isSourceOnly
    args.add(null); // accessRuleSet
    args.add(null); // destinationPath

    try {
      // 1. try Neon version
      Method method = MethodUtils.getAccessibleMethod(FileSystem.class, "getClasspath", String.class, String.class, boolean.class, AccessRuleSet.class, String.class, Map.class);
      if (method != null) {
        args.add(getOptionsMap(optionProvider)); // options
      }
      else {
        // 2. try Mars version
        method = MethodUtils.getAccessibleMethod(FileSystem.class, "getClasspath", String.class, String.class, boolean.class, AccessRuleSet.class, String.class);
        if (method == null) {
          throw new SdkException("Unable to find compatible 'getClasspath' method in class '" + FileSystem.class.getName() + "'.");
        }
      }

      // invoke
      result = (Classpath) method.invoke(null, args.toArray());
    }
    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
      throw new SdkException("Unable to call 'getClasspath' method in class '" + FileSystem.class.getName() + "'.", e);
    }

    return result;
  }

  private static Map<?, ?> getOptionsMap(Callable<Map<?, ?>> optionProvider) {
    try {
      return optionProvider.call();
    }
    catch (Exception e) {
      throw new SdkException(e);
    }
  }
}
