package com.github.exanim.designpatterngeneratorplugin.patterns.impl

import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.logger

/**
 * Temporary activity to programmatically register built-in pattern generators until we
 * possibly move to XML EP registration (keeps Kotlin-only configuration for now).
 */
class PatternGeneratorsStartupActivity : ProjectActivity {
    private val log = logger<PatternGeneratorsStartupActivity>()
    override suspend fun execute(project: Project) {
        // This approach may not be ideal; extension point supports XML. For initial bootstrap we rely on XML EP + dynamic programmatic registration if needed.
        log.info("Pattern generators startup executed for project: ${'$'}{project.name}")
    }
}

