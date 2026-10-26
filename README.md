# Fidelia

App Android para guardar las tarjetas de fidelización de las tiendas y enseñar su
código de barras en caja. Sin cuentas, sin internet: todo se queda en el móvil.

## Qué hace

1. **Añadir tarjeta**: se elige la tienda de un listado (o «Mi tienda no está en la
   lista»), se le pone nombre, se puede cambiar la imagen (foto de la tarjeta o
   galería) y se escanea el código de barras con la cámara.
2. **Mostrar tarjetas**: rejilla con la imagen y el nombre de cada tarjeta. Al
   pulsar una se abre su código de barras en grande y con el brillo al máximo.
   Manteniendo pulsada una tarjeta se selecciona y se puede arrastrar para cambiarla
   de sitio; al soltarla se queda ahí y el orden se guarda.
3. **Editar** y **eliminar** (siempre pide confirmación) desde la lista (⋮) o desde
   la ficha de la tarjeta. Todas las pantallas tienen botón «Atrás».
4. **Copia y compartir** (pantalla de inicio): **Exportar** crea un archivo `.fidelia`
   (un zip con las tarjetas, sus imágenes y los códigos) que se comparte o se guarda en
   el móvil; **Importar** lo lee y añade las tarjetas. Si una tarjeta del archivo tiene
   el mismo nombre y el mismo código que una existente, se omite; si tiene el mismo
   nombre pero otro código, se añade como «Nombre (importada)». Importar dos veces el
   mismo archivo no duplica nada.

Si la cámara no lee el código, se puede escribir el número a mano o leerlo de una
imagen de la galería (por ejemplo, una captura de pantalla).

## Notas

- Los «logos» del listado de tiendas no son los oficiales: son tarjetas dibujadas
  con el nombre y un color parecido al de la marca. Para el logo real, cargar la
  propia imagen al crear o editar la tarjeta.
- El listado incluye las tiendas más habituales y los establecimientos del centro
  comercial Parque Principado (Oviedo), tomados de su web el 26-10-2026. Los colores
  de marca son aproximados; las tiendas sin color conocido usan un color automático.
  `StoresTest` comprueba que no haya ids repetidos y que la búsqueda ignore acentos.
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

Para probar en el móvil una copia aparte, con sus propios datos y sin tocar la app real:

```powershell
.\gradlew.bat assembleDebug '-PidSuffix=.pruebas'
```

Se instala como `com.fidelia.app.pruebas` junto a la app normal (desinstalarla al terminar).

## Pila

Kotlin + Jetpack Compose (Material 3), CameraX + ML Kit (lectura del código),
ZXing (dibujo del código), Coil (imágenes).
