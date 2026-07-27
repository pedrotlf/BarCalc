# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Keep line numbers and hide original source file names so Play Console crash
# reports deobfuscate cleanly against the uploaded mapping.txt.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── ML Kit (tab scanning) ────────────────────────────────────────────────────
# The ML Kit artifacts ship their own consumer rules, so these are a backstop
# for the parts reached reflectively: the bundled text-recognition model's
# native entry points, and the options classes Play services reads when it
# hands back a scanned page. Getting this wrong only shows up in release
# builds, which is exactly where scanning would then be broken.
-keep class com.google.mlkit.vision.text.** { *; }
-keep class com.google.mlkit.vision.documentscanner.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text** { *; }
-keep class com.google.mlkit.genai.** { *; }
-dontwarn com.google.mlkit.**

# The model's reply is deserialised into these, so their names and fields have
# to survive minification even though nothing in the app calls them directly.
-keep class com.pedrotlf.barcalc.data.receipt.GeminiTabReader$* { *; }

# ── kotlinx.serialization ─────────────────────────────────────────────────────
# The runtime ships consumer rules, but we keep our own @Serializable models and
# their generated serializers explicitly: a stripped serializer would silently
# break session persistence (Person, TabItem, TabUiState under this package).
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keep,includedescriptorclasses class com.pedrotlf.barcalc.**$$serializer { *; }
-keepclassmembers class com.pedrotlf.barcalc.** {
    *** Companion;
}
-keepclasseswithmembers class com.pedrotlf.barcalc.** {
    kotlinx.serialization.KSerializer serializer(...);
}