package com.github.exanim.designpatterngeneratorplugin

import org.jetbrains.annotations.PropertyKey
import java.text.MessageFormat
import java.util.*

/** Message bundle access helper for localized plugin strings. */
object MyBundle {
    private const val BUNDLE = "messages.MyBundle"
    private val bundle: ResourceBundle by lazy { ResourceBundle.getBundle(BUNDLE) }

    /**
     * Resolve a message by key optionally formatting with params.
     */
    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any?): String {
        return try {
            val pattern = bundle.getString(key)
            if (params.isEmpty()) pattern else MessageFormat.format(pattern, *params)
        } catch (e: MissingResourceException) {
            "!$key!" // Fallback marker
        }
    }
}

