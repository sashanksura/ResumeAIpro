package com.resumeai.pro.data.export

/**
 * Template configuration engine for resume styling.
 * Provides 10 distinct resume templates with different fonts, colors, and layouts.
 */

enum class SectionStyle { UNDERLINED, BOXED, MINIMAL, ACCENT_BAR }
enum class LayoutStyle { SINGLE_COLUMN, TWO_COLUMN }
enum class BulletStyle { BULLET, DASH, ARROW, NONE }
enum class HeaderAlignment { LEFT, CENTER }

data class TemplateConfig(
    val id: String,
    val name: String,
    val description: String,
    val headerFont: String,
    val bodyFont: String,
    val headerSizePt: Int,
    val bodySizePt: Int,
    val accentColor: String,
    val sectionStyle: SectionStyle,
    val layout: LayoutStyle,
    val headerAlignment: HeaderAlignment,
    val showSkillBars: Boolean,
    val bulletStyle: BulletStyle,
    val gradientColors: List<String> = emptyList()
)

object TemplateEngine {

    private val templates = listOf(
        TemplateConfig(
            id = "ats_modern",
            name = "ATS Modern",
            description = "Clean & ATS-optimized",
            headerFont = "Calibri",
            bodyFont = "Calibri",
            headerSizePt = 24,
            bodySizePt = 11,
            accentColor = "#2563EB",
            sectionStyle = SectionStyle.UNDERLINED,
            layout = LayoutStyle.SINGLE_COLUMN,
            headerAlignment = HeaderAlignment.LEFT,
            showSkillBars = false,
            bulletStyle = BulletStyle.BULLET,
            gradientColors = listOf("#2563EB", "#1E40AF")
        ),
        TemplateConfig(
            id = "executive",
            name = "Executive",
            description = "Elegant & authoritative",
            headerFont = "Georgia",
            bodyFont = "Georgia",
            headerSizePt = 26,
            bodySizePt = 11,
            accentColor = "#1B1B3A",
            sectionStyle = SectionStyle.ACCENT_BAR,
            layout = LayoutStyle.SINGLE_COLUMN,
            headerAlignment = HeaderAlignment.CENTER,
            showSkillBars = false,
            bulletStyle = BulletStyle.DASH,
            gradientColors = listOf("#1B1B3A", "#2D2D5F")
        ),
        TemplateConfig(
            id = "corporate",
            name = "Corporate",
            description = "Professional & structured",
            headerFont = "Arial",
            bodyFont = "Arial",
            headerSizePt = 24,
            bodySizePt = 11,
            accentColor = "#0F4C81",
            sectionStyle = SectionStyle.UNDERLINED,
            layout = LayoutStyle.SINGLE_COLUMN,
            headerAlignment = HeaderAlignment.LEFT,
            showSkillBars = false,
            bulletStyle = BulletStyle.BULLET,
            gradientColors = listOf("#0F4C81", "#1A6FB5")
        ),
        TemplateConfig(
            id = "minimal",
            name = "Minimal",
            description = "Simple & clean",
            headerFont = "Helvetica",
            bodyFont = "Helvetica",
            headerSizePt = 22,
            bodySizePt = 10,
            accentColor = "#6B7280",
            sectionStyle = SectionStyle.MINIMAL,
            layout = LayoutStyle.SINGLE_COLUMN,
            headerAlignment = HeaderAlignment.LEFT,
            showSkillBars = false,
            bulletStyle = BulletStyle.BULLET,
            gradientColors = listOf("#6B7280", "#9CA3AF")
        ),
        TemplateConfig(
            id = "creative",
            name = "Creative",
            description = "Bold & expressive",
            headerFont = "Poppins",
            bodyFont = "Poppins",
            headerSizePt = 26,
            bodySizePt = 11,
            accentColor = "#E11D48",
            sectionStyle = SectionStyle.ACCENT_BAR,
            layout = LayoutStyle.SINGLE_COLUMN,
            headerAlignment = HeaderAlignment.CENTER,
            showSkillBars = true,
            bulletStyle = BulletStyle.ARROW,
            gradientColors = listOf("#E11D48", "#F43F5E")
        ),
        TemplateConfig(
            id = "tech",
            name = "Tech",
            description = "Modern & technical",
            headerFont = "Courier New",
            bodyFont = "Calibri",
            headerSizePt = 24,
            bodySizePt = 11,
            accentColor = "#10B981",
            sectionStyle = SectionStyle.BOXED,
            layout = LayoutStyle.SINGLE_COLUMN,
            headerAlignment = HeaderAlignment.LEFT,
            showSkillBars = true,
            bulletStyle = BulletStyle.DASH,
            gradientColors = listOf("#10B981", "#059669")
        ),
        TemplateConfig(
            id = "fresher",
            name = "Fresher",
            description = "Fresh graduate friendly",
            headerFont = "Calibri",
            bodyFont = "Calibri",
            headerSizePt = 24,
            bodySizePt = 11,
            accentColor = "#8B5CF6",
            sectionStyle = SectionStyle.UNDERLINED,
            layout = LayoutStyle.SINGLE_COLUMN,
            headerAlignment = HeaderAlignment.LEFT,
            showSkillBars = true,
            bulletStyle = BulletStyle.BULLET,
            gradientColors = listOf("#8B5CF6", "#7C3AED")
        ),
        TemplateConfig(
            id = "dark_pro",
            name = "Dark Pro",
            description = "Bold dark header style",
            headerFont = "Roboto",
            bodyFont = "Roboto",
            headerSizePt = 26,
            bodySizePt = 11,
            accentColor = "#F59E0B",
            sectionStyle = SectionStyle.ACCENT_BAR,
            layout = LayoutStyle.SINGLE_COLUMN,
            headerAlignment = HeaderAlignment.CENTER,
            showSkillBars = false,
            bulletStyle = BulletStyle.BULLET,
            gradientColors = listOf("#F59E0B", "#D97706")
        ),
        TemplateConfig(
            id = "anti_gravity",
            name = "Anti-Gravity",
            description = "Cosmic purple & cyan",
            headerFont = "Sans Serif",
            bodyFont = "Sans Serif",
            headerSizePt = 26,
            bodySizePt = 11,
            accentColor = "#6C3DE8",
            sectionStyle = SectionStyle.ACCENT_BAR,
            layout = LayoutStyle.SINGLE_COLUMN,
            headerAlignment = HeaderAlignment.CENTER,
            showSkillBars = true,
            bulletStyle = BulletStyle.BULLET,
            gradientColors = listOf("#6C3DE8", "#00F5FF")
        ),
        TemplateConfig(
            id = "classic",
            name = "Classic",
            description = "Traditional & conservative",
            headerFont = "Times New Roman",
            bodyFont = "Times New Roman",
            headerSizePt = 24,
            bodySizePt = 12,
            accentColor = "#1A1A1A",
            sectionStyle = SectionStyle.UNDERLINED,
            layout = LayoutStyle.SINGLE_COLUMN,
            headerAlignment = HeaderAlignment.CENTER,
            showSkillBars = false,
            bulletStyle = BulletStyle.BULLET,
            gradientColors = listOf("#1A1A1A", "#374151")
        )
    )

    fun getTemplate(id: String): TemplateConfig {
        return templates.find { it.id == id } ?: templates.first()
    }

    fun getAllTemplates(): List<TemplateConfig> = templates
}
