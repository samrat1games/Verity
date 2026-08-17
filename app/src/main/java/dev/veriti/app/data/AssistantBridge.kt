package dev.veriti.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Transfers a recognized overlay phrase into the active Compose chat. */
object AssistantBridge {
    var pendingVoiceText by mutableStateOf<String?>(null)
    var pendingNewChat by mutableStateOf(false)
    var focusComposerRequested by mutableStateOf(false)
    var chatRevision by mutableStateOf(0L)
    var lastOverlayChatId: Long? = null
    var onAssistantReply: ((String, String) -> Unit)? = null

    fun deliverAssistantReply(text: String, mood: String) {
        onAssistantReply?.invoke(text, mood)
    }

    fun notifyOverlayChatChanged(chatId: Long) {
        lastOverlayChatId = chatId
        chatRevision++
    }
}
