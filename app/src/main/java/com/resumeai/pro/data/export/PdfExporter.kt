package com.resumeai.pro.data.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.resumeai.pro.domain.model.Resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Professional PDF exporter with multi-page support and profile photo.
 * Uses Android's PdfDocument API with proper text wrapping,
 * bullet indentation, section headers, and page numbering.
 */
object PdfExporter {

    private const val PAGE_WIDTH = 595   // A4 width in points
    private const val PAGE_HEIGHT = 842  // A4 height in points
    private const val MARGIN_LEFT = 50f
    private const val MARGIN_RIGHT = 50f
    private const val MARGIN_TOP = 50f
    private const val MARGIN_BOTTOM = 60f
    private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT
    @Suppress("unused")
    private const val FOOTER_Y = PAGE_HEIGHT - 30f
    private const val LINE_HEIGHT = 15f
    private const val SECTION_GAP = 20f
    private const val BULLET_INDENT = 18f

    suspend fun export(
        context: Context,
        resume: Resume,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.05f, "Analyzing Resume...")
            delay(600)

            val fileName = "${resume.personalInfo.fullName.ifEmpty { "Resume" }.replace(" ", "_")}_Resume.pdf"
            val accentColorInt = try {
                Color.parseColor(resume.accentColor.ifEmpty { "#6C3DE8" })
            } catch (e: Exception) {
                Color.parseColor("#6C3DE8")
            }

            val document = PdfDocument()
            val pages = mutableListOf<PdfDocument.Page>()
            var pageNum = 1
            var currentPage = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
            )
            pages.add(currentPage)
            var canvas = currentPage.canvas
            var y = MARGIN_TOP

            onProgress(0.15f, "Setting up document layout...")
            delay(600)

            // === PAINTS ===
            val titlePaint = Paint().apply {
                color = Color.parseColor("#1A1A2E")
                textSize = 24f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            val subtitlePaint = Paint().apply {
                color = accentColorInt
                textSize = 14f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }
            val contactPaint = Paint().apply {
                color = Color.parseColor("#666666")
                textSize = 10f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }
            val sectionHeaderPaint = Paint().apply {
                color = Color.parseColor("#1A1A2E")
                textSize = 13f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            val bodyPaint = Paint().apply {
                color = Color.parseColor("#333333")
                textSize = 11f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }
            val boldBodyPaint = Paint().apply {
                color = Color.parseColor("#1A1A2E")
                textSize = 12f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            val datePaint = Paint().apply {
                color = Color.parseColor("#666666")
                textSize = 10f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }
            val accentLinePaint = Paint().apply {
                color = accentColorInt
                strokeWidth = 2f
                isAntiAlias = true
            }
            val bulletPaint = Paint().apply {
                color = Color.parseColor("#333333")
                textSize = 11f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                isAntiAlias = true
            }

            // Helper: check if we need a new page
            fun needsNewPage(requiredSpace: Float): Boolean {
                return y + requiredSpace > PAGE_HEIGHT - MARGIN_BOTTOM
            }

            // Helper: start a new page
            fun startNewPage() {
                document.finishPage(currentPage)
                pageNum++
                currentPage = document.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
                )
                pages.add(currentPage)
                canvas = currentPage.canvas
                y = MARGIN_TOP
            }

            // Helper: draw wrapped text, returns new Y
            fun drawWrappedText(text: String, x: Float, startY: Float, maxWidth: Float, paint: Paint, lineSpacing: Float = LINE_HEIGHT): Float {
                var currentY = startY
                val lines = text.split("\n")
                for (line in lines) {
                    if (line.isBlank()) {
                        currentY += lineSpacing * 0.5f
                        continue
                    }
                    val words = line.split(" ")
                    var currentLine = ""
                    for (word in words) {
                        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                        if (paint.measureText(testLine) <= maxWidth) {
                            currentLine = testLine
                        } else {
                            if (needsNewPage(lineSpacing)) startNewPage().also { currentY = y }
                            canvas.drawText(currentLine, x, currentY, paint)
                            currentY += lineSpacing
                            currentLine = word
                        }
                    }
                    if (currentLine.isNotEmpty()) {
                        if (needsNewPage(lineSpacing)) startNewPage().also { currentY = y }
                        canvas.drawText(currentLine, x, currentY, paint)
                        currentY += lineSpacing
                    }
                }
                return currentY
            }

            // Helper: draw section header with underline
            fun drawSectionHeader(title: String) {
                if (needsNewPage(40f)) startNewPage()
                y += SECTION_GAP * 0.5f
                canvas.drawText(title, MARGIN_LEFT, y, sectionHeaderPaint)
                y += 5f
                canvas.drawLine(MARGIN_LEFT, y, MARGIN_LEFT + CONTENT_WIDTH, y, accentLinePaint)
                y += 12f
            }

            // Helper: draw bullet point
            fun drawBulletPoint(text: String, x: Float): Float {
                val textX = x + BULLET_INDENT
                val maxW = CONTENT_WIDTH - BULLET_INDENT - (x - MARGIN_LEFT)
                if (needsNewPage(LINE_HEIGHT * 2)) startNewPage()
                canvas.drawText("•", x, y, bulletPaint)
                y = drawWrappedText(text, textX, y, maxW, bodyPaint)
                return y
            }

            onProgress(0.30f, "Enhancing Content...")
            delay(600)

            // ==========================================
            // RENDER RESUME
            // ==========================================

            // === PROFILE PHOTO ===
            var headerTextStartX = MARGIN_LEFT
            val photoUri = resume.personalInfo.photoUri
            if (!photoUri.isNullOrBlank()) {
                try {
                    val photoBitmap = loadAndScaleBitmap(context, photoUri, 80, 80)
                    if (photoBitmap != null) {
                        val photoX = MARGIN_LEFT
                        val photoY = y - 10f
                        // Draw circular photo with clipping
                        canvas.save()
                        val photoPath = android.graphics.Path()
                        photoPath.addCircle(
                            photoX + 40f, photoY + 40f, 40f,
                            android.graphics.Path.Direction.CW
                        )
                        canvas.clipPath(photoPath)
                        canvas.drawBitmap(photoBitmap, photoX, photoY, null)
                        canvas.restore()
                        // Draw circle border
                        val borderPaint = Paint().apply {
                            color = accentColorInt
                            style = Paint.Style.STROKE
                            strokeWidth = 2f
                            isAntiAlias = true
                        }
                        canvas.drawCircle(photoX + 40f, photoY + 40f, 40f, borderPaint)
                        headerTextStartX = MARGIN_LEFT + 95f
                        photoBitmap.recycle()
                    }
                } catch (_: Exception) {
                    // Fallback: no photo, use default layout
                }
            }

            // === HEADER: Name ===
            canvas.drawText(
                resume.personalInfo.fullName.ifEmpty { "Your Name" },
                headerTextStartX, y, titlePaint
            )
            y += 28f

            // === Job Title ===
            if (resume.personalInfo.jobTitle.isNotEmpty()) {
                canvas.drawText(resume.personalInfo.jobTitle, headerTextStartX, y, subtitlePaint)
                y += 18f
            }

            // === Contact Info ===
            val contactParts = listOfNotNull(
                resume.personalInfo.email.takeIf { it.isNotEmpty() },
                resume.personalInfo.phone.takeIf { it.isNotEmpty() },
                resume.personalInfo.location.takeIf { it.isNotEmpty() }
            )
            if (contactParts.isNotEmpty()) {
                canvas.drawText(contactParts.joinToString("  •  "), headerTextStartX, y, contactPaint)
                y += 14f
            }

            // === Links ===
            val links = listOfNotNull(
                resume.personalInfo.linkedinUrl.takeIf { it.isNotEmpty() },
                resume.personalInfo.githubUrl.takeIf { it.isNotEmpty() },
                resume.personalInfo.portfolioUrl.takeIf { it.isNotEmpty() }
            )
            if (links.isNotEmpty()) {
                canvas.drawText(links.joinToString("  |  "), headerTextStartX, y, contactPaint)
                y += 14f
            }

            // If we had a photo, make sure y is below the photo area
            if (headerTextStartX > MARGIN_LEFT) {
                y = maxOf(y, MARGIN_TOP + 85f)
            }

            // Accent line under header
            y += 4f
            canvas.drawLine(MARGIN_LEFT, y, MARGIN_LEFT + CONTENT_WIDTH, y, accentLinePaint)
            y += 12f

            onProgress(0.45f, "Generating PDF...")
            delay(600)

            // === PROFESSIONAL SUMMARY ===
            if (resume.summary.isNotBlank()) {
                drawSectionHeader("PROFESSIONAL SUMMARY")
                y = drawWrappedText(resume.summary, MARGIN_LEFT, y, CONTENT_WIDTH, bodyPaint)
                y += 4f
            }

            onProgress(0.55f, "Generating PDF...")
            delay(600)

            // === WORK EXPERIENCE ===
            if (resume.experiences.isNotEmpty()) {
                drawSectionHeader("WORK EXPERIENCE")
                resume.experiences.forEach { exp ->
                    if (needsNewPage(50f)) startNewPage()

                    if (exp.jobTitle.isNotEmpty()) {
                        canvas.drawText(exp.jobTitle, MARGIN_LEFT, y, boldBodyPaint)
                        y += 15f
                    }
                    val companyLine = buildString {
                        append(exp.company)
                        if (exp.startDate.isNotEmpty()) append("  |  ${exp.startDate} – ${exp.endDate}")
                    }
                    if (companyLine.isNotEmpty()) {
                        canvas.drawText(companyLine, MARGIN_LEFT, y, datePaint)
                        y += 14f
                    }

                    if (exp.description.isNotEmpty()) {
                        val descLines = exp.description.split("\n").filter { it.isNotBlank() }
                        descLines.forEach { line ->
                            val cleanLine = line.trim().removePrefix("•").removePrefix("-").removePrefix("▸").trim()
                            if (cleanLine.isNotEmpty()) {
                                drawBulletPoint(cleanLine, MARGIN_LEFT)
                            }
                        }
                    }
                    y += 8f
                }
            }

            onProgress(0.70f, "Building PDF sections...")
            delay(600)

            // === EDUCATION ===
            if (resume.educations.isNotEmpty()) {
                drawSectionHeader("EDUCATION")
                resume.educations.forEach { edu ->
                    if (needsNewPage(40f)) startNewPage()

                    val title = buildString {
                        if (edu.degree.isNotEmpty()) append(edu.degree)
                        if (edu.fieldOfStudy.isNotEmpty()) {
                            if (isNotEmpty()) append(" in ")
                            append(edu.fieldOfStudy)
                        }
                    }
                    if (title.isNotEmpty()) {
                        canvas.drawText(title, MARGIN_LEFT, y, boldBodyPaint)
                        y += 15f
                    }
                    val eduInfo = buildString {
                        append(edu.institution)
                        if (edu.startYear.isNotEmpty()) append("  |  ${edu.startYear} – ${edu.endYear}")
                        if (edu.gpa.isNotEmpty()) append("  |  GPA: ${edu.gpa}")
                    }
                    if (eduInfo.isNotEmpty()) {
                        canvas.drawText(eduInfo, MARGIN_LEFT, y, datePaint)
                        y += 18f
                    }
                }
            }

            // === PROJECTS ===
            if (resume.projects.isNotEmpty()) {
                drawSectionHeader("PROJECTS")
                resume.projects.forEach { proj ->
                    if (needsNewPage(50f)) startNewPage()

                    if (proj.name.isNotEmpty()) {
                        canvas.drawText(proj.name, MARGIN_LEFT, y, boldBodyPaint)
                        val titleWidth = boldBodyPaint.measureText(proj.name)
                        if (proj.link.isNotEmpty()) {
                            val linkPaint = Paint(datePaint).apply { color = accentColorInt }
                            canvas.drawText("  |  ${proj.link}", MARGIN_LEFT + titleWidth, y, linkPaint)
                        }
                        y += 15f
                    }
                    if (proj.technologies.isNotEmpty()) {
                        canvas.drawText("Technologies: ${proj.technologies}", MARGIN_LEFT, y, datePaint)
                        y += 14f
                    }
                    if (proj.description.isNotEmpty()) {
                        val descLines = proj.description.split("\n").filter { it.isNotBlank() }
                        descLines.forEach { line ->
                            val cleanLine = line.trim().removePrefix("•").removePrefix("-").removePrefix("▸").trim()
                            if (cleanLine.isNotEmpty()) {
                                drawBulletPoint(cleanLine, MARGIN_LEFT)
                            }
                        }
                    }
                    y += 8f
                }
            }

            // === SKILLS ===
            if (resume.skills.isNotEmpty()) {
                drawSectionHeader("SKILLS")
                val skillsText = resume.skills.joinToString("  •  ") { it.name }
                y = drawWrappedText(skillsText, MARGIN_LEFT, y, CONTENT_WIDTH, bodyPaint)
            }

            onProgress(0.85f, "Finalizing Resume...")
            delay(600)

            // Finish last page
            document.finishPage(currentPage)

            // Save file
            val outputStream: OutputStream
            val resultFile: File

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw Exception("Failed to create PDF file in Downloads")
                outputStream = context.contentResolver.openOutputStream(uri)
                    ?: throw Exception("Failed to open output stream for PDF")
                resultFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                resultFile = File(downloadsDir, fileName)
                outputStream = FileOutputStream(resultFile)
            }

            document.writeTo(outputStream)
            outputStream.close()
            document.close()

            onProgress(1.0f, "Export complete!")
            delay(400)

            Result.success(resultFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Loads a bitmap from a URI and scales it to the target dimensions.
     * Handles memory efficiently by subsampling large images.
     */
    private fun loadAndScaleBitmap(context: Context, uriString: String, targetWidth: Int, targetHeight: Int): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            // First pass: get dimensions only
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculate sample size for memory efficiency
            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
            options.inJustDecodeBounds = false

            // Second pass: decode with subsampling
            val scaledStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(scaledStream, null, options)
            scaledStream.close()

            // Scale to exact target size
            bitmap?.let {
                Bitmap.createScaledBitmap(it, targetWidth, targetHeight, true)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
