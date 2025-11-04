package com.github.exanim.designpatterngeneratorplugin.patterns.base

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternGenerator
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory

/** Represents a single generated Java source file. */
data class JavaGeneratedFile(val fileName: String, val source: String, val packageName: String?)

/** Template Method base for Java pattern generators supporting single or multi-file generation. */
abstract class BaseJavaPatternGenerator : PatternGenerator {
    final override fun generate(project: Project, targetDirectory: PsiDirectory, editor: Editor?, values: Map<String, Any>) {
        val files = buildFiles(values)
        if (files.isEmpty()) return
        WriteCommandAction.runWriteCommandAction(project) {
            for (f in files) {
                val pkgDir = f.packageName?.takeIf { it.isNotBlank() }?.let { ensurePackageDirectory(targetDirectory, it) } ?: targetDirectory
                if (pkgDir.findFile(f.fileName) != null) continue
                val psiFile = PsiFileFactory.getInstance(project).createFileFromText(f.fileName, JavaLanguage.INSTANCE, f.source)
                pkgDir.add(psiFile)
            }
        }
    }

    /** Default single-file implementation built from subclass hooks. */
    protected open fun buildFiles(values: Map<String, Any>): List<JavaGeneratedFile> {
        val name = targetFileName(values) ?: return emptyList()
        val src = buildJavaSource(values)
        val pkg = values["packageName"] as? String
        return listOf(JavaGeneratedFile(name, src, pkg))
    }

    protected abstract fun targetFileName(values: Map<String, Any>): String?
    protected abstract fun buildJavaSource(values: Map<String, Any>): String
    protected open fun generationCommandName(values: Map<String, Any>) = "Generate Pattern: ${id}"
    /** Preview combines multiple files separated by headers. */
    open fun preview(values: Map<String, Any>): String = buildFiles(values).joinToString("\n\n") { "// ${it.fileName}\n${it.source}" }

    private fun ensurePackageDirectory(root: PsiDirectory, packageName: String): PsiDirectory {
        val parts = packageName.split('.').filter { it.isNotBlank() }
        var current = root
        for (p in parts) {
            current = current.findSubdirectory(p) ?: current.createSubdirectory(p)
        }
        return current
    }
}
