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
package org.eclipse.scout.sdk.s2i.template

import com.intellij.psi.PsiClass
import com.intellij.psi.util.InheritanceUtil
import com.intellij.util.containers.ContainerUtil.emptyList
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.s.ScoutModelHierarchy
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message
import java.util.concurrent.ConcurrentHashMap

object Templates {

    const val VALUE_FIELD_TYPE_VARIABLE_NAME = "type"

    private lateinit var m_templateProvider: (PsiClass) -> Map<String, TemplateDescriptor>
    private val m_builtInTemplates = ConcurrentHashMap<String, TemplateDescriptor>()
    private val m_valueFieldTypes = listOf<String>(java.lang.Long::class.java.name, java.lang.String::class.java.name)

    init {
        registerFormFieldTemplate(IScoutRuntimeTypes.IStringField, TemplateDescriptor("templates.StringField")
                .withName(message("template.StringField"))
                .withAliasName(message("template.TextField"))
                .withSuperClassInfo(IScoutRuntimeTypes.IStringField, IScoutRuntimeTypes.AbstractStringField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyString")
                .withVariable("max", "128"))
        registerFormFieldTemplate(IScoutRuntimeTypes.IBigDecimalField, TemplateDescriptor("templates.BigDecimalField")
                .withName(message("template.BigDecimalField"))
                .withAliasNames(message("template.NumberField"), message("template.DoubleField"), message("template.FloatField"))
                .withSuperClassInfo(IScoutRuntimeTypes.IBigDecimalField, IScoutRuntimeTypes.AbstractBigDecimalField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyBigDecimal")
                .withVariable("min", "-999999999999999999")
                .withVariable("max", "999999999999999999"))
        registerFormFieldTemplate(IScoutRuntimeTypes.IBooleanField, TemplateDescriptor("templates.BooleanField")
                .withName(message("template.BooleanField"))
                .withAliasNames(message("template.CheckboxField"), message("template.TristateField"))
                .withSuperClassInfo(IScoutRuntimeTypes.IBooleanField, IScoutRuntimeTypes.AbstractBooleanField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyBoolean"))
        registerFormFieldTemplate(IScoutRuntimeTypes.IButton, TemplateDescriptor("templates.Button")
                .withName(message("template.Button"))
                .withSuperClassInfo(IScoutRuntimeTypes.IButton, IScoutRuntimeTypes.AbstractButton)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My"))
        registerFormFieldTemplate(IScoutRuntimeTypes.ICalendarField, TemplateDescriptor("templates.CalendarField")
                .withName(message("template.CalendarField"))
                .withSuperClassInfo(IScoutRuntimeTypes.ICalendarField, IScoutRuntimeTypes.AbstractCalendarField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyCalendar")
                .withBoolVariable("boolLabelVisible", false))
        registerFormFieldTemplate(IScoutRuntimeTypes.IDateField, TemplateDescriptor("templates.DateField")
                .withName(message("template.DateField"))
                .withAliasNames(message("template.DateTimeField"), message("template.TimeField"))
                .withSuperClassInfo(IScoutRuntimeTypes.IDateField, IScoutRuntimeTypes.AbstractDateField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyDate"))
        registerFormFieldTemplate(IScoutRuntimeTypes.IFileChooserField, TemplateDescriptor("templates.FileChooserField")
                .withName(message("template.FileChooserField"))
                .withSuperClassInfo(IScoutRuntimeTypes.IFileChooserField, IScoutRuntimeTypes.AbstractFileChooserField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyFileChooser"))
        registerFormFieldTemplate(IScoutRuntimeTypes.IGroupBox, TemplateDescriptor("templates.GroupBox")
                .withName(message("template.GroupBox"))
                .withSuperClassInfo(IScoutRuntimeTypes.IGroupBox, IScoutRuntimeTypes.AbstractGroupBox)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyGroup"))
        registerFormFieldTemplate(IScoutRuntimeTypes.IHtmlField, TemplateDescriptor("templates.HtmlField")
                .withName(message("template.HtmlField"))
                .withSuperClassInfo(IScoutRuntimeTypes.IHtmlField, IScoutRuntimeTypes.AbstractHtmlField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyHtml"))
        registerFormFieldTemplate(IScoutRuntimeTypes.ILabelField, TemplateDescriptor("templates.LabelField")
                .withName(message("template.LabelField"))
                .withSuperClassInfo(IScoutRuntimeTypes.ILabelField, IScoutRuntimeTypes.AbstractLabelField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyLabel"))
        registerFormFieldTemplate(IScoutRuntimeTypes.IListBox, TemplateDescriptor("templates.ListBox")
                .withName(message("template.ListBox"))
                .withSuperClassInfo(IScoutRuntimeTypes.IListBox, IScoutRuntimeTypes.AbstractListBox)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyList")
                .withEnumVariable(VALUE_FIELD_TYPE_VARIABLE_NAME, valueFieldTypes(), PsiExpressionEnumMacro.NAME))
        registerFormFieldTemplate(IScoutRuntimeTypes.IProposalField, TemplateDescriptor("templates.ProposalField")
                .withName(message("template.ProposalField"))
                .withSuperClassInfo(IScoutRuntimeTypes.IProposalField, IScoutRuntimeTypes.AbstractProposalField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyProposal")
                .withEnumVariable(VALUE_FIELD_TYPE_VARIABLE_NAME, valueFieldTypes(), PsiExpressionEnumMacro.NAME))
        registerFormFieldTemplate(IScoutRuntimeTypes.ISmartField, TemplateDescriptor("templates.SmartField")
                .withName(message("template.SmartField"))
                .withAliasNames(message("template.ComboBox"))
                .withSuperClassInfo(IScoutRuntimeTypes.ISmartField, IScoutRuntimeTypes.AbstractSmartField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MySmart")
                .withEnumVariable(VALUE_FIELD_TYPE_VARIABLE_NAME, valueFieldTypes(), PsiExpressionEnumMacro.NAME))
        registerFormFieldTemplate(IScoutRuntimeTypes.ILongField, TemplateDescriptor("templates.LongField")
                .withName(message("template.LongField"))
                .withAliasNames(message("template.NumberField"), message("template.IntegerField"))
                .withSuperClassInfo(IScoutRuntimeTypes.ILongField, IScoutRuntimeTypes.AbstractLongField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyLong")
                .withVariable("min", "-999999999999")
                .withVariable("max", "999999999999"))
        registerFormFieldTemplate(IScoutRuntimeTypes.IRadioButtonGroup, TemplateDescriptor("templates.RadioButtonGroup")
                .withName(message("template.RadioButtonGroup"))
                .withSuperClassInfo(IScoutRuntimeTypes.IRadioButtonGroup, IScoutRuntimeTypes.AbstractRadioButtonGroup)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyRadioButton")
                .withEnumVariable(VALUE_FIELD_TYPE_VARIABLE_NAME, valueFieldTypes(), PsiExpressionEnumMacro.NAME))
        registerFormFieldTemplate(IScoutRuntimeTypes.ISequenceBox, TemplateDescriptor("templates.SequenceBox")
                .withName(message("template.SequenceBox"))
                .withSuperClassInfo(IScoutRuntimeTypes.ISequenceBox, IScoutRuntimeTypes.AbstractSequenceBox)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MySequence")
                .withBoolVariable("boolAutoCheckFromTo", false))
        registerFormFieldTemplate(IScoutRuntimeTypes.ITabBox, TemplateDescriptor("templates.TabBox")
                .withName(message("template.TabBox"))
                .withSuperClassInfo(IScoutRuntimeTypes.ITabBox, IScoutRuntimeTypes.AbstractTabBox)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyTab"))
        registerFormFieldTemplate(IScoutRuntimeTypes.ITableField, TemplateDescriptor("templates.TableField")
                .withName(message("template.TableField"))
                .withSuperClassInfo(IScoutRuntimeTypes.ITableField, IScoutRuntimeTypes.AbstractTableField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyTable"))
        registerFormFieldTemplate(IScoutRuntimeTypes.ITreeField, TemplateDescriptor("templates.TreeField")
                .withName(message("template.TreeField"))
                .withSuperClassInfo(IScoutRuntimeTypes.ITreeField, IScoutRuntimeTypes.AbstractTreeField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyTree"))
        registerFormFieldTemplate(IScoutRuntimeTypes.IRadioButton, TemplateDescriptor("templates.RadioButton")
                .withName(message("template.RadioButton"))
                .withSuperClassInfo(IScoutRuntimeTypes.IRadioButton, IScoutRuntimeTypes.AbstractRadioButton)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyRadio"))
        registerFormFieldTemplate(IScoutRuntimeTypes.IImageField, TemplateDescriptor("templates.ImageField")
                .withName(message("template.ImageField"))
                .withSuperClassInfo(IScoutRuntimeTypes.IImageField, IScoutRuntimeTypes.AbstractImageField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyImage")
                .withVariable("width", "2")
                .withBoolVariable("boolAutoFit", true)
                .withBoolVariable("boolLabelVisible", false))
        registerFormFieldTemplate(IScoutRuntimeTypes.IFileChooserButton, TemplateDescriptor("templates.FileChooserButton")
                .withName(message("template.FileChooserButton"))
                .withSuperClassInfo(IScoutRuntimeTypes.IFileChooserButton, IScoutRuntimeTypes.AbstractFileChooserButton)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyFileChooser"))
        registerFormFieldTemplate(IScoutRuntimeTypes.ITagField, TemplateDescriptor("templates.TagField")
                .withName(message("template.TagField"))
                .withSuperClassInfo(IScoutRuntimeTypes.ITagField, IScoutRuntimeTypes.AbstractTagField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyTag"))
        registerFormFieldTemplate(IScoutRuntimeTypes.IModeSelectorField, TemplateDescriptor("templates.ModeSelectorField")
                .withName(message("template.ModeSelectorField"))
                .withSuperClassInfo(IScoutRuntimeTypes.IModeSelectorField, IScoutRuntimeTypes.AbstractModeSelectorField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyModeSelector")
                .withVariable("width", "2")
                .withEnumVariable(VALUE_FIELD_TYPE_VARIABLE_NAME, valueFieldTypes(), PsiExpressionEnumMacro.NAME))
        registerFormFieldTemplate(IScoutRuntimeTypes.IBrowserField, TemplateDescriptor("templates.BrowserField")
                .withName(message("template.BrowserField"))
                .withSuperClassInfo(IScoutRuntimeTypes.IBrowserField, IScoutRuntimeTypes.AbstractBrowserField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyBrowser")
                .withVariable("width", "2")
                .withVariable("height", "6")
                .withBoolVariable("boolLabelVisible", false)
                .withBoolVariable("boolScrollBarEnabled", true))
        registerFormFieldTemplate(IScoutRuntimeTypes.ITileField, TemplateDescriptor("templates.TileField")
                .withName(message("template.TileField"))
                .withSuperClassInfo(IScoutRuntimeTypes.ITileField, IScoutRuntimeTypes.AbstractTileField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyTile"))
        registerFormFieldTemplate(IScoutRuntimeTypes.IAccordionField, TemplateDescriptor("templates.AccordionField")
                .withName(message("template.AccordionField"))
                .withSuperClassInfo(IScoutRuntimeTypes.IAccordionField, IScoutRuntimeTypes.AbstractAccordionField)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyAccordion")
                .withBoolVariable("boolLabelVisible", false))

        registerTemplate(IScoutRuntimeTypes.IMenu, TemplateDescriptor("templates.Menu")
                .withName(message("template.Menu"))
                .withOrderDefinitionType(IScoutRuntimeTypes.IMenu)
                .withSuperClassInfo(IScoutRuntimeTypes.IMenu, IScoutRuntimeTypes.AbstractMenu)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My")
                .withNlsVariable("nls", "MyMenuName"))
        registerTemplate(IScoutRuntimeTypes.IKeyStroke, TemplateDescriptor("templates.KeyStroke")
                .withName(message("template.KeyStroke"))
                .withOrderDefinitionType(IScoutRuntimeTypes.IKeyStroke)
                .withSuperClassInfo(IScoutRuntimeTypes.IKeyStroke, IScoutRuntimeTypes.AbstractKeyStroke)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My"))
        registerTemplate(IScoutRuntimeTypes.ICode, TemplateDescriptor("templates.Code")
                .withName(message("template.Code"))
                .withOrderDefinitionType(IScoutRuntimeTypes.ICode)
                .withSuperClassInfo(IScoutRuntimeTypes.ICode, IScoutRuntimeTypes.AbstractCode)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My")
                .withNlsVariable("nls", "MyCodeName"))
        registerTemplate(IScoutRuntimeTypes.IFormHandler, TemplateDescriptor("templates.FormHandler")
                .withName(message("template.FormHandler"))
                .withSuperClassInfo(IScoutRuntimeTypes.IFormHandler, IScoutRuntimeTypes.AbstractFormHandler)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My"))
        registerTemplate(IScoutRuntimeTypes.IColumn, TemplateDescriptor("templates.Column")
                .withName(message("template.Column"))
                .withInnerTypeGetterInfo(IScoutRuntimeTypes.ITable, "getColumnSet().getColumnByClass", TemplateDescriptor.InnerTypeGetterLookupType.CLOSEST)
                .withOrderDefinitionType(IScoutRuntimeTypes.IColumn)
                .withSuperClassInfo(IScoutRuntimeTypes.IColumn, IScoutRuntimeTypes.AbstractStringColumn)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My")
                .withNlsVariable("nls", "MyColumnName")
                .withVariable("width", "100"))
        registerTemplate(IScoutRuntimeTypes.IExtension, TemplateDescriptor("templates.Extension")
                .withName(message("template.Extension"))
                .withSuperClassInfo(IScoutRuntimeTypes.IExtension, IScoutRuntimeTypes.AbstractStringColumn)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My"))
        registerTemplate(IScoutRuntimeTypes.ITile, TemplateDescriptor("templates.Tile")
                .withName(message("template.Tile"))
                .withOrderDefinitionType(IScoutRuntimeTypes.ITile)
                .withSuperClassInfo(IScoutRuntimeTypes.ITile, IScoutRuntimeTypes.AbstractTile)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My"))
        registerTemplate(IScoutRuntimeTypes.IGroup, TemplateDescriptor("templates.Group")
                .withName(message("template.Group"))
                .withOrderDefinitionType(IScoutRuntimeTypes.IGroup)
                .withSuperClassInfo(IScoutRuntimeTypes.IGroup, IScoutRuntimeTypes.AbstractGroup)
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My"))

        registerTemplateProvider { m_builtInTemplates }
    }

    private fun registerFormFieldTemplate(definitionInterface: String, descriptor: TemplateDescriptor) =
            registerTemplate(definitionInterface, descriptor
                    .withInnerTypeGetterInfo(IScoutRuntimeTypes.IForm, "getFieldByClass", TemplateDescriptor.InnerTypeGetterLookupType.CLOSEST)
                    .withInnerTypeGetterInfo(IScoutRuntimeTypes.ICompositeField, "getFieldByClass", TemplateDescriptor.InnerTypeGetterLookupType.FARTHEST)
                    .withInnerTypeGetterInfo(IScoutRuntimeTypes.IFormExtension, "getOwner().getFieldByClass", TemplateDescriptor.InnerTypeGetterLookupType.CLOSEST)
                    .withInnerTypeGetterInfo(IScoutRuntimeTypes.ICompositeFieldExtension, "getOwner().getFieldByClass", TemplateDescriptor.InnerTypeGetterLookupType.FARTHEST)
                    .withOrderDefinitionType(IScoutRuntimeTypes.IFormField)
                    .withNlsVariable("nls", "MyNlsKey"))

    private fun registerTemplate(definitionInterface: String, descriptor: TemplateDescriptor) = m_builtInTemplates.put(definitionInterface, descriptor)

    private fun isTemplateValidFor(definitionInterface: String, possibleChildrenIfcFqn: Set<String>) = possibleChildrenIfcFqn.any { ScoutModelHierarchy.isSubtypeOf(definitionInterface, it) }

    private fun valueFieldTypes() = m_valueFieldTypes

    /**
     * Replace the template provider with a custom instance. The provider will be used by [templatesFor].
     * @param newProvider the new provider
     * @see [templateProvider]
     */
    fun registerTemplateProvider(newProvider: (PsiClass) -> Map<String, TemplateDescriptor>) = apply { m_templateProvider = newProvider }

    /**
     * @return The active template provider.
     * The [Map] returned by a provider contains the fully qualified class name defining the template as key (e.g. ICode for a template creating Scout Codes).
     * The value contains the [TemplateDescriptor] used to create the lookup element and to create the source if selected.
     */
    fun templateProvider() = m_templateProvider

    /**
     * Invokes the active [templateProvider] and returns all [TemplateDescriptor] instances matching the given [declaringClass].
     * @param declaringClass The [PsiClass] in which the templates will be created.
     * @return All templates from the [templateProvider] that may be created within the [declaringClass].
     */
    fun templatesFor(declaringClass: PsiClass): List<TemplateDescriptor> {
        val allSuperTypes = InheritanceUtil.getSuperClasses(declaringClass).map { it.qualifiedName }
        val possibleChildrenIfcFqn: Set<String> = ScoutModelHierarchy.getPossibleChildren(allSuperTypes)
        if (possibleChildrenIfcFqn.isEmpty()) return emptyList()
        return templateProvider().invoke(declaringClass)
                .filter { isTemplateValidFor(it.key, possibleChildrenIfcFqn) }
                .map { it.value }
    }
}