package com.debanshu.shaderlab.imagelib

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberPermissionHandler(): PermissionHandler {
    return remember { JvmPermissionHandler() }
}

/**
 * JVM/Desktop permission handler.
 * On desktop platforms, file system permissions are typically not required.
 */
private class JvmPermissionHandler : PermissionHandler {
    
    override fun checkPermission(permission: ImagePermission): PermissionStatus {
        // Desktop platforms don't require explicit permissions for file access
        return PermissionStatus.NOT_REQUIRED
    }
    
    override suspend fun requestPermission(permission: ImagePermission): PermissionStatus {
        // No permission needed on desktop
        return PermissionStatus.NOT_REQUIRED
    }
}

