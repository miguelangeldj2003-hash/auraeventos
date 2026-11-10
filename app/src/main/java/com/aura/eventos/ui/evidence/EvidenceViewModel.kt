package com.aura.eventos.ui.evidence

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.eventos.data.local.EvidenceEntity
import com.aura.eventos.data.local.EvidenceType
import com.aura.eventos.data.repository.EvidenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CaptureUiState(
    val folio: String,
    val type: EvidenceType = EvidenceType.ENTREGA,
    val photoPath: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Float? = null,
    val locating: Boolean = false,
    val locationError: String? = null,
    val comment: String = "",
    val saving: Boolean = false,
    val saved: Boolean = false
)

@HiltViewModel
class EvidenceViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repo: EvidenceRepository,
    private val location: LocationProvider
) : ViewModel() {

    private val _ui = MutableStateFlow(CaptureUiState(folio = savedState.get<String>("folio").orEmpty()))
    val ui: StateFlow<CaptureUiState> = _ui.asStateFlow()

    fun onPhoto(path: String) = _ui.update { it.copy(photoPath = path) }
    fun onRetake() = _ui.update { it.copy(photoPath = null) }
    fun onType(t: EvidenceType) = _ui.update { it.copy(type = t) }
    fun onComment(c: String) = _ui.update { it.copy(comment = c) }

    fun fetchLocation() {
        _ui.update { it.copy(locating = true, locationError = null) }
        viewModelScope.launch {
            val loc = location.current()
            _ui.update {
                if (loc == null) it.copy(locating = false, locationError = "No se pudo obtener el GPS. Sal a un lugar abierto e inténtalo de nuevo.")
                else it.copy(locating = false, latitude = loc.latitude, longitude = loc.longitude, accuracy = loc.accuracy)
            }
        }
    }

    fun onPermissionDenied() = _ui.update { it.copy(locationError = "Permiso de ubicación denegado. Actívalo en Ajustes.") }

    fun save() {
        val s = _ui.value
        val path = s.photoPath ?: return
        _ui.update { it.copy(saving = true) }
        viewModelScope.launch {
            repo.saveLocal(
                EvidenceEntity(
                    eventFolio = s.folio, type = s.type, photoPath = path, comment = s.comment,
                    latitude = s.latitude, longitude = s.longitude, accuracyMeters = s.accuracy
                )
            )
            _ui.update { it.copy(saving = false, saved = true) }
        }
    }
}
