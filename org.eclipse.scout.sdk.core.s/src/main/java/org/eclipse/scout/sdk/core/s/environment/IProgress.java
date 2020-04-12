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
package org.eclipse.scout.sdk.core.s.environment;

/**
 * <h3>{@link IProgress}</h3>
 * <p>
 * Provides progress monitoring for an activity.
 * <p>
 * <b>Usage Examples:</b>
 * <ul>
 * <li>Simple Example with a branch:
 *
 * <pre>
 * void doSomething(final IProgress progress) {
 *   progress.init("task name", 100);
 *
 *   if (condition) {
 *     // Use 50% of the progress to do some work
 *     doSomeWork(progress.newChild(50));
 *   }
 *
 *   // Don't report any work, but ensure that we have 50 ticks remaining on the progress monitor.
 *   // If we already consumed 50 ticks in the above branch, this is a no-op. Otherwise, the remaining
 *   // space in the monitor is redistributed into 50 ticks.
 *   progress.setWorkRemaining(50);
 *
 *   // Use the remainder of the progress monitor to do the rest of the work
 *   doSomeWork(progress.newChild(50));
 * }
 * </pre>
 *
 * </li>
 * <li>Loop Example:
 *
 * <pre>
 * void doSomething(final IProgress progress, final Collection<?> someCollection) {
 *   progress.init("Task", 100);
 *
 *   // Create a new progress monitor that uses 70% of the main progress and will allocate one tick
 *   // for each element of the given collection.
 *   final IProgress loopProgress = progress
 *       .newChild(70)
 *       .setWorkRemaining(someCollection.size());
 *
 *   for (final Object item : someCollection) {
 *     // If progress has been canceled, newChild() will abort with an exception. Therefore no check for canceled must pollute the method logic.
 *     doWorkOnElement(item, loopProgress.newChild(1));
 *   }
 *
 *   // Use the remaining 30% of the progress monitor to do some work outside the loop
 *   doSomeWork(progress.newChild(30));
 * }
 * </pre>
 *
 * </li>
 * </ul>
 *
 * @since 7.0.0
 */
public interface IProgress {

  /**
   * Initializes this {@link IProgress} with the specified name and total work ticks.
   *
   * @param name
   *          The task description
   * @param totalWork
   *          the total number of work units into which the main task is been subdivided.
   * @return this instance.
   */
  IProgress init(String name, int totalWork);

  /**
   * Creates a sub {@link IProgress} that will consume the given number of ticks from the receiver. See
   * {@link IProgress} for examples on how to use it.
   * <p>
   * It is not necessary to allocate ticks on the result. In that case the ticks will be consumed as soon as the next
   * child {@link IProgress} is created.<br>
   * However, the resulting progress monitor will not report any intermediate work before ticks are allocated. Ticks may
   * be allocated by calling {@link #init(String, int)} or {@link #setWorkRemaining(int)}.
   * <p>
   * This method must throw a specific {@link RuntimeException} if called on an {@link IProgress} that has been
   * canceled!
   *
   * @param work
   *          number of ticks to consume from the receiver
   * @return the new sub {@link IProgress} instance.
   */
  IProgress newChild(int work);

  /**
   * Sets the work remaining for this {@link IProgress} instance. This is the total number of ticks that may be reported
   * by all subsequent calls to {@link #worked(int)} or {@link #newChild(int)}. This may be called many times for the
   * same {@link IProgress} instance. When this method is called, the remaining space on the progress monitor is
   * redistributed into the given number of ticks.
   * <p>
   * It doesn't matter how much progress has already been reported with this {@link IProgress} instance. If you call
   * setWorkRemaining(100), you will be able to report 100 more ticks of work before the progress meter reaches 100%.
   *
   * @param workRemaining
   *          total number of remaining ticks
   * @return this instance.
   */
  IProgress setWorkRemaining(int workRemaining);

  /**
   * Notifies that a given number of work unit of the main task has been completed.
   *
   * @param work
   *          a non-negative number of work units just completed
   * @return this instance.
   */
  IProgress worked(int work);
}
