package com.github.exanim.designpatterngeneratorplugin.codegen

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JavaCodeModelTest {
    @Test
    fun renderSimpleClass() {
        val classSpec = JavaClassSpec.Builder("Foo")
            .addModifier("public")
            .addField(JavaFieldSpec(listOf("private"), "int", "value", "42"))
            .addMethod(JavaMethodSpec.Builder("getValue", "int").addModifier("public").addBodyLine("return value;").build())
            .build()
        val file = JavaFileSpec("com.example", classSpec)
        val rendered = file.render()
        assertTrue(rendered.contains("class Foo"))
        assertTrue(rendered.contains("int value = 42;"))
        assertTrue(rendered.contains("return value;"))
    }
}
