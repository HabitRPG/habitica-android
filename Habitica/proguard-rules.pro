# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions

#retrolambda
-dontwarn java.lang.invoke.*
-dontwarn sun.misc.**

#rxJava
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}


#OkHttp
-keep class okhttp3.** { *; }
-keep,includedescriptorclasses class okio.Source
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**


#retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-if interface * { @retrofit2.http.* *** *(...); }
-keep,allowobfuscation interface <3>
-keep,allowobfuscation,allowshrinking interface retrofit2.Call

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

#gson
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type
-keepclassmembers,allowobfuscation class * {
 @com.google.gson.annotations.SerializedName <fields>;
}

#keep Habitica code
-keep class com.habitrpg.android.habitica.** { *; }
-keep class com.habitrpg.common.habitica.** { *; }
-keep class com.habitrpg.shared.habitica.** { *; }

#realm
-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class *
-dontwarn javax.**
-dontwarn io.realm.**

#crashlytic
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

#amplitude
-keep class com.google.android.gms.ads.** { *; }
#end amplitude


-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
    }
    -keep class com.google.android.gms.** { *; }
    -dontwarn com.google.android.gms.**
#end playservices

#add warnings here, warnings in proguard is normal
-dontwarn javax.annotation.**
-dontwarn com.squareup.picasso.**
-dontwarn okio.**
-dontwarn rx.**
-dontwarn com.android.volley.toolbox.**
-dontwarn com.facebook.infer.**
-dontwarn com.habitrpg.android.habitica.ui.views.bottombar.**
-dontwarn com.viewpagerindicator.**
#-ignorewarnings

-keep class com.google.firebase.provider.FirebaseInitProvider

#keep all enums
-keepclassmembers enum * { *; }
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepattributes Signature
-keep class kotlin.coroutines.Continuation
