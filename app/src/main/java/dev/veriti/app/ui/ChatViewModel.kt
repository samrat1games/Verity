package dev.veriti.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.veriti.app.data.AppSettings
import dev.veriti.app.data.AssistantBridge
import dev.veriti.app.data.Chat
import dev.veriti.app.data.ChatStore
import dev.veriti.app.data.Message
import dev.veriti.app.data.Provider
import dev.veriti.app.network.AiClient
import dev.veriti.app.network.RemoteModel
import dev.veriti.app.network.ReleaseUpdate
import dev.veriti.app.network.UpdateChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatUiState(
    val chats: List<Chat> = emptyList(),
    val currentChat: Chat = Chat(),
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = false,
    val models: List<RemoteModel> = emptyList(),
    val isLoadingModels: Boolean = false,
    val modelError: String? = null,
    val update: ReleaseUpdate? = null,
    val isCheckingUpdate: Boolean = false,
    val updateError: String? = null,
    val error: String? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val store = ChatStore(application)
    private val client = AiClient()
    private val updateChecker = UpdateChecker()
    var state by mutableStateOf(
        ChatUiState(chats = store.loadChats(), settings = store.loadSettings())
    )
        private set

    fun newChat() {
        state = state.copy(currentChat = Chat(), error = null)
    }

    fun openChat(id: Long) {
        state.chats.firstOrNull { it.id == id }?.let { state = state.copy(currentChat = it, error = null) }
    }

    fun deleteChat(id: Long) {
        val updated = state.chats.filterNot { it.id == id }
        store.saveChats(updated)
        state = state.copy(
            chats = updated,
            currentChat = if (state.currentChat.id == id) Chat() else state.currentChat
        )
    }

    fun selectProvider(provider: Provider) {
        updateSettings(
            state.settings.copy(
                providerName = provider.name,
                baseUrl = provider.baseUrl,
                model = provider.defaultModel
            )
        )
        state = state.copy(models = emptyList(), modelError = null)
    }

    fun selectModel(model: String) = updateSettings(state.settings.copy(model = model))

    fun refreshModels(autoSelectNewest: Boolean = true) {
        if (state.isLoadingModels) return
        state = state.copy(isLoadingModels = true, modelError = null)
        val settings = state.settings
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { client.listModels(settings) } }
                .onSuccess { models ->
                    val updatedSettings = if (autoSelectNewest && models.isNotEmpty()) {
                        settings.copy(model = models.first().id)
                    } else settings
                    if (updatedSettings != settings) store.saveSettings(updatedSettings)
                    state = state.copy(
                        settings = updatedSettings,
                        models = models,
                        isLoadingModels = false,
                        modelError = if (models.isEmpty()) "API вернул пустой список моделей" else null
                    )
                }
                .onFailure {
                    state = state.copy(isLoadingModels = false, modelError = it.message ?: "Не удалось получить модели")
                }
        }
    }

    fun updateSettings(settings: AppSettings) {
        store.saveSettings(settings)
        state = state.copy(settings = settings)
    }

    fun clearError() { state = state.copy(error = null) }

    fun checkForUpdates() {
        if (state.isCheckingUpdate) return
        state = state.copy(isCheckingUpdate = true, updateError = null)
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { updateChecker.check() } }
                .onSuccess { state = state.copy(update = it, isCheckingUpdate = false) }
                .onFailure {
                    state = state.copy(
                        isCheckingUpdate = false,
                        updateError = it.message ?: "Не удалось проверить обновление"
                    )
                }
        }
    }

    fun send(text: String) {
        val content = text.trim()
        if (content.isEmpty() || state.isLoading) return
        val userMessage = Message(role = "user", content = content)
        val base = state.currentChat
        val title = if (base.messages.isEmpty()) content.replace('\n', ' ').take(42) else base.title
        val pending = base.copy(
            title = title,
            messages = base.messages + userMessage,
            updatedAt = System.currentTimeMillis()
        )
        // Verity keeps a compact long-term memory across separate chats like a persistent pet.
        val rememberedHistory = state.chats
            .filterNot { it.id == base.id }
            .sortedByDescending { it.updatedAt }
            .take(5)
            .flatMap { it.messages.takeLast(6) }
            .takeLast(24) + pending.messages
        persist(pending)
        state = state.copy(currentChat = pending, isLoading = true, error = null)

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { client.complete(state.settings, rememberedHistory) }
            }.onSuccess { answer ->
                val complete = pending.copy(
                    messages = pending.messages + Message(role = "assistant", content = answer.text),
                    updatedAt = System.currentTimeMillis()
                )
                persist(complete)
                state = state.copy(currentChat = complete, isLoading = false)
                AssistantBridge.deliverAssistantReply(answer.text, answer.mood)
            }.onFailure { throwable ->
                state = state.copy(
                    isLoading = false,
                    error = throwable.message ?: "Не удалось получить ответ"
                )
                AssistantBridge.deliverAssistantReply("Не получилось связаться. Проверь настройки и попробуй ещё раз.", "sad")
            }
        }
    }

    private fun persist(chat: Chat) {
        val chats = (state.chats.filterNot { it.id == chat.id } + chat).sortedByDescending { it.updatedAt }
        store.saveChats(chats)
        state = state.copy(chats = chats)
    }
}
