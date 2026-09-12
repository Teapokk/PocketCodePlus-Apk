package com.example.model

object DefaultProjects {
  fun getPresetProjects(): List<Project> {
    return listOf(
      createNewcatroidPhysicsProject(),
      createIoTMqttAiProject(),
      createGalaxyDodgerProject(),
      createBouncingCatProject(),
      createNeonArtProject(),
      createCoinClickerProject()
    )
  }

  private fun createNewcatroidPhysicsProject(): Project {
    val ballActor = ProgramObject(
      id = "obj_physics_ball",
      name = "Bouncing Hero",
      looks = listOf(
        CostumeLook(id = "l_hero", name = "Super Ball", emojiOrIcon = "⚽", tintHex = "#38BDF8"),
        CostumeLook(id = "l_star", name = "Golden Star", emojiOrIcon = "⭐", tintHex = "#FACC15")
      ),
      initialX = 0f,
      initialY = 180f,
      initialSize = 120f,
      scripts = listOf(
        Script(
          id = "sc_physics_start",
          header = Brick(op = BrickOp.WHEN_SCENE_STARTS),
          bricks = listOf(
            Brick(op = BrickOp.FIX_CAMERA_TO_SPRITE),
            Brick(op = BrickOp.SET_CAMERA_ZOOM, paramNum1 = 110f),
            Brick(op = BrickOp.SET_GRAVITY, paramNum1 = 0f, paramNum2 = -520f),
            Brick(op = BrickOp.SET_BOUNCE_ELASTICITY, paramNum1 = 80f),
            Brick(op = BrickOp.SET_MASS, paramNum1 = 1.2f),
            Brick(op = BrickOp.SAY_TEXT, paramString1 = "Camera fixed to me! Tap to jump!", paramNum1 = 2f)
          )
        ),
        Script(
          id = "sc_physics_tap",
          header = Brick(op = BrickOp.WHEN_TAPPED),
          bricks = listOf(
            Brick(op = BrickOp.APPLY_IMPULSE, paramNum1 = 40f, paramNum2 = 360f),
            Brick(op = BrickOp.SHAKE_CAMERA, paramNum1 = 16f),
            Brick(op = BrickOp.PLAY_SOUND, paramString1 = "Jump"),
            Brick(op = BrickOp.CHANGE_VARIABLE_BY, paramString1 = "jumps", paramNum1 = 1f),
            Brick(op = BrickOp.NEXT_LOOK)
          )
        )
      )
    )

    val targetGoal = ProgramObject(
      id = "obj_target_flag",
      name = "Goal Portal",
      looks = listOf(
        CostumeLook(id = "l_portal", name = "Portal", emojiOrIcon = "🌀", tintHex = "#A855F7")
      ),
      initialX = 80f,
      initialY = -120f,
      initialSize = 110f,
      scripts = listOf(
        Script(
          id = "sc_portal_rot",
          header = Brick(op = BrickOp.WHEN_SCENE_STARTS),
          bricks = listOf(
            Brick(
              op = BrickOp.FOREVER,
              childBricks = listOf(
                Brick(op = BrickOp.TURN_DEGREES, paramNum1 = 12f),
                Brick(op = BrickOp.WAIT_SECONDS, paramNum1 = 0.05f)
              )
            )
          )
        )
      )
    )

    return Project(
      id = "preset_newcatroid_physics",
      title = "Newcatroid Physics & Camera",
      description = "Showcase of gravity, velocity impulses, bounce elasticity, and camera tracking following the sprite in real time!",
      backgroundHex = "#0B132B",
      objects = listOf(ballActor, targetGoal),
      variables = mapOf("jumps" to 0f, "score" to 0f),
      isPreset = true
    )
  }

  private fun createIoTMqttAiProject(): Project {
    val aiBot = ProgramObject(
      id = "obj_ai_bot",
      name = "AI Cyber Cat",
      looks = listOf(
        CostumeLook(id = "l_bot", name = "Cyber Cat", emojiOrIcon = "🤖", tintHex = "#34D399")
      ),
      initialX = 0f,
      initialY = 40f,
      initialSize = 130f,
      scripts = listOf(
        Script(
          id = "sc_iot_start",
          header = Brick(op = BrickOp.WHEN_SCENE_STARTS),
          bricks = listOf(
            Brick(op = BrickOp.MQTT_CONNECT),
            Brick(op = BrickOp.MQTT_PUBLISH, paramString1 = "pocketcode/demo/welcome", paramString2 = "Hello from PocketCodePlus!"),
            Brick(op = BrickOp.AI_GENERATE_NPC_DIALOGUE, paramString1 = "Cyber Cat"),
            Brick(op = BrickOp.WAIT_SECONDS, paramNum1 = 1f)
          )
        ),
        Script(
          id = "sc_iot_tap",
          header = Brick(op = BrickOp.WHEN_TAPPED),
          bricks = listOf(
            Brick(op = BrickOp.PLAY_SOUND, paramString1 = "Beep"),
            Brick(op = BrickOp.AI_ASK_PROMPT, paramString1 = "Give a random secret coding tip"),
            Brick(op = BrickOp.MQTT_PUBLISH, paramString1 = "pocketcode/demo/tap", paramString2 = "Device tapped!"),
            Brick(op = BrickOp.CHANGE_VARIABLE_BY, paramString1 = "ai_interactions", paramNum1 = 1f)
          )
        )
      )
    )

    return Project(
      id = "preset_iot_mqtt_ai",
      title = "EMQX MQTT & AI Cyber Lab",
      description = "Connects to broker.emqx.io, publishes IoT messages, triggers AI dialogue generation, and evaluates dynamic prompts!",
      backgroundHex = "#022C22",
      objects = listOf(aiBot),
      variables = mapOf("ai_interactions" to 0f, "score" to 0f),
      isPreset = true
    )
  }

  private fun createGalaxyDodgerProject(): Project {
    val shipLooks = listOf(
      CostumeLook(id = "ship_normal", name = "Cruiser", emojiOrIcon = "🚀", tintHex = "#00E5FF"),
      CostumeLook(id = "ship_boost", name = "Thrust", emojiOrIcon = "🛸", tintHex = "#76FF03"),
      CostumeLook(id = "ship_boom", name = "Explosion", emojiOrIcon = "💥", tintHex = "#FF1744")
    )

    val meteorLooks = listOf(
      CostumeLook(id = "meteor_1", name = "Asteroid", emojiOrIcon = "☄️", tintHex = "#FF9100"),
      CostumeLook(id = "meteor_2", name = "Space Rock", emojiOrIcon = "🪨", tintHex = "#B0BEC5")
    )

    val spaceship = ProgramObject(
      id = "obj_spaceship",
      name = "Star Cruiser",
      looks = shipLooks,
      currentLookIndex = 0,
      initialX = 0f,
      initialY = -220f,
      initialSize = 110f,
      scripts = listOf(
        Script(
          id = "sc_ship_start",
          header = Brick(op = BrickOp.WHEN_SCENE_STARTS),
          bricks = listOf(
            Brick(op = BrickOp.SET_VARIABLE, paramString1 = "score", paramNum1 = 0f),
            Brick(op = BrickOp.SET_VARIABLE, paramString1 = "shields", paramNum1 = 100f),
            Brick(op = BrickOp.PLACE_AT_XY, paramNum1 = 0f, paramNum2 = -220f),
            Brick(op = BrickOp.PLAY_SOUND, paramString1 = "Win"),
            Brick(op = BrickOp.SAY_TEXT, paramString1 = "Tap to thrust & dodge!", paramNum1 = 2f)
          )
        ),
        Script(
          id = "sc_ship_tap",
          header = Brick(op = BrickOp.WHEN_TAPPED),
          bricks = listOf(
            Brick(op = BrickOp.PLAY_SOUND, paramString1 = "Laser"),
            Brick(op = BrickOp.NEXT_LOOK),
            Brick(op = BrickOp.CHANGE_VARIABLE_BY, paramString1 = "score", paramNum1 = 10f),
            Brick(op = BrickOp.MOVE_STEPS, paramNum1 = 25f),
            Brick(op = BrickOp.BOUNCE_IF_ON_EDGE)
          )
        )
      )
    )

    val asteroid1 = ProgramObject(
      id = "obj_asteroid1",
      name = "Meteor Alpha",
      looks = meteorLooks,
      currentLookIndex = 0,
      initialX = -80f,
      initialY = 240f,
      initialSize = 90f,
      scripts = listOf(
        Script(
          id = "sc_ast1_start",
          header = Brick(op = BrickOp.WHEN_SCENE_STARTS),
          bricks = listOf(
            Brick(op = BrickOp.PLACE_AT_XY, paramNum1 = -80f, paramNum2 = 240f),
            Brick(
              op = BrickOp.FOREVER,
              childBricks = listOf(
                Brick(op = BrickOp.CHANGE_Y_BY, paramNum1 = -10f),
                Brick(op = BrickOp.TURN_DEGREES, paramNum1 = 8f),
                Brick(op = BrickOp.WAIT_SECONDS, paramNum1 = 0.04f),
                Brick(
                  op = BrickOp.IF_VARIABLE,
                  paramString1 = "score",
                  paramString2 = ">",
                  paramNum1 = 50f,
                  childBricks = listOf(
                    Brick(op = BrickOp.CHANGE_Y_BY, paramNum1 = -4f)
                  )
                )
              )
            )
          )
        ),
        Script(
          id = "sc_ast1_tap",
          header = Brick(op = BrickOp.WHEN_TAPPED),
          bricks = listOf(
            Brick(op = BrickOp.PLAY_SOUND, paramString1 = "Boom"),
            Brick(op = BrickOp.CHANGE_VARIABLE_BY, paramString1 = "score", paramNum1 = 5f),
            Brick(op = BrickOp.PLACE_AT_XY, paramNum1 = 90f, paramNum2 = 280f)
          )
        )
      )
    )

    val asteroid2 = ProgramObject(
      id = "obj_asteroid2",
      name = "Meteor Beta",
      looks = meteorLooks,
      currentLookIndex = 1,
      initialX = 80f,
      initialY = 310f,
      initialSize = 80f,
      scripts = listOf(
        Script(
          id = "sc_ast2_start",
          header = Brick(op = BrickOp.WHEN_SCENE_STARTS),
          bricks = listOf(
            Brick(op = BrickOp.PLACE_AT_XY, paramNum1 = 80f, paramNum2 = 310f),
            Brick(
              op = BrickOp.FOREVER,
              childBricks = listOf(
                Brick(op = BrickOp.CHANGE_Y_BY, paramNum1 = -14f),
                Brick(op = BrickOp.TURN_DEGREES, paramNum1 = -12f),
                Brick(op = BrickOp.WAIT_SECONDS, paramNum1 = 0.04f)
              )
            )
          )
        ),
        Script(
          id = "sc_ast2_tap",
          header = Brick(op = BrickOp.WHEN_TAPPED),
          bricks = listOf(
            Brick(op = BrickOp.PLAY_SOUND, paramString1 = "Pop"),
            Brick(op = BrickOp.CHANGE_VARIABLE_BY, paramString1 = "score", paramNum1 = 15f),
            Brick(op = BrickOp.PLACE_AT_XY, paramNum1 = -70f, paramNum2 = 300f)
          )
        )
      )
    )

    return Project(
      id = "preset_galaxy_dodger",
      title = "Galaxy Dodger",
      description = "Dodge oncoming meteors in deep space! Tap obstacles to destroy them and boost your score.",
      backgroundHex = "#0B0F19",
      objects = listOf(spaceship, asteroid1, asteroid2),
      variables = mapOf("score" to 0f, "shields" to 100f),
      isPreset = true
    )
  }

  private fun createBouncingCatProject(): Project {
    val catLooks = listOf(
      CostumeLook(id = "cat_happy", name = "Happy Cat", emojiOrIcon = "🐱", tintHex = "#FFB300"),
      CostumeLook(id = "cat_wink", name = "Playful Cat", emojiOrIcon = "😸", tintHex = "#FF7043"),
      CostumeLook(id = "cat_cool", name = "Cool Cat", emojiOrIcon = "😎", tintHex = "#29B6F6"),
      CostumeLook(id = "cat_heart", name = "Love Cat", emojiOrIcon = "😻", tintHex = "#EC407A")
    )

    val cat = ProgramObject(
      id = "obj_cat",
      name = "Catrobat Cat",
      looks = catLooks,
      currentLookIndex = 0,
      initialX = -40f,
      initialY = 40f,
      initialSize = 120f,
      scripts = listOf(
        Script(
          id = "sc_cat_start",
          header = Brick(op = BrickOp.WHEN_SCENE_STARTS),
          bricks = listOf(
            Brick(op = BrickOp.SET_VARIABLE, paramString1 = "bounces", paramNum1 = 0f),
            Brick(op = BrickOp.PEN_DOWN),
            Brick(op = BrickOp.SET_PEN_COLOR, paramString1 = "#00E5FF"),
            Brick(op = BrickOp.SET_PEN_SIZE, paramNum1 = 6f),
            Brick(
              op = BrickOp.FOREVER,
              childBricks = listOf(
                Brick(op = BrickOp.MOVE_STEPS, paramNum1 = 14f),
                Brick(op = BrickOp.BOUNCE_IF_ON_EDGE),
                Brick(op = BrickOp.TURN_DEGREES, paramNum1 = 5f),
                Brick(op = BrickOp.WAIT_SECONDS, paramNum1 = 0.035f)
              )
            )
          )
        ),
        Script(
          id = "sc_cat_tap",
          header = Brick(op = BrickOp.WHEN_TAPPED),
          bricks = listOf(
            Brick(op = BrickOp.PLAY_SOUND, paramString1 = "Jump"),
            Brick(op = BrickOp.NEXT_LOOK),
            Brick(op = BrickOp.CHANGE_VARIABLE_BY, paramString1 = "bounces", paramNum1 = 1f),
            Brick(op = BrickOp.CHANGE_SIZE_BY, paramNum1 = 10f),
            Brick(op = BrickOp.SAY_TEXT, paramString1 = "Purr! Meow!", paramNum1 = 1.2f)
          )
        )
      )
    )

    return Project(
      id = "preset_bouncing_cat",
      title = "Bouncing Cat",
      description = "Watch the Catrobat Cat bounce around leaving vibrant neon trails. Tap the cat to change costumes and hear playful sounds!",
      backgroundHex = "#1A1A2E",
      objects = listOf(cat),
      variables = mapOf("bounces" to 0f),
      isPreset = true
    )
  }

  private fun createNeonArtProject(): Project {
    val brush = ProgramObject(
      id = "obj_brush",
      name = "Neon Brush",
      looks = listOf(
        CostumeLook(id = "look_brush", name = "Rainbow Star", emojiOrIcon = "✨", tintHex = "#00E676")
      ),
      initialX = 0f,
      initialY = 0f,
      initialSize = 100f,
      scripts = listOf(
        Script(
          id = "sc_brush_start",
          header = Brick(op = BrickOp.WHEN_SCENE_STARTS),
          bricks = listOf(
            Brick(op = BrickOp.PEN_DOWN),
            Brick(op = BrickOp.SET_PEN_COLOR, paramString1 = "#FF4081"),
            Brick(op = BrickOp.SET_PEN_SIZE, paramNum1 = 8f),
            Brick(
              op = BrickOp.FOREVER,
              childBricks = listOf(
                Brick(op = BrickOp.MOVE_STEPS, paramNum1 = 12f),
                Brick(op = BrickOp.TURN_DEGREES, paramNum1 = 16f),
                Brick(op = BrickOp.BOUNCE_IF_ON_EDGE),
                Brick(op = BrickOp.WAIT_SECONDS, paramNum1 = 0.03f)
              )
            )
          )
        ),
        Script(
          id = "sc_brush_tap",
          header = Brick(op = BrickOp.WHEN_TAPPED),
          bricks = listOf(
            Brick(op = BrickOp.PLAY_SOUND, paramString1 = "Coin"),
            Brick(op = BrickOp.SET_PEN_COLOR, paramString1 = "#00E5FF"),
            Brick(op = BrickOp.CHANGE_SIZE_BY, paramNum1 = 8f),
            Brick(op = BrickOp.SAY_TEXT, paramString1 = "Color shift!", paramNum1 = 1f)
          )
        )
      )
    )

    val eraser = ProgramObject(
      id = "obj_eraser",
      name = "Clear Screen",
      looks = listOf(
        CostumeLook(id = "look_eraser", name = "Wand", emojiOrIcon = "🧹", tintHex = "#FFEA00")
      ),
      initialX = 110f,
      initialY = -230f,
      initialSize = 90f,
      scripts = listOf(
        Script(
          id = "sc_clear_tap",
          header = Brick(op = BrickOp.WHEN_TAPPED),
          bricks = listOf(
            Brick(op = BrickOp.PLAY_SOUND, paramString1 = "Pop"),
            Brick(op = BrickOp.CLEAR_PEN),
            Brick(op = BrickOp.SAY_TEXT, paramString1 = "Screen cleared!", paramNum1 = 1.2f)
          )
        )
      )
    )

    return Project(
      id = "preset_neon_art",
      title = "Neon Art Studio",
      description = "Generative drawing algorithm with colorful glowing trails. Tap the brush to shift colors or tap the broom to clear.",
      backgroundHex = "#121212",
      objects = listOf(brush, eraser),
      variables = mapOf("color_mode" to 1f),
      isPreset = true
    )
  }

  private fun createCoinClickerProject(): Project {
    val coin = ProgramObject(
      id = "obj_coin",
      name = "Golden Coin",
      looks = listOf(
        CostumeLook(id = "coin_gold", name = "Gold Coin", emojiOrIcon = "🪙", tintHex = "#FFD700"),
        CostumeLook(id = "coin_gem", name = "Diamond Gem", emojiOrIcon = "💎", tintHex = "#00E5FF"),
        CostumeLook(id = "coin_star", name = "Super Star", emojiOrIcon = "⭐", tintHex = "#FFEA00")
      ),
      initialX = 0f,
      initialY = 40f,
      initialSize = 140f,
      scripts = listOf(
        Script(
          id = "sc_coin_start",
          header = Brick(op = BrickOp.WHEN_SCENE_STARTS),
          bricks = listOf(
            Brick(op = BrickOp.SET_VARIABLE, paramString1 = "gold", paramNum1 = 0f),
            Brick(op = BrickOp.SET_VARIABLE, paramString1 = "clicks", paramNum1 = 0f),
            Brick(op = BrickOp.SET_SIZE_PERCENT, paramNum1 = 140f),
            Brick(op = BrickOp.PLAY_SOUND, paramString1 = "Win"),
            Brick(op = BrickOp.SAY_TEXT, paramString1 = "Tap to mine gold!", paramNum1 = 1.5f)
          )
        ),
        Script(
          id = "sc_coin_tap",
          header = Brick(op = BrickOp.WHEN_TAPPED),
          bricks = listOf(
            Brick(op = BrickOp.PLAY_SOUND, paramString1 = "Coin"),
            Brick(op = BrickOp.CHANGE_VARIABLE_BY, paramString1 = "gold", paramNum1 = 1f),
            Brick(op = BrickOp.CHANGE_VARIABLE_BY, paramString1 = "clicks", paramNum1 = 1f),
            Brick(op = BrickOp.SET_SIZE_PERCENT, paramNum1 = 160f),
            Brick(op = BrickOp.WAIT_SECONDS, paramNum1 = 0.08f),
            Brick(op = BrickOp.SET_SIZE_PERCENT, paramNum1 = 140f)
          )
        )
      )
    )

    val robotHelper = ProgramObject(
      id = "obj_robot",
      name = "Auto Miner Bot",
      looks = listOf(
        CostumeLook(id = "bot_look", name = "Bot 3000", emojiOrIcon = "🤖", tintHex = "#4CAF50")
      ),
      initialX = 0f,
      initialY = -180f,
      initialSize = 100f,
      scripts = listOf(
        Script(
          id = "sc_bot_start",
          header = Brick(op = BrickOp.WHEN_SCENE_STARTS),
          bricks = listOf(
            Brick(
              op = BrickOp.FOREVER,
              childBricks = listOf(
                Brick(op = BrickOp.WAIT_SECONDS, paramNum1 = 2f),
                Brick(op = BrickOp.CHANGE_VARIABLE_BY, paramString1 = "gold", paramNum1 = 1f),
                Brick(op = BrickOp.PLAY_SOUND, paramString1 = "Beep"),
                Brick(op = BrickOp.TURN_DEGREES, paramNum1 = 15f),
                Brick(op = BrickOp.WAIT_SECONDS, paramNum1 = 0.1f),
                Brick(op = BrickOp.TURN_DEGREES, paramNum1 = -15f)
              )
            )
          )
        ),
        Script(
          id = "sc_bot_tap",
          header = Brick(op = BrickOp.WHEN_TAPPED),
          bricks = listOf(
            Brick(op = BrickOp.PLAY_SOUND, paramString1 = "Laser"),
            Brick(op = BrickOp.SAY_TEXT, paramString1 = "Mining in progress...", paramNum1 = 1f)
          )
        )
      )
    )

    return Project(
      id = "preset_coin_clicker",
      title = "Coin Miner Tycoon",
      description = "Idle incremental clicker created entirely with visual blocks. Tap the shiny gold coin and let the auto-miner collect riches!",
      backgroundHex = "#111827",
      objects = listOf(coin, robotHelper),
      variables = mapOf("gold" to 0f, "clicks" to 0f),
      isPreset = true
    )
  }
}
