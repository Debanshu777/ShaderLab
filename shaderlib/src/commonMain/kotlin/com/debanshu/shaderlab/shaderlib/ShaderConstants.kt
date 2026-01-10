package com.debanshu.shaderlab.shaderlib

/**
 * AGSL/SkSL shader source code constants for various image effects.
 * These shaders are compatible with both Android's RuntimeShader (API 33+)
 * and Skia's RuntimeEffect for iOS/Desktop.
 */
object ShaderConstants {
    
    const val GRAYSCALE = """
        uniform shader content;
        uniform float intensity;
        
        half4 main(float2 fragCoord) {
            half4 color = content.eval(fragCoord);
            float gray = dot(color.rgb, half3(0.299, 0.587, 0.114));
            half3 grayscaleColor = half3(gray, gray, gray);
            half3 result = mix(color.rgb, grayscaleColor, intensity);
            return half4(result, color.a);
        }
    """
    
    const val SEPIA = """
        uniform shader content;
        uniform float intensity;
        
        half4 main(float2 fragCoord) {
            half4 color = content.eval(fragCoord);
            
            // Sepia matrix transformation
            float r = color.r * 0.393 + color.g * 0.769 + color.b * 0.189;
            float g = color.r * 0.349 + color.g * 0.686 + color.b * 0.168;
            float b = color.r * 0.272 + color.g * 0.534 + color.b * 0.131;
            
            half3 sepiaColor = half3(r, g, b);
            half3 result = mix(color.rgb, sepiaColor, intensity);
            return half4(result, color.a);
        }
    """
    
    const val COLOR_INVERSION = """
        uniform shader content;
        
        half4 main(float2 fragCoord) {
            half4 color = content.eval(fragCoord);
            return half4(1.0 - color.rgb, color.a);
        }
    """
    
    const val VIGNETTE = """
        uniform shader content;
        uniform float2 resolution;
        uniform float radius;
        uniform float intensity;
        
        half4 main(float2 fragCoord) {
            half4 color = content.eval(fragCoord);
            
            // Normalize coordinates to center
            float2 uv = fragCoord / resolution;
            float2 center = float2(0.5, 0.5);
            float dist = distance(uv, center);
            
            // Calculate vignette factor
            float vignette = smoothstep(radius, radius - intensity, dist);
            
            return half4(color.rgb * vignette, color.a);
        }
    """
    
    const val PIXELATION = """
        uniform shader content;
        uniform float2 resolution;
        uniform float pixelSize;
        
        half4 main(float2 fragCoord) {
            // Snap coordinates to pixel grid
            float2 pixelCoord = floor(fragCoord / pixelSize) * pixelSize + pixelSize * 0.5;
            
            // Clamp to valid range
            pixelCoord = clamp(pixelCoord, float2(0.0), resolution);
            
            return content.eval(pixelCoord);
        }
    """
    
    const val CHROMATIC_ABERRATION = """
        uniform shader content;
        uniform float2 resolution;
        uniform float offset;
        
        half4 main(float2 fragCoord) {
            // Calculate direction from center
            float2 center = resolution * 0.5;
            float2 dir = normalize(fragCoord - center);
            
            // Sample each color channel with offset
            float r = content.eval(fragCoord + dir * offset).r;
            float g = content.eval(fragCoord).g;
            float b = content.eval(fragCoord - dir * offset).b;
            float a = content.eval(fragCoord).a;
            
            return half4(r, g, b, a);
        }
    """
    
    const val WAVE_DISTORTION = """
        uniform shader content;
        uniform float2 resolution;
        uniform float amplitude;
        uniform float frequency;
        uniform float time;
        
        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / resolution;
            
            // Apply wave distortion
            float xOffset = sin(uv.y * frequency + time) * amplitude;
            float yOffset = cos(uv.x * frequency + time) * amplitude;
            
            float2 distortedCoord = fragCoord + float2(xOffset, yOffset);
            
            // Clamp to valid range
            distortedCoord = clamp(distortedCoord, float2(0.0), resolution);
            
            return content.eval(distortedCoord);
        }
    """
}

