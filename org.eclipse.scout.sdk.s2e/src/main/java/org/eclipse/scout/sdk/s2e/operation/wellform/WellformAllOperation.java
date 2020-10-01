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
package org.eclipse.scout.sdk.s2e.operation.wellform;

import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.apidef.IScoutInterfaceApi;
import org.eclipse.scout.sdk.core.s.apidef.ScoutApi;
import org.eclipse.scout.sdk.core.util.apidef.IClassNameSupplier;
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
    int numTicks = 100;
    int searchStepTicks = 1;
    SubMonitor progress = SubMonitor.convert(p.monitor(), "Wellform Scout classes...", numTicks);
    progress.subTask("Searching for classes...");

    Set<IType> types = new HashSet<>();
    Set<String> roots = getRootClasses();
    for (String root : roots) {
      Set<IType> rootTypes = JdtUtils.resolveJdtTypes(root);
      for (IType t : rootTypes) {
        try {
          ITypeHierarchy typeHierarchy = t.newTypeHierarchy(null);
          for (IType candidate : typeHierarchy.getAllClasses()) {
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
        .map(IClassNameSupplier::fqn)
        .collect(toSet());
  }

  protected static Stream<IClassNameSupplier> getRootClasses(IScoutInterfaceApi api) {
    return Stream.of(api.ICodeType(), api.IDesktop(), api.IDesktopExtension(), api.IForm(), api.IWizard(), api.IPage(), api.IOutline());
  }

  @Override
  public String toString() {
    return "Wellform all Scout classes";
  }
}
