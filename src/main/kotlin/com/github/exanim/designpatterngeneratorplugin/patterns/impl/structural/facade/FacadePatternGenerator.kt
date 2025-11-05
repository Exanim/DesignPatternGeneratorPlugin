package com.github.exanim.designpatterngeneratorplugin.patterns.impl.structural.facade

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternParameter
import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.github.exanim.designpatterngeneratorplugin.patterns.base.BaseJavaPatternGenerator
import com.github.exanim.designpatterngeneratorplugin.patterns.base.JavaGeneratedFile

/**
 * Facade Pattern Generator
 * Generates subsystem classes + Facade + optional Client.
 */
class FacadePatternGenerator : BaseJavaPatternGenerator() {
    override val id = "facade"
    override val nameKey = "pattern.facade.name"
    override val descriptionKey = "pattern.facade.description"

    override val parameters = listOf(
        PatternParameter("packageName", "pattern.parameters.packageName", ParameterType.STRING, required = true),
        PatternParameter("facadeClassName", "pattern.parameters.facadeClassName", ParameterType.STRING, required = true, defaultValue = "Facade"),
        PatternParameter("firstConcreteSubsystemName", "pattern.parameters.subsystem1Name", ParameterType.STRING, required = true),
        PatternParameter("secondConcreteSubsystemName", "pattern.parameters.subsystem2Name", ParameterType.STRING, required = false),
        PatternParameter("clientClassName", "pattern.parameters.clientClassName", ParameterType.STRING, required = false)
    )

    override fun buildFiles(values: Map<String, Any>): List<JavaGeneratedFile> {
        val pkg = values["packageName"] as? String ?: return emptyList()
        val facade = values["facadeClassName"] as? String ?: return emptyList()
        val s1 = values["subsystem1Name"] as? String ?: return emptyList()
        val s2 = (values["subsystem2Name"] as? String)?.takeIf { it.isNotBlank() }
        val client = (values["clientClassName"] as? String)?.takeIf { it.isNotBlank() }

        val files = mutableListOf<JavaGeneratedFile>()

        fun subsystem(name: String, role: String) = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/** Subsystem component: $name ($role) */")
            appendLine("public class $name {")
            appendLine("    public String op(){ return \"$name.op()\"; }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$s1.java", subsystem(s1, "primary"), pkg)
        s2?.let { files += JavaGeneratedFile("$it.java", subsystem(it, "secondary"), pkg) }

        val all = listOfNotNull(s1, s2)
        val facadeSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/** Facade providing simplified interface to subsystem */")
            appendLine("public class $facade {")
            all.forEach { n -> appendLine("    private final $n ${n.lowercase()} = new $n();") }
            appendLine("    public String simpleOperation(){")
            appendLine("        StringBuilder sb = new StringBuilder();")
            all.forEach { n -> appendLine("        sb.append(${n.lowercase()}.op()).append(\";\");") }
            appendLine("        return sb.toString();")
            appendLine("    }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$facade.java", facadeSrc, pkg)

        client?.let { clientName ->
            val clientSrc = buildString {
                appendLine("package $pkg;")
                appendLine()
                appendLine("/** Client uses only Facade */")
                appendLine("public class $clientName {")
                appendLine("    public static void main(String[] args){")
                appendLine("        $facade f = new $facade();")
                appendLine("        System.out.println(f.simpleOperation());")
                appendLine("    }")
                appendLine("}")
            }
            files += JavaGeneratedFile("$clientName.java", clientSrc, pkg)
        }
        return files
    }
}
