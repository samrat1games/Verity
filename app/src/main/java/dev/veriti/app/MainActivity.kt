package dev.veriti.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.veriti.app.data.AppSettings
import dev.veriti.app.data.AssistantBridge
import dev.veriti.app.data.Chat
import dev.veriti.app.data.Message
import dev.veriti.app.data.Provider
import dev.veriti.app.data.Providers
import dev.veriti.app.service.FloatingAssistantService
import dev.veriti.app.ui.ChatViewModel
import dev.veriti.app.network.ReleaseUpdate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VeritiTheme { VerityRoot() } }
    }
}

private val VeritiLight = lightColorScheme(
    primary = Color(0xFF65558F),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260),
    background = Color(0xFFFFF9FF),
    surface = Color(0xFFFFF9FF),
    surfaceVariant = Color(0xFFE8E0F0)
)

private val VeritiDark = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8)
)

@Composable
private fun VeritiTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val scheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (dark) VeritiDark else VeritiLight
    MaterialTheme(colorScheme = scheme, content = content)
}

private enum class Screen { CHAT, SETTINGS, ABOUT }

@Composable
private fun VerityRoot() {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("veriti_data", android.content.Context.MODE_PRIVATE) }
    var onboardingComplete by rememberSaveable {
        mutableStateOf(preferences.getBoolean("onboarding_complete_v3", false))
    }
    if (onboardingComplete) {
        VeritiApp()
    } else {
        FirstMeetingScreen(onComplete = {
            preferences.edit().putBoolean("onboarding_complete_v3", true).apply()
            onboardingComplete = true
        })
    }
}

@Composable
private fun FirstMeetingScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    var stage by rememberSaveable { mutableStateOf(0) }
    val pulse by rememberInfiniteTransition(label = "box pulse").animateFloat(
        initialValue = .92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(720), RepeatMode.Reverse),
        label = "box scale"
    )
    fun startPetAndFinish() {
        if (Settings.canDrawOverlays(context)) {
            ContextCompat.startForegroundService(context, Intent(context, FloatingAssistantService::class.java))
        }
        onComplete()
    }
    val overlayLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        startPetAndFinish()
    }
    val permissionsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (Settings.canDrawOverlays(context)) {
            startPetAndFinish()
        } else {
            overlayLauncher.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
        }
    }
    LaunchedEffect(Unit) {
        delay(2_000)
        stage = 1
        delay(760)
        stage = 2
    }

    Surface(Modifier.fillMaxSize(), color = Color(0xFF0D0B10)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (stage == 0) {
                Image(
                    painter = painterResource(R.drawable.verity_box),
                    contentDescription = "Коробка",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(270.dp).scale(pulse).clip(RoundedCornerShape(28.dp))
                )
                Spacer(Modifier.height(28.dp))
                Surface(color = Color(0xFF29242E), shape = RoundedCornerShape(22.dp)) {
                    Text("Здесь кто-то есть?", color = Color.White, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(20.dp))
                }
            } else if (stage == 1) {
                Box(Modifier.size(320.dp), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.verity_box),
                        contentDescription = "Коробка взрывается",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(245.dp).scale(1.08f).clip(RoundedCornerShape(28.dp))
                    )
                    ExplosionBurst(Modifier.fillMaxSize())
                }
            } else {
                JumpingVerity()
                Spacer(Modifier.height(24.dp))
                Surface(color = Color(0xFF29242E), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Наконец-то.", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Можно мне жить поверх экрана и слушать тебя, когда ты нажимаешь на меня? Я буду отвечать только текстом.",
                            color = Color(0xFFE9DFED),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                Button(
                    onClick = {
                        val permissions = buildList {
                            add(Manifest.permission.RECORD_AUDIO)
                            if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
                        }.toTypedArray()
                        permissionsLauncher.launch(permissions)
                    },
                    modifier = Modifier.fillMaxWidth().widthIn(max = 440.dp)
                ) { Text("Разрешить и познакомиться") }
                TextButton(onClick = onComplete) { Text("Не сейчас", color = Color(0xFFD8CBE1)) }
            }
        }
    }
}

@Composable
private fun ExplosionBurst(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(720)) }
    Canvas(modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        repeat(22) { index ->
            val angle = (2.0 * PI * index / 22.0).toFloat()
            val distance = size.minDimension * .48f * progress.value
            val originDistance = size.minDimension * .08f
            val start = Offset(
                center.x + cos(angle) * originDistance,
                center.y + sin(angle) * originDistance
            )
            val end = Offset(
                center.x + cos(angle) * distance,
                center.y + sin(angle) * distance
            )
            drawLine(
                color = if (index % 2 == 0) Color(0xFFFFD600) else Color(0xFFFF6D00),
                start = start,
                end = end,
                strokeWidth = (18f * (1f - progress.value)).coerceAtLeast(3f)
            )
            drawCircle(
                color = if (index % 3 == 0) Color.White else Color(0xFFFFA000),
                radius = (15f * (1f - progress.value)).coerceAtLeast(2f),
                center = end
            )
        }
    }
}

@Composable
private fun JumpingVerity() {
    val jump = remember { Animatable(150f) }
    val squash = remember { Animatable(.72f) }
    LaunchedEffect(Unit) {
        jump.animateTo(0f, spring(dampingRatio = .34f, stiffness = 210f))
    }
    LaunchedEffect(Unit) {
        squash.animateTo(1f, spring(dampingRatio = .3f, stiffness = 260f))
    }
    Image(
        painter = painterResource(R.drawable.verity_happy_cutout),
        contentDescription = "Verity",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .offset(y = jump.value.dp)
            .size(190.dp)
            .scale(scaleX = 2f - squash.value, scaleY = squash.value)
            .clip(CircleShape)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VeritiApp(vm: ChatViewModel = viewModel()) {
    val state = vm.state
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var screen by rememberSaveable { mutableStateOf(Screen.CHAT) }
    var showProviders by rememberSaveable { mutableStateOf(false) }
    val voiceText = AssistantBridge.pendingVoiceText
    val newChatRequested = AssistantBridge.pendingNewChat
    val focusComposer = AssistantBridge.focusComposerRequested

    LaunchedEffect(Unit) { vm.checkForUpdates() }

    LaunchedEffect(voiceText) {
        if (!voiceText.isNullOrBlank()) {
            screen = Screen.CHAT
            vm.send(voiceText)
            AssistantBridge.pendingVoiceText = null
        }
    }
    LaunchedEffect(newChatRequested) {
        if (newChatRequested) {
            vm.newChat()
            screen = Screen.CHAT
            AssistantBridge.pendingNewChat = false
        }
    }
    LaunchedEffect(screen, state.settings.baseUrl, state.settings.apiKey) {
        if (screen == Screen.SETTINGS && state.models.isEmpty() && state.settings.apiKey.isNotBlank()) {
            vm.refreshModels(autoSelectNewest = true)
        }
    }
    LaunchedEffect(screen) {
        if (screen == Screen.SETTINGS && state.update == null && !state.isCheckingUpdate) {
            vm.checkForUpdates()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = screen == Screen.CHAT,
        drawerContent = {
            HistoryDrawer(
                chats = state.chats,
                selectedId = state.currentChat.id,
                onNew = { vm.newChat(); screen = Screen.CHAT; scope.launch { drawerState.close() } },
                onOpen = { vm.openChat(it); screen = Screen.CHAT; scope.launch { drawerState.close() } },
                onDelete = vm::deleteChat,
                onSettings = { screen = Screen.SETTINGS; scope.launch { drawerState.close() } }
            )
        }
    ) {
        AnimatedContent(targetState = screen, label = "screen transition") { currentScreen ->
        when (currentScreen) {
            Screen.CHAT -> ChatScreen(
                chat = state.currentChat,
                model = state.settings.model,
                isLoading = state.isLoading,
                error = state.error,
                focusComposer = focusComposer,
                onMenu = { scope.launch { drawerState.open() } },
                onProvider = { showProviders = true },
                onSend = vm::send,
                onComposerFocused = { AssistantBridge.focusComposerRequested = false },
                onDismissError = vm::clearError
            )
            Screen.SETTINGS -> SettingsScreen(
                settings = state.settings,
                models = state.models.map { it.id },
                isLoadingModels = state.isLoadingModels,
                modelError = state.modelError,
                update = state.update,
                isCheckingUpdate = state.isCheckingUpdate,
                updateError = state.updateError,
                onBack = { screen = Screen.CHAT },
                onPickProvider = { showProviders = true },
                onRefreshModels = vm::refreshModels,
                onSelectModel = vm::selectModel,
                onCheckUpdate = vm::checkForUpdates,
                onAbout = { screen = Screen.ABOUT },
                onSave = vm::updateSettings
            )
            Screen.ABOUT -> AboutScreen(onBack = { screen = Screen.CHAT })
        }
        }
    }

    if (showProviders) {
        ProviderSheet(
            selected = state.settings.providerName,
            onDismiss = { showProviders = false },
            onSelect = {
                vm.selectProvider(it)
                if (state.settings.apiKey.isNotBlank()) vm.refreshModels(autoSelectNewest = true)
                showProviders = false
            }
        )
    }
}

@Composable
private fun HistoryDrawer(
    chats: List<Chat>,
    selectedId: Long,
    onNew: () -> Unit,
    onOpen: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onSettings: () -> Unit
) {
    ModalDrawerSheet(modifier = Modifier.width(320.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painterResource(R.drawable.verity_happy_cutout), null,
                Modifier.size(46.dp).clip(CircleShape), contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Verity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
        NavigationDrawerItem(
            label = { Text("Новый чат") },
            selected = false,
            icon = { Icon(Icons.Rounded.Add, null) },
            onClick = onNew,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Row(Modifier.padding(start = 28.dp, top = 20.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.History, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("ИСТОРИЯ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 12.dp)) {
            if (chats.isEmpty()) {
                item { Text("Здесь появятся сохранённые чаты", Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(chats, key = { it.id }) { chat ->
                NavigationDrawerItem(
                    label = { Text(chat.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    selected = chat.id == selectedId,
                    badge = {
                        IconButton(onClick = { onDelete(chat.id) }, Modifier.size(34.dp)) {
                            Icon(Icons.Rounded.DeleteOutline, "Удалить", Modifier.size(19.dp))
                        }
                    },
                    onClick = { onOpen(chat.id) }
                )
            }
        }
        HorizontalDivider()
        NavigationDrawerItem(
            label = { Text("Настройки") },
            selected = false,
            icon = { Icon(Icons.Rounded.Settings, null) },
            onClick = onSettings,
            modifier = Modifier.navigationBarsPadding().padding(12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreen(
    chat: Chat,
    model: String,
    isLoading: Boolean,
    error: String?,
    focusComposer: Boolean,
    onMenu: () -> Unit,
    onProvider: () -> Unit,
    onSend: (String) -> Unit,
    onComposerFocused: () -> Unit,
    onDismissError: () -> Unit
) {
    var input by rememberSaveable(chat.id) { mutableStateOf("") }
    val listState = rememberLazyListState()
    LaunchedEffect(chat.messages.size, isLoading) {
        if (chat.messages.isNotEmpty()) listState.animateScrollToItem(chat.messages.lastIndex + if (isLoading) 1 else 0)
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Surface(onClick = onProvider, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(model, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onMenu) { Icon(Icons.Rounded.Menu, "История") } },
                actions = { IconButton(onClick = onProvider) { Icon(Icons.Rounded.Settings, "Модель и API") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            MessageComposer(
                value = input,
                enabled = !isLoading,
                requestFocus = focusComposer,
                onValueChange = { input = it },
                onFocusHandled = onComposerFocused,
                onSend = { if (input.isNotBlank()) { onSend(input); input = "" } }
            )
        }
    ) { padding ->
        if (chat.messages.isEmpty()) {
            EmptyPet(modifier = Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(chat.messages, key = { it.id }) { MessageBubble(it) }
                if (isLoading) item { TypingBubble() }
            }
        }
    }
    if (error != null) {
        AlertDialog(
            onDismissRequest = onDismissError,
            icon = { Icon(Icons.Rounded.Close, null) },
            title = { Text("Verity не смог ответить") },
            text = { Text(error + "\n\nПроверьте API-ключ, URL и название модели в настройках.") },
            confirmButton = { TextButton(onClick = onDismissError) { Text("Понятно") } }
        )
    }
}

@Composable
private fun EmptyPet(modifier: Modifier) {
    val hop by rememberInfiniteTransition(label = "pet hop").animateFloat(
        initialValue = 0f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(tween(620), RepeatMode.Reverse),
        label = "hop"
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        Image(
            painterResource(R.drawable.verity_happy_cutout), "Verity",
            Modifier.offset(y = hop.dp).size(148.dp).clip(CircleShape), contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun MessageBubble(message: Message) {
    val user = message.role == "user"
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (user) Arrangement.End else Arrangement.Start) {
        if (!user) {
            Image(
                painterResource(R.drawable.verity_happy_cutout), null,
                Modifier.size(34.dp).clip(CircleShape), contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(9.dp))
        }
        Surface(
            color = if (user) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 22.dp, topEnd = 22.dp,
                bottomStart = if (user) 22.dp else 6.dp,
                bottomEnd = if (user) 6.dp else 22.dp
            ),
            modifier = Modifier.widthIn(max = 640.dp)
        ) {
            SelectionContainer { Text(message.content, Modifier.padding(horizontal = 16.dp, vertical = 12.dp), style = MaterialTheme.typography.bodyLarge) }
        }
    }
}

@Composable
private fun TypingBubble() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painterResource(R.drawable.verity_sad_talking_cutout), null,
            Modifier.size(34.dp).clip(CircleShape), contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(9.dp))
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(20.dp)) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(17.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp)); Text("Думаю…")
            }
        }
    }
}

@Composable
private fun MessageComposer(
    value: String,
    enabled: Boolean,
    requestFocus: Boolean,
    onValueChange: (String) -> Unit,
    onFocusHandled: () -> Unit,
    onSend: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
            keyboard?.show()
            onFocusHandled()
        }
    }
    Surface(tonalElevation = 2.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f).focusRequester(focusRequester),
                placeholder = { Text("Сообщение для Verity…") },
                shape = RoundedCornerShape(26.dp),
                maxLines = 6,
                enabled = enabled,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(onClick = onSend, enabled = enabled && value.isNotBlank(), modifier = Modifier.size(52.dp)) {
                Icon(Icons.AutoMirrored.Rounded.Send, "Отправить")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderSheet(selected: String, onDismiss: () -> Unit, onSelect: (Provider) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(query) { Providers.all.filter { it.name.contains(query, ignoreCase = true) } }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxHeight(.88f)) {
            Text("${Providers.all.size} API-провайдер", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
            Text("Выберите пресет или настройте Custom", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                placeholder = { Text("Найти провайдера") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(18.dp)
            )
            LazyColumn(contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 28.dp)) {
                items(filtered) { provider ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable { onSelect(provider) }.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(42.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text(provider.name.take(1), fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(provider.name, fontWeight = FontWeight.SemiBold)
                            Text(provider.defaultModel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (provider.name == selected) Icon(Icons.Rounded.Check, "Выбрано", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelSheet(
    selected: String,
    models: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(query, models) {
        models.filter { it.contains(query.trim(), ignoreCase = true) }
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxHeight(.82f)) {
            Text(
                "Выберите модель",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Text(
                "Новые модели находятся через API и появляются здесь автоматически",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                placeholder = { Text("Найти модель") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(18.dp)
            )
            LazyColumn(contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 28.dp)) {
                items(filtered) { model ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable { onSelect(model) }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(42.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text(model.take(1).uppercase(), fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(12.dp))
                        Text(model, Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (model == models.firstOrNull()) {
                            Text("NEW", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                        }
                        if (model == selected) Icon(Icons.Rounded.Check, "Выбрано", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    fun openTelegram(username: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$username")))
    }
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("О приложении", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).navigationBarsPadding().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            item {
            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 620.dp),
                shape = RoundedCornerShape(40.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = .72f),
                        shape = RoundedCornerShape(48.dp)
                    ) {
                        Image(
                            painterResource(R.drawable.verity_happy_cutout),
                            "Verity",
                            Modifier.size(170.dp).padding(12.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Verity",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Версия 0.1.0",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .72f)
                    )
                }
            }
            }
            item { Spacer(Modifier.height(28.dp)) }
            item {
            Column(Modifier.fillMaxWidth().widthIn(max = 620.dp).padding(bottom = 24.dp)) {
                Text(
                    "Команда",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 14.dp)
                )
                MaterialDeveloperCard(
                    name = "Samrat",
                    role = "Разработчик",
                    username = "@Beketov_samrat",
                    avatarRes = R.drawable.developer_samrat,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = { openTelegram("Beketov_samrat") }
                )
                Spacer(Modifier.height(12.dp))
                MaterialDeveloperCard(
                    name = "Мистер Бизнес",
                    role = "Создатель",
                    username = "@Denkvant50",
                    avatarRes = R.drawable.developer_mister_business,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    onClick = { openTelegram("Denkvant50") }
                )
                Spacer(Modifier.height(12.dp))
                MaterialDeveloperCard(
                    name = "Николай",
                    role = "Спрайтер и тестер",
                    username = null,
                    avatarRes = null,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    onClick = null
                )
            }
            }
        }
    }
}

@Composable
private fun MaterialDeveloperCard(
    name: String,
    role: String,
    username: String?,
    avatarRes: Int?,
    containerColor: Color,
    onClick: (() -> Unit)?
) {
    Surface(
        modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        color = containerColor,
        shape = RoundedCornerShape(30.dp)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = .8f)
            ) {
                if (avatarRes != null) {
                    Image(
                        painterResource(avatarRes),
                        name,
                        Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Н", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(role, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (username != null) {
                    Text(username, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .72f))
                }
            }
            if (onClick != null) Text("↗", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    settings: AppSettings,
    models: List<String>,
    isLoadingModels: Boolean,
    modelError: String?,
    update: ReleaseUpdate?,
    isCheckingUpdate: Boolean,
    updateError: String?,
    onBack: () -> Unit,
    onPickProvider: () -> Unit,
    onRefreshModels: (Boolean) -> Unit,
    onSelectModel: (String) -> Unit,
    onCheckUpdate: () -> Unit,
    onAbout: () -> Unit,
    onSave: (AppSettings) -> Unit
) {
    var draft by remember(settings) { mutableStateOf(settings) }
    var saved by remember { mutableStateOf(false) }
    var showModelPicker by rememberSaveable { mutableStateOf(false) }
    val modelChoices = remember(models, draft.providerName, draft.model) {
        val recommended = Providers.all.firstOrNull { it.name == draft.providerName }?.recommendedModels.orEmpty()
        (models + recommended + draft.model).filter { it.isNotBlank() }.distinct()
    }
    val context = LocalContext.current
    var overlayActive by rememberSaveable { mutableStateOf(false) }
    val overlayLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Settings.canDrawOverlays(context)) {
            ContextCompat.startForegroundService(context, Intent(context, FloatingAssistantService::class.java))
            overlayActive = true
        }
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val microphoneLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    fun enableOverlay() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            microphoneLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Settings.canDrawOverlays(context)) {
            ContextCompat.startForegroundService(context, Intent(context, FloatingAssistantService::class.java))
            overlayActive = true
        } else {
            overlayLauncher.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Настройки", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Назад") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(36.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(Modifier.fillMaxWidth().padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painterResource(R.drawable.ic_veriti),
                            "Иконка Verity",
                            Modifier.size(76.dp).clip(RoundedCornerShape(24.dp))
                        )
                        Spacer(Modifier.width(18.dp))
                        Column {
                            Text("Verity", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("Настройки приложения", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item { SectionTitle("ИИ И API") }
            item {
                SettingsCard {
                    Text("Провайдер", style = MaterialTheme.typography.labelLarge)
                    OutlinedButton(onClick = onPickProvider, Modifier.fillMaxWidth()) {
                        Text(draft.providerName, Modifier.weight(1f)); Icon(Icons.Rounded.AutoAwesome, null)
                    }
                    OutlinedTextField(
                        value = draft.baseUrl,
                        onValueChange = { draft = draft.copy(baseUrl = it); saved = false },
                        label = { Text("Base URL") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = draft.apiKey,
                        onValueChange = { draft = draft.copy(apiKey = it); saved = false },
                        label = { Text("API-ключ") }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()
                    )
                    Text("Модель", style = MaterialTheme.typography.labelLarge)
                    OutlinedButton(onClick = { showModelPicker = true }, Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Text(draft.model, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Нажмите, чтобы выбрать", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Rounded.AutoAwesome, null)
                    }
                    OutlinedButton(
                        onClick = {
                            onSave(draft)
                            onRefreshModels(true)
                        },
                        enabled = !isLoadingModels,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoadingModels) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (isLoadingModels) "Получаю модели…" else "Найти новые модели")
                    }
                    if (models.isNotEmpty()) {
                        Text(
                            "Новые модели добавлены в список выбора",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (modelError != null) {
                        Text(modelError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { onSave(draft); saved = true }, Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Check, null); Spacer(Modifier.width(8.dp)); Text("Сохранить")
                    }
                    AnimatedVisibility(saved) { Text("Сохранено локально на устройстве", color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall) }
                }
            }
            item { SectionTitle("АССИСТЕНТ НА ЭКРАНЕ") }
            item {
                SettingsCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painterResource(R.drawable.verity_happy_cutout), null,
                            Modifier.size(54.dp).clip(CircleShape), contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Летающий Verity", fontWeight = FontWeight.SemiBold)
                            Text("Нажмите — слушает. Бросьте — летит и отскакивает", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (!overlayActive) {
                        Button(onClick = { enableOverlay() }, Modifier.fillMaxWidth()) { Text("Включить на экране") }
                    } else {
                        OutlinedButton(
                            onClick = {
                                context.stopService(Intent(context, FloatingAssistantService::class.java)); overlayActive = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Выключить") }
                    }
                }
            }
            item {
                Text(
                    "Verity отправляет сообщения только выбранному вами API. История и ключ сохраняются локально.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            item { SectionTitle("ОБНОВЛЕНИЯ") }
            item {
                SettingsCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Info, null, Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Обновление Verity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("GitHub Releases", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (isCheckingUpdate) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp)); Text("Проверяю…")
                        }
                    } else if (update != null) {
                        if (update.hasApk) {
                            Text("Доступно обновление: ${update.name}", style = MaterialTheme.typography.bodyLarge)
                            Button(
                                onClick = {
                                    update.apkUrl?.let { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Скачать APK") }
                        } else {
                            Text("Есть обновление,оно вам не нужно", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    if (updateError != null) {
                        Text(updateError, color = MaterialTheme.colorScheme.error)
                    }
                    OutlinedButton(onClick = onCheckUpdate, enabled = !isCheckingUpdate, modifier = Modifier.fillMaxWidth()) {
                        Text("Проверить ещё раз")
                    }
                }
            }
            item {
                Card(
                    onClick = onAbout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = RoundedCornerShape(22.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = .72f)
                        ) {
                            Image(
                                painterResource(R.drawable.verity_happy_cutout),
                                null,
                                Modifier.padding(7.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(Modifier.width(18.dp))
                        Column(Modifier.weight(1f)) {
                            Text("О приложении", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Verity · разработчики", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Rounded.Info, null, Modifier.size(28.dp))
                    }
                }
            }
        }
    }

    if (showModelPicker) {
        ModelSheet(
            selected = draft.model,
            models = modelChoices,
            onDismiss = { showModelPicker = false },
            onSelect = { model ->
                draft = draft.copy(model = model)
                onSelectModel(model)
                saved = true
                showModelPicker = false
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f))) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}
