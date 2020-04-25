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
package nls;

import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.security.ScoutSecurityTextProviderService;
import org.eclipse.scout.rt.ui.html.IUiTextContributor;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreSupplierExtension;

public class TestUiTextContributor implements IUiTextContributor {
  @Override
  public void contributeUiTextKeys(Set<String> textKeys) {
    // referenced text service
    textKeys.addAll(BEANS.get(ScoutSecurityTextProviderService.class).getTextMap(null).keySet());

    // ignored by comment: textKeys.addAll(BEANS.get(DoesNotExistTextProviderService.class).getTextMap(null).keySet());

    textKeys.add(TranslationStoreSupplierExtension.TRANSLATION_KEY_1);
    textKeys.add(TranslationStoreSupplierExtension.TRANSLATION_KEY_2);
    textKeys.add("testKey1FromUiContributor");

    textKeys.add("testKey2FromUiContributor");
    textKeys.add("testKey3FromUiContributor");
    textKeys.add("testKey4FromUiContributor");
  }
}
