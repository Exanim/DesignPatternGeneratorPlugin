package com.github.exanim.designpatterngeneratorplugin.patterns.impl.singleton

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternParameter
import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.github.exanim.designpatterngeneratorplugin.patterns.base.BaseJavaPatternGenerator
import com.github.exanim.designpatterngeneratorplugin.codegen.*
import com.github.exanim.designpatterngeneratorplugin.patterns.base.JavaGeneratedFile

/** Basic Singleton pattern generator implementation using Builder + Composite for source assembly. */
class SingletonPatternGenerator : BaseJavaPatternGenerator() {
    override val id: String = "singleton"
    override val nameKey: String = "pattern.singleton.name"
    override val descriptionKey: String = "pattern.singleton.description"
    override val parameters = listOf(
        PatternParameter("packageName", "pattern.parameters.packageName", ParameterType.STRING, required = true),
        PatternParameter("className", "pattern.parameters.className", ParameterType.STRING, required = true),
        PatternParameter("threadSafe", "pattern.parameters.threadSafe", ParameterType.BOOLEAN, required = false, defaultValue = true),
        PatternParameter("lazy", "pattern.parameters.lazy", ParameterType.BOOLEAN, required = false, defaultValue = true)
    )

    override fun buildFiles(values: Map<String, Any>): List<JavaGeneratedFile> {
        val pkg = values["packageName"] as? String ?: return emptyList()
        val className = values["className"] as? String ?: return emptyList()
        val threadSafe = values["threadSafe"] as? Boolean ?: true
        val lazyInit = values["lazy"] as? Boolean ?: true

        val classBuilder = JavaClassSpec.Builder(className).addModifier("public final")
        if (lazyInit) {
            classBuilder.addField(JavaFieldSpec(listOf("private static volatile"), className, "INSTANCE"))
        } else {
            classBuilder.addField(JavaFieldSpec(listOf("private static final"), className, "INSTANCE", "new $className()"))
        }
        classBuilder.addConstructor(JavaConstructorSpec.Builder(className).addModifier("private").build())
        val getInstanceMethod = JavaMethodSpec.Builder("getInstance", className).addModifier("public static")
        if (lazyInit) {
            if (threadSafe) {
                getInstanceMethod.addBodyLine("if (INSTANCE == null) {")
                getInstanceMethod.addBodyLine("    synchronized(${className}.class) {")
                getInstanceMethod.addBodyLine("        if (INSTANCE == null) {")
                getInstanceMethod.addBodyLine("            INSTANCE = new $className();")
                getInstanceMethod.addBodyLine("        }")
                getInstanceMethod.addBodyLine("    }")
                getInstanceMethod.addBodyLine("}")
                getInstanceMethod.addBodyLine("return INSTANCE;")
            } else {
                getInstanceMethod.addBodyLine("if (INSTANCE == null) { INSTANCE = new $className(); }")
                getInstanceMethod.addBodyLine("return INSTANCE;")
            }
        } else {
            getInstanceMethod.addBodyLine("return INSTANCE;")
        }
        classBuilder.addMethod(getInstanceMethod.build())

        val source = JavaFileSpec(pkg, classBuilder.build()).render()
        return listOf(JavaGeneratedFile("$className.java", source, pkg))
    }
}
