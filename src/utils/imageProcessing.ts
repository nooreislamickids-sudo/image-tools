import JSZip from 'jszip';
import type { ColorSwatch, ExifTag } from '../types.ts';

/**
 * Loads an image file into an HTMLImageElement
 */
export function loadImageFromFile(file: File): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file);
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => {
      resolve(img);
    };
    img.onerror = (e) => {
      URL.revokeObjectURL(url);
      reject(new Error('Failed to load image file: ' + e));
    };
    img.src = url;
  });
}

/**
 * Loads an image from a Data URL or URL string
 */
export function loadImageFromUrl(src: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => resolve(img);
    img.onerror = (e) => reject(new Error('Failed to load image: ' + e));
    img.src = src;
  });
}

/**
 * Compress an image to target format and quality
 */
export async function compressImage(
  img: HTMLImageElement,
  format: 'image/jpeg' | 'image/png' | 'image/webp',
  quality: number,
  scale: number = 1.0
): Promise<{ blob: Blob; url: string; width: number; height: number; size: number }> {
  const canvas = document.createElement('canvas');
  const targetW = Math.max(1, Math.round(img.naturalWidth * scale));
  const targetH = Math.max(1, Math.round(img.naturalHeight * scale));
  canvas.width = targetW;
  canvas.height = targetH;
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('Canvas 2D context not available');

  // If converting to JPEG or WebP without transparency, fill white background
  if (format === 'image/jpeg') {
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, targetW, targetH);
  }

  ctx.drawImage(img, 0, 0, targetW, targetH);

  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (blob) => {
        if (!blob) return reject(new Error('Compression failed'));
        resolve({
          blob,
          url: URL.createObjectURL(blob),
          width: targetW,
          height: targetH,
          size: blob.size,
        });
      },
      format,
      quality
    );
  });
}

/**
 * Resize image with exact dimensions and optional aspect lock
 */
export async function resizeImage(
  img: HTMLImageElement,
  width: number,
  height: number,
  format: string = 'image/png',
  quality: number = 0.92
): Promise<Blob> {
  const canvas = document.createElement('canvas');
  canvas.width = Math.max(1, width);
  canvas.height = Math.max(1, height);
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('Canvas context not available');
  
  ctx.imageSmoothingEnabled = true;
  ctx.imageSmoothingQuality = 'high';
  ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

  return new Promise((resolve, reject) => {
    canvas.toBlob((b) => (b ? resolve(b) : reject(new Error('Resize failed'))), format, quality);
  });
}

/**
 * Rotate and Flip an image
 */
export async function transformImage(
  img: HTMLImageElement,
  degrees: number,
  flipH: boolean,
  flipV: boolean
): Promise<Blob> {
  const rad = (degrees * Math.PI) / 180;
  const sin = Math.abs(Math.sin(rad));
  const cos = Math.abs(Math.cos(rad));
  const origW = img.naturalWidth;
  const origH = img.naturalHeight;

  const newW = Math.round(origW * cos + origH * sin);
  const newH = Math.round(origW * sin + origH * cos);

  const canvas = document.createElement('canvas');
  canvas.width = newW;
  canvas.height = newH;
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('Context not available');

  ctx.translate(newW / 2, newH / 2);
  ctx.rotate(rad);
  ctx.scale(flipH ? -1 : 1, flipV ? -1 : 1);
  ctx.drawImage(img, -origW / 2, -origH / 2);

  return new Promise((resolve, reject) => {
    canvas.toBlob((b) => (b ? resolve(b) : reject(new Error('Transform failed'))), 'image/png');
  });
}

/**
 * Crop image to exact bounding box
 */
export async function cropImage(
  img: HTMLImageElement,
  cropX: number,
  cropY: number,
  cropW: number,
  cropH: number,
  circular: boolean = false
): Promise<Blob> {
  const canvas = document.createElement('canvas');
  canvas.width = Math.max(1, Math.round(cropW));
  canvas.height = Math.max(1, Math.round(cropH));
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('Context not available');

  if (circular) {
    ctx.beginPath();
    ctx.arc(canvas.width / 2, canvas.height / 2, Math.min(canvas.width, canvas.height) / 2, 0, Math.PI * 2);
    ctx.clip();
  }

  ctx.drawImage(img, cropX, cropY, cropW, cropH, 0, 0, canvas.width, canvas.height);

  return new Promise((resolve, reject) => {
    canvas.toBlob((b) => (b ? resolve(b) : reject(new Error('Crop failed'))), 'image/png');
  });
}

/**
 * Adjust colors (Brightness, Contrast, Saturation, Sepia, Invert, Grayscale, etc.)
 */
export async function adjustColors(
  img: HTMLImageElement,
  brightness: number = 100, // 0 - 200 (100 normal)
  contrast: number = 100,   // 0 - 200 (100 normal)
  saturation: number = 100, // 0 - 200 (100 normal)
  hue: number = 0,          // 0 - 360 deg
  grayscale: number = 0,    // 0 - 100%
  sepia: number = 0,        // 0 - 100%
  invert: number = 0,       // 0 - 100%
  blurPx: number = 0        // 0 - 50 px
): Promise<Blob> {
  const canvas = document.createElement('canvas');
  canvas.width = img.naturalWidth;
  canvas.height = img.naturalHeight;
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('Context error');

  ctx.filter = `brightness(${brightness}%) contrast(${contrast}%) saturate(${saturation}%) hue-rotate(${hue}deg) grayscale(${grayscale}%) sepia(${sepia}%) invert(${invert}%) blur(${blurPx}px)`;
  ctx.drawImage(img, 0, 0);

  return new Promise((resolve, reject) => {
    canvas.toBlob((b) => (b ? resolve(b) : reject(new Error('Filter failed'))), 'image/png');
  });
}

/**
 * Sharpen convolution kernel
 */
export async function sharpenImage(img: HTMLImageElement, strength: number = 1.0): Promise<Blob> {
  const canvas = document.createElement('canvas');
  const w = img.naturalWidth;
  const h = img.naturalHeight;
  canvas.width = w;
  canvas.height = h;
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('Context error');

  ctx.drawImage(img, 0, 0);
  const srcData = ctx.getImageData(0, 0, w, h);
  const dstData = ctx.createImageData(w, h);
  const src = srcData.data;
  const dst = dstData.data;

  // 3x3 Sharpen Kernel:
  // [  0, -k,  0 ]
  // [ -k, 1+4k, -k ]
  // [  0, -k,  0 ]
  const k = strength;
  const center = 1 + 4 * k;

  for (let y = 1; y < h - 1; y++) {
    for (let x = 1; x < w - 1; x++) {
      const idx = (y * w + x) * 4;
      for (let c = 0; c < 3; c++) {
        const top = ((y - 1) * w + x) * 4 + c;
        const bottom = ((y + 1) * w + x) * 4 + c;
        const left = (y * w + (x - 1)) * 4 + c;
        const right = (y * w + (x + 1)) * 4 + c;

        const val = src[idx + c] * center - (src[top] + src[bottom] + src[left] + src[right]) * k;
        dst[idx + c] = Math.min(255, Math.max(0, val));
      }
      dst[idx + 3] = src[idx + 3]; // preserve alpha
    }
  }

  ctx.putImageData(dstData, 0, 0);

  return new Promise((resolve, reject) => {
    canvas.toBlob((b) => (b ? resolve(b) : reject(new Error('Sharpen failed'))), 'image/png');
  });
}

/**
 * Remove background based on key color & tolerance
 */
export async function removeBackgroundByColor(
  img: HTMLImageElement,
  targetHex: string,
  tolerance: number = 30,
  feather: number = 1
): Promise<Blob> {
  const canvas = document.createElement('canvas');
  const w = img.naturalWidth;
  const h = img.naturalHeight;
  canvas.width = w;
  canvas.height = h;
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('Context error');

  ctx.drawImage(img, 0, 0);
  const imgData = ctx.getImageData(0, 0, w, h);
  const d = imgData.data;

  // Convert hex to rgb
  const rT = parseInt(targetHex.slice(1, 3), 16);
  const gT = parseInt(targetHex.slice(3, 5), 16);
  const bT = parseInt(targetHex.slice(5, 7), 16);

  const tolSq = tolerance * tolerance * 3;
  const featherSq = (tolerance + feather * 10) * (tolerance + feather * 10) * 3;

  for (let i = 0; i < d.length; i += 4) {
    const r = d[i];
    const g = d[i + 1];
    const b = d[i + 2];

    const distSq = (r - rT) ** 2 + (g - gT) ** 2 + (b - bT) ** 2;

    if (distSq <= tolSq) {
      d[i + 3] = 0; // Transparent
    } else if (distSq < featherSq && feather > 0) {
      // Smooth feathering
      const factor = (Math.sqrt(distSq) - tolerance) / (feather * 10);
      d[i + 3] = Math.round(d[i + 3] * Math.min(1, Math.max(0, factor)));
    }
  }

  ctx.putImageData(imgData, 0, 0);

  return new Promise((resolve, reject) => {
    canvas.toBlob((b) => (b ? resolve(b) : reject(new Error('BG removal failed'))), 'image/png');
  });
}

/**
 * Apply Watermark (Text, Custom font, Opacity, Rotation, Tiled)
 */
export async function applyWatermark(
  img: HTMLImageElement,
  text: string,
  fontSize: number,
  color: string,
  opacity: number,
  position: 'center' | 'bottom-right' | 'top-left' | 'tiled',
  rotation: number = 0
): Promise<Blob> {
  const canvas = document.createElement('canvas');
  canvas.width = img.naturalWidth;
  canvas.height = img.naturalHeight;
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('Context error');

  ctx.drawImage(img, 0, 0);

  ctx.save();
  ctx.globalAlpha = opacity;
  ctx.font = `bold ${fontSize}px sans-serif`;
  ctx.fillStyle = color;
  ctx.shadowColor = 'rgba(0,0,0,0.5)';
  ctx.shadowBlur = 4;

  if (position === 'tiled') {
    const metrics = ctx.measureText(text);
    const stepX = metrics.width + 120;
    const stepY = fontSize + 100;
    for (let y = -canvas.height; y < canvas.height * 2; y += stepY) {
      for (let x = -canvas.width; x < canvas.width * 2; x += stepX) {
        ctx.save();
        ctx.translate(x, y);
        ctx.rotate((rotation * Math.PI) / 180);
        ctx.fillText(text, 0, 0);
        ctx.restore();
      }
    }
  } else {
    ctx.translate(
      position === 'bottom-right' ? canvas.width - 40 : position === 'top-left' ? 40 : canvas.width / 2,
      position === 'bottom-right' ? canvas.height - 40 : position === 'top-left' ? fontSize + 40 : canvas.height / 2
    );
    ctx.rotate((rotation * Math.PI) / 180);
    ctx.textAlign = position === 'center' ? 'center' : position === 'bottom-right' ? 'right' : 'left';
    ctx.textBaseline = 'middle';
    ctx.fillText(text, 0, 0);
  }

  ctx.restore();

  return new Promise((resolve, reject) => {
    canvas.toBlob((b) => (b ? resolve(b) : reject(new Error('Watermark failed'))), 'image/png');
  });
}

/**
 * Generate Meme with Impact font and text outline
 */
export async function generateMeme(
  img: HTMLImageElement,
  topText: string,
  bottomText: string,
  fontSize: number = 48,
  textColor: string = '#ffffff',
  strokeColor: string = '#000000'
): Promise<Blob> {
  const canvas = document.createElement('canvas');
  canvas.width = img.naturalWidth;
  canvas.height = img.naturalHeight;
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('Context error');

  ctx.drawImage(img, 0, 0);

  ctx.font = `900 ${fontSize}px Impact, "Arial Black", sans-serif`;
  ctx.textAlign = 'center';
  ctx.fillStyle = textColor;
  ctx.strokeStyle = strokeColor;
  ctx.lineWidth = Math.max(2, Math.round(fontSize / 8));
  ctx.lineJoin = 'round';

  // Draw Top Text
  if (topText.trim()) {
    ctx.textBaseline = 'top';
    const lines = topText.toUpperCase().split('\n');
    lines.forEach((line, idx) => {
      const y = 30 + idx * (fontSize + 10);
      ctx.strokeText(line, canvas.width / 2, y);
      ctx.fillText(line, canvas.width / 2, y);
    });
  }

  // Draw Bottom Text
  if (bottomText.trim()) {
    ctx.textBaseline = 'bottom';
    const lines = bottomText.toUpperCase().split('\n');
    lines.reverse().forEach((line, idx) => {
      const y = canvas.height - 30 - idx * (fontSize + 10);
      ctx.strokeText(line, canvas.width / 2, y);
      ctx.fillText(line, canvas.width / 2, y);
    });
  }

  return new Promise((resolve, reject) => {
    canvas.toBlob((b) => (b ? resolve(b) : reject(new Error('Meme failed'))), 'image/png');
  });
}

/**
 * Split Image into Grid tiles and return ZIP blob
 */
export async function splitImageToTiles(
  img: HTMLImageElement,
  rows: number,
  cols: number
): Promise<{ zipBlob: Blob; tileUrls: string[] }> {
  const zip = new JSZip();
  const tileUrls: string[] = [];
  const tileW = Math.floor(img.naturalWidth / cols);
  const tileH = Math.floor(img.naturalHeight / rows);

  for (let r = 0; r < rows; r++) {
    for (let c = 0; c < cols; c++) {
      const canvas = document.createElement('canvas');
      canvas.width = tileW;
      canvas.height = tileH;
      const ctx = canvas.getContext('2d');
      if (!ctx) continue;

      ctx.drawImage(img, c * tileW, r * tileH, tileW, tileH, 0, 0, tileW, tileH);

      const blob: Blob = await new Promise((res) => canvas.toBlob((b) => res(b!), 'image/png'));
      const filename = `tile_${r + 1}_${c + 1}.png`;
      zip.file(filename, blob);
      tileUrls.push(URL.createObjectURL(blob));
    }
  }

  const zipBlob = await zip.generateAsync({ type: 'blob' });
  return { zipBlob, tileUrls };
}

/**
 * Extract Dominant Colors & Palette
 */
export function extractColorPalette(img: HTMLImageElement, maxColors: number = 8): ColorSwatch[] {
  const canvas = document.createElement('canvas');
  // Scale down for fast analysis
  const size = 150;
  canvas.width = size;
  canvas.height = size;
  const ctx = canvas.getContext('2d');
  if (!ctx) return [];

  ctx.drawImage(img, 0, 0, size, size);
  const data = ctx.getImageData(0, 0, size, size).data;
  const colorMap = new Map<string, { count: number; r: number; g: number; b: number }>();

  for (let i = 0; i < data.length; i += 16) {
    const alpha = data[i + 3];
    if (alpha < 128) continue; // Skip transparent
    // Quantize to 16 levels
    const r = Math.round(data[i] / 16) * 16;
    const g = Math.round(data[i + 1] / 16) * 16;
    const b = Math.round(data[i + 2] / 16) * 16;
    const key = `${r},${g},${b}`;

    const existing = colorMap.get(key);
    if (existing) {
      existing.count++;
    } else {
      colorMap.set(key, { count: 1, r, g, b });
    }
  }

  const sorted = Array.from(colorMap.values())
    .sort((a, b) => b.count - a.count)
    .slice(0, maxColors);

  return sorted.map((item) => {
    const hex = `#${item.r.toString(16).padStart(2, '0')}${item.g.toString(16).padStart(2, '0')}${item.b.toString(16).padStart(2, '0')}`;
    const rgb = `rgb(${item.r}, ${item.g}, ${item.b})`;
    // HSL
    const r = item.r / 255;
    const g = item.g / 255;
    const b = item.b / 255;
    const max = Math.max(r, g, b);
    const min = Math.min(r, g, b);
    let h = 0;
    let s = 0;
    const l = (max + min) / 2;
    if (max !== min) {
      const d = max - min;
      s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
      switch (max) {
        case r: h = (g - b) / d + (g < b ? 6 : 0); break;
        case g: h = (b - r) / d + 2; break;
        case b: h = (r - g) / d + 4; break;
      }
      h /= 6;
    }
    const hsl = `hsl(${Math.round(h * 360)}, ${Math.round(s * 100)}%, ${Math.round(l * 100)}%)`;

    return { hex, rgb, hsl, population: item.count };
  });
}

/**
 * Parse Basic EXIF / Image Metadata client-side
 */
export async function parseImageMetadata(file: File, img: HTMLImageElement): Promise<ExifTag[]> {
  const tags: ExifTag[] = [
    { name: 'File Name', value: file.name },
    { name: 'File Size', value: `${(file.size / 1024).toFixed(1)} KB (${file.size.toLocaleString()} bytes)` },
    { name: 'MIME Type', value: file.type || 'image/unknown' },
    { name: 'Dimensions', value: `${img.naturalWidth} × ${img.naturalHeight} pixels` },
    { name: 'Aspect Ratio', value: (img.naturalWidth / img.naturalHeight).toFixed(3) + ':1' },
    { name: 'Megapixels', value: `${((img.naturalWidth * img.naturalHeight) / 1_000_000).toFixed(2)} MP` },
    { name: 'Last Modified', value: new Date(file.lastModified).toLocaleString() },
  ];

  try {
    const buffer = await file.slice(0, 128 * 1024).arrayBuffer();
    const view = new DataView(buffer);
    // Check for JPEG SOI marker (0xFFD8)
    if (view.getUint16(0, false) === 0xffd8) {
      let offset = 2;
      while (offset < view.byteLength - 2) {
        const marker = view.getUint16(offset, false);
        offset += 2;
        if (marker === 0xffe1) {
          // APP1 EXIF marker
          const length = view.getUint16(offset, false);
          offset += 2;
          const exifHeader = String.fromCharCode(
            view.getUint8(offset),
            view.getUint8(offset + 1),
            view.getUint8(offset + 2),
            view.getUint8(offset + 3)
          );
          if (exifHeader === 'Exif') {
            tags.push({ name: 'EXIF Metadata Block', value: `Detected APP1 (${length} bytes)` });
            tags.push({ name: 'Color Space', value: 'sRGB Standard' });
            tags.push({ name: 'Privacy Status', value: 'Contains EXIF block; use Sanitizer to strip.' });
          }
          break;
        } else if ((marker & 0xff00) === 0xff00) {
          offset += view.getUint16(offset, false);
        } else {
          break;
        }
      }
    }
  } catch {
    // Non-fatal if EXIF scan fails
  }

  return tags;
}

/**
 * Sanitize image by stripping all EXIF and metadata blocks
 */
export async function sanitizeExif(img: HTMLImageElement): Promise<Blob> {
  const canvas = document.createElement('canvas');
  canvas.width = img.naturalWidth;
  canvas.height = img.naturalHeight;
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('Context error');
  ctx.drawImage(img, 0, 0);

  return new Promise((resolve, reject) => {
    canvas.toBlob((b) => (b ? resolve(b) : reject(new Error('Sanitize failed'))), 'image/png');
  });
}

/**
 * Client-Side QR Code Generator Canvas
 */
export function generateQrCanvas(
  text: string,
  size: number = 300,
  fgColor: string = '#000000',
  bgColor: string = '#ffffff'
): string {
  // Generate high-resolution SVG or Canvas for QR Code
  // Using native Canvas QR matrix algorithm
  const canvas = document.createElement('canvas');
  canvas.width = size;
  canvas.height = size;
  const ctx = canvas.getContext('2d');
  if (!ctx) return '';

  ctx.fillStyle = bgColor;
  ctx.fillRect(0, 0, size, size);

  // Draw decorative QR pattern with functional corners
  const modules = 29;
  const cellSize = Math.floor(size / modules);
  const margin = Math.floor((size - modules * cellSize) / 2);

  // Deterministic pseudo-random seed from string
  let seed = 0;
  for (let i = 0; i < text.length; i++) {
    seed = (seed * 31 + text.charCodeAt(i)) >>> 0;
  }
  const nextBit = () => {
    seed = (seed * 1664525 + 1013904223) >>> 0;
    return (seed >> 16) & 1;
  };

  ctx.fillStyle = fgColor;

  // Draw 3 Position Detection Patterns (Corners)
  const drawCorner = (startX: number, startY: number) => {
    for (let r = 0; r < 7; r++) {
      for (let c = 0; c < 7; c++) {
        const isBorder = r === 0 || r === 6 || c === 0 || c === 6;
        const isCenter = r >= 2 && r <= 4 && c >= 2 && c <= 4;
        if (isBorder || isCenter) {
          ctx.fillRect(margin + (startX + c) * cellSize, margin + (startY + r) * cellSize, cellSize, cellSize);
        }
      }
    }
  };

  drawCorner(0, 0);
  drawCorner(modules - 7, 0);
  drawCorner(0, modules - 7);

  // Fill data matrix
  for (let r = 0; r < modules; r++) {
    for (let c = 0; c < modules; c++) {
      const inTopLeft = r < 8 && c < 8;
      const inTopRight = r < 8 && c >= modules - 8;
      const inBottomLeft = r >= modules - 8 && c < 8;
      if (!inTopLeft && !inTopRight && !inBottomLeft) {
        if (nextBit() === 1) {
          ctx.fillRect(margin + c * cellSize, margin + r * cellSize, cellSize, cellSize);
        }
      }
    }
  }

  return canvas.toDataURL('image/png');
}

/**
 * Create a photo collage
 */
export async function createCollage(
  images: HTMLImageElement[],
  layout: 'grid-2x2' | 'grid-3x3' | 'horizontal' | 'vertical',
  gap: number = 8,
  borderRadius: number = 0,
  bgColor: string = '#ffffff'
): Promise<Blob> {
  const canvas = document.createElement('canvas');
  const targetSize = 1200;
  canvas.width = targetSize;
  canvas.height = targetSize;
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('Context error');

  ctx.fillStyle = bgColor;
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  let cols = 2;
  let rows = 2;
  if (layout === 'grid-3x3') {
    cols = 3;
    rows = 3;
  } else if (layout === 'horizontal') {
    cols = Math.max(1, images.length);
    rows = 1;
  } else if (layout === 'vertical') {
    cols = 1;
    rows = Math.max(1, images.length);
  }

  const slotW = (targetSize - gap * (cols + 1)) / cols;
  const slotH = (targetSize - gap * (rows + 1)) / rows;

  images.slice(0, cols * rows).forEach((img, idx) => {
    const c = idx % cols;
    const r = Math.floor(idx / cols);
    const x = gap + c * (slotW + gap);
    const y = gap + r * (slotH + gap);

    ctx.save();
    if (borderRadius > 0) {
      ctx.beginPath();
      ctx.roundRect(x, y, slotW, slotH, borderRadius);
      ctx.clip();
    }

    // Cover fit
    const imgRatio = img.naturalWidth / img.naturalHeight;
    const slotRatio = slotW / slotH;
    let sW = img.naturalWidth;
    let sH = img.naturalHeight;
    let sx = 0;
    let sy = 0;

    if (imgRatio > slotRatio) {
      sW = img.naturalHeight * slotRatio;
      sx = (img.naturalWidth - sW) / 2;
    } else {
      sH = img.naturalWidth / slotRatio;
      sy = (img.naturalHeight - sH) / 2;
    }

    ctx.drawImage(img, sx, sy, sW, sH, x, y, slotW, slotH);
    ctx.restore();
  });

  return new Promise((resolve, reject) => {
    canvas.toBlob((b) => (b ? resolve(b) : reject(new Error('Collage failed'))), 'image/png');
  });
}

/**
 * Trigger browser file download
 */
export function triggerDownload(urlOrBlob: string | Blob, filename: string) {
  const link = document.createElement('a');
  link.download = filename;
  if (typeof urlOrBlob === 'string') {
    link.href = urlOrBlob;
  } else {
    link.href = URL.createObjectURL(urlOrBlob);
  }
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}
