package dev.veriti.app.network

import dev.veriti.app.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ReleaseUpdate(
    val name: String,
    val version: String,
    val pageUrl: String,
    val apkUrl: String? = null,
    val apkName: String? = null,
    val isUpToDate: Boolean = false
) {
    val hasApk: Boolean get() = !apkUrl.isNullOrBlank()
}

class UpdateChecker {
    fun check(): ReleaseUpdate {
        val connection = URL(RELEASE_API).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 20_000
            connection.readTimeout = 25_000
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "VerityDroid/${BuildConfig.VERSION_NAME}")
            val code = connection.responseCode
            val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) error("GitHub $code")
            val json = JSONObject(body)
            val assets = json.optJSONArray("assets")
            var apkUrl: String? = null
            var apkName: String? = null
            if (assets != null) {
                for (index in 0 until assets.length()) {
                    val asset = assets.getJSONObject(index)
                    val name = asset.optString("name")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url").takeIf { it.isNotBlank() }
                        apkName = name
                        break
                    }
                }
            }
            val remoteVersion = json.optString("tag_name")
                .ifBlank { json.optString("name", "0.0.0") }
                .removePrefix("v")
                .trim()
            return ReleaseUpdate(
                name = json.optString("name", remoteVersion),
                version = remoteVersion,
                pageUrl = json.optString("html_url", RELEASE_PAGE),
                apkUrl = apkUrl,
                apkName = apkName,
                isUpToDate = compareVersions(BuildConfig.VERSION_NAME, remoteVersion) >= 0
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun compareVersions(local: String, remote: String): Int {
        val left = local.removePrefix("v").split('.').map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
        val right = remote.removePrefix("v").split('.').map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
        repeat(maxOf(left.size, right.size)) { index ->
            val result = (left.getOrNull(index) ?: 0).compareTo(right.getOrNull(index) ?: 0)
            if (result != 0) return result
        }
        return 0
    }

    companion object {
        const val RELEASE_PAGE = "https://github.com/samrat1games/Verity/releases/latest"
        private const val RELEASE_API = "https://api.github.com/repos/samrat1games/Verity/releases/latest"
    }
}
