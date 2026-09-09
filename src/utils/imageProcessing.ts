import removeBackground from 'https://esm.sh/@imgly/background-removal@1.5.7';

/**
 * Automatically remove background using browser-based AI model (One-Click Professional Result)
 */
export async function removeBackgroundByColor(
  img: HTMLImageElement,
  _targetHex: string = '#ffffff',
  _tolerance: number = 40,
  _feather: number = 2
): Promise<Blob> {
  try {
    // Pass image source directly to the AI background removal engine
    const imageSource = img.src;
    
    const blobResult = await removeBackground(imageSource, {
      progress: (key, current, total) => {
        // Optional: track model download progress on first run (~40MB-80MB cached locally)
        console.log(`AI Model Loading ${key}: ${Math.round((current / total) * 100)}%`);
      },
    });

    return blobResult;
  } catch (error) {
    throw new Error('AI Background removal failed: ' + error);
  }
}
