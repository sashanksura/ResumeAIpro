package com.resumeai.pro.data.mapper

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.resumeai.pro.data.local.ResumeEntity
import com.resumeai.pro.domain.model.*

/**
 * Maps between ResumeEntity (Room database) and Resume (domain model).
 * Handles migration from old 'stateDate' field to new 'startDate' field
 * in Experience JSON during deserialization.
 */
object ResumeMapper {
    private val gson = Gson()

    fun toEntity(resume: Resume): ResumeEntity {
        return ResumeEntity(
            id = resume.id,
            name = resume.name,
            templateId = resume.templateId,
            personalInfoJson = gson.toJson(resume.personalInfo),
            experiencesJson = gson.toJson(resume.experiences),
            educationsJson = gson.toJson(resume.educations),
            skillsJson = gson.toJson(resume.skills),
            projectsJson = gson.toJson(resume.projects),
            summary = resume.summary,
            jobDescription = resume.jobDescription,
            jobDescriptionUrl = resume.jobDescriptionUrl,
            accentColor = resume.accentColor,
            fontPair = resume.fontPair,
            completionPercent = resume.completionPercent,
            createdAt = resume.createdAt,
            updatedAt = resume.updatedAt
        )
    }

    fun toDomain(entity: ResumeEntity): Resume {
        return Resume(
            id = entity.id,
            name = entity.name,
            templateId = entity.templateId,
            personalInfo = try {
                gson.fromJson(entity.personalInfoJson, PersonalInfo::class.java) ?: PersonalInfo()
            } catch (e: Exception) {
                PersonalInfo()
            },
            experiences = parseExperiences(entity.experiencesJson),
            educations = parseList(entity.educationsJson),
            skills = parseList(entity.skillsJson),
            projects = parseList(entity.projectsJson),
            summary = entity.summary,
            jobDescription = entity.jobDescription,
            jobDescriptionUrl = entity.jobDescriptionUrl,
            accentColor = entity.accentColor,
            fontPair = entity.fontPair,
            completionPercent = entity.completionPercent,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    /**
     * Parses Experience list with migration support.
     * Handles old JSON with 'stateDate' field by mapping it to 'startDate'.
     */
    private fun parseExperiences(json: String): List<Experience> {
        return try {
            val jsonArray = JsonParser.parseString(json).asJsonArray
            jsonArray.map { element ->
                val obj = element.asJsonObject
                Experience(
                    company = obj.get("company")?.asString ?: "",
                    jobTitle = obj.get("jobTitle")?.asString ?: "",
                    startDate = obj.get("startDate")?.asString
                        ?: obj.get("stateDate")?.asString
                        ?: "",
                    endDate = obj.get("endDate")?.asString ?: "",
                    description = obj.get("description")?.asString ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private inline fun <reified T> parseList(json: String): List<T> {
        val type = object : TypeToken<List<T>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
