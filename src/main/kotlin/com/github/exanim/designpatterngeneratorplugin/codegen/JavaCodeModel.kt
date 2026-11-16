package com.github.exanim.designpatterngeneratorplugin.codegen

/** Simple code model for generating Java source using Builder + Composite patterns. */
class JavaFileSpec(val packageName: String?, val classSpec: JavaClassSpec) {
    fun render(): String = buildString {
        if (!packageName.isNullOrBlank()) append("package $packageName;\n\n")
        append(classSpec.render())
    }
}

class JavaClassSpec private constructor(
    private val modifiers: List<String>,
    private val name: String,
    private val fields: List<JavaFieldSpec>,
    private val methods: List<JavaMethodSpec>,
    private val constructors: List<JavaConstructorSpec>
) {
    fun render(): String = buildString {
        append(
            modifiers.
            joinToString(" ").
            takeIf { it.isNotBlank() }?.
            plus(" ") ?: ""
        )
        append("class $name {\n")
        if (fields.isNotEmpty()) {
            fields.forEach { append("    ").append(it.render()).append("\n") }
            append("\n")
        }
        constructors.forEach { append(it.render()).append("\n\n") }
        methods.forEach { append(it.render()).append("\n") }
        append("}\n")
    }

    class Builder(private val name: String) {
        private val modifiers = mutableListOf<String>()
        private val fields = mutableListOf<JavaFieldSpec>()
        private val methods = mutableListOf<JavaMethodSpec>()
        private val constructors = mutableListOf<JavaConstructorSpec>()
        fun addModifier(m: String) = apply { modifiers += m }
        fun addField(field: JavaFieldSpec) = apply { fields += field }
        fun addMethod(method: JavaMethodSpec) = apply { methods += method }
        fun addConstructor(cons: JavaConstructorSpec) = apply { constructors += cons }
        fun build() = JavaClassSpec(modifiers.toList(), name, fields.toList(), methods.toList(), constructors.toList())
    }
}

class JavaFieldSpec(private val modifiers: List<String>,
                    private val type: String,
                    private val name: String,
                    private val initializer: String? = null) {
    fun render(): String = buildString {
        append(
            modifiers.
            joinToString(" ").
            takeIf { it.isNotBlank() }?.
            plus(" ") ?: ""
        )
        append("$type $name")
        if (initializer != null) append(" = $initializer")
        append(";")
    }
}

class JavaMethodSpec private constructor(
    private val modifiers: List<String>,
    private val returnType: String,
    private val name: String,
    private val bodyLines: List<String>,
    private val params: List<Pair<String, String>>
) {
    fun render(): String = buildString {
        append("    ")
        append(
            modifiers.joinToString(" ").
            takeIf { it.isNotBlank() }?.
            plus(" ") ?: ""
        )
        append("$returnType $name(")
        append(params.joinToString(", ") { (t, n) -> "$t $n" })
        append(") {\n")
        bodyLines.forEach { append("        ").append(it).append("\n") }
        append("    }\n")
    }

    class Builder(private val name: String, private val returnType: String) {
        private val modifiers = mutableListOf<String>()
        private val bodyLines = mutableListOf<String>()
        private val params = mutableListOf<Pair<String, String>>()
        fun addModifier(m: String) = apply { modifiers += m }
        fun addBodyLine(line: String) = apply { bodyLines += line }
        fun build() = JavaMethodSpec(modifiers.toList(), returnType, name, bodyLines.toList(), params.toList())
    }
}

class JavaConstructorSpec private constructor(
    private val modifiers: List<String>,
    private val name: String,
    private val bodyLines: List<String>,
    private val params: List<Pair<String, String>>
) {
    fun render(): String = buildString {
        append("    ")
        append(modifiers.joinToString(" ").takeIf { it.isNotBlank() }?.plus(" ") ?: "")
        append("$name(")
        append(params.joinToString(", ") { (t, n) -> "$t $n" })
        append(") {\n")
        bodyLines.forEach { append("        ").append(it).append("\n") }
        append("    }")
    }

    class Builder(private val name: String) {
        private val modifiers = mutableListOf<String>()
        private val bodyLines = mutableListOf<String>()
        private val params = mutableListOf<Pair<String, String>>()
        fun addModifier(m: String) = apply { modifiers += m }
        fun build() = JavaConstructorSpec(modifiers.toList(), name, bodyLines.toList(), params.toList())
    }
}

