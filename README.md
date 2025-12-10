# Quotey

A modern Android app for creating beautiful quote images for social media. Built with Kotlin, Jetpack Compose, and Material 3.

## Features

- **Text Customization**: Full control over fonts (Poppins included), sizes, weights, colors, alignment, line height, letter spacing, and more
- **Background Options**:
  - Solid colors with 24+ preset options
  - Beautiful gradient presets (30+) with Linear, Radial, and Sweep types
  - Abstract patterns (dots, grid, waves, hexagons, triangles, organic blobs, etc.)
- **Canvas Control**: Multiple aspect ratios for all social platforms (Instagram, Facebook, Twitter, Pinterest, LinkedIn, YouTube) plus custom dimensions
- **Multi-page Support**: Create carousel posts with multiple slides
- **Export**: High-quality PNG, JPEG, or WebP export with customizable quality and resolution

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Hilt for Dependency Injection
- **Data**: DataStore for preferences
- **Navigation**: Jetpack Navigation Compose

## Screenshots

*Coming soon*

## Building

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 35

### Build Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/quotey.git
   cd quotey
   ```

2. Generate the release keystore (optional, for signed builds):
   ```bash
   cd keystore
   chmod +x generate-keystore.sh
   ./generate-keystore.sh
   ```

3. Build the project:
   ```bash
   ./gradlew assembleDebug
   ```

   Or for release:
   ```bash
   ./gradlew assembleRelease
   ```

## Project Structure

```
app/
├── src/main/
│   ├── java/com/quotey/create/
│   │   ├── data/
│   │   │   ├── model/          # Data classes and models
│   │   │   ├── preferences/    # DataStore preferences
│   │   │   └── repository/     # Data repositories
│   │   ├── di/                 # Hilt modules
│   │   ├── navigation/         # Navigation setup
│   │   ├── ui/
│   │   │   ├── components/     # Reusable UI components
│   │   │   ├── screens/        # Screen composables
│   │   │   └── theme/          # Material 3 theme
│   │   └── util/               # Utility classes
│   └── res/
│       ├── drawable/           # Vector drawables
│       ├── font/               # Poppins font family
│       ├── values/             # Colors, strings, themes
│       └── xml/                # Config files
```

## Supported Aspect Ratios

| Platform | Ratio | Dimensions |
|----------|-------|------------|
| Square | 1:1 | 1080x1080 |
| Portrait | 4:5 | 1080x1350 |
| Story | 9:16 | 1080x1920 |
| Landscape | 16:9 | 1920x1080 |
| Twitter | 16:9 | 1600x900 |
| Facebook | 1.91:1 | 1200x628 |
| Pinterest | 2:3 | 1000x1500 |
| LinkedIn | 1.91:1 | 1200x628 |
| YouTube | 16:9 | 1280x720 |
| Custom | Any | Up to 4096x4096 |

## Color Palette

The app uses a carefully crafted color palette:
- **Primary**: Soft Sage Green (#8FB996)
- **Secondary**: Dusty Rose Pink (#D4A5A5)
- **Tertiary**: Creamy Peach (#E8C8A9)

## CI/CD

The project includes GitHub Actions for automated builds:
- Automatic APK builds on push
- Release APK signing
- Artifact upload with commit hash naming

## License

This project is open source. See LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Future Plans

- AI-powered text enhancement (premium feature)
- Image backgrounds
- More pattern types
- Template library
- Cloud sync
