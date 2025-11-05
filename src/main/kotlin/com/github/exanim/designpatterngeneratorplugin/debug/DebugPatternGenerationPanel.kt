package com.github.exanim.designpatterngeneratorplugin.debug

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternGenerator
import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.github.exanim.designpatterngeneratorplugin.patterns.base.BaseJavaPatternGenerator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiManager
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.*

class DebugPatternGenerationPanel(private val project: Project, private val generators: List<PatternGenerator>) : JPanel(BorderLayout()) {
    private val outputArea = JTextArea().apply { isEditable = false; border = JBUI.Borders.empty(4) }
    private val generateButton = JButton("Generate All Patterns (Debug)").apply { addActionListener { generateAll() } }

    init {
        border = JBUI.Borders.empty(8)
        add(JLabel("Debug: Generate all patterns with default/role-based names"), BorderLayout.NORTH)
        add(JScrollPane(outputArea), BorderLayout.CENTER)
        add(generateButton, BorderLayout.SOUTH)
    }

    private fun generateAll() {
        val baseDirVFile = project.baseDir ?: run {
            Messages.showErrorDialog(project, "No project base directory.", "Debug Generation")
            return
        }
        val psiBase = PsiManager.getInstance(project).findDirectory(baseDirVFile) ?: run {
            Messages.showErrorDialog(project, "Cannot resolve PSI directory.", "Debug Generation")
            return
        }
        val report = StringBuilder()
        generators.forEach { gen ->
            try {
                val values = buildDefaultValues(gen)
                val preview = (gen as? BaseJavaPatternGenerator)?.preview(values) ?: ""
                val fileNames = preview.lines().filter { it.startsWith("// ") }.map { it.removePrefix("// ").trim() }
                (gen as? BaseJavaPatternGenerator)?.generate(project, psiBase, null, values)
                report.append("Pattern: ${gen.id} -> ${fileNames.size} files\n")
                fileNames.forEach { fn -> report.append("  - $fn [pkg=${values["packageName"]}]\n") }
            } catch (ex: Exception) {
                report.append("Pattern: ${gen.id} FAILED: ${ex.message}\n")
            }
        }
        outputArea.text = report.toString()
    }

    private fun buildDefaultValues(gen: PatternGenerator): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        val basePkg = "debug.${gen.id.lowercase()}"
        gen.parameters.forEach { p ->
            val v: Any = when (p.id) {
                "packageName" -> basePkg
                "className" -> "${cap(gen.id)}Singleton"
                "interfaceName" -> "${cap(gen.id)}Target"
                "adapteeClassName" -> "Legacy${cap(gen.id)}Service"
                "adapterClassName" -> "${cap(gen.id)}Adapter"
                "clientClassName" -> "${cap(gen.id)}Client"
                "strategyInterfaceName" -> "${cap(gen.id)}Strategy"
                "concreteStrategyName" -> "Primary${cap(gen.id)}Strategy"
                "secondConcreteStrategyName" -> "Secondary${cap(gen.id)}Strategy"
                "productClassName" -> "${cap(gen.id)}Product"
                "builderInterfaceName" -> "${cap(gen.id)}Builder"
                "firstConcreteBuilderName" -> "Primary${cap(gen.id)}Builder"
                "secondConcreteBuilderName" -> "Secondary${cap(gen.id)}Builder"
                "thirdConcreteBuilderName" -> "Tertiary${cap(gen.id)}Builder"
                "productInterfaceName" -> "${cap(gen.id)}Product"
                "firstConcreteProductName" -> "Primary${cap(gen.id)}Product"
                "secondConcreteProductName" -> "Secondary${cap(gen.id)}Product"
                "thirdConcreteProductName" -> "Tertiary${cap(gen.id)}Product"
                "creatorAbstractName" -> "${cap(gen.id)}Creator"
                "firstConcreteCreatorName" -> "Primary${cap(gen.id)}Creator"
                "secondConcreteCreatorName" -> "Secondary${cap(gen.id)}Creator"
                "componentInterfaceName" -> "${cap(gen.id)}Component"
                "concreteComponentName" -> "Base${cap(gen.id)}Component"
                "firstConcreteDecoratorName" -> "Logging${cap(gen.id)}Decorator"
                "secondConcreteDecoratorName" -> "Metrics${cap(gen.id)}Decorator"
                "handlerAbstractName" -> "${cap(gen.id)}Handler"
                "firstConcreteHandlerName" -> "Primary${cap(gen.id)}Handler"
                "secondConcreteHandlerName" -> "Secondary${cap(gen.id)}Handler"
                "thirdConcreteHandlerName" -> "Tertiary${cap(gen.id)}Handler"
                "subjectClassName" -> "${cap(gen.id)}Subject"
                "firstConcreteObserverName" -> "Primary${cap(gen.id)}Observer"
                "secondConcreteObserverName" -> "Secondary${cap(gen.id)}Observer"
                "facadeClassName" -> "${cap(gen.id)}Facade"
                "subsystem1Name" -> "${cap(gen.id)}SubsystemA"
                "subsystem2Name" -> "${cap(gen.id)}SubsystemB"
                else -> when (p.type) {
                    ParameterType.STRING -> (p.defaultValue as? String) ?: deriveFallbackString(p.id, gen.id)
                    ParameterType.BOOLEAN -> (p.defaultValue as? Boolean) ?: true
                    ParameterType.INTEGER -> (p.defaultValue as? Int) ?: 0
                }
            }
            result[p.id] = v
        }
        return result
    }

    private fun deriveFallbackString(paramId: String, patternId: String): String =
        cap(patternId) + cap(paramId.removeSuffix("Name").removeSuffix("Class").removeSuffix("Interface"))

    private fun cap(s: String) = s.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
