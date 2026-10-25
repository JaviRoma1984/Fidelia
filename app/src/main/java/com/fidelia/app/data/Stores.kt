package com.fidelia.app.data

import androidx.compose.ui.graphics.Color
import java.text.Normalizer

/**
 * Tienda del listado. No son los logos oficiales: cada una se dibuja en la app
 * como un "logo" con su nombre y un color parecido al de la marca. Quien quiera
 * el logo real puede cargar su propia imagen al crear o editar la tarjeta.
 */
data class Store(
    val id: String,
    val name: String,
    val color: Color,
    val textColor: Color = Color.White,
)

object Stores {
    val all: List<Store> = listOf(
        Store("alcampo", "Alcampo", Color(0xFFE2001A)),
        Store("aldi", "Aldi", Color(0xFF00005F)),
        Store("bp", "BP", Color(0xFF009A3D), Color(0xFFFFE600)),
        Store("burger-king", "Burger King", Color(0xFFD62300)),
        Store("caprabo", "Caprabo", Color(0xFFD71920)),
        Store("carrefour", "Carrefour", Color(0xFF004E9F)),
        Store("casa-del-libro", "Casa del Libro", Color(0xFFB3282D)),
        Store("cinesa", "Cinesa", Color(0xFFE30613)),
        Store("condis", "Condis", Color(0xFFE2231A)),
        Store("conforama", "Conforama", Color(0xFFE4002B)),
        Store("decathlon", "Decathlon", Color(0xFF0082C3)),
        Store("dia", "Dia", Color(0xFFE5252A)),
        Store("douglas", "Douglas", Color(0xFF111111)),
        Store("druni", "Druni", Color(0xFFD81B60)),
        Store("el-corte-ingles", "El Corte Inglés", Color(0xFF00843D)),
        Store("eroski", "Eroski", Color(0xFFE30613)),
        Store("fnac", "FNAC", Color(0xFFE1A400), Color(0xFF1A1A1A)),
        Store("fosters", "Foster's Hollywood", Color(0xFF1B2A41), Color(0xFFFFC107)),
        Store("galp", "Galp", Color(0xFFFF7A00)),
        Store("hm", "H&M", Color(0xFFE50010)),
        Store("hipercor", "Hipercor", Color(0xFF2E7D32)),
        Store("iberia", "Iberia Plus", Color(0xFFD7192D)),
        Store("ikea", "IKEA Family", Color(0xFF0058A3), Color(0xFFFFDA1A)),
        Store("leroy-merlin", "Leroy Merlin", Color(0xFF67B32E)),
        Store("lidl", "Lidl", Color(0xFF0050AA), Color(0xFFFFF000)),
        Store("mango", "Mango", Color(0xFF111111)),
        Store("mcdonalds", "McDonald's", Color(0xFFDA291C), Color(0xFFFFC72C)),
        Store("mediamarkt", "MediaMarkt", Color(0xFFDF0000)),
        Store("moeve", "Moeve (Cepsa)", Color(0xFF0A2D6E)),
        Store("pccomponentes", "PcComponentes", Color(0xFFFF6000)),
        Store("primor", "Primor", Color(0xFFE6007E)),
        Store("repsol", "Repsol", Color(0xFFFF8200)),
        Store("sephora", "Sephora", Color(0xFF000000)),
        Store("shell", "Shell", Color(0xFFFBCE07), Color(0xFFDD1D21)),
        Store("sprinter", "Sprinter", Color(0xFFF26522)),
        Store("starbucks", "Starbucks", Color(0xFF00704A)),
        Store("telepizza", "Telepizza", Color(0xFFE2001A), Color(0xFFFFD100)),
        Store("toysrus", "Toys\"R\"Us", Color(0xFF0066B3), Color(0xFFFFD100)),
        Store("vips", "VIPS", Color(0xFFC8102E)),
        Store("womensecret", "Women'secret", Color(0xFFB71C5A)),
        Store("worten", "Worten", Color(0xFFEB0000)),
        Store("yelmo", "Yelmo", Color(0xFF006EB6)),
    ).sortedBy { normalize(it.name) }

    private val byId = all.associateBy { it.id }

    fun find(id: String?): Store? = id?.let(byId::get)

    /** Busca ignorando mayusculas y acentos ("corte ingles" encuentra "El Corte Inglés"). */
    fun search(query: String): List<Store> {
        val q = normalize(query.trim())
        if (q.isEmpty()) return all
        return all.filter { normalize(it.name).contains(q) }
    }

    private fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
}
