# Rules for Retrofit and Gson DTOs
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.aura.eventos.data.remote.** { *; }

# Rules for Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Rules for Hilt
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keepclassmembers,allowobfuscation class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Rules for WorkManager & @HiltWorker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(...);
}

# Coil / Image loading
-keep class coil.** { *; }
