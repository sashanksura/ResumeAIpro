# ProGuard/R8 rules for ResumeAI Pro

# ─── Retrofit & OkHttp ───
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.resumeai.pro.data.api.** { *; }
-keep class retrofit2.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# ─── Gson ───
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ─── Domain Models (used with Gson) ───
-keep class com.resumeai.pro.domain.model.** { *; }
-keep class com.resumeai.pro.data.api.ChatRequest { *; }
-keep class com.resumeai.pro.data.api.ChatResponse { *; }
-keep class com.resumeai.pro.data.api.ChatMessage { *; }
-keep class com.resumeai.pro.data.api.Choice { *; }
-keep class com.resumeai.pro.data.api.ApiError { *; }

# ─── Room ───
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ─── Hilt ───
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory

# ─── Compose ───
-dontwarn androidx.compose.**

# ─── Keep BuildConfig ───
-keep class com.resumeai.pro.BuildConfig { *; }

# ─── Preserve stack traces ───
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile