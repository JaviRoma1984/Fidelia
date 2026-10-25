# Fidelia

App Android para guardar las tarjetas de fidelización de las tiendas y enseñar su
código de barras en caja. Sin cuentas, sin internet: todo se queda en el móvil.

## Qué hace

1. **Añadir tarjeta**: se elige la tienda de un listado (o «Mi tienda no está en la
   lista»), se le pone nombre, se puede cambiar la imagen (foto de la tarjeta o
   galería) y se escanea el código de barras con la cámara.
2. **Mostrar tarjetas**: rejilla con la imagen y el nombre de cada tarjeta. Al
   pulsar una se abre su código de barras en grande y con el brillo al máximo.
3. **Editar** y **eliminar** (siempre pide confirmación) desde la lista (⋮) o desde
   la ficha de la tarjeta. Todas las pantallas tienen botón «Atrás».

Si la cámara no lee el código, se puede escribir el número a mano o leerlo de una
imagen de la galería (por ejemplo, una captura de pantalla).

## Notas

- Los «logos» del listado de tiendas no son los oficiales: son tarjetas dibujadas
  con el nombre y un color parecido al de la marca. Para el logo real, cargar la
  propia imagen al crear o editar la tarjeta.
- Datos en el almacenamiento privado de la app: `cards.json` (con copia `.bak`) e
  imágenes en `images/`. Se escribe con fichero temporal + renombrado para que un
  corte no deje el fichero a medias.
- Solo se compila para `arm64-v8a`.

## Compilar e instalar por cable

`JAVA_HOME` del sistema es JDK 11 y Gradle 9.3 lo rechaza; hay que forzar JDK 23:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
.\gradlew.bat assembleDebug
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```

## Pila

Kotlin + Jetpack Compose (Material 3), CameraX + ML Kit (lectura del código),
ZXing (dibujo del código), Coil (imágenes).
