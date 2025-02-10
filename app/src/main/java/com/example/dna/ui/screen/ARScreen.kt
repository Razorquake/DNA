package com.example.dna.ui.screen

import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.dna.data.AppwriteModelLoader
import com.example.dna.util.Utils
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView
import java.io.File

@Composable
fun ARScreen(navController: NavController, model: String){
    val context = LocalContext.current
    val appwriteModelLoader = remember { AppwriteModelLoader(context) }
    val modelFile = remember { mutableStateOf<File?>(null) }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(model) {
        isLoading.value = true
        modelFile.value = appwriteModelLoader.downloadModel(Utils.getModelForAlphabet(model))
        isLoading.value = false
    }

    if (isLoading.value) {
        CircularProgressIndicator()
        return
    }

    if (modelFile.value == null) {
        // Show error state

        return
    }
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val cameraNode = rememberARCameraNode(engine)
    val childNodes = rememberNodes()
    val view = rememberView(engine = engine)
    val collisionSystem = rememberCollisionSystem(view)
    val planeRenderer = remember {
        mutableStateOf(true)
    }
    val modelInstance = remember {
        mutableListOf<ModelInstance>()
    }
    val trackingFailureReason = remember {
        mutableStateOf<TrackingFailureReason?>(null)
    }
    val frame = remember {
        mutableStateOf<Frame?>(null)
    }
    ARScene(
        modifier = Modifier.fillMaxSize(),
        childNodes = childNodes,
        engine = engine,
        view = view,
        modelLoader = modelLoader,
        collisionSystem = collisionSystem,
        planeRenderer = planeRenderer.value,
        cameraNode = cameraNode,
        materialLoader = materialLoader,
        onTrackingFailureChanged = {
            trackingFailureReason.value = it
        },
        onSessionUpdated = { _, updatedFrame ->
            frame.value = updatedFrame
        },
        sessionConfiguration = { session, config ->
            config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)){
                true -> Config.DepthMode.AUTOMATIC
                else -> Config.DepthMode.DISABLED
            }
            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        },
        onGestureListener = rememberOnGestureListener(
            onSingleTapConfirmed = { e: MotionEvent, node: Node? ->
                if (node == null){
                    val hitTestResult = frame.value?.hitTest(e.x, e.y)
                    hitTestResult?.firstOrNull {
                        it.isValid(
                            depthPoint = false,
                            point = false
                        )
                    }?.createAnchorOrNull()?.let {
                        modelFile.value?.let { file ->
                            val nodeModel = Utils.createAnchorNode(
                                engine = engine,
                                modelLoader = modelLoader,
                                materialLoader = materialLoader,
                                modelInstance = modelInstance,
                                anchor = it,
                                model = file
                            )
                            childNodes += nodeModel
                        }
                    }
                }
            }
        )
    )
}