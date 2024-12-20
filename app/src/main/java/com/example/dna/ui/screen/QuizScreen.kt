package com.example.dna.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dna.uil.Utils
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberView

@Composable
fun QuizScreen(navController: NavController){
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val score = remember {
            mutableIntStateOf(0)
        }
        val model = remember {
            mutableStateOf(Utils.randomModel())
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
                if (childNodes.isEmpty()){
                    updatedFrame.getUpdatedPlanes().firstOrNull {
                        it.type == Plane.Type.HORIZONTAL_UPWARD_FACING
                    }?.let {
                        it.createAnchorOrNull(it.centerPose)?.let {
                            childNodes += Utils.createAnchorNode(
                                engine = engine,
                                modelLoader = modelLoader,
                                materialLoader = materialLoader,
                                modelInstance = modelInstance,
                                anchor = it,
                                model = model.value.second
                            )
                        }
                    }
                }
            },
            sessionConfiguration = { session, config ->
                config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)){
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
                }
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            },
        )
        val listOfAnswers = remember {
            mutableStateOf(listOf(
                model.value.first,
                Utils.alphabets.keys.random(),
                Utils.alphabets.keys.random(),
                Utils.alphabets.keys.random()
            ).shuffled())
        }
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Quiz Screen",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 24.sp
            )
            Text(
                "Score: ${score.value}",
                modifier = Modifier.align(Alignment.TopCenter),
                fontSize = 24.sp
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOfAnswers.value.forEach {
                AlphabetItem(alphabet = it, onClick =  {
                    if (it == model.value.first){
                        score.value += 1
                        model.value = Utils.randomModel()
                        listOfAnswers.value = listOf(
                            model.value.first,
                            Utils.alphabets.keys.random(),
                            Utils.alphabets.keys.random(),
                            Utils.alphabets.keys.random()
                        ).shuffled()
                        childNodes.clear()
                        modelInstance.clear()
                        frame.value = null
                    }
                })
            }
        }
    }
}