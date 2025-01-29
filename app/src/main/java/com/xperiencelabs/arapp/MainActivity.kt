package com.xperiencelabs.arapp

import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation

class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: ArSceneView
    private lateinit var modelNode: ArModelNode
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var isModelPlaced = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize AR scene view
        sceneView = findViewById(R.id.sceneView)

        // Configure AR session
        sceneView.configureSession { session, config ->
            val inputStream = assets.open("images/ardb.imgdb")
            config.augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, inputStream)
            config.lightEstimationMode = Config.LightEstimationMode.DISABLED
        }

        // Initialize AR model node
        modelNode = ArModelNode(sceneView.engine).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/chemset.glb"
            )
        }

        // Scale gesture detector for zooming
        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scaleFactor = detector.scaleFactor
                // Update the model scale
                modelNode.scale = Position(
                    modelNode.scale.x * scaleFactor,
                    modelNode.scale.y * scaleFactor,
                    modelNode.scale.z * scaleFactor
                )
                return true
            }
        })

        // Detect images and place the model
        sceneView.onFrame = { frame ->
            val arFrame = sceneView.arSession?.update()
            val augmentedImages = arFrame?.getUpdatedTrackables(AugmentedImage::class.java)

            augmentedImages?.forEach { image ->
                if (image.trackingState == TrackingState.TRACKING && image.name == "expdiag.png") {
                    if (!isModelPlaced) {
                        modelNode.anchor = image.createAnchor(image.centerPose)
                        sceneView.addChild(modelNode)
                        modelNode.scale = Position(0.002f, 0.002f, 0.002f)
                        isModelPlaced = true
                    }
                }
            }
        }

        // Enable touch interaction
        sceneView.setOnTouchListener { _, event ->
            handleTouch(event)
            true
        }
    }

    private fun handleTouch(event: MotionEvent) {
        scaleGestureDetector.onTouchEvent(event)
        // Additional gesture handling for drag or rotation can be implemented here
    }
}
