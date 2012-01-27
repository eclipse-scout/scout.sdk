package org.eclipse.scout.sdk.ui.extensions;

import org.eclipse.scout.sdk.ui.view.outline.OutlineCopySourceEvent;

public interface ICopySourceDelegator {

  boolean performCopy(OutlineCopySourceEvent event);
}
