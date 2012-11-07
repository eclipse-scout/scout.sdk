package org.eclipse.scout.sdk.compatibility.v35.internal;

import org.eclipse.scout.sdk.compatibility.internal.AbstractCompatibilityActivator;
import org.eclipse.scout.sdk.compatibility.internal.service.IJavadocHoverCompatService;
import org.eclipse.scout.sdk.compatibility.internal.service.IP2CompatService;
import org.eclipse.scout.sdk.compatibility.internal.service.ITargetPlatformCompatService;

public class CompatibilityActivator extends AbstractCompatibilityActivator {
  @Override
  public void start() throws Exception {
    registerService(IJavadocHoverCompatService.class, new JavadocHoverCompatService());
    registerService(ITargetPlatformCompatService.class, new TargetPlatformCompatService());
    registerService(IP2CompatService.class, new P2CompatService());
  }
}
