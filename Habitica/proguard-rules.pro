# Rules covered by default proguard-android-optimize.txt or by consumer rules bundled with
# libraries (Gson, Retrofit, OkHttp, Realm, Play services, Firebase, Glance, WorkManager) are
# intentionally not repeated here.

#crashlytics
-keepattributes SourceFile,LineNumberTable

#gson
# Gson reads and writes model fields by name, and most of them have no @SerializedName.
# Keep the classes when used, so R8 full mode doesn't assume they're never instantiated,
# but allow renaming classes and optimizing their methods.
-keep,allowobfuscation,allowshrinking class com.habitrpg.android.habitica.models.**
-keep,allowobfuscation,allowshrinking class com.habitrpg.common.habitica.models.**
-keep,allowobfuscation,allowshrinking class com.habitrpg.shared.habitica.models.**
-keep,allowobfuscation,allowshrinking class com.habitrpg.android.habitica.widget.glance.data.*WidgetState
-keep,allowobfuscation,allowshrinking class com.habitrpg.android.habitica.widget.glance.data.TaskWidgetItem
-keepclassmembers class com.habitrpg.android.habitica.models.**,
                        com.habitrpg.common.habitica.models.**,
                        com.habitrpg.shared.habitica.models.**,
                        com.habitrpg.android.habitica.widget.glance.data.*WidgetState,
                        com.habitrpg.android.habitica.widget.glance.data.TaskWidgetItem {
    <fields>;
    <init>();
}
# Gson reads enum constants reflectively
-keepclassmembers enum com.habitrpg.** {
    <fields>;
}

#realm
# R8 erases type arguments (RealmList<Tag> -> RealmList) from field signatures of classes that are not kept,
# which breaks Gson's lookup of the type adapters registered for those generic types
-keep,allowobfuscation class io.realm.RealmList
-keep,allowobfuscation class io.realm.RealmObject
-keep,allowobfuscation class io.realm.RealmResults

# simpleName is used as fragment tag and sent to the server as the report sourceView
-keepnames class com.habitrpg.android.habitica.ui.** extends androidx.fragment.app.Fragment
