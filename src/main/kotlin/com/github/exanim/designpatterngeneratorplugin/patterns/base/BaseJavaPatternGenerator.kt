package com.github.exanim.designpatterngeneratorplugin.patterns.base

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternGenerator
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory

/** Template Method base for Java pattern generators. */
abstract class BaseJavaPatternGenerator : PatternGenerator {
    final override fun generate(project: Project, targetDirectory: PsiDirectory, editor: Editor?, values: Map<String, Any>) {
        val fileName = targetFileName(values) ?: return
        if (targetDirectory.findFile(fileName) != null) return
        val fileText = buildJavaSource(values)
        WriteCommandAction.runWriteCommandAction(project) {
            val psiFile = PsiFileFactory.getInstance(project).createFileFromText(fileName, JavaLanguage.INSTANCE, fileText)
            targetDirectory.add(psiFile)
        }
    }
    protected open fun generationCommandName(values: Map<String, Any>) = "Generate Pattern: ${id}"
    protected abstract fun targetFileName(values: Map<String, Any>): String?
    protected abstract fun buildJavaSource(values: Map<String, Any>): String
    // Public preview API for UI code preview
    open fun preview(values: Map<String, Any>): String = buildJavaSource(values)
}
