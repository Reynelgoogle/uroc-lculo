# Resumen de Arreglos en UroCálculos

Se han realizado varios ajustes técnicos para permitir la correcta sincronización y compilación del proyecto en tu entorno local.

## Cambios Realizados

### 1. Soporte para Rutas con Caracteres Especiales
Se añadió la propiedad `android.overridePathCheck=true` en [gradle.properties](file:///D:/baku/aplicaciones%20android/urocálculo/gradle.properties).
Esto es necesario porque la carpeta del proyecto contiene caracteres como `á` y `ú`, lo que Windows suele bloquear por seguridad en las herramientas de Android.

### 2. Actualización de Versiones de Android SDK
En [build.gradle.kts](file:///D:/baku/aplicaciones%20android/urocálculo/app/build.gradle.kts):
- Se actualizó `compileSdk` y `targetSdk` a la versión **36**.
- Se corrigió un error donde `androidx.core:core-ktx:1.18.0` requería al menos el SDK 36.

### 3. Actualización de Gradle
Se actualizó la versión de Gradle de **9.3.0** a **9.3.1** en [gradle-wrapper.properties](file:///D:/baku/aplicaciones%20android/urocálculo/gradle/wrapper/gradle-wrapper.properties) para cumplir con los requisitos mínimos de los plugins actuales.

### 4. Corrección de Firma de Depuración (Signing)
Se eliminó la referencia a un archivo `debug.keystore` inexistente, permitiendo que Android Studio use su firma de depuración estándar automática.

## Verificación
> [!NOTE]
> Se ejecutó un `gradle_sync` y un `assembleDebug` exitosamente. El proyecto ahora está listo para ser ejecutado en un emulador o dispositivo real.

## Habilitación de Vistas Previas (Previews)
Se han añadido funciones de `@Preview` en:
- [UroScreens.kt](file:///D:/baku/aplicaciones%20android/urocálculo/app/src/main/java/com/example/ui/screens/UroScreens.kt): Ahora puedes ver la pantalla de IMC y el diseño de las tarjetas directamente.
- [MainActivity.kt](file:///D:/baku/aplicaciones%20android/urocálculo/app/src/main/java/com/example/MainActivity.kt): Se añadió una vista previa del Layout Principal.

### Cómo verlas:
1. Abre cualquiera de los archivos mencionados arriba.
2. En la parte superior derecha de la ventana del editor, verás tres iconos: **Code**, **Split** y **Design**.
3. Haz clic en **Split** o **Design**.
4. Es posible que debas hacer clic en "Build & Refresh" si Android Studio te lo pide.
