package com.github.exanim.designpatterngeneratorplugin.patterns.validation

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternGenerator

/** Responsibility Chain for validating user-supplied parameter values before generation. */
interface ValidationStep {
    fun validate(generator: PatternGenerator, values: Map<String, Any?>): ValidationResult
}

data class ValidationResult(val ok: Boolean, val errors: List<String> = emptyList()) {
    companion object {
        val SUCCESS = ValidationResult(true)
        fun failure(vararg errs: String) = ValidationResult(false, errs.toList())
    }
}

class ValidationChain(private val steps: List<ValidationStep>) {
    fun run(generator: PatternGenerator, values: Map<String, Any?>): ValidationResult {
        val aggregated = mutableListOf<String>()
        for (s in steps) {
            val r = s.validate(generator, values)
            if (!r.ok) aggregated += r.errors
        }
        return if (aggregated.isEmpty()) ValidationResult.SUCCESS else ValidationResult(false, aggregated)
    }
}

/** Ensures mandatory parameters are present / non blank */
object MandatoryParametersValidation : ValidationStep {
    override fun validate(generator: PatternGenerator, values: Map<String, Any?>): ValidationResult {
        val missing = generator.parameters.filter { it.required }.mapNotNull { p ->
            val v = values[p.id]
            if (v == null) p.id else if (v is String && v.isBlank()) p.id else null
        }
        return if (missing.isEmpty()) ValidationResult.SUCCESS else ValidationResult.failure(*missing.toTypedArray())
    }
}

