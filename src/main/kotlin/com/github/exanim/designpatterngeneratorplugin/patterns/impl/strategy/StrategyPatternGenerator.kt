package com.github.exanim.designpatterngeneratorplugin.patterns.impl.strategy

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternParameter
import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.github.exanim.designpatterngeneratorplugin.patterns.base.BaseJavaPatternGenerator
import com.github.exanim.designpatterngeneratorplugin.patterns.base.JavaGeneratedFile

class StrategyPatternGenerator : BaseJavaPatternGenerator() {
    override val id: String = "strategy"
    override val nameKey: String = "pattern.strategy.name"
    override val descriptionKey: String = "pattern.strategy.description"
    override val parameters = listOf(
        PatternParameter("packageName", "pattern.parameters.packageName", ParameterType.STRING, required = true),
        PatternParameter("strategyInterfaceName", "pattern.parameters.strategyInterfaceName", ParameterType.STRING, required = true),
        PatternParameter("concreteStrategyName", "pattern.parameters.concreteStrategyName", ParameterType.STRING, required = true),
        PatternParameter("contextClassName", "pattern.parameters.contextClassName", ParameterType.STRING, required = true),
        PatternParameter("methodName", "pattern.parameters.methodName", ParameterType.STRING, required = true, defaultValue = "execute")
    )

    // Unused in multi-file scenario, but still required by base template
    override fun targetFileName(values: Map<String, Any>): String? = (values["contextClassName"] as? String)?.let { "$it.java" }

    override fun buildJavaSource(values: Map<String, Any>): String = "" // Not used; multi-file generation below

    override fun buildFiles(values: Map<String, Any>): List<JavaGeneratedFile> {
        val pkg = values["packageName"] as? String ?: return emptyList()
        val iface = values["strategyInterfaceName"] as? String ?: return emptyList()
        val concrete = values["concreteStrategyName"] as? String ?: return emptyList()
        val context = values["contextClassName"] as? String ?: return emptyList()
        val method = values["methodName"] as? String ?: return emptyList()

        val interfaceSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("// Strategy interface")
            appendLine("public interface $iface {")
            appendLine("    void $method();")
            appendLine("}")
        }
        val concreteSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("// Concrete strategy")
            appendLine("public class $concrete implements $iface {")
            appendLine("    @Override public void $method() { }")
            appendLine("}")
        }
        val contextSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("// Context holding a strategy reference")
            appendLine("public class $context {")
            appendLine("    private $iface strategy;")
            appendLine("    public $context($iface strategy) { this.strategy = strategy; }")
            appendLine("    public void setStrategy($iface strategy) { this.strategy = strategy; }")
            appendLine("    public void $method() { strategy.$method(); }")
            appendLine("}")
        }
        return listOf(
            JavaGeneratedFile("$iface.java", interfaceSrc, pkg),
            JavaGeneratedFile("$concrete.java", concreteSrc, pkg),
            JavaGeneratedFile("$context.java", contextSrc, pkg)
        )
    }
}
