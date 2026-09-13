package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MqttConfig
import com.example.repository.ConnectionTestResult
import com.example.repository.MqttConfigRepository
import com.example.ui.theme.PocketAccentAmber
import com.example.ui.theme.PocketTealDark
import com.example.ui.theme.PocketTealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MqttConfigScreen(
  initialConfig: MqttConfig,
  onBack: () -> Unit,
  onConfigSaved: (MqttConfig) -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  
  // Form editable states
  var host by remember(initialConfig) { mutableStateOf(initialConfig.host) }
  var portText by remember(initialConfig) { mutableStateOf(initialConfig.port.toString()) }
  var username by remember(initialConfig) { mutableStateOf(initialConfig.username) }
  var password by remember(initialConfig) { mutableStateOf(initialConfig.password) }
  var clientId by remember(initialConfig) { mutableStateOf(initialConfig.clientId) }
  var useSsl by remember(initialConfig) { mutableStateOf(initialConfig.useSsl) }
  var defaultTopic by remember(initialConfig) { mutableStateOf(initialConfig.defaultTopic) }

  var passwordVisible by remember { mutableStateOf(false) }
  var isTestingConnection by remember { mutableStateOf(false) }
  var testResult by remember { mutableStateOf<ConnectionTestResult?>(null) }
  var showAdvanced by remember { mutableStateOf(false) }

  // Validation
  val portInt = portText.toIntOrNull()
  val isPortValid = portInt != null && portInt in 1..65535
  val isHostValid = host.isNotBlank()
  val canSave = isHostValid && isPortValid

  fun buildCurrentConfig(): MqttConfig {
    return MqttConfig(
      host = host.trim(),
      port = portInt ?: 1883,
      username = username.trim(),
      password = password,
      clientId = clientId.trim().ifBlank { MqttConfig.generateDefaultClientId() },
      useSsl = useSsl,
      defaultTopic = defaultTopic.trim().ifBlank { "pocketcode/demo/game" }
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "MQTT Broker Setup",
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = Color.White
            )
            Text(
              text = "PocketCodePlus IoT & Multiplayer",
              fontSize = 11.sp,
              color = Color.White.copy(alpha = 0.8f)
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("mqtt_back_button")
          ) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }
        },
        actions = {
          IconButton(
            onClick = {
              val def = MqttConfig()
              host = def.host
              portText = def.port.toString()
              username = def.username
              password = def.password
              clientId = def.clientId
              useSsl = def.useSsl
              defaultTopic = def.defaultTopic
              testResult = null
              Toast.makeText(context, "Reset to default EMQX broker", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.testTag("mqtt_reset_defaults_button")
          ) {
            Icon(
              Icons.Default.RestartAlt,
              contentDescription = "Reset to Defaults",
              tint = Color.White
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PocketTealDark)
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

      // Active Connection Status Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = PocketTealPrimary.copy(alpha = 0.08f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(PocketTealPrimary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Hub,
              contentDescription = null,
              tint = PocketTealPrimary,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "ACTIVE BROKER ENDPOINT",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = PocketTealDark,
              letterSpacing = 1.sp
            )
            Text(
              text = initialConfig.uriString,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (initialConfig.hasAuth) "Authenticated (${initialConfig.username})" else "Anonymous (No auth)",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Quick Preset Brokers
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Quick Presets",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(8.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = host == MqttConfig.PRESET_EMQX.host && portText == MqttConfig.PRESET_EMQX.port.toString(),
              onClick = {
                val p = MqttConfig.PRESET_EMQX
                host = p.host
                portText = p.port.toString()
                username = p.username
                password = p.password
                useSsl = p.useSsl
                defaultTopic = p.defaultTopic
                testResult = null
              },
              label = { Text("EMQX (broker.emqx.io)") },
              leadingIcon = {
                Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = PocketTealPrimary.copy(alpha = 0.15f),
                selectedLabelColor = PocketTealDark
              )
            )

            FilterChip(
              selected = host == MqttConfig.PRESET_MOSQUITTO.host && portText == MqttConfig.PRESET_MOSQUITTO.port.toString(),
              onClick = {
                val p = MqttConfig.PRESET_MOSQUITTO
                host = p.host
                portText = p.port.toString()
                username = p.username
                password = p.password
                useSsl = p.useSsl
                defaultTopic = p.defaultTopic
                testResult = null
              },
              label = { Text("Mosquitto (test.mosquitto.org)") },
              leadingIcon = {
                Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp))
              }
            )

            FilterChip(
              selected = host == MqttConfig.PRESET_HIVEMQ.host && portText == MqttConfig.PRESET_HIVEMQ.port.toString(),
              onClick = {
                val p = MqttConfig.PRESET_HIVEMQ
                host = p.host
                portText = p.port.toString()
                username = p.username
                password = p.password
                useSsl = p.useSsl
                defaultTopic = p.defaultTopic
                testResult = null
              },
              label = { Text("HiveMQ (broker.hivemq.com)") },
              leadingIcon = {
                Icon(Icons.Default.WifiTethering, contentDescription = null, modifier = Modifier.size(16.dp))
              }
            )

            FilterChip(
              selected = host == MqttConfig.PRESET_LOCAL.host,
              onClick = {
                val p = MqttConfig.PRESET_LOCAL
                host = p.host
                portText = p.port.toString()
                username = p.username
                password = p.password
                useSsl = p.useSsl
                defaultTopic = p.defaultTopic
                testResult = null
              },
              label = { Text("Localhost / LAN (192.168.x)") }
            )
          }
        }
      }

      // Connection Details Card (Host, Port, Username, Password)
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Text(
            text = "Connection Details",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
          )

          // Host Field
          OutlinedTextField(
            value = host,
            onValueChange = {
              host = it
              testResult = null
            },
            label = { Text("MQTT Broker Host *") },
            placeholder = { Text("e.g. broker.emqx.io or 192.168.1.50") },
            leadingIcon = {
              Icon(Icons.Default.Cloud, contentDescription = "Host")
            },
            isError = !isHostValid,
            supportingText = {
              if (!isHostValid) {
                Text("Host address is required", color = MaterialTheme.colorScheme.error)
              } else {
                Text("FQDN hostname or IPv4/IPv6 server address")
              }
            },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("mqtt_host_input")
          )

          // Port Field
          OutlinedTextField(
            value = portText,
            onValueChange = {
              portText = it.filter { ch -> ch.isDigit() }
              testResult = null
            },
            label = { Text("Port *") },
            placeholder = { Text("1883 (standard) or 8883 (SSL)") },
            leadingIcon = {
              Icon(Icons.Default.Numbers, contentDescription = "Port")
            },
            isError = !isPortValid,
            supportingText = {
              if (!isPortValid) {
                Text("Must be a valid port (1 - 65535)", color = MaterialTheme.colorScheme.error)
              } else {
                Text("Standard MQTT: 1883 | Secure TLS: 8883")
              }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("mqtt_port_input")
          )

          // Username Field
          OutlinedTextField(
            value = username,
            onValueChange = {
              username = it
              testResult = null
            },
            label = { Text("Username (Optional)") },
            placeholder = { Text("Leave blank for public anonymous brokers") },
            leadingIcon = {
              Icon(Icons.Default.Person, contentDescription = "Username")
            },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("mqtt_username_input")
          )

          // Password Field
          OutlinedTextField(
            value = password,
            onValueChange = {
              password = it
              testResult = null
            },
            label = { Text("Password (Optional)") },
            placeholder = { Text("Required only for secured private brokers") },
            leadingIcon = {
              Icon(Icons.Default.Lock, contentDescription = "Password")
            },
            trailingIcon = {
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                  imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = if (passwordVisible) "Hide password" else "Show password"
                )
              }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("mqtt_password_input")
          )
        }
      }

      // Advanced Settings (Collapsible)
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Advanced MQTT Options",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedButton(
              onClick = { showAdvanced = !showAdvanced },
              contentPadding = ButtonDefaults.TextButtonContentPadding
            ) {
              Text(if (showAdvanced) "Hide" else "Show")
            }
          }

          if (showAdvanced) {
            Spacer(modifier = Modifier.height(12.dp))

            // Client ID Field
            OutlinedTextField(
              value = clientId,
              onValueChange = { clientId = it },
              label = { Text("Client Identifier") },
              trailingIcon = {
                IconButton(onClick = { clientId = MqttConfig.generateDefaultClientId() }) {
                  Icon(Icons.Default.Refresh, contentDescription = "Generate random Client ID")
                }
              },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("mqtt_client_id_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Default Topic Prefix
            OutlinedTextField(
              value = defaultTopic,
              onValueChange = { defaultTopic = it },
              label = { Text("Default Topic Channel") },
              placeholder = { Text("e.g. pocketcode/demo/game") },
              leadingIcon = {
                Icon(Icons.Default.Tag, contentDescription = "Topic")
              },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("mqtt_topic_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // SSL/TLS Toggle
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text("Use SSL/TLS Connection", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                  Text("Encrypt broker traffic (usually port 8883)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
              Switch(
                checked = useSsl,
                onCheckedChange = {
                  useSsl = it
                  if (it && portText == "1883") {
                    portText = "8883"
                  } else if (!it && portText == "8883") {
                    portText = "1883"
                  }
                }
              )
            }
          }
        }
      }

      // Connection Test Result Banner
      testResult?.let { res ->
        when (res) {
          is ConnectionTestResult.Success -> {
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text("Connection Successful", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                  Text(res.message, fontSize = 12.sp, color = Color(0xFF2E7D32))
                }
              }
            }
          }
          is ConnectionTestResult.Failure -> {
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Error, contentDescription = "Error", tint = Color(0xFFC62828))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text("Broker Unreachable", fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C))
                  Text(res.errorMessage, fontSize = 12.sp, color = Color(0xFFC62828))
                }
              }
            }
          }
        }
      }

      // Action Buttons: Test Connection & Save Configuration
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Test Connection Button
        OutlinedButton(
          onClick = {
            if (!canSave) {
              Toast.makeText(context, "Please check host and port", Toast.LENGTH_SHORT).show()
              return@OutlinedButton
            }
            val configToTest = buildCurrentConfig()
            isTestingConnection = true
            coroutineScope.launch {
              val result = MqttConfigRepository.testBrokerConnection(configToTest)
              testResult = result
              isTestingConnection = false
            }
          },
          enabled = canSave && !isTestingConnection,
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .testTag("mqtt_test_button")
        ) {
          if (isTestingConnection) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pinging...")
          } else {
            Icon(Icons.Default.WifiTethering, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Test Socket")
          }
        }

        // Save Button
        Button(
          onClick = {
            if (!canSave) {
              Toast.makeText(context, "Please fix invalid fields first", Toast.LENGTH_SHORT).show()
              return@Button
            }
            val newCfg = buildCurrentConfig()
            onConfigSaved.invoke(newCfg)
            Toast.makeText(context, "MQTT Broker configuration saved!", Toast.LENGTH_SHORT).show()
          },
          enabled = canSave,
          colors = ButtonDefaults.buttonColors(containerColor = PocketTealDark),
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .testTag("mqtt_save_button")
        ) {
          Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Save Settings", color = Color.White, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
    }
  }
}
