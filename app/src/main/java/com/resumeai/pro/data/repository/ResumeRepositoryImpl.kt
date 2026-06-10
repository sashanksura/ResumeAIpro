package com.resumeai.pro.data.repository

import com.resumeai.pro.data.local.ResumeDao
import com.resumeai.pro.data.mapper.ResumeMapper
import com.resumeai.pro.domain.model.Resume
import com.resumeai.pro.domain.repository.ResumeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ResumeRepositoryImpl(
    private val dao: ResumeDao
) : ResumeRepository {

    override fun getAllResumes(): Flow<List<Resume>> {
        return dao.getAllResumes().map { entities ->
            entities.map { ResumeMapper.toDomain(it) }
        }
    }

    override suspend fun getResumeById(id: String): Resume? {
        val entity = dao.getResumeById(id)
        return entity?.let { ResumeMapper.toDomain(it) }
    }

    override suspend fun insertResume(resume: Resume) {
        dao.insertResume(ResumeMapper.toEntity(resume))
    }

    override suspend fun updateResume(resume: Resume) {
        dao.updateResume(ResumeMapper.toEntity(resume))
    }

    override suspend fun deleteResume(id: String) {
        dao.deleteResume(id)
    }

    override fun getResumeCount(): Flow<Int> {
        return dao.getResumeCount()
    }

    override suspend fun saveDraft(draft: com.resumeai.pro.data.local.DraftEntity) {
        dao.insertDraft(draft)
    }

    override suspend fun getDraft(): com.resumeai.pro.data.local.DraftEntity? {
        return dao.getDraft()
    }

    override suspend fun deleteDraft() {
        dao.deleteDraft()
    }
}
