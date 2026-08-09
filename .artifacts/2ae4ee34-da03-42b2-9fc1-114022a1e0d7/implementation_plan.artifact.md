# Plan para Habilitar Vistas Previas (Previews) de la Interfaz

En Jetpack Compose (el sistema que usa tu app), la interfaz no se genera automáticamente en una vista previa como en los sistemas antiguos de XML. Es necesario crear funciones especiales marcadas con `@Preview` para que Android Studio sepa qué parte de la interfaz quieres visualizar mientras programas.

## User Review Required

> [!NOTE]
> Añadiré código de "vista previa" que no afecta al funcionamiento de la app en el teléfono, pero permite ver el diseño en el editor.
> Para ver estas vistas, deberás buscar el botón **"Split"** o **"Design"** en la esquina superior derecha del editor de código en Android Studio.

## Proposed Changes

### Pantallas de la UI

#### [MODIFY] [UroScreens.kt](file:///D:/baku/aplicaciones%20android/urocálculo/app/src/main/java/com/example/ui/screens/UroScreens.kt)
- Importar las herramientas de Preview.
- Añadir una función de vista previa para las tarjetas de los calculadores (`CalculatorCard`).
- Añadir una vista previa básica para la pantalla de IMC (Índice de Masa Corporal).

## Verification Plan

### Manual Verification
- Una vez aplicados los cambios, abrir [UroScreens.kt](file:///D:/baku/aplicaciones%20android/urocálculo/app/src/main/java/com/example/ui/screens/UroScreens.kt) y verificar que aparezca el panel de **"Design"** a la derecha con las imágenes de la interfaz.
