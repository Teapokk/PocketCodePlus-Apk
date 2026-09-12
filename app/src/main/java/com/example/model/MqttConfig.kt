package com.example.model

/**
 * Configuration model for MQTT broker connection details.
 */
data class MqttConfig(
  val host: String = "broker.emqx.io",
  val port: Int = 1883,
  val username: String = "",
  val password: String = "",
  val clientId: String = generateDefaultClientId(),
  val useSsl: Boolean = false,
  val keepAliveSeconds: Int = 60,
  val cleanSession: Boolean = true,
  val defaultTopic: String = "pocketcode/demo/game"
) {
  val uriString: String
    get() {
      val scheme = if (useSsl) "ssl" else "tcp"
      return "$scheme://$host:$port"
    }

  val hasAuth: Boolean
    get() = username.isNotBlank() || password.isNotBlank()

  companion object {
    fun generateDefaultClientId(): String {
      val randomNum = (1000..9999).random()
      return "PocketCodePlus_$randomNum"
    }

    val PRESET_EMQX = MqttConfig(
      host = "broker.emqx.io",
      port = 1883,
      username = "",
      password = "",
      useSsl = false,
      defaultTopic = "pocketcode/demo/game"
    )

    val PRESET_MOSQUITTO = MqttConfig(
      host = "test.mosquitto.org",
      port = 1883,
      username = "",
      password = "",
      useSsl = false,
      defaultTopic = "pocketcode/mosquitto/test"
    )

    val PRESET_HIVEMQ = MqttConfig(
      host = "broker.hivemq.com",
      port = 1883,
      username = "",
      password = "",
      useSsl = false,
      defaultTopic = "pocketcode/hivemq/test"
    )

    val PRESET_LOCAL = MqttConfig(
      host = "192.168.1.100",
      port = 1883,
      username = "",
      password = "",
      useSsl = false,
      defaultTopic = "pocketcode/lan/game"
    )
  }
}
