package org.eclipse.scout.sdk.s2i.derived

import com.intellij.psi.search.GlobalSearchScope

interface DerivedResourceManager {

    fun trigger(scope: GlobalSearchScope)

    fun addDerivedResourceHandlerFactory(factory: DerivedResourceHandlerFactory)

    fun removeDerivedResourceHandlerFactory(factory: DerivedResourceHandlerFactory)

    fun isAutoUpdateDerivedResources(): Boolean
}