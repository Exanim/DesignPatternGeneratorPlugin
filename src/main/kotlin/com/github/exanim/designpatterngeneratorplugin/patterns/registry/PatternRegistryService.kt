package com.github.exanim.designpatterngeneratorplugin.patterns.registry

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternGenerator
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.extensions.ExtensionPointName

/** Application-level service providing access to registered pattern generators. */
@Service(Service.Level.APP)
class PatternRegistryService {
    private val log = logger<PatternRegistryService>()

    private val epName = ExtensionPointName<PatternGenerator>(
        "com.github.exanim.designpatterngeneratorplugin.patternGenerator"
    )

    fun all(): List<PatternGenerator> = epName.extensionList.also {
        if (it.isEmpty()) log.warn("No PatternGenerators registered yet")
    }

    companion object {
        fun getInstance(): PatternRegistryService = service()
    }
}
