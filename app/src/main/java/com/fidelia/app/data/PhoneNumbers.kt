package com.fidelia.app.data

/** Utilidades para los telefonos de las tarjetas de visita. */
object PhoneNumbers {
    const val MAX_LENGTH = 20

    private const val ALLOWED_WHEN_TYPING = "0123456789+-(). "

    /** Lo que se deja escribir en el campo: cifras, espacios y los signos habituales de un telefono. */
    fun sanitizeInput(raw: String): String = raw.filter { it in ALLOWED_WHEN_TYPING }.take(MAX_LENGTH)

    /**
     * Lo que se marca de verdad: cifras, un "+" inicial y los signos de marcacion "*" y "#".
     * Se quitan espacios, guiones y parentesis.
     */
    fun dialable(raw: String): String = buildString {
        for (c in raw) {
            when {
                c.isDigit() || c == '*' || c == '#' -> append(c)
                c == '+' && isEmpty() -> append(c)
            }
        }
    }

    /** Si hay algo que marcar (al menos una cifra). */
    fun isCallable(raw: String): Boolean = dialable(raw).any { it.isDigit() }

    /** El telefono tal y como se escribio, sin espacios sobrantes. */
    fun display(raw: String): String = raw.trim()
}
