package com.org.humanfaceeyedetector.ui

import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext

/**
 * Handle image selection from device storage
 * Opens file picker for user to select image
 */
@Composable
fun ImagePickerLauncher(onImageSelected: (ImageBitmap) -> Unit): () -> Unit {
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
    
    return { launcher.launch("image/*") }
}

