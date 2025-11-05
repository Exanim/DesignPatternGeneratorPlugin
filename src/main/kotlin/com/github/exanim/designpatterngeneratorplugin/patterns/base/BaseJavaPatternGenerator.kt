package com.github.exanim.designpatterngeneratorplugin.patterns.base

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternGenerator
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory

/**
 * Represents a single generated Java source file.
 *
 * @property fileName The name of the generated file.
 * @property source The source code content of the file.
 * @property packageName The package name where the file should be placed, or null if no package is specified.
 */
data class JavaGeneratedFile(val fileName: String, val source: String, val packageName: String?)

/**
 * Base class for Java pattern generators that support single or multi-file generation.
 * Implements the Template Method design pattern to define the workflow for generating files.
 */
abstract class BaseJavaPatternGenerator : PatternGenerator {

    /**
     * Generates Java source files based on the provided values and writes them to the target directory.
     *
     * @param project The current IntelliJ IDEA project.
     * @param targetDirectory The directory where the generated files will be placed.
     * @param editor The current editor instance, or null if not applicable.
     * @param values A map of user-provided values used to customize the generated files.
     */
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

    /**
     * Abstract method to be implemented by subclasses to define the files to be generated.
     *
     * @param values A map of user-provided values used to customize the generated files.
     * @return A list of `JavaGeneratedFile` objects representing the files to be generated.
     */
    protected abstract fun buildFiles(values: Map<String, Any>): List<JavaGeneratedFile>

    /**
     * Generates a preview of the files to be created, combining multiple files with headers.
     *
     * @param values A map of user-provided values used to customize the generated files.
     * @return A string representation of the preview, with each file's content preceded by its name.
     */
    open fun preview(values: Map<String, Any>): String = buildFiles(values).joinToString("\n\n") { "// ${it.fileName}\n${it.source}" }

    /**
     * Ensures that the specified package directory exists within the given root directory.
     * Creates subdirectories as needed to match the package structure.
     *
     * @param root The root directory where the package structure should be created.
     * @param packageName The fully qualified package name (e.g., "com.example.myapp").
     * @return The `PsiDirectory` corresponding to the created or existing package directory.
     */
    private fun ensurePackageDirectory(root: PsiDirectory, packageName: String): PsiDirectory {
        val parts = packageName.split('.').filter { it.isNotBlank() }
        var current = root
        for (p in parts) {
            current = current.findSubdirectory(p) ?: current.createSubdirectory(p)
        }
        return current
    }
}
