package com.milkchoco.botwar

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Cocoa = Color(0xFF24130F)
private val Chocolate = Color(0xFF3B211A)
private val Milk = Color(0xFFFFE8C7)
private val Caramel = Color(0xFFFFB45B)
private val Cream = Color(0xFFFFF7EC)
private val Muted = Color(0xFFBDA99B)

data class LaunchableApp(val label: String, val packageName: String, val icon: Drawable)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BotWarApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotWarApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("botwar_slots", Context.MODE_PRIVATE) }
    val pm = context.packageManager
    var selected by remember {
        mutableStateOf((0..3).map { i ->
            prefs.getString("slot_$i", null)
        })
    }
    var pickerSlot by remember { mutableIntStateOf(-1) }
    var showReset by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf(emptyList<LaunchableApp>()) }
    var message by remember { mutableStateOf<String?>(null) }

    fun saveSlot(index: Int, packageName: String?) {
        val updated = selected.toMutableList()
        updated[index] = packageName
        selected = updated
        prefs.edit().apply {
            if (packageName == null) remove("slot_$index")
            else putString("slot_$index", packageName)
        }.apply()
    }

    fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        apps = resolved.mapNotNull { info ->
            val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
            if (pkg == context.packageName) return@mapNotNull null
            try {
                LaunchableApp(
                    info.loadLabel(pm).toString(),
                    pkg,
                    info.loadIcon(pm)
                )
            } catch (_: Exception) { null }
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Caramel,
            onPrimary = Cocoa,
            secondary = Milk,
            background = Cocoa,
            surface = Chocolate,
            onSurface = Cream,
            onBackground = Cream
        )
    ) {
        Scaffold(
            containerColor = Cocoa,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MILK CHOCO", color = Milk, fontWeight = FontWeight.Black, fontSize = 19.sp, letterSpacing = 2.sp)
                            Text("BOT WAR", color = Caramel, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 4.sp)
                        }
                    },
                    navigationIcon = {
                        Icon(Icons.Filled.SmartToy, null, tint = Caramel, modifier = Modifier.padding(start = 16.dp).size(30.dp))
                    },
                    actions = {
                        IconButton(onClick = { showReset = true }) {
                            Icon(Icons.Filled.Refresh, "Reset all slots", tint = Milk)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Chocolate)
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = remember { SnackbarHostState() })
            }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("YOUR BOT SQUAD", color = Milk, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Assign one app to each battle slot", color = Muted, fontSize = 12.sp)
                    }
                    Surface(color = Color(0xFF573528), shape = CircleShape) {
                        Text("${selected.count { it != null }}/4 ACTIVE", color = Caramel, fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp))
                    }
                }
                Spacer(Modifier.height(14.dp))

                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (row in 0..1) {
                        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (col in 0..1) {
                                val index = row * 2 + col
                                key(index) {
                                    SlotCard(
                                        index = index,
                                        packageName = selected[index],
                                        pm = pm,
                                        onAdd = {
                                            pickerSlot = index
                                            search = ""
                                            loadApps()
                                        },
                                        onLaunch = {
                                            val pkg = selected[index]
                                            if (pkg != null) {
                                                val launch = pm.getLaunchIntentForPackage(pkg)
                                                if (launch != null) {
                                                    try { context.startActivity(launch) }
                                                    catch (_: Exception) { message = "Android could not launch this app." }
                                                } else message = "No launch activity is available for this app."
                                            }
                                        },
                                        onRemove = { saveSlot(index, null) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Surface(color = Chocolate, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Apps, null, tint = Caramel, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(9.dp))
                        Text("4 independent slots • Tap + to assign an app", color = Muted, fontSize = 11.sp)
                    }
                }
            }
        }

        if (pickerSlot >= 0) {
            val filtered = apps.filter { it.label.contains(search, true) || it.packageName.contains(search, true) }
            AlertDialog(
                onDismissRequest = { pickerSlot = -1 },
                title = { Text("SELECT APP • SLOT ${pickerSlot + 1}", color = Milk, fontWeight = FontWeight.Bold) },
                text = {
                    Column(Modifier.fillMaxWidth().heightIn(max = 480.dp)) {
                        OutlinedTextField(
                            value = search, onValueChange = { search = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search installed apps") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Cream, unfocusedTextColor = Cream,
                                focusedBorderColor = Caramel, unfocusedBorderColor = Muted,
                                focusedLabelColor = Caramel, cursorColor = Caramel
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        if (filtered.isEmpty()) {
                            Text("No launchable apps found.", color = Muted, modifier = Modifier.padding(12.dp))
                        } else {
                            LazyColumn {
                                items(filtered, key = { it.packageName }) { app ->
                                    Row(
                                        Modifier.fillMaxWidth().clickable {
                                            saveSlot(pickerSlot, app.packageName)
                                            pickerSlot = -1
                                        }.padding(vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AppIcon(app.icon, 38)
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(app.label, color = Cream, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(app.packageName, color = Muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    HorizontalDivider(color = Color(0xFF62483B))
                                }
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { pickerSlot = -1 }) { Text("CANCEL", color = Caramel) } },
                containerColor = Chocolate,
                titleContentColor = Milk,
                textContentColor = Cream
            )
        }

        if (showReset) {
            AlertDialog(
                onDismissRequest = { showReset = false },
                title = { Text("RESET BOT SQUAD?", color = Milk) },
                text = { Text("This removes all four app assignments. Installed apps will not be uninstalled.", color = Cream) },
                confirmButton = {
                    TextButton(onClick = {
                        (0..3).forEach { prefs.edit().remove("slot_$it").apply() }
                        selected = listOf(null, null, null, null)
                        showReset = false
                    }) { Text("RESET ALL", color = Caramel, fontWeight = FontWeight.Bold) }
                },
                dismissButton = { TextButton(onClick = { showReset = false }) { Text("CANCEL", color = Milk) } },
                containerColor = Chocolate
            )
        }
    }
}

@Composable
private fun SlotCard(
    index: Int,
    packageName: String?,
    pm: PackageManager,
    onAdd: () -> Unit,
    onLaunch: () -> Unit,
    onRemove: () -> Unit
) {
    val appInfo = remember(packageName) {
        if (packageName == null) null else try {
            @Suppress("DEPRECATION")
            pm.getApplicationInfo(packageName, 0)
        } catch (_: Exception) { null }
    }
    val label = remember(appInfo) {
        appInfo?.let { try { pm.getApplicationLabel(it).toString() } catch (_: Exception) { "App unavailable" } }
    }
    val icon = remember(appInfo) {
        appInfo?.let { try { pm.getApplicationIcon(it) } catch (_: Exception) { null } }
    }

    Surface(
        modifier = Modifier.fillMaxHeight().weight(1f),
        color = Color(0xFF382019),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color(0xFF704832)),
        shadowElevation = 5.dp
    ) {
        Column(
            Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("BOT ${index + 1}", color = Caramel, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(7.dp).clip(CircleShape).background(if (packageName != null && appInfo != null) Color(0xFF7BD88F) else Color(0xFF806B5C)))
            }
            if (packageName != null && appInfo != null && icon != null) {
                AppIcon(icon, 56)
                Text(label ?: "Selected app", color = Cream, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("ASSIGNED", color = Color(0xFF7BD88F), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLaunch, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Filled.Launch, "Launch app", tint = Caramel)
                    }
                    IconButton(onClick = onAdd, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Filled.SwapHoriz, "Change app", tint = Milk)
                    }
                    IconButton(onClick = onRemove, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Filled.DeleteOutline, "Remove app", tint = Color(0xFFFF8A80))
                    }
                }
            } else {
                Icon(Icons.Filled.SmartToy, null, tint = Color(0xFF9B7057), modifier = Modifier.size(48.dp))
                Text(if (packageName == null) "EMPTY SLOT" else "APP NOT FOUND", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                FilledTonalButton(
                    onClick = onAdd,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Caramel, contentColor = Cocoa),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(19.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("ADD APP", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                }
            }
            Text("SLOT ${index + 1}", color = Color(0xFF806B5C), fontSize = 9.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun AppIcon(drawable: Drawable, size: Int) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { context ->
            android.widget.ImageView(context).apply {
                setImageDrawable(drawable)
                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            }
        },
        update = { it.setImageDrawable(drawable) },
        modifier = Modifier.size(size.dp).clip(RoundedCornerShape(13.dp))
    )
}
