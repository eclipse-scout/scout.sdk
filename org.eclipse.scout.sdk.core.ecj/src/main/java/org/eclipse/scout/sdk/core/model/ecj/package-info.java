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

/**
 * Contains the "Eclipse Compiler for Java" (ECJ) SPI implementation for the Scout SDK Java model API.
 * <p>
 * Usage Examples:<br>
 *
 * <pre>
 * private void samples() {
 *   // execution with predefined factory
 *   new JavaEnvironmentFactories.RunningJavaEnvironmentFactory().call(this::printRunningClasspath);
 *
 *   // execution with custom setup
 *   new JavaEnvironmentWithEcjBuilder()
 *       .withoutScoutSdk()
 *       .withSourceFolder("src/main/java")
 *       .withAbsoluteBinaryPath("C:/dev/libs/mylib.jar")
 *       .call(this::printArgumentTypesOfLongValueOf);
 * }
 *
 * private void printRunningClasspath(final IJavaEnvironment javaEnvironment) {
 *   final String cp = javaEnvironment
 *       .classpath()
 *       .map(IClasspathEntry::path)
 *       .map(Path::toString)
 *       .collect(Collectors.joining(File.pathSeparator));
 *
 *   System.out.println("classpath=" + cp);
 * }
 *
 * private void printArgumentTypesOfLongValueOf(final IJavaEnvironment javaEnvironment) {
 *   final String argTypeNames = javaEnvironment
 *       .requireType(Long.class.getName())
 *       .methods()
 *       .withName("valueOf")
 *       .withFlags(Flags.AccPublic)
 *       .first().orElseThrow(() -> Ensure.newFail("Cannot find valueOf methods in Long class"))
 *       .parameters().stream()
 *       .map(IMethodParameter::dataType)
 *       .map(IType::name)
 *       .collect(Collectors.joining(", "));
 *
 *   System.out.println(argTypeNames);
 * }
 * </pre>
 */
package org.eclipse.scout.sdk.core.model.ecj;
