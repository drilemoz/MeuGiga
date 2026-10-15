package br.com.meugiga.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Calendário independente dos gráficos: retorna um único dia civil local. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleDayPickerButton(
    selectedDay: LocalDate?,
    onDaySelected: (LocalDate) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    IconButton(onClick = { open = true }) {
        Icon(Icons.Rounded.DateRange, contentDescription = "Escolher dia no calendário")
    }
    if (open) {
        val picker = rememberDatePickerState(
            initialSelectedDateMillis = (selectedDay ?: LocalDate.now())
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        val selected = picker.selectedDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
        }
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selected?.let(onDaySelected)
                        open = false
                    },
                    enabled = selected != null && !selected.isAfter(LocalDate.now()),
                ) { Text("Aplicar") }
            },
            dismissButton = {
                TextButton(onClick = { open = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = picker, title = { Text("Escolha um dia") }, showModeToggle = false)
        }
    }
}
