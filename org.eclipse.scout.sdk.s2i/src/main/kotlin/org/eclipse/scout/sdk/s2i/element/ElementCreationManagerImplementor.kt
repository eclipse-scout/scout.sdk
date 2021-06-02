/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2i.element

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment
import org.eclipse.scout.sdk.core.s.ISdkConstants
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi
import org.eclipse.scout.sdk.core.s.codetype.CodeTypeNewOperation
import org.eclipse.scout.sdk.core.s.entity.EntityNewOperation
import org.eclipse.scout.sdk.core.s.form.FormNewOperation
import org.eclipse.scout.sdk.core.s.lookupcall.LookupCallNewOperation
import org.eclipse.scout.sdk.core.s.page.PageNewOperation
import org.eclipse.scout.sdk.core.s.util.ScoutTier
import org.eclipse.scout.sdk.core.util.JavaTypes
import org.eclipse.scout.sdk.s2i.util.SourceFolderHelper

class ElementCreationManagerImplementor : ElementCreationManager {
    override val operationMap = ElementCreationManager.OperationMap()

    init {
        initEntity()
        initForm()
        initPage()
        initLookupCall()
        initCodeType()
    }

    private fun initEntity() {
        operationMap.put(EntityNewOperation::class.java, object : ElementCreationManager.OperationStrategy<EntityNewOperation> {
            override var createOperationFunc: (IJavaEnvironment) -> EntityNewOperation = {
                EntityNewOperation()
            }
            override var prepareOperationFuncList: MutableList<(EntityNewOperation, String, String?, SourceFolderHelper) -> Unit> = mutableListOf({ op, elementName, pkg, sourceFolderHelper ->
                op.entityName = elementName
                op.clientPackage = sourceFolderHelper.tier()!!.convert(ScoutTier.Client, pkg)

                op.clientSourceFolder = sourceFolderHelper.clientSourceFolder()
                op.sharedSourceFolder = sourceFolderHelper.sharedSourceFolder()
                op.serverSourceFolder = sourceFolderHelper.serverSourceFolder()

                op.sharedGeneratedSourceFolder = sourceFolderHelper.sharedGeneratedSourceFolder()

                op.clientTestSourceFolder = sourceFolderHelper.clientTestSourceFolder()
                op.sharedTestSourceFolder = sourceFolderHelper.sharedTestSourceFolder()
                op.serverTestSourceFolder = sourceFolderHelper.serverTestSourceFolder()

                op.clientMainTestSourceFolder = sourceFolderHelper.clientMainTestSourceFolder()
                op.sharedMainTestSourceFolder = sourceFolderHelper.sharedMainTestSourceFolder()
                op.serverMainTestSourceFolder = sourceFolderHelper.serverMainTestSourceFolder()
            })
        })
    }

    private fun initForm() {
        operationMap.put(FormNewOperation::class.java, object : ElementCreationManager.OperationStrategy<FormNewOperation> {
            override var createOperationFunc: (IJavaEnvironment) -> FormNewOperation = {
                FormNewOperation()
            }
            override var prepareOperationFuncList: MutableList<(FormNewOperation, String, String?, SourceFolderHelper) -> Unit> = mutableListOf({ op, elementName, pkg, sourceFolderHelper ->
                op.formName = elementNameWithSuffix(elementName, ISdkConstants.SUFFIX_FORM)
                op.clientPackage = sourceFolderHelper.tier()!!.convert(ScoutTier.Client, pkg)

                op.clientSourceFolder = sourceFolderHelper.clientSourceFolder()
                op.sharedSourceFolder = sourceFolderHelper.sharedSourceFolder()
                op.serverSourceFolder = sourceFolderHelper.serverSourceFolder()
                op.formDataSourceFolder = sourceFolderHelper.sharedGeneratedSourceFolder() ?: sourceFolderHelper.sharedSourceFolder()

                op.clientTestSourceFolder = sourceFolderHelper.clientTestSourceFolder()
                op.serverTestSourceFolder = sourceFolderHelper.serverTestSourceFolder()

                sourceFolderHelper.clientSourceFolder()?.javaEnvironment()?.api(IScoutApi::class.java)?.ifPresent {
                    op.superType = it.AbstractForm().fqn()
                }
                sourceFolderHelper.serverSourceFolder()?.javaEnvironment()?.api(IScoutApi::class.java)?.ifPresent {
                    op.serverSession = it.IServerSession().fqn()
                }

                op.isCreateFormData = sourceFolderHelper.sharedSourceFolder() != null
                op.isCreatePermissions = sourceFolderHelper.sharedSourceFolder() != null
                op.isCreateOrAppendService = sourceFolderHelper.sharedSourceFolder() != null && sourceFolderHelper.serverSourceFolder() != null
            })
        })
    }

    private fun initPage() {
        operationMap.put(PageNewOperation::class.java, object : ElementCreationManager.OperationStrategy<PageNewOperation> {
            override var createOperationFunc: (IJavaEnvironment) -> PageNewOperation = {
                PageNewOperation()
            }
            override var prepareOperationFuncList: MutableList<(PageNewOperation, String, String?, SourceFolderHelper) -> Unit> = mutableListOf({ op, elementName, pkg, sourceFolderHelper ->
                op.pageName = elementNameWithSuffix(elementName, ISdkConstants.SUFFIX_PAGE_WITH_TABLE)
                op.`package` = sourceFolderHelper.tier()!!.convert(ScoutTier.Client, pkg)

                op.clientSourceFolder = sourceFolderHelper.clientSourceFolder()
                op.sharedSourceFolder = sourceFolderHelper.sharedSourceFolder()
                op.serverSourceFolder = sourceFolderHelper.serverSourceFolder()
                op.pageDataSourceFolder = sourceFolderHelper.sharedGeneratedSourceFolder() ?: sourceFolderHelper.sharedSourceFolder()

                op.testSourceFolder = sourceFolderHelper.serverTestSourceFolder()

                sourceFolderHelper.clientSourceFolder()?.javaEnvironment()?.api(IScoutApi::class.java)?.ifPresent {
                    op.superType = it.AbstractPageWithTable().fqn()
                }
                sourceFolderHelper.serverSourceFolder()?.javaEnvironment()?.api(IScoutApi::class.java)?.ifPresent {
                    op.serverSession = it.IServerSession().fqn()
                }

                op.isCreateAbstractPage = false
            })
        })
    }

    private fun initLookupCall() {
        operationMap.put(LookupCallNewOperation::class.java, object : ElementCreationManager.OperationStrategy<LookupCallNewOperation> {
            override var createOperationFunc: (IJavaEnvironment) -> LookupCallNewOperation = {
                LookupCallNewOperation()
            }
            override var prepareOperationFuncList: MutableList<(LookupCallNewOperation, String, String?, SourceFolderHelper) -> Unit> = mutableListOf({ op, elementName, pkg, sourceFolderHelper ->
                op.lookupCallName = elementNameWithSuffix(elementName, ISdkConstants.SUFFIX_LOOKUP_CALL)
                op.`package` = sourceFolderHelper.tier()!!.convert(ScoutTier.Shared, pkg)
                op.keyType = Long::class.javaObjectType.name

                op.sharedSourceFolder = sourceFolderHelper.sharedSourceFolder()
                op.serverSourceFolder = sourceFolderHelper.serverSourceFolder()

                op.testSourceFolder = sourceFolderHelper.serverTestSourceFolder()

                sourceFolderHelper.sharedSourceFolder()?.javaEnvironment()?.api(IScoutApi::class.java)?.ifPresent {
                    op.superType = it.LookupCall().fqn()
                    op.lookupServiceSuperType = it.AbstractLookupService().fqn()
                }
                sourceFolderHelper.serverSourceFolder()?.javaEnvironment()?.api(IScoutApi::class.java)?.ifPresent {
                    op.serverSession = it.IServerSession().fqn()
                }
            })
        })
    }

    private fun initCodeType() {
        operationMap.put(CodeTypeNewOperation::class.java, object : ElementCreationManager.OperationStrategy<CodeTypeNewOperation> {
            override var createOperationFunc: (IJavaEnvironment) -> CodeTypeNewOperation = {
                CodeTypeNewOperation()
            }
            override var prepareOperationFuncList: MutableList<(CodeTypeNewOperation, String, String?, SourceFolderHelper) -> Unit> = mutableListOf({ op, elementName, pkg, sourceFolderHelper ->
                val idDataType = JavaTypes.Long
                op.codeTypeName = elementNameWithSuffix(elementName, ISdkConstants.SUFFIX_CODE_TYPE)
                op.`package` = sourceFolderHelper.tier()!!.convert(ScoutTier.Shared, pkg)
                op.codeTypeIdDataType = idDataType

                op.sharedSourceFolder = sourceFolderHelper.sharedSourceFolder()

                sourceFolderHelper.sharedSourceFolder()?.javaEnvironment()?.api(IScoutApi::class.java)?.ifPresent {
                    op.superType = it.AbstractCodeType().fqn() + JavaTypes.C_GENERIC_START + idDataType + ", " + idDataType + JavaTypes.C_GENERIC_END
                }
            })
        })
    }
}