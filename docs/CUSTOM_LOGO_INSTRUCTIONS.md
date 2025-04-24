# Custom PinCast Logo with Jackal Image

This guide explains how to create and add a custom PinCast logo that replaces the center pin with a jackal image.

## Current Implementation

The app is currently using a simple placeholder logo (`pincast_logo_placeholder.xml`) that shows:
- A black circular background
- White signal waves
- A white circle in the center (where the jackal should go)
- A white bar at the bottom (representing "PinCast" text)

## Adding Your Custom Image

1. Create your custom PinCast logo with a jackal in the center instead of the pin/location icon
2. Save it as `pincast_jackal_bg.png` (must be lowercase with underscores)
3. Add the file to: `app/src/main/res/drawable/`

## Updating the Code

After adding your image, open `app/src/main/java/com/example/pincast/ui/screens/HomeScreen.kt` and:

1. Find this section:
```kotlin
// Background image with placeholder
Image(
    painter = painterResource(id = R.drawable.pincast_logo_placeholder),
    contentDescription = "PinCast Background Logo",
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Fit,
    alpha = 0.2f // Semi-transparent
)
```

2. Replace `R.drawable.pincast_logo_placeholder` with `R.drawable.pincast_jackal_bg`
```kotlin
// Background image with jackal
Image(
    painter = painterResource(id = R.drawable.pincast_jackal_bg),
    contentDescription = "PinCast Background Logo",
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Fit,
    alpha = 0.2f // Semi-transparent
)
```

## Image Specifications

- **Format**: PNG (preferred for transparency), JPG, or WebP
- **Resolution**: 1080x1080px or higher recommended
- **Filename**: Must be all lowercase with underscores: `pincast_jackal_bg.png`
- **Location**: Place in `app/src/main/res/drawable/` 