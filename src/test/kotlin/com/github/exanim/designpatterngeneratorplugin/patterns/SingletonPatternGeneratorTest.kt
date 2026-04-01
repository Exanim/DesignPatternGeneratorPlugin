package com.github.exanim.designpatterngeneratorplugin.patterns

import com.github.exanim.designpatterngeneratorplugin.patterns.impl.creational.singleton.SingletonPatternGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SingletonPatternGeneratorTest {
    @Test
    fun parametersMetadata() {
        val gen = SingletonPatternGenerator()
        assertEquals("singleton", gen.id)
        assertTrue(gen.parameters.any { it.id == "className" })
        assertTrue(gen.parameters.any { it.id == "packageName" })
    }
}
