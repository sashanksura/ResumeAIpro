package com.resumeai.pro.data.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import com.resumeai.pro.domain.model.Resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Professional DOCX exporter using WordprocessingML XML.
 * Includes proper styles.xml, numbering.xml, fontTable.xml, and settings.xml
 * for consistent rendering across Microsoft Word, Google Docs, and LibreOffice.
 * Supports profile photo embedding and progress reporting.
 */
object DocxExporter {

    suspend fun export(
        context: Context,
        resume: Resume,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.05f, "Analyzing Resume...")
            delay(600)

            val fileName = "${resume.personalInfo.fullName.ifEmpty { "Resume" }.replace(" ", "_")}_Resume.docx"
            val accentHex = resume.accentColor.removePrefix("#").ifEmpty { "6C3DE8" }

            val outputStream: OutputStream
            val resultFile: File

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw Exception("Failed to create file in Downloads")
                outputStream = context.contentResolver.openOutputStream(uri)
                    ?: throw Exception("Failed to open output stream")
                resultFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                resultFile = File(downloadsDir, fileName)
                outputStream = FileOutputStream(resultFile)
            }

            onProgress(0.20f, "Enhancing Content...")
            delay(600)

            // Load profile photo if available
            val photoData = loadPhotoAsBase64(context, resume.personalInfo.photoUri)
            val hasPhoto = photoData != null

            val zipOut = java.util.zip.ZipOutputStream(outputStream)

            // [Content_Types].xml
            zipOut.putNextEntry(java.util.zip.ZipEntry("[Content_Types].xml"))
            zipOut.write(contentTypesXml(hasPhoto).toByteArray())
            zipOut.closeEntry()

            // _rels/.rels
            zipOut.putNextEntry(java.util.zip.ZipEntry("_rels/.rels"))
            zipOut.write(relsXml().toByteArray())
            zipOut.closeEntry()

            // word/_rels/document.xml.rels
            zipOut.putNextEntry(java.util.zip.ZipEntry("word/_rels/document.xml.rels"))
            zipOut.write(documentRelsXml(hasPhoto).toByteArray())
            zipOut.closeEntry()

            onProgress(0.35f, "Generating styles...")
            delay(600)

            // word/styles.xml
            zipOut.putNextEntry(java.util.zip.ZipEntry("word/styles.xml"))
            zipOut.write(stylesXml(accentHex).toByteArray())
            zipOut.closeEntry()

            // word/numbering.xml
            zipOut.putNextEntry(java.util.zip.ZipEntry("word/numbering.xml"))
            zipOut.write(numberingXml().toByteArray())
            zipOut.closeEntry()

            // word/fontTable.xml
            zipOut.putNextEntry(java.util.zip.ZipEntry("word/fontTable.xml"))
            zipOut.write(fontTableXml().toByteArray())
            zipOut.closeEntry()

            // word/settings.xml
            zipOut.putNextEntry(java.util.zip.ZipEntry("word/settings.xml"))
            zipOut.write(settingsXml().toByteArray())
            zipOut.closeEntry()

            onProgress(0.50f, "Building DOCX...")
            delay(600)

            // Embed profile photo if available
            if (hasPhoto && photoData != null) {
                zipOut.putNextEntry(java.util.zip.ZipEntry("word/media/image1.png"))
                zipOut.write(Base64.decode(photoData, Base64.DEFAULT))
                zipOut.closeEntry()
            }

            onProgress(0.65f, "Building DOCX...")
            delay(600)

            // word/document.xml
            zipOut.putNextEntry(java.util.zip.ZipEntry("word/document.xml"))
            zipOut.write(buildDocumentXml(resume, accentHex, hasPhoto).toByteArray())
            zipOut.closeEntry()

            onProgress(0.85f, "Finalizing Resume...")
            delay(600)

            zipOut.close()
            outputStream.close()

            onProgress(1.0f, "Export complete!")
            delay(400)

            Result.success(resultFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun loadPhotoAsBase64(context: Context, photoUri: String?): String? {
        if (photoUri.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(photoUri)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            // Decode with subsampling for memory efficiency
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            var inSampleSize = 1
            val maxDim = 200
            if (options.outHeight > maxDim || options.outWidth > maxDim) {
                val halfH = options.outHeight / 2
                val halfW = options.outWidth / 2
                while (halfH / inSampleSize >= maxDim && halfW / inSampleSize >= maxDim) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            val stream2 = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(stream2, null, decodeOptions) ?: run { stream2.close(); return null }
            stream2.close()

            val scaled = Bitmap.createScaledBitmap(bitmap, 150, 150, true)
            if (scaled != bitmap) bitmap.recycle()

            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.PNG, 90, baos)
            scaled.recycle()

            Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
        } catch (_: Exception) {
            null
        }
    }

    private fun contentTypesXml(hasPhoto: Boolean): String {
        val imagePart = if (hasPhoto) """
  <Default Extension="png" ContentType="image/png"/>""" else ""
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>$imagePart
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
  <Override PartName="/word/numbering.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"/>
  <Override PartName="/word/fontTable.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.fontTable+xml"/>
  <Override PartName="/word/settings.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml"/>
</Types>"""
    }

    private fun relsXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

    private fun documentRelsXml(hasPhoto: Boolean): String {
        val imageRel = if (hasPhoto) """
  <Relationship Id="rId5" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/image1.png"/>""" else ""
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering" Target="numbering.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/fontTable" Target="fontTable.xml"/>
  <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings" Target="settings.xml"/>$imageRel
</Relationships>"""
    }

    private fun stylesXml(accentHex: String): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault>
      <w:rPr>
        <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:cs="Calibri"/>
        <w:sz w:val="22"/>
        <w:szCs w:val="22"/>
        <w:lang w:val="en-US"/>
      </w:rPr>
    </w:rPrDefault>
    <w:pPrDefault>
      <w:pPr>
        <w:spacing w:after="0" w:line="276" w:lineRule="auto"/>
      </w:pPr>
    </w:pPrDefault>
  </w:docDefaults>
  <w:style w:type="paragraph" w:styleId="Normal" w:default="1">
    <w:name w:val="Normal"/>
    <w:pPr><w:spacing w:after="60" w:line="276" w:lineRule="auto"/></w:pPr>
    <w:rPr><w:sz w:val="22"/><w:szCs w:val="22"/><w:color w:val="333333"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Title">
    <w:name w:val="Title"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr><w:spacing w:after="40"/><w:jc w:val="left"/></w:pPr>
    <w:rPr><w:b/><w:sz w:val="56"/><w:szCs w:val="56"/><w:color w:val="1A1A2E"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="Subtitle">
    <w:name w:val="Subtitle"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr><w:spacing w:after="40"/></w:pPr>
    <w:rPr><w:sz w:val="28"/><w:szCs w:val="28"/><w:color w:val="$accentHex"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="SectionHeader">
    <w:name w:val="Section Header"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:spacing w:before="200" w:after="80"/>
      <w:pBdr><w:bottom w:val="single" w:sz="6" w:space="2" w:color="$accentHex"/></w:pBdr>
    </w:pPr>
    <w:rPr><w:b/><w:sz w:val="24"/><w:szCs w:val="24"/><w:color w:val="1A1A2E"/><w:caps/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="ContactInfo">
    <w:name w:val="Contact Info"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr><w:spacing w:after="20"/></w:pPr>
    <w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/><w:color w:val="666666"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="JobTitle">
    <w:name w:val="Job Title"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr><w:spacing w:before="80" w:after="20"/></w:pPr>
    <w:rPr><w:b/><w:sz w:val="24"/><w:szCs w:val="24"/><w:color w:val="1A1A2E"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="CompanyDate">
    <w:name w:val="Company Date"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr><w:spacing w:after="40"/></w:pPr>
    <w:rPr><w:sz w:val="20"/><w:szCs w:val="20"/><w:color w:val="666666"/></w:rPr>
  </w:style>
  <w:style w:type="paragraph" w:styleId="BulletList">
    <w:name w:val="Bullet List"/>
    <w:basedOn w:val="Normal"/>
    <w:pPr>
      <w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr>
      <w:spacing w:after="40"/>
      <w:ind w:left="720" w:hanging="360"/>
    </w:pPr>
    <w:rPr><w:sz w:val="22"/><w:szCs w:val="22"/><w:color w:val="333333"/></w:rPr>
  </w:style>
</w:styles>"""

    private fun numberingXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:numbering xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:abstractNum w:abstractNumId="0">
    <w:lvl w:ilvl="0">
      <w:start w:val="1"/>
      <w:numFmt w:val="bullet"/>
      <w:lvlText w:val="•"/>
      <w:lvlJc w:val="left"/>
      <w:pPr><w:ind w:left="720" w:hanging="360"/></w:pPr>
      <w:rPr><w:rFonts w:ascii="Symbol" w:hAnsi="Symbol" w:hint="default"/></w:rPr>
    </w:lvl>
  </w:abstractNum>
  <w:num w:numId="1">
    <w:abstractNumId w:val="0"/>
  </w:num>
</w:numbering>"""

    private fun fontTableXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:fonts xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:font w:name="Calibri">
    <w:panose1 w:val="020F0502020204030204"/>
    <w:charset w:val="00"/>
    <w:family w:val="swiss"/>
    <w:pitch w:val="variable"/>
  </w:font>
  <w:font w:name="Symbol">
    <w:panose1 w:val="05050102010706020507"/>
    <w:charset w:val="02"/>
    <w:family w:val="roman"/>
    <w:pitch w:val="variable"/>
  </w:font>
  <w:font w:name="Times New Roman">
    <w:panose1 w:val="02020603050405020304"/>
    <w:charset w:val="00"/>
    <w:family w:val="roman"/>
    <w:pitch w:val="variable"/>
  </w:font>
</w:fonts>"""

    private fun settingsXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:settings xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:defaultTabStop w:val="720"/>
  <w:characterSpacingControl w:val="doNotCompress"/>
  <w:compat>
    <w:compatSetting w:name="compatibilityMode" w:uri="http://schemas.microsoft.com/office/word" w:val="15"/>
  </w:compat>
</w:settings>"""

    @Suppress("UNUSED_PARAMETER")
    private fun buildDocumentXml(resume: Resume, accentHex: String, hasPhoto: Boolean): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
            xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
            xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
            xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
<w:body>
""")

        // === PROFILE PHOTO (inline in document) ===
        if (hasPhoto) {
            sb.append("""<w:p><w:pPr><w:spacing w:after="80"/></w:pPr><w:r><w:drawing>
<wp:inline distT="0" distB="0" distL="0" distR="0">
  <wp:extent cx="1143000" cy="1143000"/>
  <wp:docPr id="1" name="Profile Photo"/>
  <a:graphic><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
    <pic:pic>
      <pic:nvPicPr><pic:cNvPr id="1" name="photo.png"/><pic:cNvPicPr/></pic:nvPicPr>
      <pic:blipFill><a:blip r:embed="rId5"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>
      <pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="1143000" cy="1143000"/></a:xfrm>
        <a:prstGeom prst="ellipse"><a:avLst/></a:prstGeom></pic:spPr>
    </pic:pic>
  </a:graphicData></a:graphic>
</wp:inline>
</w:drawing></w:r></w:p>
""")
        }

        // === NAME ===
        sb.append(styledParagraph(esc(resume.personalInfo.fullName.ifEmpty { "Your Name" }), "Title"))

        // === JOB TITLE ===
        if (resume.personalInfo.jobTitle.isNotEmpty()) {
            sb.append(styledParagraph(esc(resume.personalInfo.jobTitle), "Subtitle"))
        }

        // === CONTACT INFO ===
        val contactParts = listOfNotNull(
            resume.personalInfo.email.takeIf { it.isNotEmpty() },
            resume.personalInfo.phone.takeIf { it.isNotEmpty() },
            resume.personalInfo.location.takeIf { it.isNotEmpty() }
        )
        if (contactParts.isNotEmpty()) {
            sb.append(styledParagraph(esc(contactParts.joinToString("  •  ")), "ContactInfo"))
        }

        val links = listOfNotNull(
            resume.personalInfo.linkedinUrl.takeIf { it.isNotEmpty() },
            resume.personalInfo.githubUrl.takeIf { it.isNotEmpty() },
            resume.personalInfo.portfolioUrl.takeIf { it.isNotEmpty() }
        )
        if (links.isNotEmpty()) {
            sb.append(styledParagraph(esc(links.joinToString("  |  ")), "ContactInfo"))
        }

        // === PROFESSIONAL SUMMARY ===
        if (resume.summary.isNotBlank()) {
            sb.append(styledParagraph("PROFESSIONAL SUMMARY", "SectionHeader"))
            sb.append(styledParagraph(esc(resume.summary), "Normal"))
        }

        // === WORK EXPERIENCE ===
        if (resume.experiences.isNotEmpty()) {
            sb.append(styledParagraph("WORK EXPERIENCE", "SectionHeader"))
            resume.experiences.forEach { exp ->
                if (exp.jobTitle.isNotEmpty() || exp.company.isNotEmpty()) {
                    sb.append(styledParagraph(esc(exp.jobTitle), "JobTitle"))
                    val dateLine = buildString {
                        append(esc(exp.company))
                        if (exp.startDate.isNotEmpty()) append("  |  ${esc(exp.startDate)} – ${esc(exp.endDate)}")
                    }
                    sb.append(styledParagraph(dateLine, "CompanyDate"))
                    if (exp.description.isNotEmpty()) {
                        val lines = exp.description.split("\n").filter { it.isNotBlank() }
                        lines.forEach { line ->
                            val cleanLine = line.trim().removePrefix("•").removePrefix("-").removePrefix("▸").trim()
                            if (cleanLine.isNotEmpty()) {
                                sb.append(styledParagraph(esc(cleanLine), "BulletList"))
                            }
                        }
                    }
                }
            }
        }

        // === EDUCATION ===
        if (resume.educations.isNotEmpty()) {
            sb.append(styledParagraph("EDUCATION", "SectionHeader"))
            resume.educations.forEach { edu ->
                val title = buildString {
                    if (edu.degree.isNotEmpty()) append(edu.degree)
                    if (edu.fieldOfStudy.isNotEmpty()) {
                        if (isNotEmpty()) append(" in ")
                        append(edu.fieldOfStudy)
                    }
                }
                if (title.isNotEmpty()) {
                    sb.append(styledParagraph(esc(title), "JobTitle"))
                }
                val eduInfo = buildString {
                    append(esc(edu.institution))
                    if (edu.startYear.isNotEmpty()) append("  |  ${esc(edu.startYear)} – ${esc(edu.endYear)}")
                    if (edu.gpa.isNotEmpty()) append("  |  GPA: ${esc(edu.gpa)}")
                }
                if (eduInfo.isNotEmpty()) {
                    sb.append(styledParagraph(eduInfo, "CompanyDate"))
                }
            }
        }

        // === PROJECTS ===
        if (resume.projects.isNotEmpty()) {
            sb.append(styledParagraph("PROJECTS", "SectionHeader"))
            resume.projects.forEach { proj ->
                val title = buildString {
                    append(esc(proj.name))
                    if (proj.link.isNotEmpty()) append("  |  ${esc(proj.link)}")
                }
                if (title.isNotEmpty()) {
                    sb.append(styledParagraph(title, "JobTitle"))
                }
                if (proj.technologies.isNotEmpty()) {
                    sb.append(styledParagraph(esc("Technologies: ${proj.technologies}"), "CompanyDate"))
                }
                if (proj.description.isNotEmpty()) {
                    val lines = proj.description.split("\n").filter { it.isNotBlank() }
                    lines.forEach { line ->
                        val cleanLine = line.trim().removePrefix("•").removePrefix("-").removePrefix("▸").trim()
                        if (cleanLine.isNotEmpty()) {
                            sb.append(styledParagraph(esc(cleanLine), "BulletList"))
                        }
                    }
                }
            }
        }

        // === SKILLS ===
        if (resume.skills.isNotEmpty()) {
            sb.append(styledParagraph("SKILLS", "SectionHeader"))
            sb.append(styledParagraph(esc(resume.skills.joinToString("  •  ") { it.name }), "Normal"))
        }

        // Page settings (A4, margins)
        sb.append("""
<w:sectPr>
  <w:pgSz w:w="11906" w:h="16838"/>
  <w:pgMar w:top="1440" w:right="1080" w:bottom="1440" w:left="1080" w:header="720" w:footer="720" w:gutter="0"/>
  <w:cols w:space="720"/>
</w:sectPr>
</w:body>
</w:document>""")

        return sb.toString()
    }

    private fun styledParagraph(text: String, styleId: String): String {
        return """<w:p><w:pPr><w:pStyle w:val="$styleId"/></w:pPr><w:r><w:t xml:space="preserve">$text</w:t></w:r></w:p>
"""
    }

    private fun esc(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
