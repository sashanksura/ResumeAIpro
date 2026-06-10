package com.resumeai.pro.presentation.builder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.resumeai.pro.data.api.ExtractionProgress
import com.resumeai.pro.data.api.JobDescriptionExtractor
import com.resumeai.pro.data.local.DraftEntity
import com.resumeai.pro.domain.model.Education
import com.resumeai.pro.domain.model.Experience
import com.resumeai.pro.domain.model.PersonalInfo
import com.resumeai.pro.domain.model.Project
import com.resumeai.pro.domain.model.Resume
import com.resumeai.pro.domain.model.Skill
import com.resumeai.pro.domain.repository.ResumeRepository
import com.resumeai.pro.domain.usecase.EnhanceBulletPointsUseCase
import com.resumeai.pro.domain.usecase.GenerateSummaryUseCase
import com.resumeai.pro.domain.usecase.RewriteExperienceUseCase
import com.resumeai.pro.domain.usecase.SuggestSkillsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ResumeFormState(
    val personalInfo: PersonalInfo = PersonalInfo(),
    val experiences: List<Experience> = emptyList(),
    val educations: List<Education> = emptyList(),
    val skills: List<Skill> = emptyList(),
    val suggestedSkills: List<String> = emptyList(), // AI-suggested but not yet added
    val projects: List<Project> = emptyList(),
    val summary: String = "",
    val jobDescription: String = "",
    val jobDescriptionUrl: String = "",
    val selectedTone: String = "Professional",
    val templateId: String = "ats_modern",
    val accentColor: String = "#2563EB",
    val currentStep: Int = 0
)

sealed class BuilderUiState {
    object Idle : BuilderUiState()
    object Loading : BuilderUiState()
    data class ExtractionProgressState(val progress: ExtractionProgress) : BuilderUiState()
    data class AiResult(val result: String) : BuilderUiState()
    data class Error(val message: String) : BuilderUiState()
    data class Saved(val resumeId: String) : BuilderUiState()
}

@HiltViewModel
class BuilderViewModel @Inject constructor(
    private val repository: ResumeRepository,
    private val enhanceBulletPointsUseCase: EnhanceBulletPointsUseCase,
    private val generateSummaryUseCase: GenerateSummaryUseCase,
    private val suggestSkillsUseCase: SuggestSkillsUseCase,
    private val rewriteExperienceUseCase: RewriteExperienceUseCase,
    private val jobDescriptionExtractor: JobDescriptionExtractor
) : ViewModel() {

    private val _formState = MutableStateFlow(ResumeFormState())
    val formState: StateFlow<ResumeFormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<BuilderUiState>(BuilderUiState.Idle)
    val uiState: StateFlow<BuilderUiState> = _uiState.asStateFlow()

    private val _aiLoadingIndex = MutableStateFlow(-1)
    val aiLoadingIndex: StateFlow<Int> = _aiLoadingIndex.asStateFlow()

    private var activeAiJob: Job? = null
    private val gson = Gson()

    // Undo/Redo history
    private val undoStack = mutableListOf<ResumeFormState>()
    private val redoStack = mutableListOf<ResumeFormState>()
    private val maxHistorySize = 20

    init {
        // Load draft on startup
        viewModelScope.launch {
            val draft = repository.getDraft()
            if (draft != null) {
                restoreDraft(draft)
            }
            
            // Setup auto-save
            @OptIn(FlowPreview::class)
            formState
                .drop(1) // Skip initial emission
                .debounce(3000L) // Wait 3s after last edit
                .onEach { saveDraftNow() }
                .launchIn(viewModelScope)
        }
    }

    private fun restoreDraft(draft: DraftEntity) {
        try {
            _formState.update { 
                it.copy(
                    personalInfo = gson.fromJson(draft.personalInfoJson, PersonalInfo::class.java) ?: PersonalInfo(),
                    experiences = parseList(draft.experiencesJson),
                    educations = parseList(draft.educationsJson),
                    skills = parseList(draft.skillsJson),
                    suggestedSkills = parseList(draft.suggestedSkillsJson),
                    projects = parseList(draft.projectsJson),
                    summary = draft.summary,
                    jobDescription = draft.jobDescription,
                    jobDescriptionUrl = draft.jobDescriptionUrl,
                    selectedTone = draft.selectedTone,
                    currentStep = draft.currentStep,
                    templateId = draft.templateId,
                    accentColor = draft.accentColor
                )
            }
        } catch (e: Exception) {
            // Start fresh if parsing fails
        }
    }

    private inline fun <reified T> parseList(json: String): List<T> {
        return try {
            val type = object : TypeToken<List<T>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveDraftNow() {
        val state = _formState.value
        val draft = DraftEntity(
            name = state.personalInfo.fullName.ifEmpty { "Draft" },
            templateId = state.templateId,
            personalInfoJson = gson.toJson(state.personalInfo),
            experiencesJson = gson.toJson(state.experiences),
            educationsJson = gson.toJson(state.educations),
            skillsJson = gson.toJson(state.skills),
            suggestedSkillsJson = gson.toJson(state.suggestedSkills),
            projectsJson = gson.toJson(state.projects),
            summary = state.summary,
            jobDescription = state.jobDescription,
            jobDescriptionUrl = state.jobDescriptionUrl,
            selectedTone = state.selectedTone,
            currentStep = state.currentStep,
            accentColor = state.accentColor,
            lastModifiedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.saveDraft(draft)
        }
    }

    fun cancelAiOperation() {
        activeAiJob?.cancel()
        activeAiJob = null
        _uiState.value = BuilderUiState.Idle
        _aiLoadingIndex.value = -1
    }

    private fun pushUndo() {
        undoStack.add(_formState.value.copy())
        if (undoStack.size > maxHistorySize) undoStack.removeAt(0)
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(_formState.value.copy())
            _formState.value = undoStack.removeAt(undoStack.lastIndex)
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(_formState.value.copy())
            _formState.value = redoStack.removeAt(redoStack.lastIndex)
        }
    }

    fun updatePersonalInfo(info: PersonalInfo) {
        pushUndo()
        _formState.update { it.copy(personalInfo = info) }
    }

    fun addExperience(exp: Experience) {
        pushUndo()
        _formState.update { it.copy(experiences = it.experiences + exp) }
    }

    fun removeExperience(index: Int) {
        val list = _formState.value.experiences.toMutableList()
        if (index in list.indices) {
            pushUndo()
            list.removeAt(index)
            _formState.update { it.copy(experiences = list) }
        }
    }

    fun updateExperience(index: Int, exp: Experience) {
        val list = _formState.value.experiences.toMutableList()
        if (index in list.indices) {
            list[index] = exp
            _formState.update { it.copy(experiences = list) }
        }
    }

    fun addEducation(edu: Education) {
        pushUndo()
        _formState.update { it.copy(educations = it.educations + edu) }
    }

    fun removeEducation(index: Int) {
        val list = _formState.value.educations.toMutableList()
        if (index in list.indices) {
            pushUndo()
            list.removeAt(index)
            _formState.update { it.copy(educations = list) }
        }
    }

    fun updateEducation(index: Int, edu: Education) {
        val list = _formState.value.educations.toMutableList()
        if (index in list.indices) {
            list[index] = edu
            _formState.update { it.copy(educations = list) }
        }
    }

    fun addSkill(skill: Skill) {
        val existing = _formState.value.skills.map { it.name.lowercase() }
        if (skill.name.lowercase() !in existing) {
            pushUndo()
            _formState.update { it.copy(skills = it.skills + skill) }
        }
    }

    fun removeSkill(index: Int) {
        val list = _formState.value.skills.toMutableList()
        if (index in list.indices) {
            pushUndo()
            list.removeAt(index)
            _formState.update { it.copy(skills = list) }
        }
    }

    fun updateSkillLevel(index: Int, level: Float) {
        val list = _formState.value.skills.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(proficiency = level)
            _formState.update { it.copy(skills = list) }
        }
    }

    fun addProject(project: Project) {
        pushUndo()
        _formState.update { it.copy(projects = it.projects + project) }
    }

    fun removeProject(index: Int) {
        val list = _formState.value.projects.toMutableList()
        if (index in list.indices) {
            pushUndo()
            list.removeAt(index)
            _formState.update { it.copy(projects = list) }
        }
    }

    fun updateProject(index: Int, project: Project) {
        val list = _formState.value.projects.toMutableList()
        if (index in list.indices) {
            list[index] = project
            _formState.update { it.copy(projects = list) }
        }
    }

    fun updateSummary(text: String) {
        _formState.update { it.copy(summary = text) }
    }

    fun updateJobDescription(text: String) {
        _formState.update { it.copy(jobDescription = text) }
    }

    fun updateJobDescriptionUrl(url: String) {
        _formState.update { it.copy(jobDescriptionUrl = url) }
    }

    /** Attempts to extract job description from a URL using JobDescriptionExtractor. */
    fun extractJobDescription(url: String) {
        if (activeAiJob?.isActive == true || url.isBlank()) return
        saveDraftNow()
        
        activeAiJob = viewModelScope.launch {
            val result = jobDescriptionExtractor.extract(url) { progress ->
                _uiState.value = BuilderUiState.ExtractionProgressState(progress)
            }
            if (result.isSuccess) {
                val extracted = result.getOrNull() ?: ""
                _formState.update { it.copy(jobDescription = extracted, jobDescriptionUrl = url) }
                _uiState.value = BuilderUiState.AiResult("Job description extracted successfully!")
            } else {
                _uiState.value = BuilderUiState.Error(
                    result.exceptionOrNull()?.message ?: "Could not extract job description. Please paste it manually."
                )
            }
            activeAiJob = null
        }
    }

    fun updateTone(tone: String) {
        _formState.update { it.copy(selectedTone = tone) }
    }

    fun updateTemplate(templateId: String, accentColor: String) {
        pushUndo()
        _formState.update { it.copy(templateId = templateId, accentColor = accentColor) }
    }

    fun enhanceBulletPoints(index: Int) {
        if (activeAiJob?.isActive == true) return
        saveDraftNow()
        val state = _formState.value
        val exp = state.experiences.getOrNull(index) ?: return
        if (exp.description.isBlank() && exp.jobTitle.isBlank()) return

        _aiLoadingIndex.value = index
        _uiState.value = BuilderUiState.Loading

        activeAiJob = viewModelScope.launch {
            val result = enhanceBulletPointsUseCase(
                jobTitle = exp.jobTitle,
                company = exp.company,
                description = exp.description.ifBlank { "${exp.jobTitle} at ${exp.company}" },
                jobDescription = state.jobDescription
            )

            if (result.isSuccess) {
                val enhanced = result.getOrNull() ?: ""
                pushUndo()
                updateExperience(index, exp.copy(description = enhanced))
                _uiState.value = BuilderUiState.AiResult(enhanced)
            } else {
                _uiState.value = BuilderUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to enhance. Try again."
                )
            }
            _aiLoadingIndex.value = -1
            activeAiJob = null
        }
    }

    fun rewriteExperience(index: Int) {
        if (activeAiJob?.isActive == true) return
        saveDraftNow()
        val state = _formState.value
        val exp = state.experiences.getOrNull(index) ?: return

        _aiLoadingIndex.value = index
        _uiState.value = BuilderUiState.Loading

        activeAiJob = viewModelScope.launch {
            val result = rewriteExperienceUseCase(
                jobTitle = exp.jobTitle,
                company = exp.company,
                description = exp.description.ifBlank { "${exp.jobTitle} at ${exp.company}" },
                jobDescription = state.jobDescription
            )

            if (result.isSuccess) {
                val rewritten = result.getOrNull() ?: ""
                pushUndo()
                updateExperience(index, exp.copy(description = rewritten))
                _uiState.value = BuilderUiState.AiResult(rewritten)
            } else {
                _uiState.value = BuilderUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to rewrite. Try again."
                )
            }
            _aiLoadingIndex.value = -1
            activeAiJob = null
        }
    }

    fun generateSummary() {
        if (activeAiJob?.isActive == true) return
        saveDraftNow()
        _uiState.value = BuilderUiState.Loading

        activeAiJob = viewModelScope.launch {
            val state = _formState.value
            val years = if (state.experiences.isNotEmpty()) "${state.experiences.size}+" else "Several"
            val skillsText = state.skills.joinToString { it.name }
            val achievement = state.experiences.firstOrNull()?.description?.take(100) ?: ""

            val result = generateSummaryUseCase(
                name = state.personalInfo.fullName,
                jobTitle = state.personalInfo.jobTitle,
                years = years,
                skills = skillsText,
                achievement = achievement,
                tone = state.selectedTone,
                jobDescription = state.jobDescription
            )

            if (result.isSuccess) {
                val summary = result.getOrNull() ?: ""
                pushUndo()
                _formState.update { it.copy(summary = summary) }
                _uiState.value = BuilderUiState.AiResult(summary)
            } else {
                _uiState.value = BuilderUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to generate summary."
                )
            }
            activeAiJob = null
        }
    }

    fun suggestSkills() {
        if (activeAiJob?.isActive == true) return
        saveDraftNow()
        _uiState.value = BuilderUiState.Loading

        activeAiJob = viewModelScope.launch {
            val state = _formState.value
            val jobTitle = state.personalInfo.jobTitle.ifEmpty { "Software Engineer" }
            val existingSkills = state.skills.map { it.name }
            val experienceSummary = state.experiences.joinToString("; ") {
                "${it.jobTitle} at ${it.company}"
            }

            val result = suggestSkillsUseCase(
                jobTitle = jobTitle,
                existingSkills = existingSkills,
                experiences = experienceSummary,
                jobDescription = state.jobDescription
            )

            if (result.isSuccess) {
                val suggestions = result.getOrNull() ?: emptyList()
                // Store as suggested skills (chips) rather than auto-adding
                _formState.update { it.copy(suggestedSkills = suggestions) }
                _uiState.value = BuilderUiState.AiResult("Found ${suggestions.size} relevant skills. Tap to add them.")
            } else {
                _uiState.value = BuilderUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to suggest skills."
                )
            }
            activeAiJob = null
        }
    }

    /** Accepts an AI-suggested skill by moving it to the user's actual skills list. */
    fun acceptSuggestedSkill(skillName: String) {
        val suggestions = _formState.value.suggestedSkills.toMutableList()
        suggestions.remove(skillName)
        _formState.update { it.copy(suggestedSkills = suggestions) }
        addSkill(Skill(name = skillName))
    }

    /** Dismisses an AI-suggested skill without adding it. */
    fun dismissSuggestedSkill(skillName: String) {
        val suggestions = _formState.value.suggestedSkills.toMutableList()
        suggestions.remove(skillName)
        _formState.update { it.copy(suggestedSkills = suggestions) }
    }

    fun clearUiState() {
        _uiState.value = BuilderUiState.Idle
    }

    fun updatePhotoUri(uri: String) {
        pushUndo()
        _formState.update { it.copy(personalInfo = it.personalInfo.copy(photoUri = uri)) }
    }

    fun nextStep() {
        if (_formState.value.currentStep < 5) {
            _uiState.value = BuilderUiState.Idle
            _formState.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    fun previousStep() {
        if (_formState.value.currentStep > 0) {
            _uiState.value = BuilderUiState.Idle
            _formState.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun calculateCompletionPercent(): Int {
        var completed = 0
        val s = _formState.value
        if (s.personalInfo.fullName.isNotEmpty()) completed += 15
        if (s.experiences.isNotEmpty()) completed += 20
        if (s.educations.isNotEmpty()) completed += 15
        if (s.skills.isNotEmpty()) completed += 15
        if (s.projects.isNotEmpty()) completed += 15
        if (s.summary.isNotEmpty()) completed += 20
        return completed
    }

    fun saveResume() {
        viewModelScope.launch {
            val state = _formState.value
            val id = UUID.randomUUID().toString()
            val resume = Resume(
                id = id,
                name = state.personalInfo.fullName.ifEmpty { "My Resume" },
                templateId = state.templateId,
                personalInfo = state.personalInfo,
                experiences = state.experiences,
                educations = state.educations,
                skills = state.skills,
                projects = state.projects,
                summary = state.summary,
                jobDescription = state.jobDescription,
                jobDescriptionUrl = state.jobDescriptionUrl,
                accentColor = state.accentColor,
                fontPair = "Default",
                completionPercent = calculateCompletionPercent(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertResume(resume)
            // Cleanup draft since we saved successfully
            repository.deleteDraft()
            _uiState.value = BuilderUiState.Saved(id)
        }
    }
}
