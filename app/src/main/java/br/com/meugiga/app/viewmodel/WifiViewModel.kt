package br.com.meugiga.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.meugiga.app.AppContainer
import br.com.meugiga.app.data.networkstats.NetworkStatisticsUnavailableException
import br.com.meugiga.app.data.networkstats.UsageAccessDeniedException
import br.com.meugiga.app.data.repository.WifiUsageReport
import br.com.meugiga.app.domain.WifiPeriod
import br.com.meugiga.app.domain.WifiPeriodCalculator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Leitura Wi-Fi apenas ao abrir a aba, mudar o filtro ou tocar em Atualizar. */
enum class WifiReadError { PERMISSION, UNAVAILABLE, UNEXPECTED }

data class WifiUiState(
    val selectedPeriod: WifiPeriod = WifiPeriod.TODAY,
    val customDay: LocalDate? = null,
    val report: WifiUsageReport? = null,
    val refreshing: Boolean = false,
    val error: WifiReadError? = null,
    val errorMessage: String? = null,
)

class WifiViewModel(private val container: AppContainer) : ViewModel() {
    private val periods = WifiPeriodCalculator()
    private val _state = MutableStateFlow(WifiUiState())
    val state: StateFlow<WifiUiState> = _state.asStateFlow()
    private var readJob: Job? = null

    fun onOpened() = refresh()

    fun selectPeriod(period: WifiPeriod) {
        if (_state.value.selectedPeriod == period) return
        _state.update { it.copy(selectedPeriod = period, report = null, error = null) }
        refresh()
    }

    fun selectDay(day: LocalDate) {
        if (day.isAfter(LocalDate.now())) return
        _state.update {
            it.copy(selectedPeriod = WifiPeriod.CUSTOM, customDay = day, report = null, error = null)
        }
        refresh()
    }

    fun refresh() {
        readJob?.cancel()
        val selected = _state.value.selectedPeriod
        val selectedDay = _state.value.customDay
        _state.update { it.copy(refreshing = true, error = null, errorMessage = null, report = null) }
        readJob = viewModelScope.launch {
            try {
                val period = periods.resolve(selected, customDay = selectedDay)
                val report = container.wifiUsageRepository.load(period)
                _state.update { it.copy(report = report, refreshing = false) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (denied: UsageAccessDeniedException) {
                _state.update { it.copy(refreshing = false, error = WifiReadError.PERMISSION) }
            } catch (unavailable: NetworkStatisticsUnavailableException) {
                _state.update { it.copy(refreshing = false, error = WifiReadError.UNAVAILABLE) }
            } catch (error: Exception) {
                _state.update {
                    it.copy(refreshing = false, error = WifiReadError.UNEXPECTED,
                        errorMessage = error.localizedMessage)
                }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(WifiViewModel::class.java))
                    return WifiViewModel(container) as T
                }
            }
    }
}
