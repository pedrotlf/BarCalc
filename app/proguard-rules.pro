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