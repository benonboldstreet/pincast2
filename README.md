# Pincast2

A modern Android application for managing media files with IPFS integration through Jackal Protocol.

## Overview

Pincast2 is a modern Android application designed to revolutionize media management through decentralized storage with IPFS integration via the Jackal Protocol. This app offers a seamless experience for users to upload, store, manage, and share their media files while leveraging the benefits of distributed storage.

## Core Features

- **Decentralized Storage**: Store your media securely on IPFS through the Jackal Protocol, ensuring your files remain accessible and resilient.

- **Intelligent Caching System**: Our layered approach combines local caching with decentralized storage for optimal performance:
  - In-memory cache for quick access to frequently used content
  - On-device file cache to minimize network usage
  - Multiple IPFS gateway fallbacks for reliable content retrieval

- **Advanced Media Management**:
  - Upload images and videos directly to IPFS
  - Create and organize collections
  - Browse your media with a responsive gallery interface
  - Detailed media viewing with zoom and pan capabilities

- **Robust Sharing Options**:
  - Share IPFS links to your content
  - Copy media links to clipboard
  - Direct sharing to other applications

- **Gateway Resilience**: Automatically switch between multiple IPFS gateways to ensure your content is always accessible, even when certain services experience downtime.

## Technical Highlights

- **Modern Architecture**: Built with MVVM pattern and repository design for clean separation of concerns
- **Jetpack Compose UI**: Fluid, responsive interface with material design components
- **Kotlin Coroutines & Flow**: Efficient asynchronous operations and reactive data handling
- **Optimized Media Loading**: Using Coil for efficient image loading with fallback mechanisms
- **Robust Error Handling**: Graceful degradation when network issues occur

## Getting Started

1. Clone the repository
2. Open in Android Studio
3. Build and run on your device or emulator

## Planned Improvements

- Dynamic gateway selection based on performance
- Enhanced CID indexing for faster retrieval
- Location-based edge caching
- Background synchronization
- On-device file compression options
- Peer-to-peer content sharing 