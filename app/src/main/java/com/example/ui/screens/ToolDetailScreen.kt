package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ToolId
import com.example.model.ToolItem
import com.example.util.ImageOutputFormat
import com.example.util.ImageProcessingUtils
import com.example.util.PhotoFilterType
import com.example.util.WatermarkPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ToolDetailScreen(
    tool: ToolItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Original & Processed Bitmaps
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Multi-image list for PDF / Collage
    var multiImages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    // Tab state for preview (0: Processed, 1: Original)
    var selectedPreviewTab by remember { mutableIntStateOf(0) }

    // Tool specific state variables
    var compressQuality by remember { mutableIntStateOf(80) }
    var selectedOutputFormat by remember { mutableStateOf(ImageOutputFormat.JPEG) }

    var resizeWidth by remember { mutableStateOf("1080") }
    var resizeHeight by remember { mutableStateOf("1080") }
    var isAspectLocked by remember { mutableStateOf(true) }
    var resizeScale by remember { mutableFloatStateOf(100f) }

    var cropRatioPreset by remember { mutableStateOf("1:1") }
    var cropLeft by remember { mutableFloatStateOf(0f) }
    var cropTop by remember { mutableFloatStateOf(0f) }
    var cropWidth by remember { mutableFloatStateOf(1f) }
    var cropHeight by remember { mutableFloatStateOf(1f) }

    var rotateAngle by remember { mutableFloatStateOf(0f) }
    var isFlipH by remember { mutableStateOf(false) }
    var isFlipV by remember { mutableStateOf(false) }

    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var temperature by remember { mutableFloatStateOf(0f) }

    var selectedFilter by remember { mutableStateOf(PhotoFilterType.NONE) }

    var blurRadius by remember { mutableIntStateOf(0) }
    var sharpenIntensity by remember { mutableFloatStateOf(0f) }

    var cornerRadius by remember { mutableFloatStateOf(0f) }
    var borderWidth by remember { mutableFloatStateOf(0f) }
    var borderColorInt by remember { mutableIntStateOf(AndroidColor.WHITE) }
    var isCircleAvatar by remember { mutableStateOf(false) }

    var watermarkText by remember { mutableStateOf("© My Brand") }
    var watermarkPosition by remember { mutableStateOf(WatermarkPosition.BOTTOM_RIGHT) }
    var watermarkOpacity by remember { mutableFloatStateOf(0.8f) }
    var watermarkFontSize by remember { mutableFloatStateOf(0.06f) }
    var watermarkColorInt by remember { mutableIntStateOf(AndroidColor.WHITE) }

    var pixelBlockSize by remember { mutableIntStateOf(16) }

    var bgTolerance by remember { mutableFloatStateOf(0.25f) }
    var bgTargetColorInt by remember { mutableIntStateOf(AndroidColor.WHITE) }

    var qrText by remember { mutableStateOf("https://ai.studio") }
    var qrFgColorInt by remember { mutableIntStateOf(AndroidColor.BLACK) }
    var qrBgColorInt by remember { mutableIntStateOf(AndroidColor.WHITE) }

    var base64Input by remember { mutableStateOf("") }
    var generatedBase64 by remember { mutableStateOf("") }

    var extractedMetadata by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var dominantColors by remember { mutableStateOf<List<Int>>(emptyList()) }

    var socialPresetName by remember { mutableStateOf("Instagram Post (1080x1080)") }
    var collageSpacing by remember { mutableIntStateOf(16) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }

    // Image pickers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                isProcessing = true
                val bmp = ImageProcessingUtils.loadBitmapFromUri(context, uri)
                val exif = ImageProcessingUtils.extractExifMetadata(context, uri)
                withContext(Dispatchers.Main) {
                    if (bmp != null) {
                        originalBitmap = bmp
                        processedBitmap = bmp
                        resizeWidth = bmp.width.toString()
                        resizeHeight = bmp.height.toString()
                        extractedMetadata = exif
                        dominantColors = ImageProcessingUtils.extractDominantColors(bmp)
                        if (multiImages.size < 4) {
                            multiImages = multiImages + bmp
                        }
                    } else {
                        Toast.makeText(context, "Could not load image format", Toast.LENGTH_SHORT).show()
                    }
                    isProcessing = false
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        if (bmp != null) {
            originalBitmap = bmp
            processedBitmap = bmp
            resizeWidth = bmp.width.toString()
            resizeHeight = bmp.height.toString()
            dominantColors = ImageProcessingUtils.extractDominantColors(bmp)
            if (multiImages.size < 4) {
                multiImages = multiImages + bmp
            }
        }
    }

    // Initialize standalone QR tool if chosen without image
    LaunchedEffect(tool.id) {
        if (tool.id == ToolId.QR_GENERATOR) {
            val qr = ImageProcessingUtils.generateQrCodeBitmap(qrText, 600, qrFgColorInt, qrBgColorInt)
            originalBitmap = qr
            processedBitmap = qr
        }
    }

    // Auto-update processing when controls change
    fun updateProcessing() {
        val src = originalBitmap ?: return
        coroutineScope.launch(Dispatchers.Default) {
            isProcessing = true
            val res = when (tool.id) {
                ToolId.COMPRESSOR -> {
                    val bytes = ImageProcessingUtils.compressBitmap(src, selectedOutputFormat, compressQuality)
                    ImageProcessingUtils.decodeByteArrayToBitmap(bytes) ?: src
                }
                ToolId.RESIZER -> {
                    val w = (src.width * (resizeScale / 100f)).toInt().coerceAtLeast(10)
                    val h = (src.height * (resizeScale / 100f)).toInt().coerceAtLeast(10)
                    ImageProcessingUtils.resizeBitmap(src, w, h)
                }
                ToolId.CROPPER -> {
                    ImageProcessingUtils.cropBitmap(src, cropLeft, cropTop, cropWidth, cropHeight)
                }
                ToolId.ROTATE_FLIP -> {
                    ImageProcessingUtils.rotateAndFlip(src, rotateAngle, isFlipH, isFlipV)
                }
                ToolId.FORMAT_CONVERTER -> {
                    val bytes = ImageProcessingUtils.compressBitmap(src, selectedOutputFormat, 95)
                    ImageProcessingUtils.decodeByteArrayToBitmap(bytes) ?: src
                }
                ToolId.COLOR_ADJUSTER -> {
                    ImageProcessingUtils.applyColorAdjustments(src, brightness, contrast, saturation, temperature)
                }
                ToolId.FILTERS -> {
                    ImageProcessingUtils.applyFilter(src, selectedFilter)
                }
                ToolId.BLUR_SHARPEN -> {
                    var out = if (blurRadius > 0) ImageProcessingUtils.applyBlur(src, blurRadius) else src
                    if (sharpenIntensity > 0f) {
                        out = ImageProcessingUtils.applySharpen(out, sharpenIntensity)
                    }
                    out
                }
                ToolId.BORDER_CORNERS -> {
                    ImageProcessingUtils.applyBordersAndCorners(src, cornerRadius, borderWidth, borderColorInt, isCircleAvatar)
                }
                ToolId.WATERMARK_TEXT -> {
                    ImageProcessingUtils.applyWatermark(src, watermarkText, watermarkFontSize, watermarkOpacity, watermarkColorInt, watermarkPosition)
                }
                ToolId.PIXELATE_TOOL -> {
                    ImageProcessingUtils.applyPixelate(src, pixelBlockSize)
                }
                ToolId.BACKGROUND_REMOVER -> {
                    ImageProcessingUtils.removeBackgroundColor(src, bgTargetColorInt, bgTolerance)
                }
                ToolId.QR_GENERATOR -> {
                    ImageProcessingUtils.generateQrCodeBitmap(qrText, 600, qrFgColorInt, qrBgColorInt)
                }
                ToolId.BASE64_TOOL -> {
                    generatedBase64 = ImageProcessingUtils.bitmapToBase64(src, ImageOutputFormat.PNG)
                    src
                }
                ToolId.COLLAGE_MAKER -> {
                    val imgs = if (multiImages.isNotEmpty()) multiImages else listOf(src, src)
                    ImageProcessingUtils.createCollage(imgs, collageSpacing)
                }
                ToolId.SOCIAL_RESIZER -> {
                    val (w, h) = when (socialPresetName) {
                        "Instagram Post (1080x1080)" -> 1080 to 1080
                        "Instagram Story (1080x1920)" -> 1080 to 1920
                        "YouTube Thumbnail (1280x720)" -> 1280 to 720
                        "Twitter Post (1200x675)" -> 1200 to 675
                        "Facebook Cover (820x312)" -> 820 to 312
                        else -> 1080 to 1080
                    }
                    ImageProcessingUtils.resizeBitmap(src, w, h)
                }
                ToolId.PASSPORT_PHOTO -> {
                    val passport = ImageProcessingUtils.resizeBitmap(src, 600, 600)
                    ImageProcessingUtils.applyBordersAndCorners(passport, 0f, 6f, AndroidColor.WHITE, false)
                }
                ToolId.FAVICON_APP_ICON -> {
                    ImageProcessingUtils.resizeBitmap(src, 512, 512)
                }
                else -> src
            }
            withContext(Dispatchers.Main) {
                processedBitmap = res
                isProcessing = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = tool.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = "Home > ${tool.category.title}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (originalBitmap != null) {
                        IconButton(onClick = {
                            originalBitmap = null
                            processedBitmap = null
                            multiImages = emptyList()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Privacy Badge ---
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "100% Client-Side Processing • Your photos never leave this device.",
                            fontSize = 11.sp,
                            color = Color(0xFF047857),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // --- No Image Upload Zone ---
            if (originalBitmap == null && tool.id != ToolId.QR_GENERATOR) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Upload",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Select an Image to ${tool.title.replace("Image", "").trim()}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Choose from your gallery or take a new photo.\nSupports JPG, PNG, WebP, GIF, and BMP.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Choose Photo")
                                }

                                OutlinedButton(
                                    onClick = { cameraLauncher.launch(null) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Camera")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Sample photo chips
                            Text(
                                text = "Or test instantly with samples:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable {
                                        val bmp = ImageProcessingUtils.createSampleBitmap("landscape")
                                        originalBitmap = bmp
                                        processedBitmap = bmp
                                        dominantColors = ImageProcessingUtils.extractDominantColors(bmp)
                                        if (multiImages.isEmpty()) multiImages = listOf(bmp)
                                    }
                                ) {
                                    Text(
                                        text = "🌄 Sunset Mountain",
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable {
                                        val bmp = ImageProcessingUtils.createSampleBitmap("portrait")
                                        originalBitmap = bmp
                                        processedBitmap = bmp
                                        dominantColors = ImageProcessingUtils.extractDominantColors(bmp)
                                        if (multiImages.isEmpty()) multiImages = listOf(bmp)
                                    }
                                ) {
                                    Text(
                                        text = "👤 Portrait Avatar",
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- Image Preview Viewport ---
            if (originalBitmap != null || tool.id == ToolId.QR_GENERATOR) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Viewport header with Dimensions & Format
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val currentBmp = if (selectedPreviewTab == 0) processedBitmap ?: originalBitmap else originalBitmap
                                val w = currentBmp?.width ?: 0
                                val h = currentBmp?.height ?: 0

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "$w × $h px",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedOutputFormat.displayName,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isProcessing) {
                                    Text(
                                        text = "Processing...",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Tab selector: Result vs Original
                            TabRow(
                                selectedTabIndex = selectedPreviewTab,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                            ) {
                                Tab(
                                    selected = selectedPreviewTab == 0,
                                    onClick = { selectedPreviewTab = 0 },
                                    text = { Text("Result Preview", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                                )
                                Tab(
                                    selected = selectedPreviewTab == 1,
                                    onClick = { selectedPreviewTab = 1 },
                                    text = { Text("Original Image", fontSize = 12.sp) }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Viewport image box
                            val bmpToRender = if (selectedPreviewTab == 0) processedBitmap ?: originalBitmap else originalBitmap
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (bmpToRender != null) {
                                    Image(
                                        bitmap = bmpToRender.asImageBitmap(),
                                        contentDescription = "Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }

                // --- Tool Specific Controls Panel ---
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Tool Controls",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            when (tool.id) {
                                ToolId.COMPRESSOR -> {
                                    Text(
                                        text = "Compression Quality: $compressQuality%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Slider(
                                        value = compressQuality.toFloat(),
                                        onValueChange = {
                                            compressQuality = it.roundToInt()
                                            updateProcessing()
                                        },
                                        valueRange = 5f..100f,
                                        steps = 19
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Smaller File (Low)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Pristine (Lossless)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(text = "Target Format:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ImageOutputFormat.entries.forEach { fmt ->
                                            FilterChip(
                                                selected = selectedOutputFormat == fmt,
                                                onClick = {
                                                    selectedOutputFormat = fmt
                                                    updateProcessing()
                                                },
                                                label = { Text(fmt.displayName) }
                                            )
                                        }
                                    }
                                }

                                ToolId.RESIZER -> {
                                    Text(
                                        text = "Scale Percentage: ${resizeScale.roundToInt()}%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Slider(
                                        value = resizeScale,
                                        onValueChange = {
                                            resizeScale = it
                                            updateProcessing()
                                        },
                                        valueRange = 10f..200f
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(25f, 50f, 75f, 100f, 150f).forEach { preset ->
                                            FilterChip(
                                                selected = resizeScale == preset,
                                                onClick = {
                                                    resizeScale = preset
                                                    updateProcessing()
                                                },
                                                label = { Text("${preset.toInt()}%") }
                                            )
                                        }
                                    }
                                }

                                ToolId.CROPPER -> {
                                    Text(text = "Aspect Ratio Presets:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("1:1", "16:9", "4:3", "9:16", "3:2").forEach { preset ->
                                            FilterChip(
                                                selected = cropRatioPreset == preset,
                                                onClick = {
                                                    cropRatioPreset = preset
                                                    when (preset) {
                                                        "1:1" -> { cropLeft = 0.1f; cropTop = 0f; cropWidth = 0.8f; cropHeight = 0.8f }
                                                        "16:9" -> { cropLeft = 0f; cropTop = 0.2f; cropWidth = 1f; cropHeight = 0.56f }
                                                        "4:3" -> { cropLeft = 0.05f; cropTop = 0.1f; cropWidth = 0.9f; cropHeight = 0.67f }
                                                        "9:16" -> { cropLeft = 0.25f; cropTop = 0f; cropWidth = 0.5f; cropHeight = 0.88f }
                                                        "3:2" -> { cropLeft = 0.05f; cropTop = 0.15f; cropWidth = 0.9f; cropHeight = 0.6f }
                                                    }
                                                    updateProcessing()
                                                },
                                                label = { Text(preset) }
                                            )
                                        }
                                    }
                                }

                                ToolId.ROTATE_FLIP -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                rotateAngle = (rotateAngle - 90f) % 360f
                                                updateProcessing()
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.RotateLeft, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("90° Left")
                                        }
                                        Button(
                                            onClick = {
                                                rotateAngle = (rotateAngle + 90f) % 360f
                                                updateProcessing()
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.RotateRight, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("90° Right")
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                isFlipH = !isFlipH
                                                updateProcessing()
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Flip, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Flip Horizontal")
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                isFlipV = !isFlipV
                                                updateProcessing()
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Flip, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Flip Vertical")
                                        }
                                    }
                                }

                                ToolId.COLOR_ADJUSTER -> {
                                    Text("Brightness: ${brightness.roundToInt()}", fontSize = 12.sp)
                                    Slider(
                                        value = brightness,
                                        onValueChange = { brightness = it; updateProcessing() },
                                        valueRange = -80f..80f
                                    )
                                    Text("Contrast: ${String.format("%.1f", contrast)}x", fontSize = 12.sp)
                                    Slider(
                                        value = contrast,
                                        onValueChange = { contrast = it; updateProcessing() },
                                        valueRange = 0.5f..2f
                                    )
                                    Text("Saturation: ${String.format("%.1f", saturation)}x", fontSize = 12.sp)
                                    Slider(
                                        value = saturation,
                                        onValueChange = { saturation = it; updateProcessing() },
                                        valueRange = 0f..2.5f
                                    )
                                    Text("Color Warmth / Tint: ${temperature.roundToInt()}", fontSize = 12.sp)
                                    Slider(
                                        value = temperature,
                                        onValueChange = { temperature = it; updateProcessing() },
                                        valueRange = -40f..40f
                                    )
                                }

                                ToolId.FILTERS -> {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(PhotoFilterType.entries) { filter ->
                                            FilterChip(
                                                selected = selectedFilter == filter,
                                                onClick = {
                                                    selectedFilter = filter
                                                    updateProcessing()
                                                },
                                                label = { Text(filter.displayName) }
                                            )
                                        }
                                    }
                                }

                                ToolId.BLUR_SHARPEN -> {
                                    Text("Blur Radius: $blurRadius px", fontSize = 12.sp)
                                    Slider(
                                        value = blurRadius.toFloat(),
                                        onValueChange = { blurRadius = it.roundToInt(); updateProcessing() },
                                        valueRange = 0f..20f,
                                        steps = 20
                                    )
                                    Text("Sharpen Intensity: ${String.format("%.1f", sharpenIntensity)}", fontSize = 12.sp)
                                    Slider(
                                        value = sharpenIntensity,
                                        onValueChange = { sharpenIntensity = it; updateProcessing() },
                                        valueRange = 0f..1f
                                    )
                                }

                                ToolId.BORDER_CORNERS -> {
                                    Text("Corner Radius: ${cornerRadius.roundToInt()} px", fontSize = 12.sp)
                                    Slider(
                                        value = cornerRadius,
                                        onValueChange = { cornerRadius = it; updateProcessing() },
                                        valueRange = 0f..100f
                                    )
                                    Text("Border Width: ${borderWidth.roundToInt()} px", fontSize = 12.sp)
                                    Slider(
                                        value = borderWidth,
                                        onValueChange = { borderWidth = it; updateProcessing() },
                                        valueRange = 0f..30f
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        FilterChip(
                                            selected = isCircleAvatar,
                                            onClick = {
                                                isCircleAvatar = !isCircleAvatar
                                                updateProcessing()
                                            },
                                            label = { Text("Circular Avatar Cutout") }
                                        )
                                    }
                                }

                                ToolId.WATERMARK_TEXT -> {
                                    OutlinedTextField(
                                        value = watermarkText,
                                        onValueChange = { watermarkText = it; updateProcessing() },
                                        label = { Text("Watermark Text") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Position:", fontSize = 12.sp)
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(WatermarkPosition.entries) { pos ->
                                            FilterChip(
                                                selected = watermarkPosition == pos,
                                                onClick = { watermarkPosition = pos; updateProcessing() },
                                                label = { Text(pos.displayName) }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Opacity: ${(watermarkOpacity * 100).roundToInt()}%", fontSize = 12.sp)
                                    Slider(
                                        value = watermarkOpacity,
                                        onValueChange = { watermarkOpacity = it; updateProcessing() },
                                        valueRange = 0.2f..1f
                                    )
                                }

                                ToolId.PIXELATE_TOOL -> {
                                    Text("Mosaic Block Size: $pixelBlockSize px", fontSize = 12.sp)
                                    Slider(
                                        value = pixelBlockSize.toFloat(),
                                        onValueChange = { pixelBlockSize = it.roundToInt(); updateProcessing() },
                                        valueRange = 4f..48f,
                                        steps = 11
                                    )
                                }

                                ToolId.BACKGROUND_REMOVER -> {
                                    Text("Background Tolerance: ${(bgTolerance * 100).roundToInt()}%", fontSize = 12.sp)
                                    Slider(
                                        value = bgTolerance,
                                        onValueChange = { bgTolerance = it; updateProcessing() },
                                        valueRange = 0.05f..0.7f
                                    )
                                    Text("Key Target Color:", fontSize = 12.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = bgTargetColorInt == AndroidColor.WHITE,
                                            onClick = { bgTargetColorInt = AndroidColor.WHITE; updateProcessing() },
                                            label = { Text("White BG") }
                                        )
                                        FilterChip(
                                            selected = bgTargetColorInt == AndroidColor.BLACK,
                                            onClick = { bgTargetColorInt = AndroidColor.BLACK; updateProcessing() },
                                            label = { Text("Black BG") }
                                        )
                                        FilterChip(
                                            selected = bgTargetColorInt == AndroidColor.GREEN,
                                            onClick = { bgTargetColorInt = AndroidColor.GREEN; updateProcessing() },
                                            label = { Text("Studio Green") }
                                        )
                                    }
                                }

                                ToolId.QR_GENERATOR -> {
                                    OutlinedTextField(
                                        value = qrText,
                                        onValueChange = {
                                            qrText = it
                                            val qr = ImageProcessingUtils.generateQrCodeBitmap(it, 600, qrFgColorInt, qrBgColorInt)
                                            originalBitmap = qr
                                            processedBitmap = qr
                                        },
                                        label = { Text("QR Link or Content") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                ToolId.BASE64_TOOL -> {
                                    if (generatedBase64.isEmpty() && originalBitmap != null) {
                                        generatedBase64 = ImageProcessingUtils.bitmapToBase64(originalBitmap!!, ImageOutputFormat.PNG)
                                    }
                                    Text("Data URI Base64 Output:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = generatedBase64.take(200) + if (generatedBase64.length > 200) "..." else "",
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            IconButton(onClick = {
                                                val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clip.setPrimaryClip(ClipData.newPlainText("Base64", generatedBase64))
                                                Toast.makeText(context, "Base64 copied to clipboard!", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                            }
                                        }
                                    )
                                }

                                ToolId.COLOR_PALETTE -> {
                                    Text("Extracted Dominant Palette:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        dominantColors.forEach { colorInt ->
                                            val hex = ImageProcessingUtils.colorToHex(colorInt)
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        clip.setPrimaryClip(ClipData.newPlainText("Color Hex", hex))
                                                        Toast.makeText(context, "$hex copied!", Toast.LENGTH_SHORT).show()
                                                    }
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(colorInt))
                                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(hex, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                    }
                                }

                                ToolId.SOCIAL_RESIZER -> {
                                    val presets = listOf(
                                        "Instagram Post (1080x1080)",
                                        "Instagram Story (1080x1920)",
                                        "YouTube Thumbnail (1280x720)",
                                        "Twitter Post (1200x675)"
                                    )
                                    presets.forEach { preset ->
                                        FilterChip(
                                            selected = socialPresetName == preset,
                                            onClick = {
                                                socialPresetName = preset
                                                updateProcessing()
                                            },
                                            label = { Text(preset) },
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                    }
                                }

                                ToolId.COLLAGE_MAKER -> {
                                    Text("Added Photos: ${multiImages.size}/4", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { galleryLauncher.launch("image/*") }) {
                                            Text("+ Add Another Photo")
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Tile Spacing: $collageSpacing px", fontSize = 12.sp)
                                    Slider(
                                        value = collageSpacing.toFloat(),
                                        onValueChange = { collageSpacing = it.roundToInt(); updateProcessing() },
                                        valueRange = 0f..40f
                                    )
                                }

                                ToolId.IMAGES_TO_PDF -> {
                                    Text("Document Pages: ${multiImages.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                                        Text("+ Add Page Image")
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            val file = ImageProcessingUtils.generatePdfFromBitmaps(context, multiImages.ifEmpty { listOfNotNull(originalBitmap) })
                                            generatedPdfFile = file
                                            Toast.makeText(context, "PDF Generated! Saved ${file.name}", Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Generate Multi-Page PDF")
                                    }
                                }

                                else -> {
                                    Text("Select controls to adjust your photo.", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // --- Action Buttons Bar ---
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val target = processedBitmap ?: originalBitmap ?: return@Button
                                val uri = ImageProcessingUtils.saveBitmapToCache(context, target, selectedOutputFormat)
                                if (uri != null) {
                                    Toast.makeText(context, "Image ready! Saved to device cache.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download / Save Image", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val target = processedBitmap ?: originalBitmap ?: return@OutlinedButton
                                ImageProcessingUtils.shareBitmap(context, target, tool.title)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share Image")
                        }
                    }
                }
            }

            // --- Tool FAQ & Tips Section ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "How to Use & Tips",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        tool.faqs.forEach { (q, a) ->
                            Text(
                                text = "• $q",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = a,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
