# Custom Background Image Instructions

To replace the placeholder logo with your custom PinCast logo featuring a jackal:

## Option 1: Add Your Custom Image
1. Create a modified version of the PinCast logo where the pin/location icon is replaced with a jackal image
2. Save it as `pincast_jackal_bg.png` (or .jpg/.webp)
3. Add the file to the app resources directory (`app/src/main/res/drawable/`)

## Option 2: Use the Placeholder (Already Implemented)
The app currently uses `pincast_logo_placeholder.xml` as a fallback, which is a simple vector drawable version of the logo. If you don't add a custom image, this placeholder will continue to be used.

## Image Specifications
- Resolution: Recommended 1080x1080px or higher for best display
- Format: PNG (preferred for transparency), JPG, or WebP
- File name: Must be lowercase letters, numbers, and underscores only (pincast_jackal_bg)

## Quick Custom Image Creation Guide
1. Use an image editing software like Photoshop, GIMP, or online tools like Canva
2. Open the original PinCast logo (shown at the home screen)
3. Remove the pin/location icon layer
4. Add the jackal image as a new layer in the center
5. Scale and position the jackal image appropriately
6. Export the completed image as pincast_jackal_bg.png
7. Add it to the drawable directory

Note: The app will automatically detect and use your custom image once added to the drawable directory. No code changes are needed. 