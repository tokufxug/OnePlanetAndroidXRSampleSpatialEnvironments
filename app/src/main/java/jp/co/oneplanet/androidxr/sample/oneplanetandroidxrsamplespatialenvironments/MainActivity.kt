package jp.co.oneplanet.androidxr.sample.oneplanetandroidxrsamplespatialenvironments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.background
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.xr.compose.platform.LocalHasXrSpatialFeature
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.EdgeOffset
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.OrbiterEdge
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SpatialRoundedCornerShape
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.movable
import androidx.xr.compose.subspace.layout.resizable
import androidx.xr.compose.subspace.layout.width
import androidx.xr.scenecore.ExrImage
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.Session
import androidx.xr.scenecore.SpatialEnvironment
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import jp.co.oneplanet.androidxr.sample.oneplanetandroidxrsamplespatialenvironments.ui.theme.OnePlanetAndroidXRSampleSpatialEnvironmentsTheme

class MainActivity : ComponentActivity() {

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OnePlanetAndroidXRSampleSpatialEnvironmentsTheme {
                val session = LocalSession.current
                if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
                    Subspace {
                        MySpatialContent(onRequestHomeSpaceMode = { session?.platformAdapter?.requestHomeSpaceMode() })
                    }
                } else {
                    My2DContent(onRequestFullSpaceMode = { session?.platformAdapter?.requestFullSpaceMode() })
                }
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun MySpatialContent(onRequestHomeSpaceMode: () -> Unit) {
    SpatialPanel(SubspaceModifier.width(1280.dp).height(800.dp).resizable().movable()) {
        Surface {
            SpatialEnvironments(
                modifier = Modifier
                    .background(color = Color.LightGray)
                    .height(300.dp)
                    .width(300.dp)
            )
        }
        Orbiter(
            position = OrbiterEdge.Top,
            offset = EdgeOffset.inner(offset = 20.dp),
            alignment = Alignment.End,
            shape = SpatialRoundedCornerShape(CornerSize(28.dp))
        ) {
            HomeSpaceModeIconButton(
                onClick = onRequestHomeSpaceMode,
                modifier = Modifier.size(56.dp)
            )
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun My2DContent(onRequestFullSpaceMode: () -> Unit) {
    Surface {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MainContent(modifier = Modifier.padding(48.dp))
            if (LocalHasXrSpatialFeature.current) {
                FullSpaceModeIconButton(
                    onClick = onRequestFullSpaceMode,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
    }
}

@Composable
fun MainContent(modifier: Modifier = Modifier) {
    Text(text = stringResource(R.string.hello_android_xr), modifier = modifier)
}

@Composable
fun FullSpaceModeIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(id = R.drawable.ic_full_space_mode_switch),
            contentDescription = stringResource(R.string.switch_to_full_space_mode)
        )
    }
}

@Composable
fun HomeSpaceModeIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalIconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(id = R.drawable.ic_home_space_mode_switch),
            contentDescription = stringResource(R.string.switch_to_home_space_mode)
        )
    }
}

@PreviewLightDark
@Composable
fun My2dContentPreview() {
    OnePlanetAndroidXRSampleSpatialEnvironmentsTheme {
        My2DContent(onRequestFullSpaceMode = {})
    }
}

@Preview(showBackground = true)
@Composable
fun FullSpaceModeButtonPreview() {
    OnePlanetAndroidXRSampleSpatialEnvironmentsTheme {
        FullSpaceModeIconButton(onClick = {})
    }
}

@PreviewLightDark
@Composable
fun HomeSpaceModeButtonPreview() {
    OnePlanetAndroidXRSampleSpatialEnvironmentsTheme {
        HomeSpaceModeIconButton(onClick = {})
    }
}

@Composable
fun SpatialEnvironments(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val session = LocalSession.current
    Button(onClick = {
        openSpatialEnvironments(context, session)
    }) {
        Text(text = "Android XR spatial environment")
    }
}

fun openSpatialEnvironments(context: Context, session: Session?) {
    val xrCoreSession = checkNotNull(session)

    // 非同期で処理が終わるのを待つ
    val environmentGeometryFuture: ListenableFuture<GltfModel> = GltfModel.create(xrCoreSession, "models/Duck.glb")

    // ExrImage は非同期でなくそのまま取得
    val skyboxExr = ExrImage.create(xrCoreSession, "skyboxes/pretoria_gardens_4k.exr")

    // environmentGeometry の非同期結果が完了するのを待つ
    environmentGeometryFuture.addListener({
        try {
            // 非同期で取得した結果を同期的に取得
            val environmentGeometry = environmentGeometryFuture.get()

            // 空間環境の設定
            val spatialEnvironmentPreference = SpatialEnvironment.SpatialEnvironmentPreference(skyboxExr, environmentGeometry)

            val preferenceResult = xrCoreSession.spatialEnvironment.setSpatialEnvironmentPreference(spatialEnvironmentPreference)

            if (preferenceResult == SpatialEnvironment.SetSpatialEnvironmentPreferenceChangeApplied()) {
                // 環境が正常に更新され、視覚的に反映された場合の処理
            } else if (preferenceResult == SpatialEnvironment.SetSpatialEnvironmentPreferenceChangePending()) {
                // 更新中の場合の処理
            }

        } catch (e: Exception) {
            // エラー処理
            e.printStackTrace()
        }
    }, ContextCompat.getMainExecutor(context)) // UIスレッドで処理を行うためMainExecutorを指定
}

fun <T> ListenableFuture<T>.getSyncResult(context: Context): T {
    val result = SettableFuture.create<T>()
    this.addListener({
        try {
            result.set(this.get()) // 非同期結果を取得
        } catch (e: Exception) {
            result.setException(e) // エラーを処理
        }
    }, ContextCompat.getMainExecutor(context)) // メインスレッドでListenerを追加

    return result.get() // 非同期結果を待つ
}