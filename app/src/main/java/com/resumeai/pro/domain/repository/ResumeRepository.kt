package com.resumeai.pro.domain.repository

import com.resumeai.pro.domain.model.Resume
import kotlinx.coroutines.flow.Flow

interface ResumeRepository {
    fun getAllResumes(): Flow<List<Resume>>
    suspend fun getResumeById(id: String): Resume?
    suspend fun insertResume(resume: Resume)
    suspend fun updateResume(resume: Resume)
    suspend fun deleteResume(id: String)
    fun getResumeCount(): Flow<Int>

    suspend fun saveDraft(draft: com.resumeai.pro.data.local.DraftEntity)
    suspend fun getDraft(): com.resumeai.pro.data.local.DraftEntity?
    suspend fun deleteDraft()
}
