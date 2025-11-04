package com.github.exanim.designpatterngeneratorplugin.patterns.impl.strategy

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternParameter
import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.github.exanim.designpatterngeneratorplugin.patterns.base.BaseJavaPatternGenerator

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

    override fun targetFileName(values: Map<String, Any>): String? = (values["contextClassName"] as? String)?.let { "$it.java" }

    override fun buildJavaSource(values: Map<String, Any>): String {
        val pkg = values["packageName"] as? String ?: return ""
        val iface = values["strategyInterfaceName"] as? String ?: return ""
        val concrete = values["concreteStrategyName"] as? String ?: return ""
        val context = values["contextClassName"] as? String ?: return ""
        val method = values["methodName"] as? String ?: return ""
        return buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("// Generated Strategy pattern")
            appendLine("public interface $iface {")
            appendLine("    void $method();")
            appendLine("}")
            appendLine()
            appendLine("public class $concrete implements $iface {")
            appendLine("    @Override public void $method() { }")
            appendLine("}")
            appendLine()
            appendLine("public class $context {")
            appendLine("    private $iface strategy;")
            appendLine("    public $context($iface strategy) { this.strategy = strategy; }")
            appendLine("    public void setStrategy($iface strategy) { this.strategy = strategy; }")
            appendLine("    public void $method() { strategy.$method(); }")
            appendLine("}")
        }
    }
}

