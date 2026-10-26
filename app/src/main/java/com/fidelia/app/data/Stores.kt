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
    /** Si el "logo" lleva el nombre en varios colores (p. ej. tienda + animal). */
    val logoParts: List<NamePart>? = null,
)

data class NamePart(val text: String, val color: Color)

object Stores {
    private val autoPalette = listOf(
        Color(0xFF4338CA), Color(0xFF0F766E), Color(0xFFBE185D), Color(0xFFC2410C), Color(0xFF1D4ED8),
        Color(0xFF7E22CE), Color(0xFF15803D), Color(0xFF334155), Color(0xFFB91C1C), Color(0xFF0E7490),
    )

    /** Color estable (sale del id) para las tiendas de las que no se conoce el de la marca. */
    private fun auto(id: String, name: String) =
        Store(id, name, autoPalette[(id.hashCode() and Int.MAX_VALUE) % autoPalette.size])

    private val black = Color(0xFF111111)
    private val white = Color.White

    /**
     * Establecimientos del centro comercial Parque Principado (Oviedo), segun el mapa
     * del sitio de parqueprincipado.com del 23-10-2026. Los que ya estaban arriba
     * (Burger King, McDonald's, Fnac, H&M...) no se repiten.
     */
    private val parquePrincipado: List<Store> = listOf(
        auto("100-montaditos", "100 Montaditos").copy(color = Color(0xFFD7191F), textColor = Color(0xFFFFC72C)),
        auto("amaloa", "Amaloa"),
        auto("aromas-artesanales", "Aromas Artesanales"),
        auto("azulmarino-viajes", "Azulmarino Viajes"),
        auto("beds", "Beds"),
        auto("belros", "Belros"),
        auto("ben-jerrys", "Ben & Jerry's"),
        Store("bershka", "Bershka", white, black),
        auto("bijou-brigitte", "Bijou Brigitte"),
        auto("bimba-y-lola", "Bimba y Lola"),
        auto("brasaylena", "Brasayleña"),
        auto("c-and-a", "C&A"),
        auto("calzedonia", "Calzedonia"),
        auto("carte-dor", "Carte d'Or"),
        auto("chukamen", "Chukamen"),
        Store("columbia", "Columbia", Color(0xFF0073B5)),
        auto("cortefiel", "Cortefiel"),
        auto("courir", "Courir"),
        auto("decimas", "Décimas"),
        auto("deichmann", "Deichmann"),
        auto("dom", "Dom"),
        auto("dona-carmen", "Doña Carmen"),
        auto("ebanni-peluqueros", "Ebanni Peluqueros"),
        auto("el-ganso", "El Ganso"),
        auto("extensionmania", "Extensionmanía"),
        Store("flying-tiger", "Flying Tiger", Color(0xFFE2231A)),
        Store("foot-locker", "Foot Locker", black),
        auto("friking", "Friking"),
        auto("futbol-emotion", "Fútbol Emotion"),
        auto("game", "Game"),
        auto("general-optica", "General Óptica"),
        auto("ginos", "Gino's"),
        auto("gowok-ramen", "GoWok Ramen"),
        auto("gran-bufet-fuji", "Gran Bufet Fuji"),
        auto("guess", "Guess"),
        auto("halcon-viajes", "Halcón Viajes"),
        auto("harry-coffee-collective", "Harry The Coffee Collective"),
        auto("ilusiona", "Ilusiona"),
        auto("intecat", "Intecat"),
        auto("intimissimi", "Intimissimi"),
        auto("jack-and-jones", "Jack & Jones"),
        Store("jd", "JD", black),
        auto("jose-luis-joyerias", "José Luis Joyerías"),
        auto("joyeria-siglo-xxi", "Joyería Siglo XXI"),
        auto("juguettos", "Juguettos"),
        Store("kfc", "KFC", Color(0xFFE4002B)),
        Store("kiko", "Kiko", black),
        auto("kimono", "Kimono"),
        auto("koroshi", "Koröshi"),
        auto("krack", "Krack"),
        auto("krunch", "Krunch"),
        auto("la-casa-de-las-carcasas", "La Casa de las Carcasas"),
        auto("la-mina", "La Mina"),
        auto("la-tagliatella", "La Tagliatella"),
        auto("legends-nails", "Legends Nails"),
        auto("lefties", "Lefties"),
        Store("levis", "Levi's", Color(0xFFC41230)),
        auto("luxenter", "Luxenter"),
        Store("mango-teen", "Mango Teen", black),
        auto("manolo-bakes", "Manolo Bakes"),
        auto("masmadera", "Masmadera"),
        auto("maslife", "Maslife"),
        auto("masymas", "Masymas"),
        auto("maxcolchon", "Maxcolchón"),
        auto("maydo", "Maydo"),
        auto("mayoral", "Mayoral"),
        auto("megacalzado", "Megacalzado"),
        auto("milbby", "Milbby"),
        auto("miniso", "Miniso").copy(color = Color(0xFFE60012)),
        auto("misako", "Misako"),
        auto("mr-blue", "Mr. Blue"),
        auto("multiopticas", "Multiópticas"),
        auto("mundicafes", "Mundicafés"),
        auto("navoptica", "Navóptica"),
        Store("nespresso", "Nespresso", Color(0xFF1B1B1B)),
        auto("new-era", "New Era"),
        auto("new-yorker", "New Yorker"),
        auto("norauto", "Norauto"),
        auto("nunez-de-arenas", "Núñez de Arenas"),
        Store("orange", "Orange", Color(0xFFFF7900)),
        auto("oro-vivo", "Oro Vivo"),
        auto("paco-martinez", "Paco Martínez"),
        auto("pandora", "Pandora"),
        auto("parfois", "Parfois"),
        auto("pepco", "Pepco"),
        auto("pepe-jeans", "Pepe Jeans"),
        auto("polinesia", "Polinesia"),
        Store("primark", "Primark", Color(0xFF0DAEEF)),
        Store("pull-and-bear", "Pull&Bear", white, black),
        auto("punt-roma", "Punt Roma"),
        auto("qiosqo", "Qiosqo"),
        auto("queens", "Queens"),
        Store("real-oviedo", "Real Oviedo", Color(0xFF1B4DA1)),
        auto("rituals", "Rituals"),
        auto("rks-footwear", "RKS Footwear"),
        auto("salsa", "Salsa"),
        auto("santa-gloria", "Santa Gloria"),
        auto("scalpers", "Scalpers"),
        auto("sfera", "Sfera"),
        auto("singularu", "Singularu"),
        auto("skechers", "Skechers"),
        auto("snipes", "Snipes"),
        auto("soloptical", "Soloptical"),
        auto("springfield", "Springfield"),
        Store("stradivarius", "Stradivarius", white, black),
        auto("sundara", "Sundara"),
        Store("taco-bell", "Taco Bell", Color(0xFF702082)),
        auto("teashop", "Teashop"),
        auto("tecno-gallery", "Tecno Gallery"),
        auto("telecable", "Telecable"),
        auto("tezenis", "Tezenis"),
        auto("the-black-turtle", "The Black Turtle"),
        auto("tierras-gallegas", "Tierras Gallegas"),
        auto("time-road", "Time Road"),
        Store("tous", "Tous", white, black),
        auto("tramas", "Tramas"),
        auto("ulanka", "Ulanka"),
        auto("uncode", "Uncode"),
        auto("usa-fitness", "USA Fitness"),
        auto("vans", "Vans"),
        auto("vezzo", "Vezzo"),
        auto("washprin", "Washprin"),
        auto("wewash", "WeWash"),
        Store("movistar", "Movistar", Color(0xFF019DF4)),
        Store("yves-rocher", "Yves Rocher", Color(0xFF1E8A4C)),
        Store("zara", "Zara", white, black),
        auto("zurimed", "Zurimed"),
        auto("yoyogurt", "Yoyogurt"),
    )

    val all: List<Store> = (listOf(
        Store("alcampo", "Alcampo", Color(0xFFE2001A)),
        Store("aldi", "Aldi", Color(0xFF00005F)),
        Store("alimerka", "Alimerka", Color(0xFFFFD100)),
        Store("alvaro-moreno", "Álvaro Moreno", Color(0xFF1F2A44)),
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
        Store("forum-sport", "Forum Sport", Color(0xFF1B1B1B)),
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
        Store(
            "tiendanimal", "Tiendanimal", Color(0xFF5B6470),
            logoParts = listOf(NamePart("Tiend", Color.White), NamePart("animal", Color(0xFF8BD13F))),
        ),
        Store("toysrus", "Toys\"R\"Us", Color(0xFF0066B3), Color(0xFFFFD100)),
        Store("vips", "VIPS", Color(0xFFC8102E)),
        Store("womensecret", "Women'secret", Color(0xFFB71C5A)),
        Store("worten", "Worten", Color(0xFFEB0000)),
        Store("yelmo", "Yelmo", Color(0xFF006EB6)),
    ) + parquePrincipado).sortedBy { normalize(it.name) }

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
