package com.org.humanfaceeyedetector.ml

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import kotlinx.coroutines.tasks.await

private const val TAG = "FaceDetector"

data class FaceResult(
    val boundingBox: Rect,
    val leftEye: android.graphics.PointF?,
    val rightEye: android.graphics.PointF?,
    val trackingId: Int? = null
)

object FaceDetector {

    private val detector: com.google.mlkit.vision.face.FaceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.05f)
            .enableTracking()
            .build()

        FaceDetection.getClient(options)
    }

    suspend fun detect(image: InputImage): List<FaceResult> {
        return try {
            val faces = detector.process(image).await()

            faces.map { face ->
                FaceResult(
                    boundingBox = face.boundingBox,
                    leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)?.position,
                    rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position,
                    trackingId = face.trackingId
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Face detection failed", e)
            emptyList()
        }
    }
}
