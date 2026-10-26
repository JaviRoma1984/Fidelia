package com.fidelia.app.data

/** Utilidades para el correo electronico de las tarjetas de visita. */
object Emails {
    const val MAX_LENGTH = 100

    private val shape = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$")

    /** Lo que se deja escribir en el campo: un correo no lleva espacios. */
    fun sanitizeInput(raw: String): String = raw.filterNot { it.isWhitespace() }.take(MAX_LENGTH)

    /** Si tiene forma de correo (algo@dominio.xx); no se comprueba que exista. */
    fun looksValid(raw: String): Boolean = shape.matches(raw.trim())
}
