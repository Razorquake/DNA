package com.example.dna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.dna.data.AppwriteModelLoader
import com.example.dna.ui.nav.ARScreen
import com.example.dna.ui.nav.AlphabetScreen
import com.example.dna.ui.nav.HomeScreen
import com.example.dna.ui.nav.QuizScreen
import com.example.dna.ui.screen.ARScreen
import com.example.dna.ui.screen.AlphabetScreen
import com.example.dna.ui.screen.HomeScreen
import com.example.dna.ui.screen.QuizScreen
import com.example.dna.ui.theme.DNATheme
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import io.github.sceneview.Scene
import io.github.sceneview.animation.Transition.animateRotation
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.loaders.EnvironmentLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNode
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class MainActivity : ComponentActivity() {
    private lateinit var appwriteModelLoader: AppwriteModelLoader
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appwriteModelLoader = AppwriteModelLoader(this)
        setContent {
            DNATheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appwriteModelLoader.cleanOldCacheFiles()
    }
}

@Composable
fun NavGraph() {
    Scaffold(modifier = Modifier.fillMaxSize()) {
        val inner = it
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = HomeScreen) {
            composable<HomeScreen> {
                HomeScreen(navController = navController)
            }
            composable<ARScreen> {
                val alphabet = it.toRoute<ARScreen>().model
                ARScreen(navController = navController, model = alphabet)
            }
            composable<AlphabetScreen> {
                AlphabetScreen(navController = navController)
            }
            composable<QuizScreen> {
                QuizScreen(navController = navController)
            }
        }
    }
}

@Composable
fun ModelView() {
    Box(modifier = Modifier
        .height(400.dp)
        .padding(10.dp)){
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)
        val context = LocalContext.current
        val cameraNode = rememberCameraNode(engine).apply {
            position = Position(z = 4.0f)
        }
        val centerNode = rememberNode(engine)
            .addChildNode(cameraNode)
        val cameraTransition = rememberInfiniteTransition(label = "CameraTransition")
        val cameraRotation by cameraTransition.animateRotation(
            initialValue = Rotation(y = 0.0f),
            targetValue = Rotation(y = 360.0f),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 7.seconds.toInt(DurationUnit.MILLISECONDS))
            )
        )
        Scene(
            modifier = Modifier.fillMaxSize(),

            engine = engine,
            modelLoader = modelLoader,
            childNodes = rememberNodes {
                add(ModelNode(modelLoader.createModelInstance("dna.glb")).apply {
                    // Move the node 4 units in Camera front direction
                    scaleToUnitCube(0.8f)
                })
            },
            environmentLoader = EnvironmentLoader(engine, context),
            onFrame = {
                centerNode.rotation = cameraRotation
                cameraNode.lookAt(centerNode)
            }
        )
    }
}


@Composable
fun ARView(){
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val modelInstances = remember { mutableListOf<ModelInstance>() }
    var frame by remember { mutableStateOf<Frame?>(null) }
    val childNodes = rememberNodes()
    val view = rememberView(engine = engine)
    ARScene(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        view = view,
        modelLoader = modelLoader,
        childNodes = childNodes,
        sessionConfiguration = { session, config ->
            config.depthMode =
                when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
                }
            config.lightEstimationMode = Config.LightEstimationMode.DISABLED
            config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
        },
        onSessionUpdated = { _, updatedFrame ->
            frame = updatedFrame

            if (childNodes.isEmpty()) {
                updatedFrame.getUpdatedPlanes()
                    .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                    ?.let { it.createAnchorOrNull(it.centerPose) }?.let { anchor ->
                        childNodes += createAnchorNode(
                            engine = engine,
                            modelLoader = modelLoader,
                            modelInstances = modelInstances,
                            anchor = anchor
                        )
                    }
            }
        },
        planeRenderer = false,
        onGestureListener = rememberOnGestureListener(
            onSingleTapConfirmed = { motionEvent, node ->
                if (node == null) {
                    val hitResults = frame?.hitTest(motionEvent.x, motionEvent.y)
                    hitResults?.firstOrNull {
                        it.isValid(
                            depthPoint = false,
                            point = false
                        )
                    }?.createAnchorOrNull()
                        ?.let { anchor ->
                            childNodes += createAnchorNode(
                                engine = engine,
                                modelLoader = modelLoader,
                                modelInstances = modelInstances,
                                anchor = anchor
                            )
                        }
                }
            })
        /*onSessionUpdated = { session, updatedFrame ->
            frame = updatedFrame
        },
        sessionConfiguration = {session, config ->
                               config.lightEstimationMode = Config.LightEstimationMode.DISABLED
            config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
        },
        onGestureListener = rememberOnGestureListener(
            onSingleTapConfirmed = { motionEvent, node ->
                val hitResults = frame?.hitTest(motionEvent.x, motionEvent.y)
                val anchor = hitResults?.firstOrNull {
                    it.isValid(depthPoint = false, point = false)
                }?.createAnchorOrNull()

                if (anchor != null) {
                    val anchorNode = AnchorNode(engine = engine, anchor = anchor)
                    anchorNode.addChildNode(
                        ModelNode(modelLoader.createModelInstance("dna.glb").apply {

                        })
                    )
                    childNodes += anchorNode
                }
            }
        )*/
    )
}

fun createAnchorNode(
    engine: Engine,
    modelLoader: ModelLoader,
    modelInstances: MutableList<ModelInstance>,
    anchor: Anchor
): AnchorNode {
    val anchorNode = AnchorNode(engine = engine, anchor = anchor)
    val modelNode = ModelNode(
        modelInstance = modelInstances.apply {
            if (isEmpty()) {
                this += modelLoader.createInstancedModel("dna.glb", 10)
            }
        }.removeAt(modelInstances.apply {
            if (isEmpty()) {
                this += modelLoader.createInstancedModel("dna.glb", 10)
            }
        }.lastIndex),
        // Scale to fit in a 0.5 meters cube
        scaleToUnits = 0.5f
    ).apply {
        // Model Node needs to be editable for independent rotation from the anchor rotation
        isShadowReceiver = false
        isPositionEditable = false
        isScaleEditable = true
        isRotationEditable = true
    }
    anchorNode.addChildNode(modelNode)
    return anchorNode
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DNATheme {
        HomeScreen(navController = rememberNavController())
    }
}