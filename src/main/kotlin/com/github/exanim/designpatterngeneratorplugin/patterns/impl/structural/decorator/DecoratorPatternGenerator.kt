package com.github.exanim.designpatterngeneratorplugin.patterns.impl.structural.decorator

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternParameter
import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.github.exanim.designpatterngeneratorplugin.patterns.base.BaseJavaPatternGenerator
import com.github.exanim.designpatterngeneratorplugin.patterns.base.JavaGeneratedFile

/**
 * Decorator Pattern Generator
 * Generates structure:
 *  - Component interface
 *  - Concrete Component
 *  - Base Decorator holding a reference to Component
 *  - One or two Concrete Decorators adding behavior before/after delegation
 *  - Optional Client demonstrating stacking decorators.
 */
class DecoratorPatternGenerator : BaseJavaPatternGenerator() {
    override val id: String = "decorator"
    override val nameKey: String = "pattern.decorator.name"
    override val descriptionKey: String = "pattern.decorator.description"

    override val parameters = listOf(
        PatternParameter("packageName", "pattern.parameters.packageName", ParameterType.STRING, required = true),
        PatternParameter("componentInterfaceName", "pattern.parameters.componentInterfaceName", ParameterType.STRING, required = true),
        PatternParameter("concreteComponentName", "pattern.parameters.concreteComponentName", ParameterType.STRING, required = true),
        PatternParameter("baseDecoratorName", "pattern.parameters.baseDecoratorName", ParameterType.STRING, required = true, defaultValue = "BaseDecorator"),
        PatternParameter("firstConcreteDecoratorName", "pattern.parameters.firstConcreteDecoratorName", ParameterType.STRING, required = true),
        PatternParameter("secondConcreteDecoratorName", "pattern.parameters.secondConcreteDecoratorName", ParameterType.STRING, required = false),
        PatternParameter("operationName", "pattern.parameters.operationName", ParameterType.STRING, required = true, defaultValue = "operation"),
        PatternParameter("clientClassName", "pattern.parameters.clientClassName", ParameterType.STRING, required = false)
    )

    override fun buildFiles(values: Map<String, Any>): List<JavaGeneratedFile> {
        val pkg = values["packageName"] as? String ?: return emptyList()
        val compIface = values["componentInterfaceName"] as? String ?: return emptyList()
        val concreteComp = values["concreteComponentName"] as? String ?: return emptyList()
        val baseDec = values["baseDecoratorName"] as? String ?: return emptyList()
        val dec1 = values["firstConcreteDecoratorName"] as? String ?: return emptyList()
        val dec2 = (values["secondConcreteDecoratorName"] as? String)?.takeIf { it.isNotBlank() }
        val op = values["operationName"] as? String ?: return emptyList()
        val client = (values["clientClassName"] as? String)?.takeIf { it.isNotBlank() }

        val files = mutableListOf<JavaGeneratedFile>()

        val ifaceSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/** Component interface declares operations that can be extended dynamically */")
            appendLine("public interface $compIface {")
            appendLine("    void $op();")
            appendLine("}")
        }
        files += JavaGeneratedFile("$compIface.java", ifaceSrc, pkg)

        val concreteSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/** Concrete Component provides the core behavior subject to decoration */")
            appendLine("public class $concreteComp implements $compIface {")
            appendLine("    @Override public void $op() { System.out.println(\"$concreteComp.$op(): base behavior\"); }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$concreteComp.java", concreteSrc, pkg)

        val baseDecoratorSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/**")
            appendLine(" * Base Decorator maintains a reference to a Component.\n * It forwards operations to the wrapped component allowing subclasses to add behavior.")
            appendLine(" */")
            appendLine("public abstract class $baseDec implements $compIface {")
            appendLine("    protected final $compIface wrappee;")
            appendLine("    protected $baseDec($compIface wrappee) { this.wrappee = wrappee; }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$baseDec.java", baseDecoratorSrc, pkg)

        fun decoratorImpl(name: String, behaviorMsg: String) = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/** Concrete Decorator: $name adds $behaviorMsg */")
            appendLine("public class $name extends $baseDec {")
            appendLine("    public $name($compIface wrappee) { super(wrappee); }")
            appendLine("    @Override public void $op() {")
            appendLine("        // Pre-behavior")
            appendLine("        System.out.println(\"$name.pre: $behaviorMsg\");")
            appendLine("        wrappee.$op();")
            appendLine("        // Post-behavior")
            appendLine("        System.out.println(\"$name.post: $behaviorMsg\");")
            appendLine("    }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$dec1.java", decoratorImpl(dec1, "additional responsibility"), pkg)
        dec2?.let { files += JavaGeneratedFile("$it.java", decoratorImpl(it, "alternative responsibility"), pkg) }

        client?.let { clientName ->
            val clientSrc = buildString {
                appendLine("package $pkg;")
                appendLine()
                appendLine("/** Client demonstrates stacking of decorators */")
                appendLine("public class $clientName {")
                appendLine("    public static void main(String[] args) {")
                appendLine("        $compIface component = new $concreteComp();")
                appendLine("        component.$op();")
                appendLine("        System.out.println(\"---- decorate once ----\");")
                appendLine("        component = new $dec1(component);")
                appendLine("        component.$op();")
                dec2?.let { alt ->
                    appendLine("        System.out.println(\"---- decorate twice ----\");")
                    appendLine("        component = new $alt(component);")
                    appendLine("        component.$op();")
                }
                appendLine("    }")
                appendLine("}")
            }
            files += JavaGeneratedFile("$clientName.java", clientSrc, pkg)
        }
        return files
    }
}
