package com.example.dna

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.example.dna.ui.theme.DNATheme
import com.google.ar.core.Config
import com.google.ar.core.Frame
import io.github.sceneview.Scene
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.environment.Environment
import io.github.sceneview.gesture.RotateGestureDetector
import io.github.sceneview.loaders.EnvironmentLoader
import io.github.sceneview.model.isShadowCaster
import io.github.sceneview.model.isShadowReceiver
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}

enum class Screen {
    Home,
    ARView
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.name) {
        composable(Screen.Home.name) {
            HomeScreen(navController)
        }
        composable(Screen.ARView.name) {
            ARView()
        }
    }
}


@Composable
fun HomeScreen(navController: NavController){
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "DNA", style = MaterialTheme.typography.titleLarge, modifier = Modifier
            .padding(16.dp)
            .align(
                Alignment.CenterHorizontally
            ), color = Color.White)
        Text(text = "Deoxyribonucleic acid (abbreviated DNA) is the molecule that carries genetic information for the development and functioning of an organism. DNA is made of two linked strands that wind around each other to resemble a twisted ladder — a shape known as a double helix.", style = MaterialTheme.typography.bodyLarge, modifier = Modifier
            .padding(horizontal = 16.dp)
            .align(
                Alignment.CenterHorizontally
            ), color = Color.White)
        ModelView()
        Button(onClick = {navController.navigate(Screen.ARView.name)}, modifier = Modifier.padding(16.dp)) {
            Text(text = "Open in AR")
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
        )
    }
}


@Composable
fun ARView(){
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val model = modelLoader.createModelInstance("dna.glb")
    var frame by remember { mutableStateOf<Frame?>(null) }
    val childNodes = rememberNodes()
    var view = rememberView(engine = engine)
    ARScene(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        view = view,
        modelLoader = modelLoader,
        childNodes = rememberNodes{
                                  add(ModelNode(model.apply {
                                      isShadowReceiver = false
                                  }).apply{
                                      scaleToUnitCube(0.3f)

                                  })
        },
        sessionConfiguration = { session, config ->
            config.depthMode =
                when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    true -> Config.DepthMode.AUTOMATIC
                    else -> Config.DepthMode.DISABLED
                }
            config.lightEstimationMode = Config.LightEstimationMode.DISABLED
            config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
        },
        planeRenderer = true,
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DNATheme {
        Greeting("Android")
    }
}