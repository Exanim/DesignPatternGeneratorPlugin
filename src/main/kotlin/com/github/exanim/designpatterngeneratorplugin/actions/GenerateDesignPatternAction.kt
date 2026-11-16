package com.github.exanim.designpatterngeneratorplugin.actions

import com.github.exanim.designpatterngeneratorplugin.MyBundle
import com.github.exanim.designpatterngeneratorplugin.patterns.registry.PatternRegistryService
import com.github.exanim.designpatterngeneratorplugin.ui.PatternGenerationDialog
import com.github.exanim.designpatterngeneratorplugin.patterns.validation.MandatoryParametersValidation
import com.github.exanim.designpatterngeneratorplugin.patterns.validation.ValidationChain
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.openapi.ui.Messages

class GenerateDesignPatternAction : AnAction(
    MyBundle.message("action.generateDesignPattern.text"),
    MyBundle.message("action.generateDesignPattern.description"),
    null
) {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val registry = PatternRegistryService.getInstance()
        val view = PatternGenerationDialog(project, registry.all())
        if (!view.showAndGet()) return
        val generator = view.getSelectedGenerator() ?: return
        val values = view.getValues()

        val validation = ValidationChain(
            listOf(MandatoryParametersValidation)
        ).run(generator, values)
        if (!validation.ok) {
            Messages.showErrorDialog(project,
                "Validation failed: ${validation.errors.joinToString()}",
                "Design Pattern Generation")
            return
        }

        val vFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val targetVFile = when {
            vFile == null -> project.basePath?.let { LocalFileSystem.getInstance().findFileByPath(it) }
            vFile.isDirectory -> vFile
            else -> vFile.parent
        } ?: return
        val psiDir: PsiDirectory = PsiManager.getInstance(project).findDirectory(targetVFile) ?: return

        generator.generate(project, psiDir, e.getData(CommonDataKeys.EDITOR), values)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
