package com.github.exanim.designpatterngeneratorplugin.patterns.registry

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternGenerator
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

/** Application-level service providing access to registered pattern generators. */
@Service(Service.Level.APP)
class PatternRegistryService {
    private val log = logger<PatternRegistryService>()

    private val epName = ExtensionPointName<PatternGenerator>("com.github.exanim.designpatterngeneratorplugin.patternGenerator")

    fun all(): List<PatternGenerator> = epName.extensionList.also {
        if (it.isEmpty()) log.warn("No PatternGenerators registered yet")
    }

    fun find(id: String): PatternGenerator? = all().find { it.id == id }

    companion object {
        fun getInstance(): PatternRegistryService = service()
    }
}

/** Simple validation utilities. */
object PatternValidation {
    fun validateMandatory(parameters: List<PatternGenerator>, values: Map<String, Any?>): List<String> = buildList {
        parameters.flatMap { it.parameters }.forEach { p ->
            if (p.required && (values[p.id] == null || (values[p.id] is String && (values[p.id] as String).isBlank()))) {
                add(p.id)
            }
        }
    }
}
