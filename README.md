# 🐱 ClubCat

![Android CI](../../actions/workflows/android.yml/badge.svg)
![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)
![Platform: Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-3DDC84.svg)

Reproductor de música local para **tablets Android**, con diseño moderno inspirado en Apple Music y Material You.

- **Pantallas anchas (≥ 840 dp):** biblioteca a la izquierda y panel fijo de "Reproduciendo ahora" a la derecha.
- **Pantallas angostas:** mini reproductor abajo y hoja modal para los controles completos.

<!-- Añade capturas de pantalla aquí: docs/screenshots/home.png -->

*Open source bajo licencia MIT. Issues y PRs son bienvenidos.*

## Stack

- **Kotlin** + **Jetpack Compose** (Material 3, colores dinámicos en Android 12+)
- **Media3** (ExoPlayer + MediaSession) para reproducción en segundo plano, notificación y controles de pantalla de bloqueo
- **Coil** para carátulas
- `MediaStore` para leer la música del dispositivo

## Estructura

```
app/src/main/java/com/clubcat/player/
├── MainActivity.kt
├── data/
│   ├── Song.kt                 # modelo
│   └── MusicRepository.kt      # escaneo de música local (MediaStore)
├── playback/
│   └── PlaybackService.kt      # MediaSessionService + ExoPlayer
└── ui/
    ├── ClubCatApp.kt           # pantalla principal, permisos, estados
    ├── PlayerViewModel.kt      # conecta la UI con el servicio (MediaController)
    ├── PlayerControls.kt       # mini reproductor + hoja "Reproduciendo ahora"
    ├── SongRow.kt              # fila de canción y carátula
    └── theme/Theme.kt          # tema Material You + paleta de respaldo
```

## Cómo correrlo

1. Abre la carpeta en **Android Studio** (Ladybug o más reciente) y espera el Gradle Sync.
2. Corre la app en tu tablet o en un emulador de tablet (por ejemplo **Pixel Tablet**) con archivos de música (en el emulador, súbelos a `Music/` con Device Explorer).
3. Acepta los permisos de audio y notificaciones.

> Las versiones de dependencias están en `gradle/libs.versions.toml`; Android Studio te sugerirá actualizaciones.

> **Gradle wrapper:** el repo incluye `gradle-wrapper.properties` pero no `gradlew`. Genéralo una vez con `gradle wrapper` (o deja que Android Studio lo cree) y haz commit de `gradlew`, `gradlew.bat` y `gradle/wrapper/gradle-wrapper.jar`.

## Contribuir

1. Haz fork y crea una rama (`git checkout -b feature/mi-idea`).
2. Verifica que compile con `./gradlew assembleDebug`.
3. Abre un Pull Request usando la plantilla.

## Roadmap

- [x] Escaneo de música local
- [x] Reproducción en segundo plano con notificación
- [x] Mini reproductor y pantalla "Reproduciendo ahora"
- [x] Diseño de tablet con panel dividido
- [ ] Navegación lateral (NavigationRail): Canciones · Álbumes · Artistas
- [ ] Cuadrícula de álbumes para pantallas grandes
- [ ] Cola de reproducción, aleatorio y repetir
- [ ] Playlists y favoritos (Room)
- [ ] Colores dinámicos a partir de la carátula
- [ ] Letras sincronizadas
- [ ] Búsqueda
- [ ] Ecualizador
- [ ] Ícono de la app y splash screen
