# Keep Firestore model classes — deserialized via reflection, require no-arg constructors and field names
-keep class com.maxot.seekandcatch.core.common.model.** { *; }

# Keep attributes required by Firebase (generic type info + annotations for Firestore)
-keepattributes Signature
-keepattributes *Annotation*

# Firebase Auth — reCAPTCHA dependency used by firebase-auth 22.x for anonymous sign-in
-keep class com.google.android.recaptcha.** { *; }
-dontwarn com.google.android.recaptcha.**

# Crashlytics — preserve source file names and line numbers in stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keep public class * extends java.lang.Exception