package com.github.exanim.designpatterngeneratorplugin.patterns.impl.factorymethod

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternParameter
import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.github.exanim.designpatterngeneratorplugin.patterns.base.BaseJavaPatternGenerator
import com.github.exanim.designpatterngeneratorplugin.patterns.base.JavaGeneratedFile

/**
 * Factory Method Pattern Generator
 * Generates a minimal but extensible structure:
 *  - Product interface
 *  - Concrete Product(s)
 *  - Creator abstract class defining factory method
 *  - Concrete Creators overriding factory method
 *  - Optional Client demonstrating polymorphic creation without referencing concrete products directly.
 */
class FactoryMethodPatternGenerator : BaseJavaPatternGenerator() {
    override val id: String = "factorymethod"
    override val nameKey: String = "pattern.factorymethod.name"
    override val descriptionKey: String = "pattern.factorymethod.description"

    override val parameters = listOf(
        PatternParameter("packageName", "pattern.parameters.packageName", ParameterType.STRING, required = true),
        PatternParameter("productInterfaceName", "pattern.parameters.productInterfaceName", ParameterType.STRING, required = true),
        PatternParameter("firstConcreteProductName", "pattern.parameters.firstConcreteProductName", ParameterType.STRING, required = true),
        PatternParameter("secondConcreteProductName", "pattern.parameters.secondConcreteProductName", ParameterType.STRING, required = false),
        PatternParameter("creatorAbstractName", "pattern.parameters.creatorAbstractName", ParameterType.STRING, required = true, defaultValue = "BaseCreator"),
        PatternParameter("firstConcreteCreatorName", "pattern.parameters.firstConcreteCreatorName", ParameterType.STRING, required = true),
        PatternParameter("secondConcreteCreatorName", "pattern.parameters.secondConcreteCreatorName", ParameterType.STRING, required = false),
        PatternParameter("factoryMethodName", "pattern.parameters.factoryMethodName", ParameterType.STRING, required = true, defaultValue = "create"),
        PatternParameter("clientClassName", "pattern.parameters.clientClassName", ParameterType.STRING, required = false)
    )

    override fun buildFiles(values: Map<String, Any>): List<JavaGeneratedFile> {
        val pkg = values["packageName"] as? String ?: return emptyList()
        val productIface = values["productInterfaceName"] as? String ?: return emptyList()
        val product1 = values["firstConcreteProductName"] as? String ?: return emptyList()
        val product2 = (values["secondConcreteProductName"] as? String)?.takeIf { it.isNotBlank() }
        val creatorAbs = values["creatorAbstractName"] as? String ?: return emptyList()
        val creator1 = values["firstConcreteCreatorName"] as? String ?: return emptyList()
        val creator2 = (values["secondConcreteCreatorName"] as? String)?.takeIf { it.isNotBlank() }
        val factoryMethod = values["factoryMethodName"] as? String ?: return emptyList()
        val client = (values["clientClassName"] as? String)?.takeIf { it.isNotBlank() }

        val files = mutableListOf<JavaGeneratedFile>()
        val productSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/** Product interface defines operations that concrete products must implement */")
            appendLine("public interface $productIface {")
            appendLine("    void use();")
            appendLine("}")
        }
        files += JavaGeneratedFile("$productIface.java", productSrc, pkg)

        fun productImpl(name: String, msg: String) = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/** Concrete Product: $name */")
            appendLine("public class $name implements $productIface {")
            appendLine("    @Override public void use() { System.out.println(\"$name.use(): $msg\"); }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$product1.java", productImpl(product1, "Primary product behavior"), pkg)
        product2?.let { files += JavaGeneratedFile("$it.java", productImpl(it, "Secondary product behavior"), pkg) }

        val creatorAbsSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/**")
            appendLine(" * Creator (abstract) declares the factory method that returns a Product.\n * Subclasses override the factory method to change the Product type.\n * Creator may provide default business logic that relies on abstract factory method.")
            appendLine(" */")
            appendLine("public abstract class $creatorAbs {")
            appendLine("    public abstract $productIface $factoryMethod();")
            appendLine("    public void run() {")
            appendLine("        // Business logic decoupled from concrete product classes")
            appendLine("        $productIface product = $factoryMethod();")
            appendLine("        product.use();")
            appendLine("    }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$creatorAbs.java", creatorAbsSrc, pkg)

        fun concreteCreator(name: String, productProvided: String) = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/** Concrete Creator: $name produces $productProvided */")
            appendLine("public class $name extends $creatorAbs {")
            appendLine("    @Override public $productIface $factoryMethod() { return new $productProvided(); }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$creator1.java", concreteCreator(creator1, product1), pkg)
        if (creator2 != null && product2 != null) {
            files += JavaGeneratedFile("$creator2.java", concreteCreator(creator2, product2), pkg)
        }

        client?.let { clientName ->
            val clientSrc = buildString {
                appendLine("package $pkg;")
                appendLine()
                appendLine("/** Client demonstrates usage without coupling to concrete product classes */")
                appendLine("public class $clientName {")
                appendLine("    public static void main(String[] args) {")
                appendLine("        $creatorAbs creator = new $creator1();")
                appendLine("        creator.run();")
                if (creator2 != null) {
                    appendLine("        creator = new $creator2();")
                    appendLine("        creator.run();")
                }
                appendLine("    }")
                appendLine("}")
            }
            files += JavaGeneratedFile("$clientName.java", clientSrc, pkg)
        }
        return files
    }
}

