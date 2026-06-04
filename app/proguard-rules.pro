# Proguard rules for Yemen Services App

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep serializable model names
-keep class com.example.data.** { *; }
