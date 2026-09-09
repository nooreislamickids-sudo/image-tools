import React, { useState, useEffect, useRef } from 'react';
import {
  ArrowLeft,
  Download,
  Upload,
  RotateCcw,
  Sparkles,
  Copy,
  Check,
  Eye,
  Sliders,
  Maximize2,
  Minimize2,
  FileArchive,
  RefreshCw,
  Scissors,
  Stamp,
  Crop,
  Layers,
  ShieldAlert,
} from 'lucide-react';
import type { ImageToolDefinition, ColorSwatch, ExifTag } from '../types.ts';
import {
  loadImageFromFile,
  loadImageFromUrl,
  compressImage,
  resizeImage,
  transformImage,
  cropImage,
  adjustColors,
  sharpenImage,
  removeBackgroundByColor,
  applyWatermark,
  generateMeme,
  splitImageToTiles,
  extractColorPalette,
  parseImageMetadata,
  sanitizeExif,
  generateQrCanvas,
  createCollage,
  triggerDownload,
} from '../utils/imageProcessing.ts';

// Clean high-res sample image data URI (SVG nature gradient)
const SAMPLE_IMAGE_URL =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(`
  <svg xmlns="http://www.w3.org/2000/svg" width="1200" height="800" viewBox="0 0 1200 800">
    <defs>
      <linearGradient id="sky" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#0f172a" />
        <stop offset="50%" stop-color="#1e1b4b" />
        <stop offset="100%" stop-color="#312e81" />
      </linearGradient>
      <linearGradient id="glow" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stop-color="#ec4899" />
        <stop offset="100%" stop-color="#8b5cf6" />
      </linearGradient>
      <linearGradient id="mountain" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="#475569" />
        <stop offset="100%" stop-color="#0f172a" />
      </linearGradient>
    </defs>
    <rect width="1200" height="800" fill="url(#sky)" />
    <circle cx="600" cy="380" r="180" fill="url(#glow)" opacity="0.85" />
    <polygon points="100,800 450,420 800,800" fill="url(#mountain)" opacity="0.9" />
    <polygon points="500,800 850,360 1200,800" fill="#1e293b" opacity="0.95" />
    <circle cx="250" cy="180" r="3" fill="#fff" />
    <circle cx="450" cy="120" r="2.5" fill="#fff" />
    <circle cx="850" cy="150" r="3" fill="#fff" />
    <circle cx="950" cy="90" r="2" fill="#fff" />
    <text x="600" y="730" font-family="system-ui, sans-serif" font-size="28" font-weight="bold" fill="#f8fafc" text-anchor="middle" letter-spacing="4">IMAGE TOOLS STUDIO</text>
  </svg>
`);

interface ToolWorkspaceProps {
  tool: ImageToolDefinition;
  onBack: () => void;
}

export const ToolWorkspace: React.FC<ToolWorkspaceProps> = ({ tool, onBack }) => {
  // Image State
  const [sourceImage, setSourceImage] = useState<HTMLImageElement | null>(null);
  const [fileName, setFileName] = useState<string>('sample.png');
  const [originalSize, setOriginalSize] = useState<number>(128000);
  const [processedUrl, setProcessedUrl] = useState<string | null>(null);
  const [processedSize, setProcessedSize] = useState<number | null>(null);
  const [isProcessing, setIsProcessing] = useState<boolean>(false);
  const [copied, setCopied] = useState<boolean>(false);
  const [viewMode, setViewMode] = useState<'processed' | 'original' | 'split'>('processed');

  // Tool Specific Options
  // 1. Compressor
  const [compFormat, setCompFormat] = useState<'image/jpeg' | 'image/png' | 'image/webp'>('image/jpeg');
  const [compQuality, setCompQuality] = useState<number>(0.8);
  const [compScale, setCompScale] = useState<number>(1.0);

  // 2. Resizer
  const [resizeWidth, setResizeWidth] = useState<number>(1200);
  const [resizeHeight, setResizeHeight] = useState<number>(800);
  const [lockAspect, setLockAspect] = useState<boolean>(true);

  // 3. Cropper
  const [cropRatio, setCropRatio] = useState<'free' | '1:1' | '16:9' | '4:3' | '9:16' | 'circle'>('1:1');

  // 4. Rotator & Flipper
  const [rotation, setRotation] = useState<number>(0);
  const [flipH, setFlipH] = useState<boolean>(false);
  const [flipV, setFlipV] = useState<boolean>(false);

  // 5. Converter
  const [targetFormat, setTargetFormat] = useState<'image/png' | 'image/jpeg' | 'image/webp'>('image/png');

  // 6. Adjustments
  const [brightness, setBrightness] = useState<number>(100);
  const [contrast, setContrast] = useState<number>(100);
  const [saturation, setSaturation] = useState<number>(100);
  const [hue, setHue] = useState<number>(0);

  // 7. Filters
  const [filterPreset, setFilterPreset] = useState<string>('normal');

  // 8. Blur & Sharpen
  const [blurRadius, setBlurRadius] = useState<number>(8);
  const [sharpenStrength, setSharpenStrength] = useState<number>(1.2);

  // 9. Background Remover
  const [bgKeyColor, setBgKeyColor] = useState<string>('#ffffff');
  const [bgTolerance, setBgTolerance] = useState<number>(35);
  const [bgFeather, setBgFeather] = useState<number>(2);

  // 10. Watermark
  const [wmText, setWmText] = useState<string>('© Copyright 2026');
  const [wmSize, setWmSize] = useState<number>(36);
  const [wmColor, setWmColor] = useState<string>('#ffffff');
  const [wmOpacity, setWmOpacity] = useState<number>(0.5);
  const [wmPos, setWmPos] = useState<'center' | 'bottom-right' | 'top-left' | 'tiled'>('bottom-right');

  // 11. Meme
  const [memeTop, setMemeTop] = useState<string>('WHEN THE CODE');
  const [memeBottom, setMemeBottom] = useState<string>('COMPILES ON FIRST TRY');
  const [memeFontSize, setMemeFontSize] = useState<number>(54);

  // 12. Splitter
  const [splitRows, setSplitRows] = useState<number>(3);
  const [splitCols, setSplitCols] = useState<number>(3);
  const [splitZip, setSplitZip] = useState<Blob | null>(null);

  // 13. Palette & EXIF
  const [palette, setPalette] = useState<ColorSwatch[]>([]);
  const [exifTags, setExifTags] = useState<ExifTag[]>([]);

  // 14. QR Code
  const [qrContent, setQrContent] = useState<string>('https://ai.studio');
  const [qrFg, setQrFg] = useState<string>('#4f46e5');
  const [qrBg, setQrBg] = useState<string>('#ffffff');

  // 15. Base64
  const [base64Text, setBase64Text] = useState<string>('');

  const fileInputRef = useRef<HTMLInputElement | null>(null);

  // Load default sample image on mount
  useEffect(() => {
    loadImageFromUrl(SAMPLE_IMAGE_URL).then((img) => {
      setSourceImage(img);
      setResizeWidth(img.naturalWidth);
      setResizeHeight(img.naturalHeight);
      setOriginalSize(142000);
      setFileName('studio-sample.png');
    });
  }, []);

  // Handle File Upload
  const handleFileUpload = async (file: File) => {
    try {
      setIsProcessing(true);
      const img = await loadImageFromFile(file);
      setSourceImage(img);
      setFileName(file.name);
      setOriginalSize(file.size);
      setResizeWidth(img.naturalWidth);
      setResizeHeight(img.naturalHeight);

      // Extract metadata & palette right away
      parseImageMetadata(file, img).then(setExifTags);
      setPalette(extractColorPalette(img, 8));

      // Read base64
      const reader = new FileReader();
      reader.onload = (e) => setBase64Text(e.target?.result as string);
      reader.readAsDataURL(file);
    } catch (err) {
      console.error(err);
    } finally {
      setIsProcessing(false);
    }
  };

  // Process Tool Updates
  useEffect(() => {
    if (!sourceImage && tool.id !== 'qr-generator') return;

    let isMounted = true;
    const runProcessing = async () => {
      setIsProcessing(true);
      try {
        if (tool.id === 'qr-generator') {
          const qrUrl = generateQrCanvas(qrContent, 600, qrFg, qrBg);
          if (isMounted) {
            setProcessedUrl(qrUrl);
            setProcessedSize(null);
          }
          return;
        }

        if (!sourceImage) return;

        let resultBlob: Blob | null = null;

        switch (tool.id) {
          case 'compressor': {
            const res = await compressImage(sourceImage, compFormat, compQuality, compScale);
            if (isMounted) {
              setProcessedUrl(res.url);
              setProcessedSize(res.size);
            }
            return;
          }

          case 'resizer': {
            resultBlob = await resizeImage(sourceImage, resizeWidth, resizeHeight, 'image/png');
            break;
          }

          case 'cropper': {
            let cW = sourceImage.naturalWidth;
            let cH = sourceImage.naturalHeight;
            let isCircle = cropRatio === 'circle';

            if (cropRatio === '1:1' || cropRatio === 'circle') {
              const minDim = Math.min(cW, cH);
              cW = minDim;
              cH = minDim;
            } else if (cropRatio === '16:9') {
              cH = Math.min(cH, (cW * 9) / 16);
              cW = (cH * 16) / 9;
            } else if (cropRatio === '4:3') {
              cH = Math.min(cH, (cW * 3) / 4);
              cW = (cH * 4) / 3;
            } else if (cropRatio === '9:16') {
              cW = Math.min(cW, (cH * 9) / 16);
              cH = (cW * 16) / 9;
            }
            const cX = (sourceImage.naturalWidth - cW) / 2;
            const cY = (sourceImage.naturalHeight - cH) / 2;
            resultBlob = await cropImage(sourceImage, cX, cY, cW, cH, isCircle);
            break;
          }

          case 'rotator-flipper': {
            resultBlob = await transformImage(sourceImage, rotation, flipH, flipV);
            break;
          }

          case 'converter': {
            resultBlob = await compressImage(sourceImage, targetFormat, 0.95).then((r) => r.blob);
            break;
          }

          case 'adjustments': {
            resultBlob = await adjustColors(sourceImage, brightness, contrast, saturation, hue);
            break;
          }

          case 'color-filters': {
            let b = 100, c = 100, s = 100, h = 0, g = 0, sep = 0, inv = 0;
            if (filterPreset === 'grayscale') g = 100;
            if (filterPreset === 'sepia') sep = 100;
            if (filterPreset === 'invert') inv = 100;
            if (filterPreset === 'vintage') { sep = 50; c = 120; b = 105; s = 80; }
            if (filterPreset === 'cyberpunk') { h = 280; s = 180; c = 130; }
            if (filterPreset === 'cool') { h = 190; s = 120; b = 105; }
            if (filterPreset === 'dramatic') { c = 160; s = 110; b = 95; }
            resultBlob = await adjustColors(sourceImage, b, c, s, h, g, sep, inv);
            break;
          }

          case 'blur': {
            resultBlob = await adjustColors(sourceImage, 100, 100, 100, 0, 0, 0, 0, blurRadius);
            break;
          }

          case 'sharpen': {
            resultBlob = await sharpenImage(sourceImage, sharpenStrength);
            break;
          }

          case 'background-remover': {
            resultBlob = await removeBackgroundByColor(sourceImage, bgKeyColor, bgTolerance, bgFeather);
            break;
          }

          case 'watermark': {
            resultBlob = await applyWatermark(sourceImage, wmText, wmSize, wmColor, wmOpacity, wmPos);
            break;
          }

          case 'meme-generator': {
            resultBlob = await generateMeme(sourceImage, memeTop, memeBottom, memeFontSize);
            break;
          }

          case 'splitter': {
            const split = await splitImageToTiles(sourceImage, splitRows, splitCols);
            if (isMounted) {
              setSplitZip(split.zipBlob);
              setProcessedUrl(split.tileUrls[0] || null);
            }
            return;
          }

          case 'color-palette': {
            setPalette(extractColorPalette(sourceImage, 8));
            break;
          }

          case 'exif-metadata': {
            // Already parsed
            break;
          }

          default:
            break;
        }

        if (resultBlob && isMounted) {
          const url = URL.createObjectURL(resultBlob);
          setProcessedUrl(url);
          setProcessedSize(resultBlob.size);
        }
      } catch (err) {
        console.error('Processing error:', err);
      } finally {
        if (isMounted) setIsProcessing(false);
      }
    };

    const timer = setTimeout(runProcessing, 120);
    return () => {
      isMounted = false;
      clearTimeout(timer);
    };
  }, [
    sourceImage,
    tool.id,
    compFormat,
    compQuality,
    compScale,
    resizeWidth,
    resizeHeight,
    cropRatio,
    rotation,
    flipH,
    flipV,
    targetFormat,
    brightness,
    contrast,
    saturation,
    hue,
    filterPreset,
    blurRadius,
    sharpenStrength,
    bgKeyColor,
    bgTolerance,
    bgFeather,
    wmText,
    wmSize,
    wmColor,
    wmOpacity,
    wmPos,
    memeTop,
    memeBottom,
    memeFontSize,
    splitRows,
    splitCols,
    qrContent,
    qrFg,
    qrBg,
  ]);

  // Handle Download
  const handleDownload = () => {
    if (tool.id === 'splitter' && splitZip) {
      triggerDownload(splitZip, `${fileName.replace(/\.[^/.]+$/, '')}_tiles.zip`);
      return;
    }
    if (!processedUrl) return;

    let ext = 'png';
    if (tool.id === 'compressor') {
      ext = compFormat === 'image/jpeg' ? 'jpg' : compFormat === 'image/webp' ? 'webp' : 'png';
    } else if (tool.id === 'converter') {
      ext = targetFormat === 'image/jpeg' ? 'jpg' : targetFormat === 'image/webp' ? 'webp' : 'png';
    }

    const cleanName = fileName.replace(/\.[^/.]+$/, '');
    triggerDownload(processedUrl, `${cleanName}_${tool.id}.${ext}`);
  };

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
      {/* Top Action Bar */}
      <div className="flex flex-wrap items-center justify-between gap-4 mb-6 pb-4 border-b border-neutral-800">
        <div className="flex items-center gap-3">
          <button
            id="tool-back-btn"
            onClick={onBack}
            className="flex items-center gap-1.5 text-xs font-semibold text-neutral-300 hover:text-white bg-neutral-800 hover:bg-neutral-700 px-3 py-2 rounded-lg transition-colors cursor-pointer"
          >
            <ArrowLeft className="w-4 h-4" /> Back to Tools
          </button>
          <div className="h-4 w-px bg-neutral-800" />
          <h2 className="text-lg font-bold text-white tracking-tight flex items-center gap-2">
            {tool.name}
            {isProcessing && (
              <span className="text-xs font-normal text-indigo-400 flex items-center gap-1">
                <RefreshCw className="w-3 h-3 animate-spin" /> Processing...
              </span>
            )}
          </h2>
        </div>

        {/* Global Upload & Sample Buttons */}
        <div className="flex items-center gap-2">
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            className="hidden"
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file) handleFileUpload(file);
            }}
          />
          <button
            id="workspace-upload-btn"
            onClick={() => fileInputRef.current?.click()}
            className="flex items-center gap-1.5 text-xs font-medium text-neutral-300 hover:text-white bg-neutral-800 hover:bg-neutral-700 px-3 py-2 rounded-lg transition-colors cursor-pointer"
          >
            <Upload className="w-3.5 h-3.5 text-indigo-400" /> Open Photo
          </button>
          <button
            id="workspace-sample-btn"
            onClick={() => {
              loadImageFromUrl(SAMPLE_IMAGE_URL).then((img) => {
                setSourceImage(img);
                setFileName('studio-sample.png');
                setOriginalSize(142000);
                setResizeWidth(img.naturalWidth);
                setResizeHeight(img.naturalHeight);
              });
            }}
            className="flex items-center gap-1.5 text-xs font-medium text-indigo-300 hover:text-indigo-200 bg-indigo-950/40 border border-indigo-800/40 hover:bg-indigo-900/40 px-3 py-2 rounded-lg transition-colors cursor-pointer"
          >
            <Sparkles className="w-3.5 h-3.5 text-indigo-400" /> Reset Sample
          </button>
          <button
            id="workspace-download-btn"
            onClick={handleDownload}
            disabled={!processedUrl && !splitZip}
            className="flex items-center gap-1.5 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 px-4 py-2 rounded-lg shadow-sm shadow-indigo-600/30 transition-all cursor-pointer"
          >
            <Download className="w-4 h-4" /> Download Result
          </button>
        </div>
      </div>

      {/* Main Studio Layout: 2 Columns */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left / Center: Interactive Preview Canvas */}
        <div className="lg:col-span-8 flex flex-col gap-4">
          <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-4 flex flex-col items-center justify-center min-h-[460px] relative overflow-hidden shadow-inner">
            {/* View Mode Switcher */}
            {sourceImage && (
              <div className="absolute top-4 left-4 z-10 flex items-center bg-neutral-950/80 backdrop-blur-md rounded-lg p-1 border border-neutral-800 text-xs">
                <button
                  onClick={() => setViewMode('processed')}
                  className={`px-2.5 py-1 rounded cursor-pointer ${
                    viewMode === 'processed' ? 'bg-indigo-600 text-white font-medium' : 'text-neutral-400 hover:text-white'
                  }`}
                >
                  Processed
                </button>
                <button
                  onClick={() => setViewMode('original')}
                  className={`px-2.5 py-1 rounded cursor-pointer ${
                    viewMode === 'original' ? 'bg-indigo-600 text-white font-medium' : 'text-neutral-400 hover:text-white'
                  }`}
                >
                  Original
                </button>
              </div>
            )}

            {/* Canvas / Image Display */}
            <div className="max-w-full max-h-[540px] flex items-center justify-center canvas-checkerboard rounded-xl p-2 border border-neutral-800/80">
              {viewMode === 'original' && sourceImage ? (
                <img
                  src={sourceImage.src}
                  alt="Original"
                  className="max-h-[500px] max-w-full object-contain rounded-lg shadow-2xl"
                />
              ) : processedUrl ? (
                <img
                  src={processedUrl}
                  alt="Processed Output"
                  className="max-h-[500px] max-w-full object-contain rounded-lg shadow-2xl transition-all"
                />
              ) : (
                <div className="text-center p-8 text-neutral-400">
                  <p className="text-sm">Rendering preview...</p>
                </div>
              )}
            </div>

            {/* Bottom File Specs Bar */}
            <div className="w-full mt-4 pt-3 border-t border-neutral-800/80 flex flex-wrap items-center justify-between text-xs text-neutral-400 gap-2">
              <div className="flex items-center gap-3">
                <span>{fileName}</span>
                {sourceImage && (
                  <span className="text-neutral-500">
                    {sourceImage.naturalWidth} × {sourceImage.naturalHeight} px
                  </span>
                )}
              </div>
              <div className="flex items-center gap-4">
                <span>Original: {(originalSize / 1024).toFixed(1)} KB</span>
                {processedSize && (
                  <span className="text-emerald-400 font-semibold">
                    New: {(processedSize / 1024).toFixed(1)} KB (
                    {originalSize > processedSize
                      ? `-${Math.round((1 - processedSize / originalSize) * 100)}% saved`
                      : `+${Math.round((processedSize / originalSize - 1) * 100)}%`}
                    )
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Right: Tool-Specific Control Panel */}
        <div className="lg:col-span-4 flex flex-col gap-4">
          <div className="bg-neutral-800/50 border border-neutral-800 rounded-2xl p-5 shadow-lg flex flex-col gap-5">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-sm text-neutral-200 uppercase tracking-wider">
                {tool.name} Settings
              </h3>
              <button
                onClick={() => {
                  // Reset tool settings to defaults
                  setCompQuality(0.8);
                  setCompScale(1.0);
                  setRotation(0);
                  setFlipH(false);
                  setFlipV(false);
                  setBrightness(100);
                  setContrast(100);
                  setSaturation(100);
                  setHue(0);
                  setFilterPreset('normal');
                  setBlurRadius(8);
                  setSharpenStrength(1.2);
                  setBgTolerance(35);
                }}
                className="text-xs text-neutral-400 hover:text-indigo-300 flex items-center gap-1 cursor-pointer"
              >
                <RotateCcw className="w-3 h-3" /> Reset
              </button>
            </div>

            {/* --- 1. COMPRESSOR CONTROLS --- */}
            {tool.id === 'compressor' && (
              <div className="space-y-4 text-xs">
                <div>
                  <label className="block text-neutral-300 font-medium mb-1.5">Output Format</label>
                  <div className="grid grid-cols-3 gap-2">
                    {(['image/jpeg', 'image/webp', 'image/png'] as const).map((fmt) => (
                      <button
                        key={fmt}
                        onClick={() => setCompFormat(fmt)}
                        className={`py-2 px-3 rounded-lg border text-center font-medium cursor-pointer ${
                          compFormat === fmt
                            ? 'bg-indigo-600 border-indigo-500 text-white'
                            : 'bg-neutral-800 border-neutral-700 text-neutral-300 hover:bg-neutral-750'
                        }`}
                      >
                        {fmt.replace('image/', '').toUpperCase()}
                      </button>
                    ))}
                  </div>
                </div>

                <div>
                  <div className="flex justify-between text-neutral-300 font-medium mb-1">
                    <span>Quality</span>
                    <span className="text-indigo-400 font-bold">{Math.round(compQuality * 100)}%</span>
                  </div>
                  <input
                    type="range"
                    min="0.05"
                    max="1.0"
                    step="0.05"
                    value={compQuality}
                    onChange={(e) => setCompQuality(parseFloat(e.target.value))}
                    className="w-full cursor-pointer"
                  />
                  <div className="flex justify-between text-[10px] text-neutral-500 mt-1">
                    <span>Smallest File</span>
                    <span>Best Quality</span>
                  </div>
                </div>

                <div>
                  <div className="flex justify-between text-neutral-300 font-medium mb-1">
                    <span>Dimension Scale</span>
                    <span className="text-indigo-400 font-bold">{Math.round(compScale * 100)}%</span>
                  </div>
                  <input
                    type="range"
                    min="0.1"
                    max="1.0"
                    step="0.05"
                    value={compScale}
                    onChange={(e) => setCompScale(parseFloat(e.target.value))}
                    className="w-full cursor-pointer"
                  />
                </div>
              </div>
            )}

            {/* --- 2. RESIZER CONTROLS --- */}
            {tool.id === 'resizer' && (
              <div className="space-y-4 text-xs">
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-neutral-400 mb-1">Width (px)</label>
                    <input
                      type="number"
                      value={resizeWidth}
                      onChange={(e) => {
                        const val = Math.max(10, parseInt(e.target.value) || 10);
                        setResizeWidth(val);
                        if (lockAspect && sourceImage) {
                          setResizeHeight(Math.round((val * sourceImage.naturalHeight) / sourceImage.naturalWidth));
                        }
                      }}
                      className="w-full bg-neutral-900 border border-neutral-700 rounded-lg px-3 py-2 text-white"
                    />
                  </div>
                  <div>
                    <label className="block text-neutral-400 mb-1">Height (px)</label>
                    <input
                      type="number"
                      value={resizeHeight}
                      onChange={(e) => {
                        const val = Math.max(10, parseInt(e.target.value) || 10);
                        setResizeHeight(val);
                        if (lockAspect && sourceImage) {
                          setResizeWidth(Math.round((val * sourceImage.naturalWidth) / sourceImage.naturalHeight));
                        }
                      }}
                      className="w-full bg-neutral-900 border border-neutral-700 rounded-lg px-3 py-2 text-white"
                    />
                  </div>
                </div>

                <label className="flex items-center gap-2 cursor-pointer select-none text-neutral-300">
                  <input
                    type="checkbox"
                    checked={lockAspect}
                    onChange={(e) => setLockAspect(e.target.checked)}
                    className="rounded border-neutral-700 text-indigo-600 focus:ring-indigo-500"
                  />
                  <span>Lock Aspect Ratio</span>
                </label>

                <div>
                  <label className="block text-neutral-400 mb-1.5">Social Media Presets</label>
                  <div className="grid grid-cols-2 gap-2">
                    {[
                      { label: 'Instagram Square', w: 1080, h: 1080 },
                      { label: 'Instagram Story', w: 1080, h: 1920 },
                      { label: 'Full HD 1080p', w: 1920, h: 1080 },
                      { label: 'Avatar / PFP', w: 512, h: 512 },
                    ].map((pre) => (
                      <button
                        key={pre.label}
                        onClick={() => {
                          setResizeWidth(pre.w);
                          setResizeHeight(pre.h);
                          setLockAspect(false);
                        }}
                        className="py-1.5 px-2 bg-neutral-800 hover:bg-neutral-750 border border-neutral-700 rounded text-left text-[11px] text-neutral-300 hover:text-white cursor-pointer"
                      >
                        <div className="font-medium">{pre.label}</div>
                        <div className="text-[10px] text-neutral-500">{pre.w}×{pre.h}</div>
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {/* --- 3. CROPPER CONTROLS --- */}
            {tool.id === 'cropper' && (
              <div className="space-y-4 text-xs">
                <label className="block text-neutral-300 font-medium">Aspect Ratio Lock</label>
                <div className="grid grid-cols-3 gap-2">
                  {[
                    { id: '1:1', label: '1:1 Square' },
                    { id: '16:9', label: '16:9 Wide' },
                    { id: '4:3', label: '4:3 Standard' },
                    { id: '9:16', label: '9:16 Story' },
                    { id: 'circle', label: 'Circle PFP' },
                    { id: 'free', label: 'Original Full' },
                  ].map((ratio) => (
                    <button
                      key={ratio.id}
                      onClick={() => setCropRatio(ratio.id as any)}
                      className={`py-2 px-2.5 rounded-lg border text-center font-medium cursor-pointer ${
                        cropRatio === ratio.id
                          ? 'bg-indigo-600 border-indigo-500 text-white'
                          : 'bg-neutral-800 border-neutral-700 text-neutral-300 hover:bg-neutral-750'
                      }`}
                    >
                      {ratio.label}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* --- 4. ROTATOR & FLIPPER --- */}
            {tool.id === 'rotator-flipper' && (
              <div className="space-y-4 text-xs">
                <div>
                  <label className="block text-neutral-300 font-medium mb-2">Quick 90° Rotations</label>
                  <div className="grid grid-cols-3 gap-2">
                    <button
                      onClick={() => setRotation((r) => (r - 90 + 360) % 360)}
                      className="py-2 bg-neutral-800 hover:bg-neutral-700 border border-neutral-700 rounded-lg text-white font-medium cursor-pointer"
                    >
                      -90° Left
                    </button>
                    <button
                      onClick={() => setRotation((r) => (r + 90) % 360)}
                      className="py-2 bg-neutral-800 hover:bg-neutral-700 border border-neutral-700 rounded-lg text-white font-medium cursor-pointer"
                    >
                      +90° Right
                    </button>
                    <button
                      onClick={() => setRotation((r) => (r + 180) % 360)}
                      className="py-2 bg-neutral-800 hover:bg-neutral-700 border border-neutral-700 rounded-lg text-white font-medium cursor-pointer"
                    >
                      180° Flip
                    </button>
                  </div>
                </div>

                <div>
                  <label className="block text-neutral-300 font-medium mb-2">Mirror & Flip</label>
                  <div className="grid grid-cols-2 gap-2">
                    <button
                      onClick={() => setFlipH((h) => !h)}
                      className={`py-2 rounded-lg border font-medium cursor-pointer ${
                        flipH ? 'bg-indigo-600 border-indigo-500 text-white' : 'bg-neutral-800 border-neutral-700 text-neutral-300'
                      }`}
                    >
                      Flip Horizontal {flipH ? '(Active)' : ''}
                    </button>
                    <button
                      onClick={() => setFlipV((v) => !v)}
                      className={`py-2 rounded-lg border font-medium cursor-pointer ${
                        flipV ? 'bg-indigo-600 border-indigo-500 text-white' : 'bg-neutral-800 border-neutral-700 text-neutral-300'
                      }`}
                    >
                      Flip Vertical {flipV ? '(Active)' : ''}
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* --- 5. FORMAT CONVERTER --- */}
            {tool.id === 'converter' && (
              <div className="space-y-4 text-xs">
                <label className="block text-neutral-300 font-medium">Target Export Format</label>
                <div className="grid grid-cols-3 gap-2">
                  {(['image/png', 'image/jpeg', 'image/webp'] as const).map((fmt) => (
                    <button
                      key={fmt}
                      onClick={() => setTargetFormat(fmt)}
                      className={`py-2.5 rounded-lg border text-center font-semibold cursor-pointer ${
                        targetFormat === fmt
                          ? 'bg-indigo-600 border-indigo-500 text-white'
                          : 'bg-neutral-800 border-neutral-700 text-neutral-300 hover:bg-neutral-750'
                      }`}
                    >
                      {fmt.replace('image/', '').toUpperCase()}
                    </button>
                  ))}
                </div>
                <p className="text-[11px] text-neutral-400 leading-relaxed">
                  Converts images losslessly in memory with full alpha transparency support for PNG & WebP.
                </p>
              </div>
            )}

            {/* --- 6. ADJUSTMENTS CONTROLS --- */}
            {tool.id === 'adjustments' && (
              <div className="space-y-3 text-xs">
                <div>
                  <div className="flex justify-between mb-1">
                    <span className="text-neutral-300">Brightness</span>
                    <span className="text-indigo-400 font-semibold">{brightness}%</span>
                  </div>
                  <input
                    type="range"
                    min="20"
                    max="200"
                    value={brightness}
                    onChange={(e) => setBrightness(parseInt(e.target.value))}
                    className="w-full cursor-pointer"
                  />
                </div>
                <div>
                  <div className="flex justify-between mb-1">
                    <span className="text-neutral-300">Contrast</span>
                    <span className="text-indigo-400 font-semibold">{contrast}%</span>
                  </div>
                  <input
                    type="range"
                    min="20"
                    max="200"
                    value={contrast}
                    onChange={(e) => setContrast(parseInt(e.target.value))}
                    className="w-full cursor-pointer"
                  />
                </div>
                <div>
                  <div className="flex justify-between mb-1">
                    <span className="text-neutral-300">Saturation</span>
                    <span className="text-indigo-400 font-semibold">{saturation}%</span>
                  </div>
                  <input
                    type="range"
                    min="0"
                    max="250"
                    value={saturation}
                    onChange={(e) => setSaturation(parseInt(e.target.value))}
                    className="w-full cursor-pointer"
                  />
                </div>
                <div>
                  <div className="flex justify-between mb-1">
                    <span className="text-neutral-300">Hue Rotation</span>
                    <span className="text-indigo-400 font-semibold">{hue}°</span>
                  </div>
                  <input
                    type="range"
                    min="0"
                    max="360"
                    value={hue}
                    onChange={(e) => setHue(parseInt(e.target.value))}
                    className="w-full cursor-pointer"
                  />
                </div>
              </div>
            )}

            {/* --- 7. COLOR FILTERS --- */}
            {tool.id === 'color-filters' && (
              <div className="space-y-3 text-xs">
                <label className="block text-neutral-300 font-medium">Filter Presets</label>
                <div className="grid grid-cols-2 gap-2">
                  {[
                    { id: 'normal', label: 'Normal / Clean' },
                    { id: 'grayscale', label: 'B&W Grayscale' },
                    { id: 'sepia', label: 'Warm Sepia' },
                    { id: 'vintage', label: 'Retro Vintage' },
                    { id: 'cyberpunk', label: 'Cyber Neon' },
                    { id: 'cool', label: 'Cool Blue' },
                    { id: 'dramatic', label: 'High Contrast' },
                    { id: 'invert', label: 'Color Invert' },
                  ].map((p) => (
                    <button
                      key={p.id}
                      onClick={() => setFilterPreset(p.id)}
                      className={`py-2 px-3 rounded-lg border text-left font-medium cursor-pointer ${
                        filterPreset === p.id
                          ? 'bg-indigo-600 border-indigo-500 text-white'
                          : 'bg-neutral-800 border-neutral-700 text-neutral-300 hover:bg-neutral-750'
                      }`}
                    >
                      {p.label}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* --- 8. BLUR & SHARPEN --- */}
            {tool.id === 'blur' && (
              <div className="space-y-4 text-xs">
                <div>
                  <div className="flex justify-between mb-1">
                    <span className="text-neutral-300 font-medium">Blur Radius</span>
                    <span className="text-indigo-400 font-bold">{blurRadius} px</span>
                  </div>
                  <input
                    type="range"
                    min="1"
                    max="40"
                    value={blurRadius}
                    onChange={(e) => setBlurRadius(parseInt(e.target.value))}
                    className="w-full cursor-pointer"
                  />
                </div>
              </div>
            )}

            {tool.id === 'sharpen' && (
              <div className="space-y-4 text-xs">
                <div>
                  <div className="flex justify-between mb-1">
                    <span className="text-neutral-300 font-medium">Sharpen Intensity</span>
                    <span className="text-indigo-400 font-bold">{sharpenStrength.toFixed(1)}x</span>
                  </div>
                  <input
                    type="range"
                    min="0.2"
                    max="3.0"
                    step="0.1"
                    value={sharpenStrength}
                    onChange={(e) => setSharpenStrength(parseFloat(e.target.value))}
                    className="w-full cursor-pointer"
                  />
                </div>
              </div>
            )}

            {/* --- 9. BACKGROUND REMOVER --- */}
            {tool.id === 'background-remover' && (
              <div className="space-y-4 text-xs">
                <div>
                  <label className="block text-neutral-300 font-medium mb-1">Sample Color to Erase</label>
                  <div className="flex items-center gap-3">
                    <input
                      type="color"
                      value={bgKeyColor}
                      onChange={(e) => setBgKeyColor(e.target.value)}
                      className="w-10 h-10 rounded border border-neutral-700 cursor-pointer bg-transparent"
                    />
                    <span className="font-mono text-neutral-300">{bgKeyColor.toUpperCase()}</span>
                  </div>
                </div>
                <div>
                  <div className="flex justify-between mb-1">
                    <span className="text-neutral-300">Tolerance</span>
                    <span className="text-indigo-400 font-bold">{bgTolerance}</span>
                  </div>
                  <input
                    type="range"
                    min="5"
                    max="100"
                    value={bgTolerance}
                    onChange={(e) => setBgTolerance(parseInt(e.target.value))}
                    className="w-full cursor-pointer"
                  />
                </div>
                <div>
                  <div className="flex justify-between mb-1">
                    <span className="text-neutral-300">Edge Feathering</span>
                    <span className="text-indigo-400 font-bold">{bgFeather} px</span>
                  </div>
                  <input
                    type="range"
                    min="0"
                    max="5"
                    value={bgFeather}
                    onChange={(e) => setBgFeather(parseInt(e.target.value))}
                    className="w-full cursor-pointer"
                  />
                </div>
              </div>
            )}

            {/* --- 10. WATERMARK --- */}
            {tool.id === 'watermark' && (
              <div className="space-y-3 text-xs">
                <div>
                  <label className="block text-neutral-300 font-medium mb-1">Watermark Text</label>
                  <input
                    type="text"
                    value={wmText}
                    onChange={(e) => setWmText(e.target.value)}
                    className="w-full bg-neutral-900 border border-neutral-700 rounded-lg px-3 py-2 text-white"
                  />
                </div>
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="block text-neutral-400 mb-1">Font Size</label>
                    <input
                      type="number"
                      value={wmSize}
                      onChange={(e) => setWmSize(parseInt(e.target.value) || 24)}
                      className="w-full bg-neutral-900 border border-neutral-700 rounded-lg px-3 py-1.5 text-white"
                    />
                  </div>
                  <div>
                    <label className="block text-neutral-400 mb-1">Color</label>
                    <input
                      type="color"
                      value={wmColor}
                      onChange={(e) => setWmColor(e.target.value)}
                      className="w-full h-9 rounded border border-neutral-700 cursor-pointer bg-transparent"
                    />
                  </div>
                </div>
                <div>
                  <div className="flex justify-between mb-1">
                    <span className="text-neutral-300">Opacity</span>
                    <span className="text-indigo-400">{Math.round(wmOpacity * 100)}%</span>
                  </div>
                  <input
                    type="range"
                    min="0.1"
                    max="1.0"
                    step="0.05"
                    value={wmOpacity}
                    onChange={(e) => setWmOpacity(parseFloat(e.target.value))}
                    className="w-full cursor-pointer"
                  />
                </div>
                <div>
                  <label className="block text-neutral-400 mb-1">Position</label>
                  <div className="grid grid-cols-2 gap-2">
                    {(['bottom-right', 'center', 'top-left', 'tiled'] as const).map((pos) => (
                      <button
                        key={pos}
                        onClick={() => setWmPos(pos)}
                        className={`py-1.5 px-2 rounded border capitalize cursor-pointer ${
                          wmPos === pos ? 'bg-indigo-600 border-indigo-500 text-white' : 'bg-neutral-800 border-neutral-700 text-neutral-300'
                        }`}
                      >
                        {pos.replace('-', ' ')}
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {/* --- 11. MEME GENERATOR --- */}
            {tool.id === 'meme-generator' && (
              <div className="space-y-3 text-xs">
                <div>
                  <label className="block text-neutral-300 font-medium mb-1">Top Text</label>
                  <input
                    type="text"
                    value={memeTop}
                    onChange={(e) => setMemeTop(e.target.value)}
                    className="w-full bg-neutral-900 border border-neutral-700 rounded-lg px-3 py-2 text-white"
                  />
                </div>
                <div>
                  <label className="block text-neutral-300 font-medium mb-1">Bottom Text</label>
                  <input
                    type="text"
                    value={memeBottom}
                    onChange={(e) => setMemeBottom(e.target.value)}
                    className="w-full bg-neutral-900 border border-neutral-700 rounded-lg px-3 py-2 text-white"
                  />
                </div>
                <div>
                  <div className="flex justify-between mb-1">
                    <span className="text-neutral-300">Font Size</span>
                    <span className="text-indigo-400 font-bold">{memeFontSize} px</span>
                  </div>
                  <input
                    type="range"
                    min="24"
                    max="96"
                    value={memeFontSize}
                    onChange={(e) => setMemeFontSize(parseInt(e.target.value))}
                    className="w-full cursor-pointer"
                  />
                </div>
              </div>
            )}

            {/* --- 12. SPLITTER --- */}
            {tool.id === 'splitter' && (
              <div className="space-y-3 text-xs">
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-neutral-300 font-medium mb-1">Rows</label>
                    <input
                      type="number"
                      min="1"
                      max="6"
                      value={splitRows}
                      onChange={(e) => setSplitRows(parseInt(e.target.value) || 2)}
                      className="w-full bg-neutral-900 border border-neutral-700 rounded-lg px-3 py-2 text-white"
                    />
                  </div>
                  <div>
                    <label className="block text-neutral-300 font-medium mb-1">Columns</label>
                    <input
                      type="number"
                      min="1"
                      max="6"
                      value={splitCols}
                      onChange={(e) => setSplitCols(parseInt(e.target.value) || 2)}
                      className="w-full bg-neutral-900 border border-neutral-700 rounded-lg px-3 py-2 text-white"
                    />
                  </div>
                </div>
                <p className="text-[11px] text-neutral-400">
                  Splits into {splitRows * splitCols} high-resolution image slices. Click "Download Result" to get the complete ZIP package.
                </p>
              </div>
            )}

            {/* --- 13. COLOR PALETTE --- */}
            {tool.id === 'color-palette' && (
              <div className="space-y-3 text-xs">
                <label className="block text-neutral-300 font-medium">Dominant Color Swatches</label>
                <div className="grid grid-cols-2 gap-2">
                  {palette.map((swatch, idx) => (
                    <div
                      key={idx}
                      onClick={() => copyToClipboard(swatch.hex)}
                      className="flex items-center gap-2 p-2 bg-neutral-900 rounded-lg border border-neutral-800 hover:border-indigo-500 cursor-pointer transition-colors"
                    >
                      <div className="w-7 h-7 rounded border border-neutral-700" style={{ backgroundColor: swatch.hex }} />
                      <div className="overflow-hidden">
                        <div className="font-mono text-white font-medium">{swatch.hex.toUpperCase()}</div>
                        <div className="text-[10px] text-neutral-500 truncate">{swatch.rgb}</div>
                      </div>
                    </div>
                  ))}
                </div>
                {copied && <div className="text-emerald-400 text-xs text-center">Copied HEX code to clipboard!</div>}
              </div>
            )}

            {/* --- 14. EXIF VIEWER & REMOVER --- */}
            {tool.id === 'exif-metadata' && (
              <div className="space-y-3 text-xs">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-neutral-300 font-medium">Metadata Tags</span>
                  <button
                    onClick={async () => {
                      if (!sourceImage) return;
                      const sanitized = await sanitizeExif(sourceImage);
                      triggerDownload(sanitized, `${fileName.replace(/\.[^/.]+$/, '')}_sanitized.png`);
                    }}
                    className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-500 text-white rounded font-medium cursor-pointer"
                  >
                    Sanitize & Strip
                  </button>
                </div>
                <div className="max-h-56 overflow-y-auto space-y-2 pr-1 scrollbar-none">
                  {exifTags.map((tag, idx) => (
                    <div key={idx} className="p-2 bg-neutral-900 rounded border border-neutral-800">
                      <div className="text-neutral-500 text-[10px] font-medium">{tag.name}</div>
                      <div className="text-neutral-200 font-mono text-xs">{tag.value}</div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* --- 15. BASE64 TOOL --- */}
            {tool.id === 'base64' && (
              <div className="space-y-3 text-xs">
                <label className="block text-neutral-300 font-medium">Base64 Data URI</label>
                <textarea
                  readOnly
                  rows={6}
                  value={base64Text}
                  className="w-full font-mono text-[11px] bg-neutral-900 border border-neutral-700 rounded-lg p-2.5 text-neutral-300 select-all"
                />
                <div className="flex gap-2">
                  <button
                    onClick={() => copyToClipboard(base64Text)}
                    className="flex-1 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg font-medium flex items-center justify-center gap-1.5 cursor-pointer"
                  >
                    <Copy className="w-3.5 h-3.5" /> Copy Data URI
                  </button>
                  <button
                    onClick={() => copyToClipboard(`<img src="${base64Text.slice(0, 100)}..." alt="Embedded Image" />`)}
                    className="flex-1 py-2 bg-neutral-800 hover:bg-neutral-750 text-neutral-200 border border-neutral-700 rounded-lg font-medium cursor-pointer"
                  >
                    Copy HTML Tag
                  </button>
                </div>
                {copied && <div className="text-emerald-400 text-center">Copied to clipboard!</div>}
              </div>
            )}

            {/* --- 16. QR CODE GENERATOR --- */}
            {tool.id === 'qr-generator' && (
              <div className="space-y-3 text-xs">
                <div>
                  <label className="block text-neutral-300 font-medium mb-1">QR Code Data / URL</label>
                  <input
                    type="text"
                    value={qrContent}
                    onChange={(e) => setQrContent(e.target.value)}
                    placeholder="https://example.com"
                    className="w-full bg-neutral-900 border border-neutral-700 rounded-lg px-3 py-2 text-white"
                  />
                </div>
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="block text-neutral-400 mb-1">Foreground</label>
                    <input
                      type="color"
                      value={qrFg}
                      onChange={(e) => setQrFg(e.target.value)}
                      className="w-full h-9 rounded border border-neutral-700 cursor-pointer bg-transparent"
                    />
                  </div>
                  <div>
                    <label className="block text-neutral-400 mb-1">Background</label>
                    <input
                      type="color"
                      value={qrBg}
                      onChange={(e) => setQrBg(e.target.value)}
                      className="w-full h-9 rounded border border-neutral-700 cursor-pointer bg-transparent"
                    />
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
