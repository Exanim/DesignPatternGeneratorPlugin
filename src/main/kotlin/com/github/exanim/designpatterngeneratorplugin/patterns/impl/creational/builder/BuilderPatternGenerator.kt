package com.github.exanim.designpatterngeneratorplugin.patterns.impl.creational.builder

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternParameter
import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.github.exanim.designpatterngeneratorplugin.patterns.base.BaseJavaPatternGenerator
import com.github.exanim.designpatterngeneratorplugin.patterns.base.JavaGeneratedFile

/**
 * Builder Pattern Generator
 * Generates: Product class, Builder interface, Concrete Builders (named), Director (optional), Client (optional).
 */
class BuilderPatternGenerator : BaseJavaPatternGenerator() {
    override val id = "builder"
    override val nameKey = "pattern.builder.name"
    override val descriptionKey = "pattern.builder.description"

    override val parameters = listOf(
        PatternParameter("packageName", "pattern.parameters.packageName", ParameterType.STRING, required = true),
        PatternParameter("productClassName", "pattern.parameters.productClassName", ParameterType.STRING, required = true),
        PatternParameter("builderInterfaceName", "pattern.parameters.builderInterfaceName", ParameterType.STRING, required = true, defaultValue = "Builder"),
        PatternParameter("firstConcreteBuilderName", "pattern.parameters.firstConcreteBuilderName", ParameterType.STRING, required = true),
        PatternParameter("secondConcreteBuilderName", "pattern.parameters.secondConcreteBuilderName", ParameterType.STRING, required = false),
        PatternParameter("directorClassName", "pattern.parameters.directorClassName", ParameterType.STRING, required = false, defaultValue = "Director"),
        PatternParameter("clientClassName", "pattern.parameters.clientClassName", ParameterType.STRING, required = false)
    )

    override fun buildFiles(values: Map<String, Any>): List<JavaGeneratedFile> {
        val pkg = values["packageName"] as? String ?: return emptyList()
        val product = values["productClassName"] as? String ?: return emptyList()
        val builderIface = values["builderInterfaceName"] as? String ?: return emptyList()
        val b1 = values["firstConcreteBuilderName"] as? String ?: return emptyList()
        val b2 = (values["secondConcreteBuilderName"] as? String)?.takeIf { it.isNotBlank() }
        val director = (values["directorClassName"] as? String)?.takeIf { it.isNotBlank() }
        val client = (values["clientClassName"] as? String)?.takeIf { it.isNotBlank() }

        val files = mutableListOf<JavaGeneratedFile>()

        val productSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/** Product built step-by-step by Builders */")
            appendLine("public class $product {")
            appendLine("    private String partA; private String partB; private String partC;")
            appendLine("    public void setPartA(String v){ partA=v; }")
            appendLine("    public void setPartB(String v){ partB=v; }")
            appendLine("    public void setPartC(String v){ partC=v; }")
            appendLine("    @Override public String toString(){ return \"$product{\"+partA+','+partB+','+partC+'}\"; }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$product.java", productSrc, pkg)

        val builderIfaceSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/** Builder interface defines build steps and finalization */")
            appendLine("public interface $builderIface {")
            appendLine("    void buildPartA();")
            appendLine("    void buildPartB();")
            appendLine("    void buildPartC();")
            appendLine("    $product getResult();")
            appendLine("}")
        }
        files += JavaGeneratedFile("$builderIface.java", builderIfaceSrc, pkg)

        fun concreteBuilder(name: String, style: String) = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/** Concrete Builder: $name ($style) */")
            appendLine("public class $name implements $builderIface {")
            appendLine("    private final $product prod = new $product();")
            appendLine("    public void buildPartA(){ prod.setPartA(\"$style-A\"); }")
            appendLine("    public void buildPartB(){ prod.setPartB(\"$style-B\"); }")
            appendLine("    public void buildPartC(){ prod.setPartC(\"$style-C\"); }")
            appendLine("    public $product getResult(){ return prod; }")
            appendLine("}")
        }
        val allBuilders = mutableListOf<String>()
        allBuilders += b1
        if (b2 != null) allBuilders += b2
        files += JavaGeneratedFile("$b1.java", concreteBuilder(b1, "Primary"), pkg)
        if (b2 != null) files += JavaGeneratedFile("$b2.java", concreteBuilder(b2, "Secondary"), pkg)

        director?.let { dirName ->
            val directorSrc = buildString {
                appendLine("package $pkg;")
                appendLine()
                appendLine("/** Director orchestrates a construction sequence */")
                appendLine("public class $dirName {")
                appendLine("    public $product construct($builderIface builder){")
                appendLine("        builder.buildPartA(); builder.buildPartB(); builder.buildPartC();")
                appendLine("        return builder.getResult();")
                appendLine("    }")
                appendLine("}")
            }
            files += JavaGeneratedFile("$dirName.java", directorSrc, pkg)
        }

        client?.let { clientName ->
            val clientSrc = buildString {
                appendLine("package $pkg;")
                appendLine()
                appendLine("/** Client demonstrates switching builders */")
                appendLine("public class $clientName {")
                appendLine("    public static void main(String[] args){")
                if (director != null) appendLine("        $director director = new $director();")
                allBuilders.forEach { bn ->
                    appendLine("        $builderIface builder = new $bn();")
                    if (director != null) {
                        appendLine("        System.out.println(director.construct(builder));")
                    } else {
                        appendLine("        builder.buildPartA(); builder.buildPartB(); builder.buildPartC();")
                        appendLine("        System.out.println(builder.getResult());")
                    }
                }
                appendLine("    }")
                appendLine("}")
            }
            files += JavaGeneratedFile("$clientName.java", clientSrc, pkg)
        }
        return files
    }
}
