# Weight

A private, native Android weight tracker for one person. Jetpack Compose, Room, WorkManager, and Kyant's [Backdrop](https://kyant.gitbook.io/backdrop) library for a liquid-glass UI.

## Features

- **Weigh in** — enter today's weight prefilled with yesterday's, tuned with plus/minus stepper buttons on either side.
- **Metrics** — current weight, 7-day change, 30-day change, distance to goal.
- **Chart** — smooth line of every weigh-in with a dashed goal reference line.
- **Goal** — target weight (+ optional deadline), plus a "at your current pace you'll get there in N days" ETA.
- **Morning reminder** — WorkManager schedules a local notification at a time you pick.
- **Out-of-bed detection** — instead of firing at a fixed time, the reminder can wait for the accelerometer to see sustained motion (i.e., you've picked up the phone). Toggle in Settings.
- **Home Assistant integration** — if your bed sensor is in HA, the app can either:
  - Be *pushed* by a HA automation via a broadcast intent (`com.aidenir.weighttracker.action.OUT_OF_BED`); or
  - *Pull* the state of a `binary_sensor.*` entity every 15 minutes and fire when it goes `off`.

## Home Assistant

1. Create a long-lived access token in HA → your profile → security → tokens.
2. In the app's **Settings → Home Assistant**, enter your base URL (e.g. `https://homeassistant.local:8123`), the token, and the entity id for your bed sensor (e.g. `binary_sensor.bed_occupancy`). It will start polling every 15 min.

For an *instant* trigger, add an automation in HA:

```yaml
alias: Bed empty → fire weigh-in reminder
trigger:
  - platform: state
    entity_id: binary_sensor.bed_occupancy
    from: "on"
    to: "off"
action:
  - service: notify.mobile_app_your_phone
    data:
      message: command_broadcast_intent
      data:
        intent_action: com.aidenir.weighttracker.action.OUT_OF_BED
        intent_package_name: com.aidenir.weighttracker
```

(This uses the Home Assistant Android companion app's [`command_broadcast_intent`](https://companion.home-assistant.io/docs/notifications/notification-commands/#broadcast-intent).)

## Build

Requires Android Studio Ladybug or later (AGP 8.7, Kotlin 2.1, JDK 17).

```bash
./gradlew assembleDebug
```

If you don't have a wrapper jar yet, run `gradle wrapper --gradle-version 8.11.1` once to generate it.

Install:

```bash
./gradlew installDebug
```

## Module map

```
app/
  src/main/java/com/aidenir/weighttracker/
    MainActivity.kt              # Compose entry + tab shell + glass bottom bar
    WeightApp.kt                 # Application; boots reminder + HA watcher
    AppContainer.kt              # Manual DI
    data/                        # Room DB, DataStore settings, metrics, unit conversion
    ui/
      theme/                     # Colors, typography, edge-to-edge theme
      components/
        AnimatedBackground.kt    # Animated aurora gradient (the "backdrop")
        LiquidGlassScaffold.kt   # Captures the background as a Kyant Backdrop
        GlassCard.kt             # Blur+lens+vibrancy wrapper
        StepperButton.kt         # Circular glass +/- buttons
        WeightChart.kt           # Canvas line chart with gradient fill + goal line
        MetricPill.kt            # Metric tile with trend color
      screens/                   # Home, History, Goal, Settings
      WeightViewModel.kt
    notifications/               # Daily WorkManager reminder + boot receiver
    motion/OutOfBedService.kt    # Foreground service watching the accelerometer
    homeassistant/               # HA REST client + broadcast receiver + poller
```

Data is stored locally in a SQLite database (`weight.db`) and shared prefs (`settings.preferences_pb`) — both are backed up.
