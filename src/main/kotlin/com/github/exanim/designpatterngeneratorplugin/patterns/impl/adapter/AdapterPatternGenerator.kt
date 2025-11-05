package com.github.exanim.designpatterngeneratorplugin.patterns.impl.adapter

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternParameter
import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.github.exanim.designpatterngeneratorplugin.patterns.base.BaseJavaPatternGenerator
import com.github.exanim.designpatterngeneratorplugin.patterns.base.JavaGeneratedFile

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

    override fun buildFiles(values: Map<String, Any>): List<JavaGeneratedFile> {
        val pkg = values["packageName"] as? String ?: return emptyList()
        val iface = values["interfaceName"] as? String ?: return emptyList()
        val adaptee = values["adapteeClassName"] as? String ?: return emptyList()
        val adapter = values["adapterClassName"] as? String ?: return emptyList()

        val interfaceSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("// Adapter interface")
            appendLine("public interface $iface {")
            appendLine("    void request();")
            appendLine("}")
        }
        val adapteeSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("// Adaptee class")
            appendLine("class $adaptee {")
            appendLine("    public void specificRequest() { }")
            appendLine("}")
            appendLine()
        }

        val adapterSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("// Adapter class implementing the interface and using the Adaptee")
            appendLine("public class $adapter implements $iface {")
            appendLine("    private final $adaptee adaptee = new $adaptee();")
            appendLine("    @Override public void request() { adaptee.specificRequest(); }")
            appendLine("}")
        }
        return listOf(
            JavaGeneratedFile("$iface.java", interfaceSrc, pkg),
            JavaGeneratedFile("$adaptee.java", adapteeSrc, pkg),
            JavaGeneratedFile("$adapter.java", adapterSrc, pkg)
        )
    }
}
