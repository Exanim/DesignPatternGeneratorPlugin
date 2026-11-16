package com.github.exanim.designpatterngeneratorplugin.patterns.api

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.openapi.editor.Editor

/**
 * SPI for contributing design pattern code generators.
 * Registered via the `patternGenerator` extension point in plugin.xml.
 * Implementations should be stateless and thread-safe.
 */
interface PatternGenerator {
    val id: String
    val nameKey: String
    val descriptionKey: String
    val parameters: List<PatternParameter>

    fun generate(project: Project, targetDirectory: PsiDirectory, editor: Editor?, values: Map<String, Any>)
}

data class PatternParameter(
    val id: String,
    val labelKey: String,
    val type: ParameterType,
    val required: Boolean = true,
    val defaultValue: Any? = null
)

enum class ParameterType { STRING, BOOLEAN, INTEGER }
