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
    /** Stable unique id (e.g., "singleton") */
    val id: String
    /** Message bundle key for localized display name */
    val nameKey: String
    /** Message bundle key for localized description */
    val descriptionKey: String
    /** Ordered list of parameter descriptors defining the generator's inputs */
    val parameters: List<PatternParameter>

    /**
     * Generate the pattern code into the given target directory.
     * @param project current project
     * @param targetDirectory directory where code should be created
     * @param editor optional editor for caret-based insertion
     * @param values resolved parameter values keyed by parameter id
     */
    fun generate(project: Project, targetDirectory: PsiDirectory, editor: Editor?, values: Map<String, Any>)
}

/** Descriptor for a single user-configurable parameter. */
data class PatternParameter(
    val id: String,
    val labelKey: String,
    val type: ParameterType,
    val required: Boolean = true,
    val defaultValue: Any? = null
)

enum class ParameterType { STRING, BOOLEAN, INTEGER }
