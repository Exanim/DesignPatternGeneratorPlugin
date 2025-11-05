package com.github.exanim.designpatterngeneratorplugin.patterns.impl.behavioural.strategy

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
        PatternParameter("secondConcreteStrategyName", "pattern.parameters.secondConcreteStrategyName", ParameterType.STRING, required = false),
        PatternParameter("contextClassName", "pattern.parameters.contextClassName", ParameterType.STRING, required = true),
        PatternParameter("clientClassName", "pattern.parameters.clientClassName", ParameterType.STRING, required = false),
        PatternParameter("methodName", "pattern.parameters.methodName", ParameterType.STRING, required = true, defaultValue = "execute")
    )

    override fun buildFiles(values: Map<String, Any>): List<JavaGeneratedFile> {
        val pkg = values["packageName"] as? String ?: return emptyList()
        val iface = values["strategyInterfaceName"] as? String ?: return emptyList()
        val concrete1 = values["concreteStrategyName"] as? String ?: return emptyList()
        val concrete2 = (values["secondConcreteStrategyName"] as? String)?.takeIf { it.isNotBlank() }
        val context = values["contextClassName"] as? String ?: return emptyList()
        val client = (values["clientClassName"] as? String)?.takeIf { it.isNotBlank() }
        val method = values["methodName"] as? String ?: return emptyList()

        val files = mutableListOf<JavaGeneratedFile>()

        val interfaceSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/**")
            appendLine(" * Strategy (Interface / Abstract Strategy)\n * Defines a common operation (algorithm) that all concrete strategies must implement.\n * The Context maintains a reference to this interface and delegates algorithmic work to it.")
            appendLine(" */")
            appendLine("public interface $iface {")
            appendLine("    void $method();")
            appendLine("}")
        }
        files += JavaGeneratedFile("$iface.java", interfaceSrc, pkg)

        fun concreteStrategySource(name: String, behaviorMsg: String) = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/**")
            appendLine(" * Concrete Strategy: $name\n * Implements the algorithm defined by the Strategy interface.\n * This variant: $behaviorMsg")
            appendLine(" */")
            appendLine("public class $name implements $iface {")
            appendLine("    @Override public void $method() {")
            appendLine("        System.out.println(\"$name.$method(): $behaviorMsg\");")
            appendLine("    }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$concrete1.java", concreteStrategySource(concrete1, "Primary strategy behavior"), pkg)
        concrete2?.let { files += JavaGeneratedFile("$it.java", concreteStrategySource(it, "Alternate strategy behavior"), pkg) }

        val contextSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/**")
            appendLine(" * Context\n * Holds a reference to a Strategy instance.\n * Delegates the algorithm call to the currently set Strategy.\n * Can change the active Strategy at runtime via setStrategy().")
            appendLine(" */")
            appendLine("public class $context {")
            appendLine("    private $iface strategy;")
            appendLine()
            appendLine("    public $context($iface strategy) {")
            appendLine("        this.strategy = strategy;")
            appendLine("    }")
            appendLine()
            appendLine("    public void setStrategy($iface strategy) {")
            appendLine("        this.strategy = strategy;")
            appendLine("    }")
            appendLine()
            appendLine("    public void $method() {")
            appendLine("        // Delegate to current strategy")
            appendLine("        strategy.$method();")
            appendLine("    }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$context.java", contextSrc, pkg)

        client?.let { clientName ->
            val clientSrc = buildString {
                appendLine("package $pkg;")
                appendLine()
                appendLine("/**")
                appendLine(" * Client (optional demo)\n * Demonstrates configuring the Context with different Strategy implementations at runtime.")
                appendLine(" */")
                appendLine("public class $clientName {")
                appendLine("    public static void main(String[] args) {")
                appendLine("        $iface first = new $concrete1();")
                appendLine("        $context ctx = new $context(first);")
                appendLine("        ctx.$method(); // executes primary strategy")
                concrete2?.let { alt ->
                    appendLine()
                    appendLine("        // Switch to second strategy dynamically")
                    appendLine("        ctx.setStrategy(new $alt());")
                    appendLine("        ctx.$method(); // executes alternate strategy")
                }
                appendLine("    }")
                appendLine("}")
            }
            files += JavaGeneratedFile("$clientName.java", clientSrc, pkg)
        }

        return files
    }
}
