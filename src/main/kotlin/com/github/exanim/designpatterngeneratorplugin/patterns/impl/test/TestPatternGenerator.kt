package com.github.exanim.designpatterngeneratorplugin.patterns.impl.test

import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternParameter
import com.github.exanim.designpatterngeneratorplugin.patterns.base.BaseJavaPatternGenerator

class TestPatternGenerator : BaseJavaPatternGenerator() {
    override val id: String = "testPattern"
    override val nameKey: String = "pattern.test.name"
    override val descriptionKey: String = "pattern.test.description"
    override val parameters = listOf(
        PatternParameter("packageName", "pattern.parameters.packageName", ParameterType.STRING, required = true),
        PatternParameter("className", "pattern.parameters.className", ParameterType.STRING, required = true),
        PatternParameter("methodName", "pattern.parameters.methodName", ParameterType.STRING, required = true),
        PatternParameter("threadSafe", "pattern.parameters.threadSafe", ParameterType.BOOLEAN, required = false, defaultValue = true),
        PatternParameter("lazy", "pattern.parameters.lazy", ParameterType.BOOLEAN, required = false, defaultValue = true)
    )

    override fun targetFileName(values: Map<String, Any>): String? = (values["className"] as? String)?.let { "$it.java" }

    override fun buildJavaSource(values: Map<String, Any>): String {
        val pkg = values["packageName"] as? String ?: return ""
        val className = values["className"] as? String ?: return ""
        val methodName = values["methodName"] as? String ?: return ""

        return buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("public class $className {")
            appendLine("    public void $methodName() {")
            appendLine("    }")
            appendLine("}")
        }
    }
}