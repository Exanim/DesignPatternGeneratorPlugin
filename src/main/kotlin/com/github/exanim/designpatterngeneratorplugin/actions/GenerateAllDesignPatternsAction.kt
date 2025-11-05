package com.github.exanim.designpatterngeneratorplugin.actions

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternGenerator
import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.github.exanim.designpatterngeneratorplugin.patterns.registry.PatternRegistryService
import com.github.exanim.designpatterngeneratorplugin.patterns.base.BaseJavaPatternGenerator
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager

/** Action to bulk-generate all supported design patterns with default role-based parameter values. */
class GenerateAllDesignPatternsAction : AnAction(
    "Generate All Design Patterns (Debug)",
    "Generates all registered design patterns with default role-based parameter values into chosen directory.",
    null
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val registry = PatternRegistryService.getInstance()
        val patterns = registry.all()
        if (patterns.isEmpty()) {
            Messages.showInfoMessage(project, "No pattern generators registered.", "Generate All")
            return
        }

        val vFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val targetVFile = when {
            vFile == null -> project.basePath?.let { LocalFileSystem.getInstance().findFileByPath(it) }
            vFile.isDirectory -> vFile
            else -> vFile.parent
        } ?: run {
            Messages.showErrorDialog(project, "Cannot resolve target directory.", "Generate All")
            return
        }
        val psiDir: PsiDirectory = PsiManager.getInstance(project).findDirectory(targetVFile) ?: run {
            Messages.showErrorDialog(project, "Cannot resolve PSI directory.", "Generate All")
            return
        }

        val report = StringBuilder()
        patterns.forEach { gen ->
            try {
                val values = buildDefaults(gen)
                (gen as? BaseJavaPatternGenerator)?.generate(project, psiDir, null, values)
                report.append("${gen.id}: OK\n")
            } catch (ex: Exception) {
                report.append("${gen.id}: FAILED - ${ex.message}\n")
            }
        }
        Messages.showInfoMessage(project, report.toString(), "Generate All Result")
    }

    private fun buildDefaults(gen: PatternGenerator): Map<String, Any> {
        val result = HashMap<String, Any>()
        val basePkg = "allpatterns.${gen.id.lowercase()}"
        gen.parameters.forEach { p ->
            val v: Any = when (p.id) {
                "packageName" -> basePkg
                "className" -> cap(gen.id) + "Singleton"
                "interfaceName" -> cap(gen.id) + "Target"
                "adapteeClassName" -> "Legacy" + cap(gen.id) + "Service"
                "adapterClassName" -> cap(gen.id) + "Adapter"
                "clientClassName" -> cap(gen.id) + "Client"
                "strategyInterfaceName" -> cap(gen.id) + "Strategy"
                "concreteStrategyName" -> "Primary" + cap(gen.id) + "Strategy"
                "secondConcreteStrategyName" -> "Secondary" + cap(gen.id) + "Strategy"
                "productInterfaceName", "productClassName" -> cap(gen.id) + "Product"
                "firstConcreteProductName" -> "Primary" + cap(gen.id) + "Product"
                "secondConcreteProductName" -> "Secondary" + cap(gen.id) + "Product"
                "thirdConcreteProductName" -> "Tertiary" + cap(gen.id) + "Product"
                "creatorAbstractName" -> cap(gen.id) + "Creator"
                "firstConcreteCreatorName" -> "Primary" + cap(gen.id) + "Creator"
                "secondConcreteCreatorName" -> "Secondary" + cap(gen.id) + "Creator"
                "builderInterfaceName" -> cap(gen.id) + "Builder"
                "firstConcreteBuilderName" -> "Primary" + cap(gen.id) + "Builder"
                "secondConcreteBuilderName" -> "Secondary" + cap(gen.id) + "Builder"
                "thirdConcreteBuilderName" -> "Tertiary" + cap(gen.id) + "Builder"
                "componentInterfaceName" -> cap(gen.id) + "Component"
                "concreteComponentName" -> "Base" + cap(gen.id) + "Component"
                "firstConcreteDecoratorName" -> "Logging" + cap(gen.id) + "Decorator"
                "secondConcreteDecoratorName" -> "Metrics" + cap(gen.id) + "Decorator"
                "handlerAbstractName" -> cap(gen.id) + "Handler"
                "firstConcreteHandlerName" -> "Primary" + cap(gen.id) + "Handler"
                "secondConcreteHandlerName" -> "Secondary" + cap(gen.id) + "Handler"
                "thirdConcreteHandlerName" -> "Tertiary" + cap(gen.id) + "Handler"
                "subjectClassName" -> cap(gen.id) + "Subject"
                "firstConcreteObserverName" -> "Primary" + cap(gen.id) + "Observer"
                "secondConcreteObserverName" -> "Secondary" + cap(gen.id) + "Observer"
                "facadeClassName" -> cap(gen.id) + "Facade"
                "subsystem1Name" -> cap(gen.id) + "SubsystemA"
                "subsystem2Name" -> cap(gen.id) + "SubsystemB"
                "methodName" -> "execute"
                else -> when (p.type) {
                    ParameterType.STRING -> (p.defaultValue as? String) ?: (cap(gen.id) + cap(p.id))
                    ParameterType.BOOLEAN -> (p.defaultValue as? Boolean) ?: true
                    ParameterType.INTEGER -> (p.defaultValue as? Int) ?: 0
                }
            }
            result[p.id] = v
        }
        return result
    }

    private fun cap(s: String) = s.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
