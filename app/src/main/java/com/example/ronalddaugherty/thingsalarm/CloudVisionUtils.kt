package com.example.ronalddaugherty.thingsalarm

import android.util.Log

import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.AnnotateImageRequest
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse
import com.google.api.services.vision.v1.model.EntityAnnotation
import com.google.api.services.vision.v1.model.Feature

import java.io.IOException
import java.util.Collections
import java.util.HashMap



object CloudVisionUtils {
    private val TAG = CloudVisionUtils::class.java.simpleName

    private val CLOUD_VISION_API_KEY = "AIzaSyCqHzSbl1lR0wiB2aOZbWFhkLHD6U1ZK7Y"
    private val LABEL_DETECTION = "LABEL_DETECTION"

    private val MAX_LABEL_RESULTS = 10

    @Throws(IOException::class)
    fun annotateImage(imageBytes: ByteArray): Map<String, Float> {
        // Construct the Vision API instance
        val httpTransport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()
        val initializer = VisionRequestInitializer(CLOUD_VISION_API_KEY)
        val vision = Vision.Builder(httpTransport, jsonFactory, null)
                .setVisionRequestInitializer(initializer)
                .build()

        // Create the image request
        val imageRequest = AnnotateImageRequest()
        val img = com.google.api.services.vision.v1.model.Image()
        img.encodeContent(imageBytes)
        imageRequest.image = img

        // Add the features we want
        val labelDetection = Feature()
        labelDetection.type = LABEL_DETECTION
        labelDetection.maxResults = MAX_LABEL_RESULTS
        imageRequest.features = listOf(labelDetection)


        // Batch and execute the request
        val requestBatch = BatchAnnotateImagesRequest()
        requestBatch.requests = listOf(imageRequest)
        val response = vision.images()
                .annotate(requestBatch)
                // Due to a bug: requests to Vision API containing large images fail when GZipped.
                .setDisableGZipContent(true)
                .execute()

        return convertResponseToMap(response)
    }

    private fun convertResponseToMap(response: BatchAnnotateImagesResponse): Map<String, Float> {

        // Convert response into a readable collection of annotations
        val annotations = HashMap<String, Float>()
        val labels = response.responses[0].labelAnnotations
        if (labels != null) {
            for (label in labels) {
                annotations.put(label.description, label.score)
            }
        }

        Log.d(TAG, "Cloud Vision request completed:" + annotations)
        return annotations
    }
}
