/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.operation.wellform;

import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutInterfaceApi;
import org.eclipse.scout.sdk.core.s.java.apidef.ScoutApi;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link WellformAllOperation}</h3>
 *
 * @since 5.1.0
 */
public class WellformAllOperation implements BiConsumer<EclipseEnvironment, EclipseProgress> {

  @Override
  @SuppressWarnings("squid:S1067")
  public void accept(EclipseEnvironment env, EclipseProgress p) {
    var numTicks = 100;
    var searchStepTicks = 1;
    var progress = SubMonitor.convert(p.monitor(), "Wellform Scout classes...", numTicks);
    progress.subTask("Searching for classes...");

    Set<IType> types = new HashSet<>();
    var roots = getRootClasses();
    for (var root : roots) {
      var rootTypes = JdtUtils.resolveJdtTypes(root);
      for (var t : rootTypes) {
        try {
          var typeHierarchy = t.newTypeHierarchy(null);
          for (var candidate : typeHierarchy.getAllClasses()) {
            if (JdtUtils.exists(candidate) && !candidate.isInterface() && !candidate.isBinary() && !candidate.isAnonymous() && candidate.getDeclaringType() == null) {
              types.add(candidate);
            }
            if (progress.isCanceled()) {
              return;
            }
          }
        }
        catch (JavaModelException e) {
          SdkLog.warning("Unable to collect classes to wellform for base type '{}'. These classes will be skipped.", t.getFullyQualifiedName(), e);
        }
      }
      progress.worked(searchStepTicks);
    }

    progress.subTask("Wellform classes...");
    new WellformScoutTypeOperation(types, true).accept(env, p.newChild(numTicks - (searchStepTicks * roots.size())));
  }

  protected static Set<String> getRootClasses() {
    return ScoutApi.allKnown()
        .flatMap(WellformAllOperation::getRootClasses)
        .map(ITypeNameSupplier::fqn)
        .collect(toSet());
  }

  protected static Stream<ITypeNameSupplier> getRootClasses(IScoutInterfaceApi api) {
    return Stream.of(api.ICodeType(), api.IDesktop(), api.IDesktopExtension(), api.IForm(), api.IWizard(), api.IPage(), api.IOutline());
  }

  @Override
  public String toString() {
    return "Wellform all Scout classes";
  }
}
