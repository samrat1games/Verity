package dev.veriti.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dev.veriti.app.MainActivity
import dev.veriti.app.R
import dev.veriti.app.data.AssistantBridge
import dev.veriti.app.data.Chat
import dev.veriti.app.data.ChatStore
import dev.veriti.app.data.Message
import dev.veriti.app.network.AiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.random.Random

class FloatingAssistantService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayRoot: LinearLayout
    private lateinit var character: ImageView
    private lateinit var speechCard: LinearLayout
    private lateinit var speechText: TextView
    private lateinit var moreButton: Button
    private var bubble: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var physicsFrame: Runnable? = null
    private var speechPages: List<String> = emptyList()
    private var speechPage = 0
    private var listening = false
    private var requestInFlight = false
    private var overlayChatId: Long? = null
    private var startNewOverlayChat = false
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val chatStore by lazy { ChatStore(applicationContext) }
    private val aiClient = AiClient()

    private val wanderRunnable = object : Runnable {
        override fun run() {
            val params = layoutParams
            if (params != null && physicsFrame == null && !listening && speechCard.visibility != View.VISIBLE) {
                val vx = Random.nextInt(-650, 651).toFloat().let { if (abs(it) < 180) 260f else it }
                val vy = Random.nextInt(-360, 361).toFloat()
                hop()
                startPhysics(overlayRoot, params, vx, vy)
            }
            handler.postDelayed(this, Random.nextLong(4_500L, 8_000L))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        startForeground(
            NOTIFICATION_ID,
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_veriti_foreground)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.overlay_active))
                .setContentIntent(openIntent)
                .setOngoing(true)
                .build()
        )
        showBubble()
        prepareSpeechRecognizer()
        AssistantBridge.onAssistantReply = { text, mood -> handler.post { showSpeech(text, mood) } }
        handler.postDelayed(wanderRunnable, 3_000L)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun showBubble() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        speechText = TextView(this).apply {
            setTextColor(Color.rgb(33, 28, 38))
            textSize = 15f
            maxLines = 6
            setLineSpacing(0f, 1.08f)
            setOnClickListener {
                AssistantBridge.focusComposerRequested = true
                openApp()
            }
        }
        moreButton = Button(this).apply {
            text = "Ещё"
            isAllCaps = false
            setOnClickListener { showNextSpeechPage() }
        }
        val closeButton = Button(this).apply {
            text = "Закрыть"
            isAllCaps = false
            setOnClickListener {
                closeSpeech()
                AssistantBridge.pendingNewChat = true
                startNewOverlayChat = true
                overlayChatId = null
            }
        }
        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            addView(moreButton, LinearLayout.LayoutParams(0, dp(44), 1f))
            addView(closeButton, LinearLayout.LayoutParams(0, dp(44), 1f))
        }
        speechCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(10), dp(8))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(18).toFloat()
                setColor(Color.rgb(250, 246, 255))
                setStroke(dp(1), Color.rgb(211, 199, 226))
            }
            elevation = dp(8).toFloat()
            visibility = View.GONE
            addView(speechText, LinearLayout.LayoutParams(dp(218), LinearLayout.LayoutParams.WRAP_CONTENT))
            addView(actions, LinearLayout.LayoutParams(dp(218), LinearLayout.LayoutParams.WRAP_CONTENT))
        }
        character = ImageView(this).apply {
            setImageResource(R.drawable.verity_happy_cutout)
            scaleType = ImageView.ScaleType.CENTER_CROP
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
            clipToOutline = true
            elevation = dp(14).toFloat()
            contentDescription = "Поговорить с Verity"
        }
        overlayRoot = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(speechCard, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = dp(8)
            })
            addView(character, LinearLayout.LayoutParams(dp(82), dp(82)))
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = resources.displayMetrics.widthPixels - dp(92)
            y = resources.displayMetrics.heightPixels / 3
        }
        layoutParams = params
        character.setOnTouchListener(DragTouchListener(params))
        windowManager.addView(overlayRoot, params)
        bubble = overlayRoot
    }

    private inner class DragTouchListener(private val params: WindowManager.LayoutParams) : View.OnTouchListener {
        private var startX = 0
        private var startY = 0
        private var touchX = 0f
        private var touchY = 0f
        private var previousX = 0f
        private var previousY = 0f
        private var previousTime = 0L
        private var velocityX = 0f
        private var velocityY = 0f

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    stopPhysics()
                    startX = params.x; startY = params.y
                    touchX = event.rawX; touchY = event.rawY
                    previousX = event.rawX; previousY = event.rawY
                    previousTime = event.eventTime
                    velocityX = 0f; velocityY = 0f
                    view.animate().scaleX(1.08f).scaleY(1.08f).setDuration(90).start()
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val elapsed = (event.eventTime - previousTime).coerceAtLeast(1L) / 1000f
                    velocityX = (event.rawX - previousX) / elapsed
                    velocityY = (event.rawY - previousY) / elapsed
                    previousX = event.rawX; previousY = event.rawY; previousTime = event.eventTime
                    params.x = startX + (event.rawX - touchX).toInt()
                    params.y = startY + (event.rawY - touchY).toInt()
                    windowManager.updateViewLayout(overlayRoot, params)
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                    if (abs(event.rawX - touchX) < dp(8) && abs(event.rawY - touchY) < dp(8)) startListening()
                    else startPhysics(overlayRoot, params, velocityX, velocityY)
                    return true
                }
                MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                    return true
                }
            }
            return false
        }
    }

    private fun startPhysics(view: View, params: WindowManager.LayoutParams, initialVelocityX: Float, initialVelocityY: Float) {
        stopPhysics()
        var vx = initialVelocityX.coerceIn(-6000f, 6000f)
        var vy = initialVelocityY.coerceIn(-6000f, 6000f)
        var previousNanos = System.nanoTime()
        lateinit var frame: Runnable
        frame = Runnable {
            val now = System.nanoTime()
            val dt = ((now - previousNanos) / 1_000_000_000f).coerceIn(0f, .033f)
            previousNanos = now
            params.x += (vx * dt).toInt()
            params.y += (vy * dt).toInt()
            val maxX = (resources.displayMetrics.widthPixels - view.width).coerceAtLeast(0)
            val maxY = (resources.displayMetrics.heightPixels - view.height).coerceAtLeast(0)
            var bounced = false
            if (params.x <= 0 || params.x >= maxX) {
                params.x = params.x.coerceIn(0, maxX); vx *= -0.72f; bounced = true
            }
            if (params.y <= 0 || params.y >= maxY) {
                params.y = params.y.coerceIn(0, maxY); vy *= -0.72f; bounced = true
            }
            if (bounced) showOuch()
            vx *= 0.975f; vy *= 0.975f
            runCatching { windowManager.updateViewLayout(view, params) }
            if (hypot(vx, vy) > 28f) handler.postDelayed(frame, 16L) else physicsFrame = null
        }
        physicsFrame = frame
        handler.post(frame)
    }

    private fun stopPhysics() {
        physicsFrame?.let(handler::removeCallbacks)
        physicsFrame = null
    }

    private fun showOuch() {
        if (speechCard.visibility == View.VISIBLE || listening) return
        character.setImageResource(R.drawable.verity_ouch_cutout)
        character.animate().cancel()
        character.animate().rotation(12f).scaleX(.84f).scaleY(.9f).setDuration(90L).withEndAction {
            character.animate().rotation(-10f).scaleX(1.08f).scaleY(1.02f).setDuration(90L).withEndAction {
                character.animate().rotation(0f).scaleX(1f).scaleY(1f).setDuration(110L).start()
            }.start()
        }.start()
        handler.removeCallbacks(resetFaceRunnable)
        handler.postDelayed(resetFaceRunnable, 420L)
    }

    private val resetFaceRunnable = Runnable {
        if (!listening && speechCard.visibility != View.VISIBLE) character.setImageResource(R.drawable.verity_happy_cutout)
    }

    private fun hop() {
        character.animate().cancel()
        character.animate().translationY(-dp(20).toFloat()).scaleX(1.05f).scaleY(.94f).setDuration(220L).withEndAction {
            character.animate().translationY(0f).scaleX(.96f).scaleY(1.06f).setDuration(240L).withEndAction {
                character.animate().scaleX(1f).scaleY(1f).setDuration(100L).start()
            }.start()
        }.start()
    }

    private fun showSpeech(text: String, mood: String) {
        speechPages = paginate(text)
        speechPage = 0
        speechText.text = speechPages.firstOrNull().orEmpty()
        moreButton.visibility = if (speechPages.size > 1) View.VISIBLE else View.INVISIBLE
        speechCard.visibility = View.VISIBLE
        character.setImageResource(
            when (mood) {
                "angry" -> R.drawable.verity_angry_cutout
                "sad" -> R.drawable.verity_sad_talking_cutout
                "normal" -> R.drawable.verity_normal_cutout
                else -> R.drawable.verity_happy_cutout
            }
        )
        stopPhysics()
        handler.post {
            val params = layoutParams ?: return@post
            val maxX = (resources.displayMetrics.widthPixels - overlayRoot.width).coerceAtLeast(0)
            params.x = params.x.coerceIn(0, maxX)
            windowManager.updateViewLayout(overlayRoot, params)
        }
    }

    private fun showNextSpeechPage() {
        if (speechPage + 1 >= speechPages.size) return
        speechPage++
        speechText.text = speechPages[speechPage]
        moreButton.visibility = if (speechPage + 1 < speechPages.size) View.VISIBLE else View.INVISIBLE
    }

    private fun closeSpeech() {
        speechCard.visibility = View.GONE
        speechPages = emptyList()
        speechPage = 0
        character.setImageResource(R.drawable.verity_happy_cutout)
    }

    private fun paginate(text: String, pageLength: Int = 230): List<String> {
        if (text.length <= pageLength) return listOf(text)
        val result = mutableListOf<String>()
        var remaining = text.trim()
        while (remaining.isNotEmpty()) {
            if (remaining.length <= pageLength) { result += remaining; break }
            val boundary = remaining.take(pageLength).lastIndexOfAny(charArrayOf(' ', '\n', '.', '!', '?'))
                .takeIf { it > pageLength / 2 } ?: pageLength
            result += remaining.take(boundary + 1).trim()
            remaining = remaining.drop(boundary + 1).trim()
        }
        return result
    }

    private fun prepareSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { setListeningVisual(true) }
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) {
                    val scale = (1f + rmsdB.coerceAtLeast(0f) / 60f).coerceAtMost(1.18f)
                    character.scaleX = scale; character.scaleY = scale
                }
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() { setListeningVisual(false) }
                override fun onError(error: Int) {
                    setListeningVisual(false)
                    val message = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "Я ничего не услышал."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Тут слишком тихо. Скажи ещё раз."
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                        SpeechRecognizer.ERROR_SERVER -> "Связь сломалась. Попробуем ещё раз."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Подожди, я ещё слушаю."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Мне не разрешили слушать микрофон."
                        else -> "Что-то пошло не так. Попробуй ещё раз."
                    }
                    showSpeech(message, "sad")
                }
                override fun onResults(results: Bundle?) {
                    setListeningVisual(false)
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!text.isNullOrBlank()) {
                        speechCard.visibility = View.GONE
                        character.setImageResource(R.drawable.verity_happy_cutout)
                        sendVoiceMessage(text)
                    } else showSpeech("Я ничего не услышал.", "sad")
                }
                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }
    }

    private fun startListening() {
        if (requestInFlight) {
            showSpeech("Подожди немного, я ещё думаю над прошлой фразой.", "normal")
            return
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            showSpeech("Мне нужно разрешение на микрофон, иначе я тебя не услышу.", "sad")
            return
        }
        closeSpeech()
        speechRecognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите с Verity")
        }) ?: showSpeech("На этом устройстве распознавание речи недоступно.", "sad")
    }

    private fun sendVoiceMessage(text: String) {
        val settings = chatStore.loadSettings()
        if (settings.apiKey.isBlank() && !settings.baseUrl.startsWith("http://10.0.2.2")) {
            showSpeech("Сначала добавь API-ключ в настройках. Например, можно быстро подключить Gemini.", "sad")
            return
        }
        requestInFlight = true
        showSpeech("Секунду…", "normal")
        val createNewChat = startNewOverlayChat
        startNewOverlayChat = false
        serviceScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val chats = chatStore.loadChats()
                    val base = if (createNewChat) null else {
                        overlayChatId?.let { id -> chats.firstOrNull { it.id == id } } ?: chats.firstOrNull()
                    } ?: Chat()
                    val userMessage = Message(role = "user", content = text.trim())
                    val pending = base.copy(
                        title = if (base.messages.isEmpty()) text.replace('\n', ' ').take(42) else base.title,
                        messages = base.messages + userMessage,
                        updatedAt = System.currentTimeMillis()
                    )
                    val rememberedHistory = chats
                        .filterNot { it.id == base.id }
                        .sortedByDescending { it.updatedAt }
                        .take(5)
                        .flatMap { it.messages.takeLast(6) }
                        .takeLast(24) + pending.messages
                    chatStore.saveChats((chats.filterNot { it.id == pending.id } + pending).sortedByDescending { it.updatedAt })
                    val answer = aiClient.complete(settings, rememberedHistory)
                    val complete = pending.copy(
                        messages = pending.messages + Message(role = "assistant", content = answer.text),
                        updatedAt = System.currentTimeMillis()
                    )
                    chatStore.saveChats((chats.filterNot { it.id == complete.id } + complete).sortedByDescending { it.updatedAt })
                    Triple(answer.text, answer.mood, complete.id)
                }
            }.onSuccess { (answer, mood, chatId) ->
                overlayChatId = chatId
                AssistantBridge.notifyOverlayChatChanged(chatId)
                showSpeech(answer, mood)
            }.onFailure { error ->
                showSpeech(error.message?.take(220) ?: "Не получилось связаться с API. Проверь настройки.", "sad")
            }
            requestInFlight = false
        }
    }

    private fun setListeningVisual(value: Boolean) {
        listening = value
        character.alpha = if (value) .78f else 1f
        character.setImageResource(if (value) R.drawable.verity_sad_talking_cutout else R.drawable.verity_happy_cutout)
        if (!value) { character.scaleX = 1f; character.scaleY = 1f }
    }

    private fun openApp() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, getString(R.string.overlay_channel), NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(wanderRunnable)
        handler.removeCallbacks(resetFaceRunnable)
        stopPhysics()
        speechRecognizer?.destroy()
        speechRecognizer = null
        AssistantBridge.onAssistantReply = null
        serviceScope.cancel()
        bubble?.let { windowManager.removeView(it) }
        bubble = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "veriti_overlay"
        private const val NOTIFICATION_ID = 77
    }
}
