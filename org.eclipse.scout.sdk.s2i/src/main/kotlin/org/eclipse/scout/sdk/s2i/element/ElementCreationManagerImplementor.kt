/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.element

import org.eclipse.scout.sdk.core.java.JavaTypes
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment
import org.eclipse.scout.sdk.core.s.ISdkConstants
import org.eclipse.scout.sdk.core.s.codetype.CodeTypeNewOperation
import org.eclipse.scout.sdk.core.s.entity.EntityNewOperation
import org.eclipse.scout.sdk.core.s.form.FormNewOperation
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi
import org.eclipse.scout.sdk.core.s.lookupcall.LookupCallNewOperation
import org.eclipse.scout.sdk.core.s.page.PageNewOperation
import org.eclipse.scout.sdk.core.s.util.ScoutTier
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

                op.clientSourceFolder = sourceFolderHelper.sourceFolder(ScoutTier.Client)
                op.sharedSourceFolder = sourceFolderHelper.sourceFolder(ScoutTier.Shared)
                op.serverSourceFolder = sourceFolderHelper.sourceFolder(ScoutTier.Server)

                op.sharedGeneratedSourceFolder = sourceFolderHelper.generatedSourceFolder(ScoutTier.Shared)

                op.clientTestSourceFolder = sourceFolderHelper.testSourceFolder(ScoutTier.Client)
                op.sharedTestSourceFolder = sourceFolderHelper.testSourceFolder(ScoutTier.Shared)
                op.serverTestSourceFolder = sourceFolderHelper.testSourceFolder(ScoutTier.Server)

                op.clientMainTestSourceFolder = sourceFolderHelper.mainTestSourceFolder(ScoutTier.Client)
                op.sharedMainTestSourceFolder = sourceFolderHelper.mainTestSourceFolder(ScoutTier.Shared)
                op.serverMainTestSourceFolder = sourceFolderHelper.mainTestSourceFolder(ScoutTier.Server)
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

                op.clientSourceFolder = sourceFolderHelper.sourceFolder(ScoutTier.Client)
                op.sharedSourceFolder = sourceFolderHelper.sourceFolder(ScoutTier.Shared)
                op.serverSourceFolder = sourceFolderHelper.sourceFolder(ScoutTier.Server)
                op.formDataSourceFolder = sourceFolderHelper.generatedSourceFolder(ScoutTier.Shared) ?: sourceFolderHelper.sourceFolder(ScoutTier.Shared)

                op.clientTestSourceFolder = sourceFolderHelper.testSourceFolder(ScoutTier.Client)
                op.serverTestSourceFolder = sourceFolderHelper.testSourceFolder(ScoutTier.Server)

                sourceFolderHelper.sourceFolder(ScoutTier.Client)?.javaEnvironment()?.api(IScoutApi::class.java)?.ifPresent {
                    op.superType = it.AbstractForm().fqn()
                }
                sourceFolderHelper.sourceFolder(ScoutTier.Server)?.javaEnvironment()?.api(IScoutApi::class.java)?.ifPresent {
                    op.serverSession = it.IServerSession().fqn()
                }

                op.isCreateFormData = sourceFolderHelper.sourceFolder(ScoutTier.Shared) != null
                op.isCreatePermissions = sourceFolderHelper.sourceFolder(ScoutTier.Shared) != null
                op.isCreateOrAppendService = sourceFolderHelper.sourceFolder(ScoutTier.Shared) != null && sourceFolderHelper.sourceFolder(ScoutTier.Server) != null
            })
        })
    }

    private fun initPage() {
        operationMap.put(PageNewOperation::class.java, object : ElementCreationManager.OperationStrategy<PageNewOperation> {
            override var createOperationFunc: (IJavaEnvironment) -> PageNewOperation = {
                PageNewOperation()
            }
            override var prepareOperationFuncList: MutableList<(PageNewOperation, String, String?, SourceFolderHelper) -> Unit> = mutableListOf({ op, elementName, pkg, sourceFolderHelper ->
                val cleanedName = elementName.removeSuffix(ISdkConstants.SUFFIX_PAGE_WITH_NODES).removeSuffix(ISdkConstants.SUFFIX_OUTLINE_PAGE)
                op.pageName = elementNameWithSuffix(cleanedName, ISdkConstants.SUFFIX_PAGE_WITH_TABLE)
                op.`package` = sourceFolderHelper.tier()!!.convert(ScoutTier.Client, pkg)

                op.clientSourceFolder = sourceFolderHelper.sourceFolder(ScoutTier.Client)
                op.sharedSourceFolder = sourceFolderHelper.sourceFolder(ScoutTier.Shared)
                op.serverSourceFolder = sourceFolderHelper.sourceFolder(ScoutTier.Server)
                op.pageDataSourceFolder = sourceFolderHelper.generatedSourceFolder(ScoutTier.Shared) ?: sourceFolderHelper.sourceFolder(ScoutTier.Shared)

                op.testSourceFolder = sourceFolderHelper.testSourceFolder(ScoutTier.Server)

                sourceFolderHelper.sourceFolder(ScoutTier.Client)?.javaEnvironment()?.api(IScoutApi::class.java)?.ifPresent {
                    op.superType = it.AbstractPageWithTable().fqn()
                }
                sourceFolderHelper.sourceFolder(ScoutTier.Server)?.javaEnvironment()?.api(IScoutApi::class.java)?.ifPresent {
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

                op.sharedSourceFolder = sourceFolderHelper.sourceFolder(ScoutTier.Shared)
                op.serverSourceFolder = sourceFolderHelper.sourceFolder(ScoutTier.Server)

                op.testSourceFolder = sourceFolderHelper.testSourceFolder(ScoutTier.Server)

                sourceFolderHelper.sourceFolder(ScoutTier.Shared)?.javaEnvironment()?.api(IScoutApi::class.java)?.ifPresent {
                    op.superType = it.LookupCall().fqn()
                    op.lookupServiceSuperType = it.AbstractLookupService().fqn()
                }
                sourceFolderHelper.sourceFolder(ScoutTier.Server)?.javaEnvironment()?.api(IScoutApi::class.java)?.ifPresent {
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

                op.sharedSourceFolder = sourceFolderHelper.sourceFolder(ScoutTier.Shared)

                sourceFolderHelper.sourceFolder(ScoutTier.Shared)?.javaEnvironment()?.api(IScoutApi::class.java)?.ifPresent {
                    op.superType = it.AbstractCodeType().fqn() + JavaTypes.C_GENERIC_START + idDataType + ", " + idDataType + JavaTypes.C_GENERIC_END
                }
            })
        })
    }
}