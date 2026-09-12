package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PocketAccentAmber
import com.example.ui.theme.PocketTealDark
import com.example.ui.theme.PocketTealPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreditsScreen(modifier: Modifier = Modifier) {
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(scrollState)
      .padding(16.dp)
      .testTag("credits_screen")
  ) {
    // Header Banner
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = PocketTealDark),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.linearGradient(
              colors = listOf(PocketTealDark, Color(0xFF004D40), Color(0xFF0F172A))
            )
          )
          .padding(20.dp)
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              color = PocketAccentAmber,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "MOD v2.5",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontFamily = FontFamily.Monospace
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              color = Color.White.copy(alpha = 0.15f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "NEWCATROID ENGINE",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "PocketCodePlus",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
          )

          Text(
            text = "The Ultimate Catrobat Visual Programming Mod with Physics, Camera Follow, MQTT IoT, AI Bricks & Diagnostic Tools.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Section 1: Mod Overview & Features
    CreditSectionTitle("Mod Features & Enhancements", Icons.Default.AutoAwesome)

    FeatureGrid(
      features = listOf(
        CreditFeature("Newcatroid Physics", "Real-time gravity, velocity, bounce elasticity, and impulse physics engine.", Icons.Default.Speed, Color(0xFF0284C7)),
        CreditFeature("Camera & 3D System", "Camera follow sprite, dynamic zoom in/out, screen shake, and smooth panning.", Icons.Default.CameraAlt, Color(0xFF06B6D4)),
        CreditFeature("MQTT & IoT", "Public broker.emqx.io integration with live publish, subscribe, and event triggers.", Icons.Default.Hub, Color(0xFF0D9488)),
        CreditFeature("AI Blocks", "Smart AI prompt evaluation, NPC dialogue generator, and text sentiment classification.", Icons.Default.AutoAwesome, Color(0xFF8B5CF6)),
        CreditFeature("Diagnostics & Log Tab", "Static syntax analyzer catching orphan broadcasts, undefined vars, and infinite loops.", Icons.Default.Code, Color(0xFFF59E0B)),
        CreditFeature("PCP & Catrobat Formats", "Export and import project packages in .pcp and standard Catrobat format.", Icons.Default.Language, Color(0xFFE11D48)),
        CreditFeature("Video & Web Integration", "Embedded video player, in-app WebView, and HTTP GET requests.", Icons.Default.Videocam, Color(0xFF10B981))
      )
    )

    Spacer(modifier = Modifier.height(22.dp))

    // Section 2: Hall of Fame & Acknowledgements
    CreditSectionTitle("Giant Hall of Credits", Icons.Default.Favorite)

    CreditCard(
      title = "Catrobat Project & Foundation",
      subtitle = "Original Creators of Pocket Code",
      description = "Honoring Wolfgang Slany, the Catrobat Research Lab, Graz University of Technology (TU Graz), and the global Catrobat open-source contributors who created the visual programming paradigm that inspired millions of young programmers worldwide.",
      tags = listOf("TU Graz", "Wolfgang Slany", "Catrobat.org", "Open Source FOSS"),
      accentColor = PocketAccentAmber
    )

    Spacer(modifier = Modifier.height(10.dp))

    CreditCard(
      title = "Newcatroid Mod & Community",
      subtitle = "Physics & Advanced Engine Pioneer",
      description = "Dedicated to the Newcatroid modding community who envisioned taking mobile visual coding to the next level with full rigid-body physics, gravity parameters, customizable cameras, and experimental scripting blocks.",
      tags = listOf("Newcatroid Mod", "Rigid Body Physics", "Camera Tracking", "Community Mod"),
      accentColor = Color(0xFF0284C7)
    )

    Spacer(modifier = Modifier.height(10.dp))

    CreditCard(
      title = "Open Infrastructure & Protocols",
      subtitle = "Powering Connectivity & Networking",
      description = "EMQX Public MQTT Broker (broker.emqx.io:1883) for zero-setup multiplayer and IoT synchronization; Jetpack Compose & Android Modern Tooling for high-performance reactive UI rendering.",
      tags = listOf("broker.emqx.io", "MQTT v3.1.1", "Jetpack Compose", "Kotlin Coroutines"),
      accentColor = Color(0xFF0D9488)
    )

    Spacer(modifier = Modifier.height(22.dp))

    // Section 3: File Format Specification
    CreditSectionTitle("File Formats (.pcp & .catrobat)", Icons.Default.Info)

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = ".pcp (PocketCodePlus Archive)",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = "A modern zip container holding 'project.json' and 'manifest.json'. Supports all extended blocks: physics gravity vectors, camera tracking locks, MQTT pub/sub, AI heuristics, and custom costumes.",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )

        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = ".catrobat (Standard Catrobat Archive)",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.secondary
        )
        Text(
          text = "Standard Catrobat zip archive containing 'code.xml' and 'description.txt', maintaining interoperability with official Catrobat and Pocket Code tools.",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 4.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Footer
    Box(
      modifier = Modifier.fillMaxWidth(),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "Built with ❤️ for the Catrobat and Pocket Code Community",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Medium
        )
        Text(
          text = "PocketCodePlus • All AI images replaced with vector & Compose graphics",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          modifier = Modifier.padding(top = 2.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun CreditSectionTitle(title: String, icon: ImageVector) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(vertical = 8.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = PocketTealPrimary,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )
  }
}

data class CreditFeature(
  val title: String,
  val description: String,
  val icon: ImageVector,
  val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeatureGrid(features: List<CreditFeature>) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    features.forEach { feature ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(feature.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = feature.icon,
              contentDescription = null,
              tint = feature.color,
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = feature.title,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = feature.description,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreditCard(
  title: String,
  subtitle: String,
  description: String,
  tags: List<String>,
  accentColor: Color
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .background(accentColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = title,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      Text(
        text = subtitle,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = accentColor,
        modifier = Modifier.padding(top = 2.dp, start = 18.dp)
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = description,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 18.sp
      )
      Spacer(modifier = Modifier.height(10.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        tags.forEach { tag ->
          Surface(
            color = accentColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = tag,
              color = accentColor,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }
    }
  }
}
