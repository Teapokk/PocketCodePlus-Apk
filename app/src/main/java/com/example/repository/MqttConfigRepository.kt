package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.model.MqttConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

sealed class ConnectionTestResult {
  data class Success(val latencyMs: Long, val message: String) : ConnectionTestResult()
  data class Failure(val errorMessage: String) : ConnectionTestResult()
}

class MqttConfigRepository(private val context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  private val _config = MutableStateFlow(loadConfig())
  val config: StateFlow<MqttConfig> = _config.asStateFlow()

  private fun loadConfig(): MqttConfig {
    return MqttConfig(
      host = prefs.getString(KEY_HOST, "broker.emqx.io") ?: "broker.emqx.io",
      port = prefs.getInt(KEY_PORT, 1883),
      username = prefs.getString(KEY_USERNAME, "") ?: "",
      password = prefs.getString(KEY_PASSWORD, "") ?: "",
      clientId = prefs.getString(KEY_CLIENT_ID, MqttConfig.generateDefaultClientId())
        ?: MqttConfig.generateDefaultClientId(),
      useSsl = prefs.getBoolean(KEY_USE_SSL, false),
      keepAliveSeconds = prefs.getInt(KEY_KEEPALIVE, 60),
      cleanSession = prefs.getBoolean(KEY_CLEAN_SESSION, true),
      defaultTopic = prefs.getString(KEY_DEFAULT_TOPIC, "pocketcode/demo/game") ?: "pocketcode/demo/game"
    )
  }

  fun saveConfig(newConfig: MqttConfig) {
    prefs.edit().apply {
      putString(KEY_HOST, newConfig.host.trim())
      putInt(KEY_PORT, newConfig.port)
      putString(KEY_USERNAME, newConfig.username.trim())
      putString(KEY_PASSWORD, newConfig.password)
      putString(KEY_CLIENT_ID, newConfig.clientId.trim())
      putBoolean(KEY_USE_SSL, newConfig.useSsl)
      putInt(KEY_KEEPALIVE, newConfig.keepAliveSeconds)
      putBoolean(KEY_CLEAN_SESSION, newConfig.cleanSession)
      putString(KEY_DEFAULT_TOPIC, newConfig.defaultTopic.trim())
      apply()
    }
    _config.value = newConfig
    currentInstance = this
  }

  fun resetToDefaults(): MqttConfig {
    val defaultCfg = MqttConfig()
    saveConfig(defaultCfg)
    return defaultCfg
  }

  companion object {
    suspend fun testBrokerConnection(config: MqttConfig): ConnectionTestResult {
      return withContext(Dispatchers.IO) {
        val host = config.host.trim()
        val port = config.port

        if (host.isBlank()) {
          return@withContext ConnectionTestResult.Failure("Host address cannot be blank.")
        }
        if (port !in 1..65535) {
          return@withContext ConnectionTestResult.Failure("Port must be between 1 and 65535.")
        }

        val startTime = System.currentTimeMillis()
        val socket = Socket()
        try {
          val socketAddress = InetSocketAddress(host, port)
          socket.connect(socketAddress, 4000)
          val latency = System.currentTimeMillis() - startTime
          socket.close()

          val authInfo = if (config.hasAuth) {
            "Authenticated as '${config.username}'"
          } else {
            "Anonymous access"
          }
          ConnectionTestResult.Success(
            latencyMs = latency,
            message = "Successfully reached $host:$port in ${latency}ms ($authInfo)."
          )
        } catch (e: Exception) {
          try {
            socket.close()
          } catch (_: Exception) {}
          ConnectionTestResult.Failure("Connection failed: ${e.localizedMessage ?: e.javaClass.simpleName}")
        }
      }
    }

    private const val PREFS_NAME = "pocket_code_mqtt_settings"
    private const val KEY_HOST = "mqtt_host"
    private const val KEY_PORT = "mqtt_port"
    private const val KEY_USERNAME = "mqtt_username"
    private const val KEY_PASSWORD = "mqtt_password"
    private const val KEY_CLIENT_ID = "mqtt_client_id"
    private const val KEY_USE_SSL = "mqtt_use_ssl"
    private const val KEY_KEEPALIVE = "mqtt_keepalive"
    private const val KEY_CLEAN_SESSION = "mqtt_clean_session"
    private const val KEY_DEFAULT_TOPIC = "mqtt_default_topic"

    @Volatile
    private var currentInstance: MqttConfigRepository? = null

    fun getInstance(context: Context): MqttConfigRepository {
      return currentInstance ?: synchronized(this) {
        currentInstance ?: MqttConfigRepository(context.applicationContext).also {
          currentInstance = it
        }
      }
    }
  }
}
