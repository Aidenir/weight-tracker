# Keep Kotlin metadata
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault,Signature,InnerClasses,EnclosingMethod

# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }

# Compose already covered by androidx default rules.
