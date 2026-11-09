package com.notivault.app.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val masterIntercept by viewModel.masterIntercept.collectAsState()
    val autoCleanupDays by viewModel.autoCleanupDays.collectAsState()
    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsState()

    var showCleanupDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Interception Category
            Text("INTERCEPTION & PRIVACY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Card(shape = RoundedCornerShape(12.dp)) {
                Column {
                    ListItem(
                        headlineContent = { Text("Master Intercept Switch") },
                        supportingContent = { Text("Cancel notifications from notification shade") },
                        trailingContent = {
                            Switch(
                                checked = masterIntercept,
                                onCheckedChange = { viewModel.setMasterIntercept(it) }
                            )
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Auto-Delete Retention") },
                        supportingContent = {
                            Text(if (autoCleanupDays == 0) "Keep Forever" else "Delete after $autoCleanupDays days")
                        },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier.clickable { showCleanupDialog = true }
                    )
                }
            }

            // Security Category
            Text("SECURITY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Card(shape = RoundedCornerShape(12.dp)) {
                Column {
                    ListItem(
                        headlineContent = { Text("Biometric / PIN Lock") },
                        supportingContent = { Text("Require authentication when opening NotiVault") },
                        trailingContent = {
                            Switch(
                                checked = isAppLockEnabled,
                                onCheckedChange = { viewModel.setAppLock(it) }
                            )
                        }
                    )
                }
            }

            // Data Export Category
            Text("DATA EXPORT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Card(shape = RoundedCornerShape(12.dp)) {
                Column {
                    ListItem(
                        headlineContent = { Text("Export Notifications as JSON") },
                        supportingContent = { Text("Download complete structured notification log") },
                        leadingContent = { Icon(Icons.Default.Download, contentDescription = null) },
                        modifier = Modifier.clickable { viewModel.exportData(context, "json") }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Export Notifications as CSV") },
                        supportingContent = { Text("Spreadsheet compatible format") },
                        leadingContent = { Icon(Icons.Default.TableChart, contentDescription = null) },
                        modifier = Modifier.clickable { viewModel.exportData(context, "csv") }
                    )
                }
            }

            // Diagnostics & Background Restrictions
            Text("SYSTEM & PERMISSIONS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Card(shape = RoundedCornerShape(12.dp)) {
                Column {
                    ListItem(
                        headlineContent = { Text("Permission & Battery Optimization Wizard") },
                        supportingContent = { Text("Configure Notification Access and OEM auto-kill exemptions") },
                        leadingContent = { Icon(Icons.Default.BatteryAlert, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier.clickable { onNavigateToOnboarding() }
                    )
                }
            }
        }
    }

    if (showCleanupDialog) {
        val options = listOf(0 to "Keep Forever", 7 to "7 Days", 30 to "30 Days", 90 to "90 Days")
        AlertDialog(
            onDismissRequest = { showCleanupDialog = false },
            title = { Text("Select Retention Period") },
            text = {
                Column {
                    options.forEach { (days, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAutoCleanupDays(days)
                                    showCleanupDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = autoCleanupDays == days, onClick = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}
