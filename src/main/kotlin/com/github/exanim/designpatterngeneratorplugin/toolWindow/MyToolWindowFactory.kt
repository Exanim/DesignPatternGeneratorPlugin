package com.github.exanim.designpatterngeneratorplugin.toolWindow

import com.github.exanim.designpatterngeneratorplugin.debug.DebugPatternGenerationPanel
import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternGenerator
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentFactory
import javax.swing.JPanel

class MyToolWindowFactory : ToolWindowFactory {
    private val ep = ExtensionPointName.create<PatternGenerator>("com.github.exanim.designpatterngeneratorplugin.patternGenerator")
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val patterns = ep.extensions.toList()
        val mainPanel = JPanel().apply { add(JBLabel("Design pattern generator")) }
        val debugPanel = DebugPatternGenerationPanel(project, patterns)
        val contentFactory = ContentFactory.getInstance()
        val mainContent = contentFactory.createContent(mainPanel, "Generator", false)
        val debugContent = contentFactory.createContent(debugPanel, "Debug", false)
        toolWindow.contentManager.addContent(mainContent)
        toolWindow.contentManager.addContent(debugContent)
    }
}
