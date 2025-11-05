package com.github.exanim.designpatterngeneratorplugin.patterns.impl.structural.adapter

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
        PatternParameter("adapterClassName", "pattern.parameters.adapterClassName", ParameterType.STRING, required = true),
        PatternParameter("clientClassName", "pattern.parameters.clientClassName", ParameterType.STRING, required = true)
    )

    override fun buildFiles(values: Map<String, Any>): List<JavaGeneratedFile> {
        val pkg = values["packageName"] as? String ?: return emptyList()
        val iface = values["interfaceName"] as? String ?: return emptyList()
        val adaptee = values["adapteeClassName"] as? String ?: return emptyList()
        val adapter = values["adapterClassName"] as? String ?: return emptyList()
        val client = values["clientClassName"] as? String ?: return emptyList()

        val interfaceSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/**")
            appendLine(" * Client Interface (Target)\n * Defines the protocol that Client code relies on.\n * The Adapter will implement this interface to become compatible with Client code.")
            appendLine(" */")
            appendLine("public interface $iface {")
            appendLine("    void request();")
            appendLine("}")
        }
        val adapteeSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/**")
            appendLine(" * Service (Adaptee)\n * A legacy or 3rd-party component with an incompatible interface.\n * We cannot modify it, so we wrap it inside an Adapter.")
            appendLine(" */")
            appendLine("public class $adaptee {")
            appendLine("    public void specificRequest() {")
            appendLine("        // Legacy logic or complex operation")
            appendLine("        System.out.println(\"$adaptee.specificRequest executed\");")
            appendLine("    }")
            appendLine("}")
        }
        val adapterSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/**")
            appendLine(" * Adapter\n * Implements the Client Interface (Target) and wraps a Service (Adaptee).\n * Translates calls from the Client Interface to the Adaptee's interface using composition.\n * This is an Object Adapter implementation.")
            appendLine(" */")
            appendLine("public class $adapter implements $iface {")
            appendLine("    private final $adaptee service;")
            appendLine()
            appendLine("    public $adapter($adaptee service) {")
            appendLine("        this.service = service;")
            appendLine("    }")
            appendLine()
            appendLine("    @Override public void request() {")
            appendLine("        // Translate the generic request into the specific Adaptee call")
            appendLine("        service.specificRequest();")
            appendLine("    }")
            appendLine("}")
        }
        val clientSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/**")
            appendLine(" * Client\n * Uses only the Client Interface (Target) and stays decoupled from concrete Adapter and Adaptee.\n * New adapters can be introduced without changing Client code.")
            appendLine(" */")
            appendLine("public class $client {")
            appendLine("    private final $iface dependency;")
            appendLine()
            appendLine("    public $client($iface dependency) {")
            appendLine("        this.dependency = dependency;")
            appendLine("    }")
            appendLine()
            appendLine("    public void doWork() {")
            appendLine("        // Client remains unaware of Adaptee; interacts via interface")
            appendLine("        dependency.request();")
            appendLine("    }")
            appendLine()
            appendLine("    public static void main(String[] args) {")
            appendLine("        // Wiring: create Adaptee, wrap in Adapter, inject into Client")
            appendLine("        $adaptee service = new $adaptee();")
            appendLine("        $iface target = new $adapter(service);")
            appendLine("        $client client = new $client(target);")
            appendLine("        client.doWork();")
            appendLine("    }")
            appendLine("}")
        }
        return listOf(
            JavaGeneratedFile("$iface.java", interfaceSrc, pkg),
            JavaGeneratedFile("$adaptee.java", adapteeSrc, pkg),
            JavaGeneratedFile("$adapter.java", adapterSrc, pkg),
            JavaGeneratedFile("$client.java", clientSrc, pkg)
        )
    }
}
