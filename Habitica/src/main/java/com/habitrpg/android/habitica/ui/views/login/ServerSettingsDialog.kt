package com.habitrpg.android.habitica.ui.views.login

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.common.habitica.api.ServerSettings
import com.habitrpg.common.habitica.theme.HabiticaTheme

@Composable
fun ServerSettingsDialog(
    serverSettings: ServerSettings,
    onApply: (String) -> Unit,
    onReset: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
        ),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = HabiticaTheme.colors.windowBackground,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 16.dp),
            ) {
                Text(
                    text = "Server",
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                )
                Text(
                    text = "Warning: Accounts on custom servers are separate from your main Habitica.com account.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                )
                val (baseUrl, customUrl) = serverSettings
                val radioOptions = listOf("$baseUrl (Default)", "Custom")
                val (selectedOption, onOptionSelected) = remember {
                    mutableStateOf(
                        if (customUrl.isNullOrEmpty()) radioOptions.first() else radioOptions.last()
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .selectableGroup()
                ) {
                    radioOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (option == selectedOption),
                                    onClick = { onOptionSelected(option) },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (option == selectedOption),
                                onClick = null,
                            )
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
                var input by remember { mutableStateOf(customUrl ?: "") }
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Enter custom server address") },
                    supportingText = { Text("e.g. http://localhost:3000") },
                    enabled = selectedOption == radioOptions.last(),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val applyEnabled = if (customUrl == null) {
                        selectedOption == radioOptions.last() && input.isNotBlank()
                    } else {
                        selectedOption == radioOptions.first() || input != customUrl
                    }
                    TextButton(
                        onClick = {
                            if (selectedOption == radioOptions.last()) {
                                onApply(input)
                            } else {
                                onReset()
                            }
                        },
                        enabled = applyEnabled,
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Preview
@Preview(
    name = "Night", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun ServerSettingsDialogPreview() {
    HabiticaTheme {
        ServerSettingsDialog(
            serverSettings = ServerSettings(
                baseUrl = "https://habitica.com/",
                customUrl = null
            ),
            onApply = {},
            onReset = {},
            onDismissRequest = {},
        )
    }
}

@Preview
@Preview(
    name = "Night", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun ServerSettingsDialogPreviewCustomUrl() {
    HabiticaTheme {
        ServerSettingsDialog(
            serverSettings = ServerSettings(
                baseUrl = "https://habitica.com/",
                customUrl = "localhost:3000",
            ),
            onApply = {},
            onReset = {},
            onDismissRequest = {},
        )
    }
}
