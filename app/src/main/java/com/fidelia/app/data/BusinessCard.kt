package com.fidelia.app.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.BusinessCenter
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Carpenter
import androidx.compose.material.icons.rounded.CarRepair
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.ElectricalServices
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.FormatPaint
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.HomeWork
import androidx.compose.material.icons.rounded.Hvac
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocalPharmacy
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Plumbing
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Roofing
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Yard
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Una tarjeta de visita guardada. Lleva un icono de oficio ([tradeId]) o una foto de la
 * propia tarjeta ([imageFile]); hasta dos telefonos, el nombre de la empresa y el del titular.
 */
data class BusinessCard(
    val id: String,
    val company: String,
    val holder: String,
    val phone1: String,
    val phone2: String,
    val tradeId: String?,
    val imageFile: String?,
    val createdAt: Long,
    /** Correo electronico (opcional). Al final y con valor por defecto: los datos guardados antes siguen valiendo. */
    val email: String = "",
) {
    /** Como se llama en las listas: la empresa o, si no hay, el titular. */
    val title: String get() = company.ifBlank { holder }

    /** Los telefonos rellenados (0, 1 o 2). */
    val phones: List<String> get() = listOf(phone1, phone2).filter { it.isNotBlank() }
}

/**
 * Un icono de la lista de oficios para las tarjetas de visita. En la tarjeta el icono va
 * siempre en blanco sobre su [color]; con [solid], en la lista de oficios tambien se ve
 * as (blanco sobre fondo de color) aunque no este elegido, para que destaque.
 */
class TradeIcon(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val solid: Boolean = false,
)

object TradeIcons {
    val all: List<TradeIcon> = listOf(
        TradeIcon("empresa", "Empresa", Icons.Rounded.BusinessCenter, Color(0xFF334155)),
        TradeIcon("emergencias", "Emergencias", Icons.Rounded.Emergency, Color(0xFFDC2626), solid = true),
        TradeIcon("fontanero", "Fontanero", Icons.Rounded.Plumbing, Color(0xFF0E7490)),
        TradeIcon("electricista", "Electricista", Icons.Rounded.ElectricalServices, Color(0xFFCA8A04)),
        TradeIcon("carpintero", "Carpintero", Icons.Rounded.Carpenter, Color(0xFF92400E)),
        TradeIcon("pintor", "Pintor", Icons.Rounded.FormatPaint, Color(0xFFBE185D)),
        TradeIcon("albanil", "Albañil", Icons.Rounded.Construction, Color(0xFFC2410C)),
        TradeIcon("reformas", "Reformas", Icons.Rounded.Handyman, Color(0xFF7C2D12)),
        TradeIcon("tejados", "Tejados", Icons.Rounded.Roofing, Color(0xFF9A3412)),
        TradeIcon("climatizacion", "Climatización", Icons.Rounded.Hvac, Color(0xFF1D4ED8)),
        TradeIcon("cerrajero", "Cerrajero", Icons.Rounded.Key, Color(0xFF475569)),
        TradeIcon("mecanico", "Mecánico", Icons.Rounded.CarRepair, Color(0xFFB91C1C)),
        TradeIcon("limpieza", "Limpieza", Icons.Rounded.CleaningServices, Color(0xFF0D9488)),
        TradeIcon("jardineria", "Jardinería", Icons.Rounded.Yard, Color(0xFF15803D)),
        // ContentCut es el icono de las tijeras.
        TradeIcon("peluqueria", "Peluquería", Icons.Rounded.ContentCut, Color(0xFF7E22CE)),
        TradeIcon("estetica", "Estética", Icons.Rounded.Spa, Color(0xFFDB2777)),
        TradeIcon("medico", "Médico", StethoscopeIcon, Color(0xFF0369A1)),
        TradeIcon("clinica", "Clínica", Icons.Rounded.LocalHospital, Color(0xFF0E7490)),
        TradeIcon("farmacia", "Farmacia", Icons.Rounded.LocalPharmacy, Color(0xFF16A34A)),
        TradeIcon("veterinario", "Veterinario", Icons.Rounded.Pets, Color(0xFF0F766E)),
        TradeIcon("restaurante", "Restaurante", Icons.Rounded.Restaurant, Color(0xFFDC2626)),
        TradeIcon("abogado", "Abogado", Icons.Rounded.Gavel, Color(0xFF1E3A8A)),
        TradeIcon("gestoria", "Gestoría", Icons.Rounded.AccountBalance, Color(0xFF3730A3)),
        TradeIcon("seguros", "Seguros", Icons.Rounded.VerifiedUser, Color(0xFF2563EB)),
        TradeIcon("informatica", "Informática", Icons.Rounded.Computer, Color(0xFF4338CA)),
        TradeIcon("fotografia", "Fotografía", Icons.Rounded.CameraAlt, Color(0xFF52525B)),
        TradeIcon("transporte", "Transporte", Icons.Rounded.LocalShipping, Color(0xFF0C4A6E)),
        TradeIcon("formacion", "Formación", Icons.Rounded.School, Color(0xFF6D28D9)),
        TradeIcon("deporte", "Deporte", Icons.Rounded.FitnessCenter, Color(0xFFEA580C)),
        TradeIcon("inmobiliaria", "Inmobiliaria", Icons.Rounded.HomeWork, Color(0xFF065F46)),
        TradeIcon("comercio", "Comercio", Icons.Rounded.Store, Color(0xFFA16207)),
        TradeIcon("diseno", "Diseño", Icons.Rounded.Palette, Color(0xFF9333EA)),
    )

    private val byId = all.associateBy { it.id }

    fun find(id: String?): TradeIcon? = id?.let(byId::get)
}
