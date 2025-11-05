package com.github.exanim.designpatterngeneratorplugin.patterns.impl.chain

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternParameter
import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.github.exanim.designpatterngeneratorplugin.patterns.base.BaseJavaPatternGenerator
import com.github.exanim.designpatterngeneratorplugin.patterns.base.JavaGeneratedFile

/**
 * Chain of Responsibility Pattern Generator
 * Generates:
 *  - Handler abstract class / interface
 *  - Concrete Handlers (one mandatory, second optional)
 *  - Request class (simple data holder)
 *  - Optional Client wiring a chain and dispatching requests.
 */
class ChainOfResponsibilityPatternGenerator : BaseJavaPatternGenerator() {
    override val id: String = "chainofresponsibility"
    override val nameKey: String = "pattern.chain.name"
    override val descriptionKey: String = "pattern.chain.description"

    override val parameters = listOf(
        PatternParameter("packageName", "pattern.parameters.packageName", ParameterType.STRING, required = true),
        PatternParameter("handlerAbstractName", "pattern.parameters.handlerAbstractName", ParameterType.STRING, required = true, defaultValue = "Handler"),
        PatternParameter("firstConcreteHandlerName", "pattern.parameters.firstConcreteHandlerName", ParameterType.STRING, required = true),
        PatternParameter("secondConcreteHandlerName", "pattern.parameters.secondConcreteHandlerName", ParameterType.STRING, required = false),
        PatternParameter("requestClassName", "pattern.parameters.requestClassName", ParameterType.STRING, required = true, defaultValue = "Request"),
        PatternParameter("handleMethodName", "pattern.parameters.handleMethodName", ParameterType.STRING, required = true, defaultValue = "handle"),
        PatternParameter("canHandleMethodName", "pattern.parameters.canHandleMethodName", ParameterType.STRING, required = true, defaultValue = "canHandle"),
        PatternParameter("clientClassName", "pattern.parameters.clientClassName", ParameterType.STRING, required = false)
    )

    override fun buildFiles(values: Map<String, Any>): List<JavaGeneratedFile> {
        val pkg = values["packageName"] as? String ?: return emptyList()
        val handlerAbs = values["handlerAbstractName"] as? String ?: return emptyList()
        val h1 = values["firstConcreteHandlerName"] as? String ?: return emptyList()
        val h2 = (values["secondConcreteHandlerName"] as? String)?.takeIf { it.isNotBlank() }
        val requestName = values["requestClassName"] as? String ?: return emptyList()
        val handleMethod = values["handleMethodName"] as? String ?: return emptyList()
        val canHandleMethod = values["canHandleMethodName"] as? String ?: return emptyList()
        val client = (values["clientClassName"] as? String)?.takeIf { it.isNotBlank() }

        val files = mutableListOf<JavaGeneratedFile>()

        val requestSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/** Simple Request object passed along the chain */")
            appendLine("public class $requestName {")
            appendLine("    private final String type;")
            appendLine("    public $requestName(String type) { this.type = type; }")
            appendLine("    public String getType() { return type; }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$requestName.java", requestSrc, pkg)

        val handlerAbsSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/**")
            appendLine(" * Handler abstract class defines linking and processing contract.\n * Each concrete handler decides whether it can handle the request; otherwise forwards it.")
            appendLine(" */")
            appendLine("public abstract class $handlerAbs {")
            appendLine("    private $handlerAbs next;")
            appendLine("    public $handlerAbs setNext($handlerAbs next) { this.next = next; return next; }")
            appendLine("    public final void $handleMethod($requestName request) {")
            appendLine("        if ($canHandleMethod(request)) {")
            appendLine("            process(request);")
            appendLine("        } else if (next != null) {")
            appendLine("            next.$handleMethod(request);")
            appendLine("        } else {")
            appendLine("            System.out.println(\"No handler could process request type: \" + request.getType());")
            appendLine("        }")
            appendLine("    }")
            appendLine("    protected abstract boolean $canHandleMethod($requestName request);")
            appendLine("    protected abstract void process($requestName request);")
            appendLine("}")
        }
        files += JavaGeneratedFile("$handlerAbs.java", handlerAbsSrc, pkg)

        fun concreteHandler(name: String, typeCode: String) = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/** Concrete Handler: $name responsible for type '$typeCode' */")
            appendLine("public class $name extends $handlerAbs {")
            appendLine("    @Override protected boolean $canHandleMethod($requestName request) { return request.getType().equals(\"$typeCode\"); }")
            appendLine("    @Override protected void process($requestName request) { System.out.println(\"$name processed \" + request.getType()); }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$h1.java", concreteHandler(h1, "A"), pkg)
        h2?.let { files += JavaGeneratedFile("$it.java", concreteHandler(it, "B"), pkg) }

        client?.let { clientName ->
            val clientSrc = buildString {
                appendLine("package $pkg;")
                appendLine()
                appendLine("/** Client wires the chain and sends requests */")
                appendLine("public class $clientName {")
                appendLine("    public static void main(String[] args) {")
                appendLine("        $handlerAbs first = new $h1();")
                if (h2 != null) appendLine("        $handlerAbs second = new $h2(); first.setNext(second);")
                appendLine("        first.$handleMethod(new $requestName(\"A\"));")
                if (h2 != null) appendLine("        first.$handleMethod(new $requestName(\"B\"));")
                appendLine("        first.$handleMethod(new $requestName(\"Z\"));")
                appendLine("    }")
                appendLine("}")
            }
            files += JavaGeneratedFile("$clientName.java", clientSrc, pkg)
        }
        return files
    }
}

