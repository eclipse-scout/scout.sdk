package org.eclipse.scout.sdk.s2i.derived

import com.intellij.openapi.Disposable
import com.intellij.psi.search.SearchScope

interface DerivedResourceManager : Disposable {

    fun trigger(scope: SearchScope)

    fun addDerivedResourceHandlerFactory(factory: DerivedResourceHandlerFactory)

    fun removeDerivedResourceHandlerFactory(factory: DerivedResourceHandlerFactory)

    fun isAutoUpdateDerivedResources(): Boolean
}
