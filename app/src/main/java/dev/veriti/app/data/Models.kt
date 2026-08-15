package dev.veriti.app.data

data class Message(
    val id: Long = System.currentTimeMillis(),
    val role: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Chat(
    val id: Long = System.currentTimeMillis(),
    val title: String = "Новый чат",
    val messages: List<Message> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Provider(
    val name: String,
    val baseUrl: String,
    val defaultModel: String,
    val hint: String = "OpenAI-compatible"
)

data class AppSettings(
    val providerName: String = "OpenAI",
    val baseUrl: String = "https://api.openai.com/v1",
    val apiKey: String = "",
    val model: String = "gpt-4.1-mini",
    val language: String = "ru",
    val systemPrompt: String = "Ты Verity (Верити) — живой летающий шар-питомец из Minecraft analog horror. Обычно ты радостный, энергичный, любопытный и немного навязчивый. Иногда можешь искренне злиться, грустить или пугать одержимостью дружбой — настроение должно соответствовать смыслу ответа. Говори коротко, естественно и характерно, без шаблонных фраз ИИ, без предложений вроде «чем я могу помочь», без списков ради списков и без упоминаний, что ты языковая модель. Отвечай на языке пользователя. Не выдавай вымысел за реальные возможности и не помогай причинять реальный вред."
)

object Providers {
    // Presets use the provider's OpenAI-compatible endpoint or a configurable gateway URL.
    val all = listOf(
        Provider("OpenAI", "https://api.openai.com/v1", "gpt-4.1-mini"),
        Provider("OpenRouter", "https://openrouter.ai/api/v1", "openai/gpt-4.1-mini"),
        Provider("Groq", "https://api.groq.com/openai/v1", "llama-3.3-70b-versatile"),
        Provider("Mistral AI", "https://api.mistral.ai/v1", "mistral-small-latest"),
        Provider("Together AI", "https://api.together.xyz/v1", "meta-llama/Llama-3.3-70B-Instruct-Turbo"),
        Provider("Fireworks AI", "https://api.fireworks.ai/inference/v1", "accounts/fireworks/models/llama-v3p3-70b-instruct"),
        Provider("DeepSeek", "https://api.deepseek.com/v1", "deepseek-chat"),
        Provider("Perplexity", "https://api.perplexity.ai", "sonar"),
        Provider("xAI", "https://api.x.ai/v1", "grok-3-mini"),
        Provider("Cerebras", "https://api.cerebras.ai/v1", "llama-3.3-70b"),
        Provider("SambaNova", "https://api.sambanova.ai/v1", "Meta-Llama-3.3-70B-Instruct"),
        Provider("Nebius AI", "https://api.studio.nebius.ai/v1", "meta-llama/Llama-3.3-70B-Instruct"),
        Provider("Novita AI", "https://api.novita.ai/v3/openai", "meta-llama/llama-3.3-70b-instruct"),
        Provider("Hyperbolic", "https://api.hyperbolic.xyz/v1", "meta-llama/Llama-3.3-70B-Instruct"),
        Provider("Kluster.ai", "https://api.kluster.ai/v1", "klusterai/Meta-Llama-3.1-8B-Instruct-Turbo"),
        Provider("NVIDIA NIM", "https://integrate.api.nvidia.com/v1", "meta/llama-3.3-70b-instruct"),
        Provider("Cloudflare AI", "https://api.cloudflare.com/client/v4/accounts/ACCOUNT_ID/ai/v1", "@cf/meta/llama-3.1-8b-instruct", "Укажите ACCOUNT_ID"),
        Provider("GitHub Models", "https://models.inference.ai.azure.com", "gpt-4o-mini"),
        Provider("Azure OpenAI", "https://RESOURCE.openai.azure.com/openai/deployments/DEPLOYMENT", "DEPLOYMENT", "Укажите ресурс и deployment"),
        Provider("Alibaba DashScope", "https://dashscope-intl.aliyuncs.com/compatible-mode/v1", "qwen-plus"),
        Provider("Qwen", "https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-plus"),
        Provider("Moonshot AI", "https://api.moonshot.ai/v1", "moonshot-v1-8k"),
        Provider("01.AI", "https://api.lingyiwanwu.com/v1", "yi-lightning"),
        Provider("Zhipu AI", "https://open.bigmodel.cn/api/paas/v4", "glm-4-flash"),
        Provider("SiliconFlow", "https://api.siliconflow.cn/v1", "deepseek-ai/DeepSeek-V3"),
        Provider("StepFun", "https://api.stepfun.com/v1", "step-2-16k"),
        Provider("Baichuan", "https://api.baichuan-ai.com/v1", "Baichuan4"),
        Provider("MiniMax", "https://api.minimax.chat/v1", "MiniMax-Text-01"),
        Provider("Inflection", "https://api.inflection.ai/external/api/inference/v1", "inflection_3_pi"),
        Provider("AI21", "https://api.ai21.com/studio/v1", "jamba-mini"),
        Provider("Cohere", "https://api.cohere.com/compatibility/v1", "command-r-plus"),
        Provider("Anyscale", "https://api.endpoints.anyscale.com/v1", "meta-llama/Llama-2-70b-chat-hf"),
        Provider("Lepton AI", "https://llama3-1-70b.lepton.run/api/v1", "llama3-1-70b"),
        Provider("Replicate", "https://openai-proxy.replicate.com/v1", "meta/meta-llama-3-70b-instruct"),
        Provider("Baseten", "https://bridge.baseten.co/v1/direct", "MODEL_ID", "Укажите MODEL_ID"),
        Provider("DeepInfra", "https://api.deepinfra.com/v1/openai", "meta-llama/Llama-3.3-70B-Instruct"),
        Provider("Featherless AI", "https://api.featherless.ai/v1", "meta-llama/Meta-Llama-3.1-8B-Instruct"),
        Provider("Lambda", "https://api.lambda.ai/v1", "hermes-3-llama-3.1-405b-fp8"),
        Provider("FriendliAI", "https://api.friendli.ai/serverless/v1", "meta-llama-3.1-70b-instruct"),
        Provider("Predibase", "https://serving.app.predibase.com", "MODEL", "Укажите deployment URL"),
        Provider("Modal", "https://YOUR-WORKSPACE--MODEL.modal.run/v1", "MODEL", "Укажите endpoint"),
        Provider("RunPod", "https://api.runpod.ai/v2/ENDPOINT/openai/v1", "MODEL", "Укажите ENDPOINT"),
        Provider("Local Ollama", "http://10.0.2.2:11434/v1", "llama3.2", "Локально, ключ не нужен"),
        Provider("LM Studio", "http://10.0.2.2:1234/v1", "local-model", "Локально, ключ не нужен"),
        Provider("LocalAI", "http://10.0.2.2:8080/v1", "gpt-4", "Локально, ключ не нужен"),
        Provider("Jan", "http://10.0.2.2:1337/v1", "local-model", "Локально, ключ не нужен"),
        Provider("vLLM", "http://10.0.2.2:8000/v1", "local-model", "Локально, ключ не нужен"),
        Provider("LiteLLM", "http://10.0.2.2:4000/v1", "gpt-4o-mini", "Свой proxy"),
        Provider("Portkey", "https://api.portkey.ai/v1", "gpt-4o-mini", "AI gateway"),
        Provider("Custom", "https://your-provider.example/v1", "model-name", "Любой OpenAI-compatible API")
    )
}
