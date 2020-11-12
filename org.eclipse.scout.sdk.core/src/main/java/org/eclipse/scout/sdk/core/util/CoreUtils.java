/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.util;

import java.awt.*;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.log.SdkLog;

/**
 * <h3>{@link CoreUtils}</h3> Holds core utilities.
 *
 * @since 5.1.0
 */
public final class CoreUtils {

  private static final Pattern PATH_SEGMENT_SPLIT_PATTERN = Pattern.compile("/");
  @SuppressWarnings("HardcodedLineSeparator")
  private static final Pattern REGEX_COMMENT_REMOVE_1 = Pattern.compile("//.*?\r\n");
  @SuppressWarnings("HardcodedLineSeparator")
  private static final Pattern REGEX_COMMENT_REMOVE_2 = Pattern.compile("//.*?\n");
  private static final Pattern REGEX_COMMENT_REMOVE_3 = Pattern.compile("(?s)/\\*.*?\\*/");
  private static final ThreadLocal<String> CURRENT_USER_NAME = ThreadLocal.withInitial(() -> null);

  private CoreUtils() {
  }

  /**
   * Returns the user name of the current thread. If the current thread has no user name set, the system property is
   * returned.<br>
   * Use {@link CoreUtils#setUsernameForThread(String)} to define the user name for the current thread.
   *
   * @return The user name of the thread or the system if no user name is defined on the thread.
   */
  public static String getUsername() {
    var name = CURRENT_USER_NAME.get();
    if (name != null) {
      return name;
    }
    //noinspection AccessOfSystemProperties
    return System.getProperty("user.name");
  }

  /**
   * Sets the user name that should be returned by {@link CoreUtils#getUsername()} for the current thread.
   *
   * @param newUsernameForCurrentThread
   *          the new user name
   */
  public static void setUsernameForThread(String newUsernameForCurrentThread) {
    setThreadLocal(CURRENT_USER_NAME, newUsernameForCurrentThread);
  }

  /**
   * Removes all comments in the given java source.
   *
   * @param methodBody
   *          The java source
   * @return The source with all comments (single line & multi line) removed.
   */
  public static String removeComments(CharSequence methodBody) {
    if (methodBody == null) {
      return null;
    }
    if (Strings.isBlank(methodBody)) {
      return methodBody.toString();
    }
    var retVal = REGEX_COMMENT_REMOVE_1.matcher(methodBody).replaceAll("");
    retVal = REGEX_COMMENT_REMOVE_2.matcher(retVal).replaceAll("");
    retVal = REGEX_COMMENT_REMOVE_3.matcher(retVal).replaceAll("");
    return retVal;
  }

  /**
   * Deletes the given file or folder.<br>
   * In case the given {@link Path} is a folder the contents of the folder are deleted recursively.<br>
   * In case the given {@link Path} does not exist this method does nothing.
   *
   * @param toDelete
   *          The file or folder to delete. Must not be {@code null}
   * @throws IOException
   *           if there is an error deleting the directory
   */
  public static void deleteDirectory(Path toDelete) throws IOException {
    if (!Files.exists(Ensure.notNull(toDelete))) {
      return;
    }

    Files.walkFileTree(toDelete, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return super.visitFile(file, attrs);
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        try {
          // try to delete the file anyway, even if its attributes could not be read, since delete-only access is theoretically possible
          Files.delete(file);
        }
        catch (IOException e) {
          SdkLog.debug("Unable to delete '{}' after failed visit.", file, e);
        }
        if (exc instanceof NoSuchFileException) {
          return FileVisitResult.SKIP_SUBTREE;
        }
        throw exc;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return super.postVisitDirectory(dir, exc);
      }
    });
  }

  /**
   * Moves the given directory to the given target directory. This means after this method call the source directory
   * does not exist anymore and the target directory contains a new folder with the name of the source and its content.
   *
   * @param sourceDir
   *          Must be an existing directory.
   * @param targetDir
   *          Must be an existing directory.
   * @throws IOException
   *           if there is an error moving the directory
   */
  public static void moveDirectory(Path sourceDir, Path targetDir) throws IOException {
    Ensure.notNull(sourceDir);
    Ensure.notNull(targetDir);
    Ensure.isDirectory(sourceDir);
    Ensure.isDirectory(targetDir);

    var fileName = Ensure.notNull(sourceDir.getFileName());
    var targetPath = targetDir.resolve(fileName.toString());
    Files.createDirectories(targetPath); // ensure target exists

    if (Objects.equals(Files.getFileStore(sourceDir), Files.getFileStore(targetPath))) {
      Files.move(sourceDir, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }
    else {
      Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Files.copy(file, targetPath.resolve(sourceDir.relativize(file)));
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          Files.createDirectories(targetPath.resolve(sourceDir.relativize(dir)));
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }

  /**
   * Creates a relative {@link URI} which leads from base to child.<br>
   * <br>
   * <b>Note:</b>This method is capable to also construct relative {@link URI}s with parent references (/../) unlike
   * {@link URI#relativize(URI)}.
   *
   * @param base
   *          The base {@link URI} from which point the relative {@link URI} should be created. Must not be
   *          {@code null}.
   * @param child
   *          The target {@link URI} that should be relatively expressed from the point of the base {@link URI}. Must
   *          not be {@code null}.
   * @return A new relative {@link URI} to get to the child {@link URI} from the base {@link URI}.
   */
  public static URI relativizeURI(URI base, URI child) {
    if (!Objects.equals(base.getAuthority(), child.getAuthority())
        || !Objects.equals(base.getScheme(), child.getScheme())) {
      return child;
    }

    // Normalize paths to remove . and .. segments
    base = base.normalize();
    child = child.normalize();

    var bParts = PATH_SEGMENT_SPLIT_PATTERN.split(base.getRawPath());
    var cParts = PATH_SEGMENT_SPLIT_PATTERN.split(child.getRawPath());

    // Discard trailing segment of base path
    if (bParts.length > 0 && !base.getPath().endsWith("/")) {
      bParts = Arrays.copyOf(bParts, bParts.length - 1);
    }

    // Remove common prefix segments
    var i = 0;
    while (i < bParts.length
        && i < cParts.length
        && bParts[i].equals(cParts[i])) {
      i++;
    }

    // Construct the relative path
    var sb = new StringBuilder();
    sb.append("../".repeat(Math.max(0, bParts.length - i)));
    for (var j = i; j < cParts.length; j++) {
      if (j != i) {
        sb.append('/');
      }
      sb.append(cParts[j]);
    }

    return URI.create(sb.toString()).normalize();
  }

  /**
   * Gets the parent of the given {@link URI}. This means it removes the last segment of the path of the given
   * {@link URI}.
   *
   * @param uri
   *          the parent path (one folder up) this {@link URI} is returned.
   * @return A new {@link URI} pointing to the parent of the given {@link URI} or {@code null} if the given {@link URI}
   *         is {@code null}.
   */
  public static URI getParentURI(URI uri) {
    if (uri == null) {
      return null;
    }
    if (uri.getPath().endsWith("/")) {
      return uri.resolve("..");
    }
    return uri.resolve(".");
  }

  /**
   * Compares the given double values. Returns {@code true} if the difference between the two double values is bigger
   * than the given delta.<br>
   * <br>
   * Special cases:
   * <ul>
   * <li>{@code -0} and {@code +0} are considered to be equal even though
   * {@code Double.valueOf(0d).equals(Double.valueOf(-0d))} returns {@code false}!</li>
   * <li>{@link Double#NaN} and {@link Double#NaN} are considered to be equal even though
   * {@code Double.NaN == Double.NaN} returns {@code false}!</li>
   * </ul>
   *
   * @param d1
   *          The first double value
   * @param d2
   *          The second double value
   * @param delta
   *          The difference between the two to be considered equal.
   * @return {@code false} if the difference between the two values is less or equal to the given delta.
   */
  public static boolean isDoubleDifferent(double d1, double d2, double delta) {
    if (Double.compare(d1, d2) == 0) {
      // handles NaN, Double.POSITIVE_INFINITY and Double.NEGATIVE_INFINITY
      return false;
    }
    return !(Math.abs(d1 - d2) <= Math.abs(delta));
  }

  /**
   * Executes the specified {@link Supplier} while the specified {@link ThreadLocal} has the specified context value.
   *
   * @param threadLocal
   *          The {@link ThreadLocal} in which the context should be stored. The initial value of the
   *          {@link ThreadLocal} must be {@code null}!
   * @param context
   *          The context to store. May be {@code null}.
   * @param callable
   *          The {@link Supplier} to execute. Must not be {@code null}.
   */
  public static <T, R> R callInContext(ThreadLocal<T> threadLocal, T context, Supplier<R> callable) {
    var orig = threadLocal.get();
    try {
      setThreadLocal(threadLocal, context);
      return callable.get();
    }
    finally {
      setThreadLocal(threadLocal, orig);
    }
  }

  private static <T> void setThreadLocal(ThreadLocal<T> tl, T context) {
    if (context == null) {
      tl.remove();
    }
    else {
      tl.set(context);
    }
  }

  /**
   * Gets the file extension of the file path specified. This is the part after the last dot in the path given.
   *
   * @param filePath
   *          The file path for which the extension should be returned.
   * @return The extension if it exists or an empty {@link String} otherwise (never returns {@code null}). The extension
   *         is always lowercase and does not contain the dot.
   */
  public static String extensionOf(String filePath) {
    if (Strings.isBlank(filePath)) {
      return "";
    }
    var lastDot = filePath.lastIndexOf('.');
    if (lastDot < 0) {
      return "";
    }
    return filePath.substring(lastDot + 1).toLowerCase(Locale.US);
  }

  /**
   * Gets the file extension of the {@link Path} specified. This is the part after the last dot in the last segment of
   * the given file.
   *
   * @param file
   *          The file for which the extension should be returned.
   * @return The extension if it exists or an empty {@link String} otherwise (never returns {@code null}). The extension
   *         is always lowercase and does not contain the dot.
   */
  public static String extensionOf(Path file) {
    if (file == null) {
      return "";
    }
    var lastSegment = file.getFileName();
    if (lastSegment == null) {
      return "";
    }
    return extensionOf(lastSegment.toString());
  }

  /**
   * Gets the value of {@link Object#toString()} of the specified object if the method has been overwritten with a
   * custom implementation.
   *
   * @param o
   *          The {@link Object} for which the {@link String} representation should be returned.
   * @return An {@link Optional} holding the value of {@link Object#toString()} if it has been overwritten and could be
   *         invoked successfully. Otherwise an empty {@link Optional} is returned.
   */
  @SuppressWarnings("squid:S1181")
  public static Optional<String> toStringIfOverwritten(Object o) {
    if (o == null) {
      return Optional.empty();
    }

    try {
      var declaringClassFqn = o.getClass().getMethod("toString").getDeclaringClass().getName();
      if (Object.class.getName().equals(declaringClassFqn) || "kotlin.jvm.internal.Lambda".equals(declaringClassFqn)) {
        return Optional.empty();
      }
    }
    catch (NoSuchMethodException e) {
      SdkLog.debug("Cannot get toString method of object {}", o.getClass(), e);
      return Optional.empty();
    }

    String val = null;
    try {
      val = o.toString();
    }
    catch (Throwable t) {
      SdkLog.warning("Failed toString() invocation on an object of type [{}]", o.getClass().getName(), t);
    }

    return Strings.notBlank(val);
  }

  /**
   * Sets the given {@link String} into the system clipboard
   *
   * @param text
   *          The text to set
   * @return {@code true} if the text has been successfully set to the system clipboard.
   */
  public static boolean setTextToClipboard(String text) {
    return setTextToClipboard(text, null);
  }

  /**
   * Sets the given {@link String} into the system clipboard
   *
   * @param text
   *          The text to set
   * @param ownershipLostCallback
   *          An optional callback invoked if the owner ship of the clipboard is lost.
   * @return {@code true} if the text has been successfully set to the system clipboard.
   */
  public static boolean setTextToClipboard(String text, ClipboardOwner ownershipLostCallback) {
    if (GraphicsEnvironment.isHeadless()) {
      return false;
    }

    try {
      var stringSelection = new StringSelection(text);
      var owner = ownershipLostCallback == null ? stringSelection : ownershipLostCallback;
      var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(stringSelection, owner);
      return true;
    }
    catch (Exception e) {
      SdkLog.debug("Error setting text to system clipboard.", e);
      return false;
    }
  }

  /**
   * Invokes a default method of an interface.
   * 
   * @param instance
   *          The instance on which the default method should be called.
   * @param method
   *          The method in the interface to call.
   * @param args
   *          The arguments to pass to the the method.
   * @return The result of the method call
   * @throws RuntimeException
   *           if the called method threw a {@link RuntimeException}
   * @throws SdkException
   *           wrapping the original exception if it was a checked exception.
   */
  public static Object invokeDefaultMethod(Object instance, Method method, Object[] args) {
    try {
      var ifcClass = method.getDeclaringClass();
      return MethodHandles.lookup()
          .findSpecial(ifcClass, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()), ifcClass)
          .bindTo(instance)
          .invokeWithArguments(args);
    }
    catch (RuntimeException | Error e) {
      throw e;
    }
    catch (Throwable e) {
      SdkLog.debug("Exception calling default method '{}'.", method, e);
      var cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      throw new SdkException(cause);
    }
  }
}
