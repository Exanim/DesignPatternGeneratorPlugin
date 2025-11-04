package com.github.exanim.designpatterngeneratorplugin.startup

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.diagnostic.logger

class MyProjectActivity : ProjectActivity {
    private val log = logger<MyProjectActivity>()
    override suspend fun execute(project: Project) {
        log.info("DesignPatternGeneratorPlugin project startup: ${project.name}")
    }
}

