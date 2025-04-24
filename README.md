# Pincast2

A modern Android application for managing media files with IPFS integration through Jackal Protocol.

## Features

- **IPFS Integration**: Connect to Jackal IPFS for decentralized storage
- **Media Management**: Upload, view, share, and delete media files
- **Smart Caching**: Efficient local caching with gateway fallbacks
- **Modern UI**: Built with Jetpack Compose

## Architecture

- **MVVM Architecture**: Clear separation of concerns
- **Repository Pattern**: Centralized data management
- **Kotlin Coroutines & Flow**: Asynchronous operations

## Caching Strategy

The app implements a layered caching approach:
1. Local database for metadata and frequently accessed items
2. On-device file cache for media files
3. Multiple IPFS gateway fallbacks for reliability

## Getting Started

1. Clone the repository
2. Open in Android Studio
3. Build and run on your device or emulator

## Future Improvements

- Dynamic gateway selection
- CID indexing for faster retrieval
- Location-based edge caching
- Background synchronization 