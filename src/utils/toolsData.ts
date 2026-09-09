import type { ImageToolDefinition } from '../types.ts';

export const ALL_TOOLS: ImageToolDefinition[] = [
  // Core Editing
  {
    id: 'compressor',
    name: 'Image Compressor',
    category: 'core',
    description: 'Compress JPG, PNG, WebP with custom quality slider and target size savings.',
    iconName: 'Minimize2',
    badge: 'Popular',
  },
  {
    id: 'resizer',
    name: 'Image Resizer',
    category: 'core',
    description: 'Resize dimensions by exact pixels, percentage, or social media presets.',
    iconName: 'Maximize2',
    badge: 'Popular',
  },
  {
    id: 'cropper',
    name: 'Image Cropper',
    category: 'core',
    description: 'Crop freely or lock aspect ratio (1:1, 16:9, 4:3, 9:16) or circular avatar.',
    iconName: 'Crop',
  },
  {
    id: 'rotator-flipper',
    name: 'Rotator & Flipper',
    category: 'core',
    description: 'Rotate 90°, 180°, 270°, arbitrary degrees and flip horizontal or vertical.',
    iconName: 'RotateCw',
  },

  // Format Conversion
  {
    id: 'converter',
    name: 'Format Converter',
    category: 'convert',
    description: 'Convert between PNG, JPG, WebP, GIF, BMP, and SVG seamlessly in browser.',
    iconName: 'RefreshCw',
    badge: 'Essential',
  },
  {
    id: 'base64',
    name: 'Image to Base64',
    category: 'convert',
    description: 'Convert image to Data URL Base64 or decode Base64 strings to PNG/JPG.',
    iconName: 'Code',
  },
  {
    id: 'svg-rasterizer',
    name: 'SVG to PNG Converter',
    category: 'convert',
    description: 'Render SVG markup or files to high-resolution PNG/JPG with scale factor.',
    iconName: 'FileCode2',
  },

  // Adjust & Filter
  {
    id: 'adjustments',
    name: 'Brightness & Contrast',
    category: 'adjust',
    description: 'Fine-tune exposure, brightness, contrast, saturation, and hue rotation.',
    iconName: 'Sliders',
  },
  {
    id: 'color-filters',
    name: 'Color Filters & Presets',
    category: 'adjust',
    description: 'Apply Sepia, Grayscale, Invert, Vintage, Cyberpunk, and Duotone effects.',
    iconName: 'Palette',
  },
  {
    id: 'blur',
    name: 'Image Blur Tool',
    category: 'adjust',
    description: 'Apply smooth Gaussian blur, privacy face blur, or tilt-shift focus effect.',
    iconName: 'Droplet',
  },
  {
    id: 'sharpen',
    name: 'Sharpen Tool',
    category: 'adjust',
    description: 'Enhance details and edge clarity with customizable unsharp convolution filter.',
    iconName: 'Zap',
  },

  // Creative & Effects
  {
    id: 'background-remover',
    name: 'Background Remover',
    category: 'creative',
    description: 'Extract subjects or remove green screen and background colors with tolerance.',
    iconName: 'Scissors',
    badge: 'AI / Canvas',
  },
  {
    id: 'watermark',
    name: 'Watermark Tool',
    category: 'creative',
    description: 'Protect images with customizable text or repeated tiled copyright watermark.',
    iconName: 'Stamp',
  },
  {
    id: 'meme-generator',
    name: 'Meme Generator',
    category: 'creative',
    description: 'Create viral memes with top & bottom Impact text, outline strokes, and styling.',
    iconName: 'Smile',
  },
  {
    id: 'collage',
    name: 'Collage Maker',
    category: 'creative',
    description: 'Combine multiple images into 2x2 or 3x3 grids with custom margins and rounding.',
    iconName: 'LayoutGrid',
  },

  // Utilities
  {
    id: 'splitter',
    name: 'Image Splitter / Slicer',
    category: 'utilities',
    description: 'Cut image into grid tiles for Instagram carousel or web slices with ZIP download.',
    iconName: 'Grid',
  },
  {
    id: 'color-palette',
    name: 'Color Palette Extractor',
    category: 'utilities',
    description: 'Extract dominant color palettes with HEX, RGB, and HSL values.',
    iconName: 'Pipette',
  },
  {
    id: 'exif-metadata',
    name: 'EXIF Viewer & Remover',
    category: 'utilities',
    description: 'Inspect camera metadata, timestamps, and 1-click sanitize for privacy.',
    iconName: 'Info',
    badge: 'Privacy',
  },
  {
    id: 'qr-generator',
    name: 'QR Code Generator',
    category: 'utilities',
    description: 'Generate high-res customizable QR code images with colors and export options.',
    iconName: 'QrCode',
  },
];
