/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2i.template.java

import com.intellij.psi.PsiClass
import com.intellij.psi.util.InheritanceUtil
import com.intellij.util.containers.ContainerUtil.emptyList
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi
import org.eclipse.scout.sdk.core.s.java.apidef.ScoutModelHierarchy
import org.eclipse.scout.sdk.s2i.EclipseScoutBundle.message

object Templates {

    const val VALUE_FIELD_TYPE_VARIABLE_NAME = "type"

    private lateinit var m_templateProvider: (PsiClass, ScoutModelHierarchy) -> Map<String, TemplateDescriptor>
    private val m_valueFieldTypes = listOf<String>(java.lang.Long::class.java.name, java.lang.String::class.java.name)

    private fun builtInTemplates(hierarchy: ScoutModelHierarchy): Map<String, TemplateDescriptor> {
        val api = hierarchy.api()
        val builtInTemplates = HashMap<String, TemplateDescriptor>()

        builtInTemplates[api.IStringField().fqn()] = TemplateDescriptor("templates.StringField")
                .withName(message("template.StringField"))
                .withAliasName(message("template.TextField"))
                .withSuperClassInfo(api.IStringField().fqn(), api.AbstractStringField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyString")
                .withVariable("max", "128")
                .asFormFieldTemplate(api)
        builtInTemplates[api.IBigDecimalField().fqn()] = TemplateDescriptor("templates.BigDecimalField")
                .withName(message("template.BigDecimalField"))
                .withAliasNames(message("template.NumberField"), message("template.DoubleField"), message("template.FloatField"))
                .withSuperClassInfo(api.IBigDecimalField().fqn(), api.AbstractBigDecimalField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyBigDecimal")
                .withVariable("min", "-999999999999999999")
                .withVariable("max", "999999999999999999")
                .asFormFieldTemplate(api)
        builtInTemplates[api.IBooleanField().fqn()] = TemplateDescriptor("templates.BooleanField")
                .withName(message("template.BooleanField"))
                .withAliasNames(message("template.CheckboxField"), message("template.TristateField"))
                .withSuperClassInfo(api.IBooleanField().fqn(), api.AbstractBooleanField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyBoolean")
                .asFormFieldTemplate(api)
        builtInTemplates[api.IButton().fqn()] = TemplateDescriptor("templates.Button")
                .withName(message("template.Button"))
                .withSuperClassInfo(api.IButton().fqn(), api.AbstractButton().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My")
                .asFormFieldTemplate(api)
        builtInTemplates[api.ICalendarField().fqn()] = TemplateDescriptor("templates.CalendarField")
                .withName(message("template.CalendarField"))
                .withSuperClassInfo(api.ICalendarField().fqn(), api.AbstractCalendarField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyCalendar")
                .withBoolVariable("boolLabelVisible", false)
                .asFormFieldTemplate(api)
        builtInTemplates[api.IDateField().fqn()] = TemplateDescriptor("templates.DateField")
                .withName(message("template.DateField"))
                .withAliasNames(message("template.DateTimeField"), message("template.TimeField"))
                .withSuperClassInfo(api.IDateField().fqn(), api.AbstractDateField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyDate")
                .asFormFieldTemplate(api)
        builtInTemplates[api.IFileChooserField().fqn()] = TemplateDescriptor("templates.FileChooserField")
                .withName(message("template.FileChooserField"))
                .withSuperClassInfo(api.IFileChooserField().fqn(), api.AbstractFileChooserField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyFileChooser")
                .asFormFieldTemplate(api)
        builtInTemplates[api.IGroupBox().fqn()] = TemplateDescriptor("templates.GroupBox")
                .withName(message("template.GroupBox"))
                .withSuperClassInfo(api.IGroupBox().fqn(), api.AbstractGroupBox().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyGroup")
                .asFormFieldTemplate(api)
        builtInTemplates[api.IHtmlField().fqn()] = TemplateDescriptor("templates.HtmlField")
                .withName(message("template.HtmlField"))
                .withSuperClassInfo(api.IHtmlField().fqn(), api.AbstractHtmlField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyHtml")
                .asFormFieldTemplate(api)
        builtInTemplates[api.ILabelField().fqn()] = TemplateDescriptor("templates.LabelField")
                .withName(message("template.LabelField"))
                .withSuperClassInfo(api.ILabelField().fqn(), api.AbstractLabelField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyLabel")
                .asFormFieldTemplate(api)
        builtInTemplates[api.IListBox().fqn()] = TemplateDescriptor("templates.ListBox")
                .withName(message("template.ListBox"))
                .withSuperClassInfo(api.IListBox().fqn(), api.AbstractListBox().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyList")
                .withEnumVariable(VALUE_FIELD_TYPE_VARIABLE_NAME, valueFieldTypes(), PsiExpressionEnumMacro.NAME)
                .asFormFieldTemplate(api)
        builtInTemplates[api.IProposalField().fqn()] = TemplateDescriptor("templates.ProposalField")
                .withName(message("template.ProposalField"))
                .withSuperClassInfo(api.IProposalField().fqn(), api.AbstractProposalField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyProposal")
                .withEnumVariable(VALUE_FIELD_TYPE_VARIABLE_NAME, valueFieldTypes(), PsiExpressionEnumMacro.NAME)
                .asFormFieldTemplate(api)
        builtInTemplates[api.ISmartField().fqn()] = TemplateDescriptor("templates.SmartField")
                .withName(message("template.SmartField"))
                .withAliasNames(message("template.ComboBox"))
                .withSuperClassInfo(api.ISmartField().fqn(), api.AbstractSmartField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MySmart")
                .withEnumVariable(VALUE_FIELD_TYPE_VARIABLE_NAME, valueFieldTypes(), PsiExpressionEnumMacro.NAME)
                .asFormFieldTemplate(api)
        builtInTemplates[api.ILongField().fqn()] = TemplateDescriptor("templates.LongField")
                .withName(message("template.LongField"))
                .withAliasNames(message("template.NumberField"), message("template.IntegerField"))
                .withSuperClassInfo(api.ILongField().fqn(), api.AbstractLongField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyLong")
                .withVariable("min", "-999999999999")
                .withVariable("max", "999999999999")
                .asFormFieldTemplate(api)
        builtInTemplates[api.IRadioButtonGroup().fqn()] = TemplateDescriptor("templates.RadioButtonGroup")
                .withName(message("template.RadioButtonGroup"))
                .withSuperClassInfo(api.IRadioButtonGroup().fqn(), api.AbstractRadioButtonGroup().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyRadioButton")
                .withEnumVariable(VALUE_FIELD_TYPE_VARIABLE_NAME, valueFieldTypes(), PsiExpressionEnumMacro.NAME)
                .asFormFieldTemplate(api)
        builtInTemplates[api.ISequenceBox().fqn()] = TemplateDescriptor("templates.SequenceBox")
                .withName(message("template.SequenceBox"))
                .withSuperClassInfo(api.ISequenceBox().fqn(), api.AbstractSequenceBox().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MySequence")
                .withBoolVariable("boolAutoCheckFromTo", false)
                .asFormFieldTemplate(api)
        builtInTemplates[api.ITabBox().fqn()] = TemplateDescriptor("templates.TabBox")
                .withName(message("template.TabBox"))
                .withSuperClassInfo(api.ITabBox().fqn(), api.AbstractTabBox().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyTab")
                .asFormFieldTemplate(api)
        builtInTemplates[api.ITableField().fqn()] = TemplateDescriptor("templates.TableField")
                .withName(message("template.TableField"))
                .withSuperClassInfo(api.ITableField().fqn(), api.AbstractTableField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyTable")
                .asFormFieldTemplate(api)
        builtInTemplates[api.ITreeField().fqn()] = TemplateDescriptor("templates.TreeField")
                .withName(message("template.TreeField"))
                .withSuperClassInfo(api.ITreeField().fqn(), api.AbstractTreeField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyTree")
                .asFormFieldTemplate(api)
        builtInTemplates[api.IRadioButton().fqn()] = TemplateDescriptor("templates.RadioButton")
                .withName(message("template.RadioButton"))
                .withSuperClassInfo(api.IRadioButton().fqn(), api.AbstractRadioButton().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyRadio")
                .asFormFieldTemplate(api)
        builtInTemplates[api.IImageField().fqn()] = TemplateDescriptor("templates.ImageField")
                .withName(message("template.ImageField"))
                .withSuperClassInfo(api.IImageField().fqn(), api.AbstractImageField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyImage")
                .withVariable("width", "2")
                .withBoolVariable("boolAutoFit", true)
                .withBoolVariable("boolLabelVisible", false)
                .asFormFieldTemplate(api)
        builtInTemplates[api.IFileChooserButton().fqn()] = TemplateDescriptor("templates.FileChooserButton")
                .withName(message("template.FileChooserButton"))
                .withSuperClassInfo(api.IFileChooserButton().fqn(), api.AbstractFileChooserButton().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyFileChooser")
                .asFormFieldTemplate(api)
        builtInTemplates[api.ITagField().fqn()] = TemplateDescriptor("templates.TagField")
                .withName(message("template.TagField"))
                .withSuperClassInfo(api.ITagField().fqn(), api.AbstractTagField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyTag")
                .asFormFieldTemplate(api)
        builtInTemplates[api.IModeSelectorField().fqn()] = TemplateDescriptor("templates.ModeSelectorField")
                .withName(message("template.ModeSelectorField"))
                .withSuperClassInfo(api.IModeSelectorField().fqn(), api.AbstractModeSelectorField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyModeSelector")
                .withVariable("width", "2")
                .withEnumVariable(VALUE_FIELD_TYPE_VARIABLE_NAME, valueFieldTypes(), PsiExpressionEnumMacro.NAME)
                .asFormFieldTemplate(api)
        builtInTemplates[api.IBrowserField().fqn()] = TemplateDescriptor("templates.BrowserField")
                .withName(message("template.BrowserField"))
                .withSuperClassInfo(api.IBrowserField().fqn(), api.AbstractBrowserField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyBrowser")
                .withVariable("width", "2")
                .withVariable("height", "6")
                .withBoolVariable("boolLabelVisible", false)
                .withBoolVariable("boolScrollBarEnabled", true)
                .asFormFieldTemplate(api)
        builtInTemplates[api.ITileField().fqn()] = TemplateDescriptor("templates.TileField")
                .withName(message("template.TileField"))
                .withSuperClassInfo(api.ITileField().fqn(), api.AbstractTileField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyTile")
                .asFormFieldTemplate(api)
        builtInTemplates[api.IAccordionField().fqn()] = TemplateDescriptor("templates.AccordionField")
                .withName(message("template.AccordionField"))
                .withSuperClassInfo(api.IAccordionField().fqn(), api.AbstractAccordionField().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "MyAccordion")
                .withBoolVariable("boolLabelVisible", false)
                .asFormFieldTemplate(api)

        builtInTemplates[api.IMenu().fqn()] = TemplateDescriptor("templates.Menu")
                .withName(message("template.Menu"))
                .withOrderDefinitionType(api.IMenu().fqn())
                .withSuperClassInfo(api.IMenu().fqn(), api.AbstractMenu().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My")
                .withNlsVariable("nls", "MyMenuName")
        builtInTemplates[api.IKeyStroke().fqn()] = TemplateDescriptor("templates.KeyStroke")
                .withName(message("template.KeyStroke"))
                .withOrderDefinitionType(api.IKeyStroke().fqn())
                .withSuperClassInfo(api.IKeyStroke().fqn(), api.AbstractKeyStroke().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My")
        builtInTemplates[api.ICode().fqn()] = TemplateDescriptor("templates.Code")
                .withName(message("template.Code"))
                .withOrderDefinitionType(api.ICode().fqn())
                .withSuperClassInfo(api.ICode().fqn(), api.AbstractCode().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My")
                .withNlsVariable("nls", "MyCodeName")
        builtInTemplates[api.IFormHandler().fqn()] = TemplateDescriptor("templates.FormHandler")
                .withName(message("template.FormHandler"))
                .withSuperClassInfo(api.IFormHandler().fqn(), api.AbstractFormHandler().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My")
        builtInTemplates[api.IColumn().fqn()] = TemplateDescriptor("templates.Column")
                .withName(message("template.Column"))
                .withInnerTypeGetterInfo(api.ITable().fqn(), api.ITable().columnSetMethodName + "()." + api.ColumnSet().columnByClassMethodName, TemplateDescriptor.InnerTypeGetterLookupType.CLOSEST)
                .withOrderDefinitionType(api.IColumn().fqn())
                .withSuperClassInfo(api.IColumn().fqn(), api.AbstractStringColumn().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My")
                .withNlsVariable("nls", "MyColumnName")
                .withVariable("width", "100")
        builtInTemplates[api.IExtension().fqn()] = TemplateDescriptor("templates.Extension")
                .withName(message("template.Extension"))
                .withSuperClassInfo(api.IExtension().fqn(), api.AbstractStringColumn().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My")
        builtInTemplates[api.ITile().fqn()] = TemplateDescriptor("templates.Tile")
                .withName(message("template.Tile"))
                .withOrderDefinitionType(api.ITile().fqn())
                .withSuperClassInfo(api.ITile().fqn(), api.AbstractTile().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My")
        builtInTemplates[api.IGroup().fqn()] = TemplateDescriptor("templates.Group")
                .withName(message("template.Group"))
                .withOrderDefinitionType(api.IGroup().fqn())
                .withSuperClassInfo(api.IGroup().fqn(), api.AbstractGroup().fqn())
                .withVariable(TemplateDescriptor.VARIABLE_NAME, "My")
        return builtInTemplates
    }

    init {
        registerTemplateProvider { _, scoutModelHierarchy -> builtInTemplates(scoutModelHierarchy) }
    }

    private fun TemplateDescriptor.asFormFieldTemplate(api: IScoutApi): TemplateDescriptor {
        val formGetFieldByClass = api.IForm().fieldByClassMethodName
        val compositeFieldByClass = api.ICompositeField().fieldByClassMethodName
        val getOwnerMethodName = api.IExtension().ownerMethodName
        withInnerTypeGetterInfo(api.IForm().fqn(), formGetFieldByClass, TemplateDescriptor.InnerTypeGetterLookupType.CLOSEST)
                .withInnerTypeGetterInfo(api.ICompositeField().fqn(), compositeFieldByClass, TemplateDescriptor.InnerTypeGetterLookupType.FARTHEST)
                .withInnerTypeGetterInfo(api.IFormExtension().fqn(), "$getOwnerMethodName().$formGetFieldByClass", TemplateDescriptor.InnerTypeGetterLookupType.CLOSEST)
                .withInnerTypeGetterInfo(api.ICompositeFieldExtension().fqn(), "$getOwnerMethodName().$compositeFieldByClass", TemplateDescriptor.InnerTypeGetterLookupType.FARTHEST)
                .withOrderDefinitionType(api.IFormField().fqn())
                .withNlsVariable("nls", "MyNlsKey")
        return this
    }


    private fun ScoutModelHierarchy.isTemplateValidFor(definitionInterface: String, possibleChildrenIfcFqn: Set<String>) = possibleChildrenIfcFqn.any { isSubtypeOf(definitionInterface, it) }

    private fun valueFieldTypes() = m_valueFieldTypes

    /**
     * Replace the template provider with a custom instance. The provider will be used by [templatesFor].
     * @param newProvider the new provider
     * @see [templateProvider]
     */
    fun registerTemplateProvider(newProvider: (PsiClass, ScoutModelHierarchy) -> Map<String, TemplateDescriptor>) = apply { m_templateProvider = newProvider }

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
    fun templatesFor(declaringClass: PsiClass, scoutHierarchy: ScoutModelHierarchy): List<TemplateDescriptor> {
        val allSuperTypes = InheritanceUtil.getSuperClasses(declaringClass).map { it.qualifiedName }
        val possibleChildrenIfcFqn: Set<String> = scoutHierarchy.possibleChildrenFor(allSuperTypes)
        if (possibleChildrenIfcFqn.isEmpty()) return emptyList()
        return templateProvider().invoke(declaringClass, scoutHierarchy)
                .filter { scoutHierarchy.isTemplateValidFor(it.key, possibleChildrenIfcFqn) }
                .map { it.value }
    }
}