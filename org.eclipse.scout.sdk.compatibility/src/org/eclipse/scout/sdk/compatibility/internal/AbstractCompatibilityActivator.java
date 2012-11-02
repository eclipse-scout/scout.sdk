package org.eclipse.scout.sdk.compatibility.internal;

public abstract class AbstractCompatibilityActivator implements ICompatibilityActivator {
  protected <T extends Object> void registerService(Class<T> type, T service) {
    ScoutCompatibilityActivator.getDefault().registerService(type, service);
  }
}
