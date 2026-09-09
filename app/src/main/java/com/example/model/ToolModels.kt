package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PhotoSizeSelectActual
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(val title: String, val description: String) {
    ALL("All Tools", "Browse our complete catalog of image utilities"),
    COMPRESS("Compress & Optimize", "Shrink file sizes while maintaining pristine quality"),
    RESIZE_CROP("Resize & Crop", "Adjust dimensions, scale, crop ratios and orientation"),
    CONVERT("Format Converter", "Transform between JPG, PNG, and WebP seamlessly"),
    EDIT_ADJUST("Edit & Adjust", "Fine-tune lighting, colors, sharpness and contrast"),
    FILTERS("Filters & Effects", "Stunning artistic styles, vintage, and color gradings"),
    WATERMARK("Watermark & Borders", "Protect images with text captions, logos and framing"),
    METADATA("Metadata & Privacy", "Inspect EXIF properties or strip data for online safety"),
    DEVELOPER("Developer Tools", "Base64 data URIs, QR codes, and color palette extraction"),
    SOCIAL_ICONS("Social & Icons", "Presets for Instagram, YouTube, App Icons and Passports"),
    PDF_MULTI("PDF & Multi-Image", "Combine, merge or compile photos into PDF documents")
}

enum class ToolId {
    COMPRESSOR,
    RESIZER,
    CROPPER,
    ROTATE_FLIP,
    FORMAT_CONVERTER,
    BACKGROUND_REMOVER,
    COLOR_ADJUSTER,
    FILTERS,
    BLUR_SHARPEN,
    BORDER_CORNERS,
    WATERMARK_TEXT,
    PIXELATE_TOOL,
    EXIF_VIEWER,
    BASE64_TOOL,
    COLOR_PALETTE,
    QR_GENERATOR,
    FAVICON_APP_ICON,
    SOCIAL_RESIZER,
    PASSPORT_PHOTO,
    COLLAGE_MAKER,
    IMAGES_TO_PDF
}

data class ToolItem(
    val id: ToolId,
    val title: String,
    val subtitle: String,
    val description: String,
    val category: ToolCategory,
    val icon: ImageVector,
    val badge: String? = null,
    val keywords: List<String>,
    val faqs: List<Pair<String, String>>
)

object ToolRegistry {
    val tools: List<ToolItem> = listOf(
        ToolItem(
            id = ToolId.COMPRESSOR,
            title = "Image Compressor",
            subtitle = "Reduce KB & MB without noticeable loss",
            description = "Compress JPG, PNG, and WebP files to your exact target quality or file size. Drastically reduce storage and load times while preserving visual fidelity.",
            category = ToolCategory.COMPRESS,
            icon = Icons.Default.HighQuality,
            badge = "Popular",
            keywords = listOf("compress", "compressor", "shrink", "optimize", "reduce size", "file size", "kb", "mb", "quality"),
            faqs = listOf(
                "How does on-device compression work?" to "Your image is re-encoded directly within the browser/device memory using optimized compression algorithms without ever sending your photos to any remote server.",
                "Which format compresses best?" to "WebP generally provides 25-35% better compression efficiency than standard JPEG at similar visual quality."
            )
        ),
        ToolItem(
            id = ToolId.RESIZER,
            title = "Image Resizer",
            subtitle = "Scale dimensions by pixels or percentage",
            description = "Resize photos to custom width and height with optional aspect ratio lock. Choose between bilinear and bicubic resampling for crisp results.",
            category = ToolCategory.RESIZE_CROP,
            icon = Icons.Default.PhotoSizeSelectActual,
            badge = "Essential",
            keywords = listOf("resize", "resizer", "dimensions", "width", "height", "scale", "pixels", "percentage", "aspect ratio"),
            faqs = listOf(
                "Can I keep the original aspect ratio?" to "Yes, enabling the Aspect Ratio Lock automatically calculates the corresponding height when you change the width, preventing image distortion.",
                "Will resizing reduce file size?" to "Downscaling to smaller pixel dimensions significantly reduces the resulting file size."
            )
        ),
        ToolItem(
            id = ToolId.CROPPER,
            title = "Image Cropper",
            subtitle = "Cut to 1:1, 16:9, 4:3, or custom ratios",
            description = "Crop out unwanted areas and frame subjects with precision. Includes presets for square (1:1), widescreen (16:9), portrait (9:16), standard (4:3), and freeform.",
            category = ToolCategory.RESIZE_CROP,
            icon = Icons.Default.Crop,
            badge = "Popular",
            keywords = listOf("crop", "cropper", "cut", "trim", "square", "aspect ratio", "frame", "1:1", "16:9"),
            faqs = listOf(
                "What aspect ratio should I use for Instagram?" to "Use 1:1 for standard feed posts, 4:5 for vertical portraits, and 9:16 for Stories and Reels."
            )
        ),
        ToolItem(
            id = ToolId.ROTATE_FLIP,
            title = "Rotator & Flipper",
            subtitle = "Rotate 90°, 180° or flip horizontal/vertical",
            description = "Fix photo orientation instantly. Rotate clockwise, counter-clockwise, or mirror your image horizontally and vertically with zero loss in resolution.",
            category = ToolCategory.RESIZE_CROP,
            icon = Icons.Default.Transform,
            keywords = listOf("rotate", "rotator", "flip", "flipper", "mirror", "horizontal", "vertical", "orientation", "turn"),
            faqs = listOf(
                "Does rotating degrade image quality?" to "No, 90-degree rotations and flipping are geometric pixel rearrangements without loss of fidelity."
            )
        ),
        ToolItem(
            id = ToolId.FORMAT_CONVERTER,
            title = "Format Converter",
            subtitle = "Convert JPG, PNG, and WebP formats",
            description = "Easily convert between JPG, PNG, and WebP. Turn transparent PNGs into lightweight JPEGs with custom background colors, or modernize JPEGs to next-gen WebP.",
            category = ToolCategory.CONVERT,
            icon = Icons.Default.Refresh,
            badge = "Versatile",
            keywords = listOf("convert", "converter", "jpg to png", "png to jpg", "webp to png", "jpg to webp", "png to webp", "format"),
            faqs = listOf(
                "When should I use PNG over JPG?" to "Use PNG when your image requires transparent backgrounds or sharp line art and text. Use JPG for natural photographs to keep file sizes small."
            )
        ),
        ToolItem(
            id = ToolId.BACKGROUND_REMOVER,
            title = "Background Remover",
            subtitle = "Cutout subjects & clear solid backgrounds",
            description = "Erase solid, studio, or keyed backgrounds to create transparent PNGs. Adjust tolerance thresholds to get smooth edge cutouts.",
            category = ToolCategory.EDIT_ADJUST,
            icon = Icons.Default.AutoAwesome,
            badge = "Smart",
            keywords = listOf("background", "remove background", "transparent", "png", "cutout", "eraser", "chromakey"),
            faqs = listOf(
                "Does it export with transparency?" to "Yes, results are exported as 32-bit ARGB PNG files supporting full alpha transparency."
            )
        ),
        ToolItem(
            id = ToolId.COLOR_ADJUSTER,
            title = "Color & Tone Adjuster",
            subtitle = "Fine-tune Brightness, Contrast & Saturation",
            description = "Enhance lighting, boost colors, or fix exposure. Features dynamic sliders for Brightness, Contrast, Saturation, and Color Temperature/Tint.",
            category = ToolCategory.EDIT_ADJUST,
            icon = Icons.Default.Tune,
            keywords = listOf("adjust", "color", "brightness", "contrast", "saturation", "exposure", "lighting", "tone", "vibrance"),
            faqs = listOf(
                "Can I preview adjustments in real time?" to "Yes, all slider adjustments update the live viewport immediately."
            )
        ),
        ToolItem(
            id = ToolId.FILTERS,
            title = "Photo Filters & Styles",
            subtitle = "Vintage, Grayscale, Cyberpunk & Sepia",
            description = "Transform photo mood with curated cinematic and artistic filters: Grayscale, Sepia, Invert, Warm Sunset, Cool Breeze, Cyberpunk, and Dramatic High-Contrast.",
            category = ToolCategory.FILTERS,
            icon = Icons.Default.Filter,
            badge = "Creative",
            keywords = listOf("filter", "filters", "sepia", "grayscale", "black and white", "vintage", "cyberpunk", "warm", "cool", "effects"),
            faqs = listOf(
                "Can I combine filters?" to "Apply a filter and continue editing colors or saving intermediate results easily."
            )
        ),
        ToolItem(
            id = ToolId.BLUR_SHARPEN,
            title = "Blur & Sharpen",
            subtitle = "Smooth out noise or enhance edge definition",
            description = "Add smooth background blur for privacy and aesthetic bokeh, or apply unsharp masking convolution to crisp up soft details.",
            category = ToolCategory.EDIT_ADJUST,
            icon = Icons.Default.BlurOn,
            keywords = listOf("blur", "sharpen", "gaussian blur", "soften", "clarity", "details", "focus"),
            faqs = listOf(
                "Does sharpening fix blurry photos?" to "Sharpening enhances local edge contrast to make slightly soft photos appear significantly crisper."
            )
        ),
        ToolItem(
            id = ToolId.BORDER_CORNERS,
            title = "Borders & Rounded Corners",
            subtitle = "Add framing, corner radius or circle cutout",
            description = "Frame pictures with customizable border widths and colors. Round corners with smooth curvature or generate circular profile pictures.",
            category = ToolCategory.WATERMARK,
            icon = Icons.Default.AspectRatio,
            keywords = listOf("border", "frame", "rounded corners", "radius", "circle", "avatar", "profile", "corners"),
            faqs = listOf(
                "Can I export circular images with transparent corners?" to "Yes, selecting Circle Avatar automatically clips outside corners into a transparent PNG."
            )
        ),
        ToolItem(
            id = ToolId.WATERMARK_TEXT,
            title = "Watermark & Text Overlay",
            subtitle = "Add copyright, brand logo or custom captions",
            description = "Protect your creative work with text watermarks. Customize text content, font size, opacity, color, and 9-point positional anchors.",
            category = ToolCategory.WATERMARK,
            icon = Icons.Default.TextFields,
            badge = "Pro",
            keywords = listOf("watermark", "text", "overlay", "caption", "copyright", "brand", "stamp", "signature"),
            faqs = listOf(
                "Can I adjust watermark transparency?" to "Yes, the opacity slider ranges from subtle 10% translucent stamps to 100% solid text."
            )
        ),
        ToolItem(
            id = ToolId.PIXELATE_TOOL,
            title = "Pixelate & Anonymize",
            subtitle = "Conceal sensitive info or create 8-bit retro art",
            description = "Pixelate photos with customizable mosaic block sizes. Perfect for obscuring faces, license plates, private text, or crafting pixel-art vibes.",
            category = ToolCategory.EDIT_ADJUST,
            icon = Icons.Default.GridOn,
            keywords = listOf("pixelate", "mosaic", "blur face", "censor", "anonymize", "retro", "8-bit", "pixel"),
            faqs = listOf(
                "Is pixelation reversible?" to "No, pixelation groups and averages pixel data irreversibly, ensuring strong privacy protection."
            )
        ),
        ToolItem(
            id = ToolId.EXIF_VIEWER,
            title = "EXIF & Metadata Studio",
            subtitle = "View camera details or scrub all data for privacy",
            description = "Inspect embedded EXIF metadata including camera model, exposure settings, GPS coordinates, dimensions, and date. Strip metadata with one tap before posting online.",
            category = ToolCategory.METADATA,
            icon = Icons.Default.Info,
            badge = "Privacy",
            keywords = listOf("exif", "metadata", "camera", "privacy", "gps", "scrub", "clean", "strip metadata", "info"),
            faqs = listOf(
                "Why should I strip metadata?" to "Photos taken with smartphones often embed exact GPS latitude/longitude and device serials. Removing EXIF protects your privacy."
            )
        ),
        ToolItem(
            id = ToolId.BASE64_TOOL,
            title = "Base64 & Data URI",
            subtitle = "Convert image to code string & decode back",
            description = "Encode images directly into Data URI Base64 strings for CSS/HTML embedding, or paste Base64 code to restore and download the original picture.",
            category = ToolCategory.DEVELOPER,
            icon = Icons.Default.Code,
            keywords = listOf("base64", "data uri", "encode", "decode", "code", "html", "css", "developer", "string"),
            faqs = listOf(
                "What is a Data URI?" to "A Data URI allows you to embed image data directly into HTML or CSS files without requiring a separate network request."
            )
        ),
        ToolItem(
            id = ToolId.COLOR_PALETTE,
            title = "Color Palette & Eyedropper",
            subtitle = "Extract dominant colors & pick pixel hex codes",
            description = "Extract the top 6 dominant color swatches from any picture with HEX and RGB values. Tap anywhere on the image to inspect exact pixel colors.",
            category = ToolCategory.DEVELOPER,
            icon = Icons.Default.ColorLens,
            badge = "Design",
            keywords = listOf("palette", "colors", "dominant color", "color picker", "eyedropper", "hex", "rgb", "extract colors"),
            faqs = listOf(
                "Can I copy hex codes?" to "Yes, tapping any color swatch copies its hex string directly to your clipboard."
            )
        ),
        ToolItem(
            id = ToolId.QR_GENERATOR,
            title = "QR Code Generator",
            subtitle = "Create high-res QR codes with custom styling",
            description = "Generate clean, crisp QR code images for URLs, text, contacts, or Wi-Fi logins. Customize foreground and background colors and export as high-res PNG.",
            category = ToolCategory.DEVELOPER,
            icon = Icons.Default.QrCode,
            keywords = listOf("qr", "qr code", "generator", "link", "barcode", "scan", "url"),
            faqs = listOf(
                "Are generated QR codes permanent?" to "Yes! These standard QR codes encode raw text directly into the matrix, so they never expire."
            )
        ),
        ToolItem(
            id = ToolId.FAVICON_APP_ICON,
            title = "Favicon & App Icon Studio",
            subtitle = "Generate complete multi-resolution icon packs",
            description = "Upload your logo or artwork and generate standard web favicons (16x16, 32x32, 48x48) and mobile app launcher icons (192x192, 512x512).",
            category = ToolCategory.SOCIAL_ICONS,
            icon = Icons.Default.Layers,
            keywords = listOf("favicon", "app icon", "icon generator", "16x16", "32x32", "192x192", "512x512", "manifest"),
            faqs = listOf(
                "What icon sizes are included?" to "Generates 16x16, 32x32, 48x48, 180x180 (Apple Touch), 192x192 and 512x512 (PWA)."
            )
        ),
        ToolItem(
            id = ToolId.SOCIAL_RESIZER,
            title = "Social Media Resizer",
            subtitle = "Instagram, YouTube, Twitter & LinkedIn presets",
            description = "Resize photos for social channels with exact platform dimensions: Instagram Square (1080x1080), Story (1080x1920), YouTube Thumbnail (1280x720), Twitter Post (1200x675), and Facebook Cover.",
            category = ToolCategory.SOCIAL_ICONS,
            icon = Icons.Default.Share,
            badge = "Popular",
            keywords = listOf("social media", "instagram", "youtube", "twitter", "facebook", "linkedin", "thumbnail", "story", "banner"),
            faqs = listOf(
                "Does it prevent blurry uploads?" to "Yes, matching platform-native pixel dimensions prevents aggressive social media downsampling."
            )
        ),
        ToolItem(
            id = ToolId.PASSPORT_PHOTO,
            title = "Passport & ID Photo Maker",
            subtitle = "Standard 2x2 inch and 35x45mm specs",
            description = "Prepare official biometric passport and visa photos. Includes guide margins for eye level and head height, solid background lighting, and standard document presets.",
            category = ToolCategory.SOCIAL_ICONS,
            icon = Icons.Default.Badge,
            keywords = listOf("passport", "id photo", "visa", "2x2", "35x45", "biometric", "headshot", "document"),
            faqs = listOf(
                "What are standard passport dimensions?" to "US passports require 2x2 inches (600x600 px at 300 DPI). Schengen and UK passports require 35x45mm."
            )
        ),
        ToolItem(
            id = ToolId.COLLAGE_MAKER,
            title = "Collage & Grid Maker",
            subtitle = "Combine 2 to 4 photos into stylish layouts",
            description = "Arrange multiple images side-by-side (split), vertical stack, or in a 2x2 photo grid with customizable border spacing and background color.",
            category = ToolCategory.PDF_MULTI,
            icon = Icons.Default.GridOn,
            keywords = listOf("collage", "grid", "combine", "side by side", "merge photos", "montage", "multi photo"),
            faqs = listOf(
                "Can I adjust grid spacing?" to "Yes, you can customize outer border margins and inner gap padding between photo tiles."
            )
        ),
        ToolItem(
            id = ToolId.IMAGES_TO_PDF,
            title = "Images to PDF Document",
            subtitle = "Compile single or multiple photos into PDF",
            description = "Convert photos, receipts, or document scans into a clean, paginated PDF file directly on your device. Perfect for sharing or printing.",
            category = ToolCategory.PDF_MULTI,
            icon = Icons.Default.PictureAsPdf,
            badge = "Documents",
            keywords = listOf("pdf", "images to pdf", "jpg to pdf", "convert to pdf", "scan", "document", "receipt", "print"),
            faqs = listOf(
                "Is there a limit on pages?" to "You can add multiple images in order; each image creates an optimized PDF page fitting standard paper dimensions."
            )
        )
    )

    fun searchTools(query: String, category: ToolCategory = ToolCategory.ALL): List<ToolItem> {
        val cleanQuery = query.trim().lowercase()
        return tools.filter { tool ->
            val matchesCategory = (category == ToolCategory.ALL || tool.category == category)
            if (!matchesCategory) return@filter false

            if (cleanQuery.isEmpty()) return@filter true

            tool.title.lowercase().contains(cleanQuery) ||
                tool.subtitle.lowercase().contains(cleanQuery) ||
                tool.description.lowercase().contains(cleanQuery) ||
                tool.category.title.lowercase().contains(cleanQuery) ||
                tool.keywords.any { it.contains(cleanQuery) }
        }
    }

    fun getPopularTools(): List<ToolItem> {
        return tools.filter { it.badge == "Popular" || it.badge == "Essential" }
    }
}
