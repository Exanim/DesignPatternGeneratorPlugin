package com.github.exanim.designpatterngeneratorplugin.patterns.impl.adapter

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternParameter
import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.github.exanim.designpatterngeneratorplugin.patterns.base.BaseJavaPatternGenerator

class AdapterPatternGenerator : BaseJavaPatternGenerator() {
    override val id: String = "adapter"
    override val nameKey: String = "pattern.adapter.name"
    override val descriptionKey: String = "pattern.adapter.description"
    override val parameters = listOf(
        PatternParameter("packageName", "pattern.parameters.packageName", ParameterType.STRING, required = true),
        PatternParameter("interfaceName", "pattern.parameters.interfaceName", ParameterType.STRING, required = true),
        PatternParameter("adapteeClassName", "pattern.parameters.adapteeClassName", ParameterType.STRING, required = true),
        PatternParameter("adapterClassName", "pattern.parameters.adapterClassName", ParameterType.STRING, required = true)
    )

    override fun targetFileName(values: Map<String, Any>): String? = (values["adapterClassName"] as? String)?.let { "$it.java" }

    override fun buildJavaSource(values: Map<String, Any>): String {
        val pkg = values["packageName"] as? String ?: return ""
        val iface = values["interfaceName"] as? String ?: return ""
        val adaptee = values["adapteeClassName"] as? String ?: return ""
        val adapter = values["adapterClassName"] as? String ?: return ""
        return buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("// Generated Adapter pattern")
            appendLine("public interface $iface {")
            appendLine("    void request();")
            appendLine("}")
            appendLine()
            appendLine("class $adaptee {")
            appendLine("    public void specificRequest() { }")
            appendLine("}")
            appendLine()
            appendLine("public class $adapter implements $iface {")
            appendLine("    private final $adaptee adaptee = new $adaptee();")
            appendLine("    @Override public void request() { adaptee.specificRequest(); }")
            appendLine("}")
        }
    }
}

