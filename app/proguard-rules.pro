# ===== Hilt =====
-keep class com.gosyria.app.**_HiltComponents* { *; }
-keep class dagger.hilt.android.internal.managers.** { *; }

# ===== Keep data models / DTOs (Gson uses field names via reflection) =====
-keep class com.gosyria.app.data.model.** { *; }
-keep class com.gosyria.app.data.remote.dto.** { *; }
# Keep enum values (e.g. RideStatus) used in JSON (de)serialization
-keepclassmembers enum com.gosyria.app.data.** { *; }

# ===== Gson =====
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-dontwarn sun.misc.**
# Keep generic type info for TypeToken
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ===== Retrofit =====
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
# Keep Retrofit service interface method annotations
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# ===== OkHttp =====
-dontwarn okhttp3.**
-dontwarn okio.**

# ===== Kotlin =====
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
