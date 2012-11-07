package org.eclipse.scout.sdk.compatibility.internal.service;

import org.eclipse.jdt.core.IJavaElement;

public interface IJavadocHoverCompatService {
	String addImageAndLabel(IJavaElement member, String imageName, String label);
}
