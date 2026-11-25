package co.bubotech.app_device_integrity

import android.app.Activity
import android.content.Context
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** AppDeviceIntegrityPlugin */
class AppDeviceIntegrityPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {

  private lateinit var channel: MethodChannel
  private lateinit var context: Context
  private lateinit var activity: Activity

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "app_attestation")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method != "getAttestationServiceSupport") {
      result.notImplemented()
      return
    }

    val gcp = call.argument<Long>("gcp")
    if (gcp == null) {
      result.error(
        "MISSING_GCP",
        "GCP Project ID is required for Play Integrity",
        null,
      )
      return
    }

    val attestation = AppDeviceIntegrity(context, gcp)
    attestation.integrityTokenResponse
      .addOnSuccessListener { response ->
        result.success(response.token())
      }
      .addOnFailureListener { error ->
        // Sin esto el Future de Dart no termina y el splash se queda cargando.
        result.error(
          "INTEGRITY_REQUEST_FAILED",
          error.message ?: "Play Integrity request failed",
          error.javaClass.simpleName,
        )
      }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onDetachedFromActivity() {
    // TODO
  }

  @Suppress("UNUSED_PARAMETER")
  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    // TODO
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity;
  }

  override fun onDetachedFromActivityForConfigChanges() {
    // TODO
  }
}
