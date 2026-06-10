package com.resumeai.pro.presentation.preview

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resumeai.pro.data.export.DocxExporter
import com.resumeai.pro.data.export.PdfExporter
import com.resumeai.pro.domain.model.ATSResult
import com.resumeai.pro.domain.model.Resume
import com.resumeai.pro.domain.repository.ResumeRepository
import com.resumeai.pro.domain.usecase.ATSOptimizeUseCase
import com.resumeai.pro.domain.usecase.ImproveResumeUseCase
import com.resumeai.pro.domain.usecase.JobMatchUseCase
import com.resumeai.pro.domain.usecase.ResumeFormattingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PreviewUiState {
    object Idle : PreviewUiState()
    object Loading : PreviewUiState()
    data class Exporting(
        val progress: Float,
        val statusMessage: String,
        val format: String
    ) : PreviewUiState()
    data class AiResult(val title: String, val result: String) : PreviewUiState()
    data class Error(val message: String) : PreviewUiState()
    data class ExportSuccess(val message: String) : PreviewUiState()
}

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val repository: ResumeRepository,
    private val improveResumeUseCase: ImproveResumeUseCase,
    private val atsOptimizeUseCase: ATSOptimizeUseCase,
    private val jobMatchUseCase: JobMatchUseCase,
    private val resumeFormattingUseCase: ResumeFormattingUseCase
) : ViewModel() {
    private val _resume = MutableStateFlow<Resume?>(null)
    val resume: StateFlow<Resume?> = _resume

    private val _uiState = MutableStateFlow<PreviewUiState>(PreviewUiState.Idle)
    val uiState: StateFlow<PreviewUiState> = _uiState

    private val _atsResult = MutableStateFlow<ATSResult?>(null)
    val atsResult: StateFlow<ATSResult?> = _atsResult

    private var activeAiJob: kotlinx.coroutines.Job? = null
    private var isExporting = false

    fun cancelAiOperation() {
        activeAiJob?.cancel()
        activeAiJob = null
        _uiState.value = PreviewUiState.Idle
    }

    fun loadResume(id: String) {
        viewModelScope.launch {
            _resume.value = repository.getResumeById(id)
        }
    }

    fun exportPdf(context: Context) {
        if (isExporting || activeAiJob?.isActive == true) return
        val resume = _resume.value ?: return
        isExporting = true

        viewModelScope.launch {
            try {
                _uiState.value = PreviewUiState.Exporting(0f, "Preparing resume data...", "PDF")
                val result = PdfExporter.export(context, resume) { progress, message ->
                    _uiState.value = PreviewUiState.Exporting(progress, message, "PDF")
                }
                if (result.isSuccess) {
                    _uiState.value = PreviewUiState.ExportSuccess("PDF saved to Downloads: ${result.getOrNull()?.name}")
                } else {
                    _uiState.value = PreviewUiState.Error("PDF export failed: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _uiState.value = PreviewUiState.Error("PDF export failed: ${e.message}")
            } finally {
                isExporting = false
            }
        }
    }

    fun exportDocx(context: Context) {
        if (isExporting || activeAiJob?.isActive == true) return
        val resume = _resume.value ?: return
        isExporting = true

        viewModelScope.launch {
            try {
                _uiState.value = PreviewUiState.Exporting(0f, "Preparing resume data...", "DOCX")
                val result = DocxExporter.export(context, resume) { progress, message ->
                    _uiState.value = PreviewUiState.Exporting(progress, message, "DOCX")
                }
                if (result.isSuccess) {
                    _uiState.value = PreviewUiState.ExportSuccess("DOCX saved to Downloads: ${result.getOrNull()?.name}")
                } else {
                    _uiState.value = PreviewUiState.Error("DOCX export failed: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _uiState.value = PreviewUiState.Error("DOCX export failed: ${e.message}")
            } finally {
                isExporting = false
            }
        }
    }

    fun improveResume() {
        if (activeAiJob?.isActive == true || isExporting) return
        val resume = _resume.value ?: return
        
        cancelAiOperation()
        _uiState.value = PreviewUiState.Loading

        activeAiJob = viewModelScope.launch {
            val result = improveResumeUseCase(resume)
            if (result.isSuccess) {
                _uiState.value = PreviewUiState.AiResult(
                    title = "Resume Improvements",
                    result = result.getOrNull() ?: ""
                )
            } else {
                _uiState.value = PreviewUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to improve resume"
                )
            }
            activeAiJob = null
        }
    }

    fun atsOptimize(jobDescription: String = "") {
        if (activeAiJob?.isActive == true || isExporting) return
        val resume = _resume.value ?: return
        
        cancelAiOperation()
        _uiState.value = PreviewUiState.Loading

        activeAiJob = viewModelScope.launch {
            val result = atsOptimizeUseCase(resume, jobDescription)
            if (result.isSuccess) {
                val resultText = result.getOrNull() ?: ""
                // Also parse into structured ATSResult for dashboard
                _atsResult.value = atsOptimizeUseCase.parseATSResult(resultText)
                _uiState.value = PreviewUiState.AiResult(
                    title = "ATS Optimization",
                    result = resultText
                )
            } else {
                _uiState.value = PreviewUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to analyze ATS compatibility"
                )
            }
            activeAiJob = null
        }
    }

    fun jobMatch(jobDescription: String) {
        if (activeAiJob?.isActive == true || isExporting) return
        val resume = _resume.value ?: return
        
        cancelAiOperation()
        _uiState.value = PreviewUiState.Loading

        activeAiJob = viewModelScope.launch {
            val result = jobMatchUseCase(resume, jobDescription)
            if (result.isSuccess) {
                _uiState.value = PreviewUiState.AiResult(
                    title = "Job Match Analysis",
                    result = result.getOrNull() ?: ""
                )
            } else {
                _uiState.value = PreviewUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to match against job description"
                )
            }
            activeAiJob = null
        }
    }

    fun analyzeFormatting() {
        if (activeAiJob?.isActive == true || isExporting) return
        val resume = _resume.value ?: return
        
        cancelAiOperation()
        _uiState.value = PreviewUiState.Loading

        activeAiJob = viewModelScope.launch {
            val result = resumeFormattingUseCase(resume)
            if (result.isSuccess) {
                _uiState.value = PreviewUiState.AiResult(
                    title = "Formatting Analysis",
                    result = result.getOrNull() ?: ""
                )
            } else {
                _uiState.value = PreviewUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to analyze formatting"
                )
            }
            activeAiJob = null
        }
    }

    fun clearUiState() {
        _uiState.value = PreviewUiState.Idle
    }

    fun clearATSResult() {
        _atsResult.value = null
    }
}
