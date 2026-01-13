package com.debanshu.shaderlab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.debanshu.shaderlab.shaderlib.areShadersSupported
import com.debanshu.shaderlab.shaderlib.ShaderRegistry
import com.debanshu.shaderlab.shaders.BlurShader
import com.debanshu.shaderlab.shaders.ChromaticAberrationShader
import com.debanshu.shaderlab.shaders.ColorInversionShader
import com.debanshu.shaderlab.shaders.GrayscaleShader
import com.debanshu.shaderlab.shaders.PixelationShader
import com.debanshu.shaderlab.shaders.SepiaShader
import com.debanshu.shaderlab.shaders.VignetteShader
import com.debanshu.shaderlab.shaders.WaveDistortionShader
import com.debanshu.shaderlab.ui.ShaderLabContent
import com.debanshu.shaderlab.ui.theme.ShaderLabTheme
import com.debanshu.shaderlab.viewmodel.ShaderLabViewModel

private val shadersInitialized: Boolean by lazy {
    if (ShaderRegistry.size == 0) {
        ShaderRegistry.registerAll(
            GrayscaleShader(),
            SepiaShader(),
            ColorInversionShader,
            VignetteShader(),
            BlurShader(),
            PixelationShader(),
            ChromaticAberrationShader(),
            WaveDistortionShader()
        )
    }
    true
}

@Composable
fun ShaderLabApp() {
    shadersInitialized
    val viewModel: ShaderLabViewModel = viewModel { ShaderLabViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.setShadersSupported(areShadersSupported())
    }
    
    ShaderLabTheme(darkTheme = uiState.isDarkTheme) {
        ShaderLabContent(
            viewModel = viewModel
        )
    }
}
