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
-keep class sun.misc.Unsafe { *; }

#retrolambda
-dontwarn java.lang.invoke.*
-dontwarn sun.misc.**

#rxJava
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}

-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

#OkHttp
-keep class okhttp3.** { *; }
-keep,includedescriptorclasses class okio.Source
-keep,includedescriptorclasses class okio.okio.Buffer
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**


#retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

#gson
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

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

#fresko
# Keep our interfaces so they can be used by other ProGuard rules.
# See http://sourceforge.net/p/proguard/bugs/466/
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

#amplitude
-keep class com.google.android.gms.ads.** { *; }
#end amplitude

#playservices
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

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

#checkout
-keep class com.android.vending.billing.**

-assumenosideeffects class org.solovyev.android.checkout.Billing {
    public static void debug(...);
    public static void warning(...);
    public static void error(...);
}

-assumenosideeffects class org.solovyev.android.checkout.Check {
    static *;
}
#end chekout

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
-keep class Type {
    public *;
}
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}