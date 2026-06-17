package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// ThemeHolder manages the dynamic theme selection (recomposes Compose reading values)
object ThemeHolder {
    var activeTheme by mutableStateOf("solo_leveling") // "solo_leveling", "dungeon_master", "heaven"
}

// Solo Leveling System UI Palette (Dynamic Multi-Theme System Matrix)

val SoloBlack: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0xFF030000) // Deep dark obsidian red tint
        "heaven", "heven" -> Color(0xFFE8ECEF) // Off-white/slate light card backing
        else -> Color(0xFF030303) // Dark Cyberpunk Carbon Black
    }

val SoloDarkGrey: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0xFF140707) // Deep red tinted system gray
        "heaven", "heven" -> Color(0xFFF3F5FA) // Divine light cream/sky white background
        else -> Color(0xFF0E0E12) // Tech Obsidian Grey background
    }

val SoloCardBg: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0xDF1E0E0E) // Crimson dark translucent core
        "heaven", "heven" -> Color(0xF5FFFFFF) // Solid divine elevated white surface
        else -> Color(0xC00A0B10) // Tech Translucent Obsidian Grey
    }

val SoloBlueAccent: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0xFFFF1A1A) // Dungeon Master Flame Red
        "heaven", "heven" -> Color(0xFFC59510) // Heaven Majestic Rich Gold Accent
        else -> Color(0xFF00F0FF) // Solo Leveling Signature Cyber Cyan Accent
    }

val SoloBlueDark: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0xFF8C0B0B) // Muted Demon Core Crimson
        "heaven", "heven" -> Color(0xFF8E6F1B) // Divine Antique Muted Gold
        else -> Color(0xFF005F66) // Muted Cyber Cyan
    }

val SoloPurpleAccent: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0xFFFF6F00) // Blazing Orange Secondary Accent
        "heaven", "heven" -> Color(0xFF3B82F6) // Celestial High-Sky Blue Secondary Accent
        else -> Color(0xFFFF0055) // Cyberpunk Hot Pink/Magenta
    }

val SoloGold: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0xFFFFCA00) // Blazing Ember Yellow
        "heaven", "heven" -> Color(0xFFD4AF37) // Golden Metallic
        else -> Color(0xFFFCEE09) // Cyberpunk Signature Neon Yellow
    }

val SoloTextPrimary: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0xFFFFF0F0) // Off-white Red Hint
        "heaven", "heven" -> Color(0xFF0F172A) // Slate Deep Dark Blue Text
        else -> Color(0xFFF0F5F9) // Ice White text
    }

val SoloTextSecondary: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0xFFAFA0A0) // Warn Slate Ash
        "heaven", "heven" -> Color(0xFF475569) // Slate Blue Dark Gray Secondary
        else -> Color(0xFF8A95A5) // Tech HUD Slate Grey
    }

val SoloGlowBlue: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0x4DFF1A1A) // Flame Red Glow
        "heaven", "heven" -> Color(0x2BD4AF37) // Cloud Gold Glow
        else -> Color(0x6600F0FF) // Cyber Cyan Glow
    }

val SoloGlowPurple: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0x4DFF6F00) // Orange Glow
        "heaven", "heven" -> Color(0x2B3B82F6) // Heavenly Blue Glow
        else -> Color(0x66FF0055) // Cyber Pink Glow
    }

val SoloSuccess: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0xFF00FFCC) // Shadow Cyan
        "heaven", "heven" -> Color(0xFF10B981) // Solid Emerald Green
        else -> Color(0xFF00FF88) // Cyberpunk Neon Green
    }

val SoloDanger: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0xFFFF0055) // Brutal Blood Red
        "heaven", "heven" -> Color(0xFFEF4444) // Divine Coral Red
        else -> Color(0xFFFF1A1A) // Cyberpunk Core Red
    }

val BentoBorderSky: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0x66FF1A1A) // Edge red highlights
        "heaven", "heven" -> Color(0x4DC59510) // Soft gold borders
        else -> Color(0x6600F0FF) // Blue cyberpunk wireframes
    }

val BentoBorderSlate: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0x4D2D1B1B) // Dark grid borders
        "heaven", "heven" -> Color(0xFFD8E1E9) // Light grey slate borders
        else -> Color(0x4D2A2E3D) // Muted HUD line
    }

val BentoRedBg: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0x4D3F0000) // Deep underworld red background
        "heaven", "heven" -> Color(0x1AFEE2E2) // Soft pastel red background
        else -> Color(0x403A0010) // Cyberpink dim fill
    }

val BentoRedBorder: Color
    get() = when (ThemeHolder.activeTheme) {
        "dungeon_master" -> Color(0x88FF1A1A)
        "heaven", "heven" -> Color(0x4DEF4444)
        else -> Color(0x88FF0055)
    }
