package com.debanshu.shaderlab.imagelib

import androidx.compose.runtime.Composable

/**
 * Status of a permission request.
 */
enum class PermissionStatus {
    /** Permission has been granted. */
    GRANTED,
    /** Permission has been denied. */
    DENIED,
    /** Permission has not been requested yet. */
    NOT_REQUESTED,
    /** Permission is not required on this platform. */
    NOT_REQUIRED
}

/**
 * Types of image-related permissions.
 */
enum class ImagePermission {
    /** Permission to read images from the device. */
    READ_IMAGES,
    /** Permission to write/save images to the device. */
    WRITE_IMAGES
}

/**
 * Interface for handling image-related permissions.
 */
interface PermissionHandler {
    /**
     * Check the current status of a permission.
     */
    fun checkPermission(permission: ImagePermission): PermissionStatus
    
    /**
     * Request a permission from the user.
     * 
     * @return The resulting permission status after the request
     */
    suspend fun requestPermission(permission: ImagePermission): PermissionStatus
}

/**
 * Remember a platform-specific permission handler.
 */
@Composable
expect fun rememberPermissionHandler(): PermissionHandler

