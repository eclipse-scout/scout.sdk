package org.eclipse.scout.sdk.s2i.classid

import com.intellij.psi.*
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes
import org.eclipse.scout.sdk.core.util.FinalValue
import org.eclipse.scout.sdk.core.util.Strings
import org.eclipse.scout.sdk.s2i.environment.IdeaEnvironment

open class ClassIdAnnotation private constructor(val psiClass: PsiClass, val psiAnnotation: PsiAnnotation) {

    private val m_value = FinalValue<String>()

    companion object {

        const val VALUE_ATTRIBUTE_NAME = "value"

        fun of(owner: PsiClass?): ClassIdAnnotation? {
            if (owner == null) {
                return null
            }
            val classIdAnnotation = IdeaEnvironment.computeInReadAction(owner.project) {
                owner.getAnnotation(IScoutRuntimeTypes.ClassId)
            } ?: return null
            return ClassIdAnnotation(owner, classIdAnnotation)
        }
    }

    fun value(): String? = m_value.computeIfAbsentAndGet {
        IdeaEnvironment.computeInReadAction(psiClass.project) {
            psiAnnotation.findAttributeValue(VALUE_ATTRIBUTE_NAME)?.stringValue()
        }
    }

    fun hasValue(): Boolean = Strings.hasText(value())

    protected fun PsiAnnotationMemberValue.stringValue(): String? {
        var computedValue: Any? = null

        if (this is PsiLiteral) {
            computedValue = value
        } else if (this is PsiReference) {
            val target = resolve()
            if (target is PsiVariable) {
                computedValue = target.computeConstantValue()
            }
        }

        return if (computedValue is String) computedValue else null
    }
}
