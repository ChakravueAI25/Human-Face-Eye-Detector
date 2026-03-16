package com.org.humanfaceeyedetector.platform

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import android.provider.MediaStore

/**
 * Composable to launch image picker and handle selection
 * Used for "Upload Image" functionality
 */
@Composable
fun ImagePickerLauncher(onImageSelected: (ImageBitmap) -> Unit) {
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                val imageBitmap = bitmap.asImageBitmap()
                onImageSelected(imageBitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Store launcher for external access
    ImagePickerHolder.launcher = launcher
}

/**
 * Holder for image picker launcher
 * Allows HomeScreen to trigger picker without holding state
 */
object ImagePickerHolder {
    var launcher: Any? = null
    
    fun launchImagePicker() {
        @Suppress("UNCHECKED_CAST")
        val launcherCast = launcher as? androidx.activity.result.ActivityResultLauncher<String>
        launcherCast?.launch("image/*")
    }
}

