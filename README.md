# Fidelia

App Android para guardar las tarjetas de fidelización de las tiendas y las tarjetas
de visita de profesionales y empresas. Sin cuentas, sin internet: todo se queda en el móvil.

## Pantallas

- **Principal**: cabecera con el nombre y, arriba a la derecha, una rueda dentada que
  lleva a la **Configuración**; dos botones, **Tarjetas de fidelización** y **Tarjetas
  de visita**; y debajo **Copia y compartir** (Exportar / Importar).
- **Configuración**: elegir la combinación de colores de la app.

### Tarjetas de fidelización

Un menú con **Ver tarjetas** y **Añadir tarjeta**.

1. **Añadir**: se elige la tienda de un listado (o «Mi tienda no está en la lista»), se le
   pone nombre, se puede cambiar la imagen (foto de la tarjeta, galería o **uno de los
   logos de la lista**, también al editar) y se escanea el código de barras con la cámara.
   Si la cámara no lo lee, se puede escribir el número o leerlo de una imagen (por ejemplo,
   una captura de pantalla).
2. **Ver**: rejilla con la imagen y el nombre de cada tarjeta. Al pulsar una se abre su
   código de barras en grande y con el brillo al máximo. Manteniendo pulsada una tarjeta
   se selecciona y se puede arrastrar para cambiarla de sitio; al soltarla se queda ahí
   y el orden se guarda.
3. **Editar** y **eliminar** (siempre pide confirmación) desde la lista (⋮) o desde la ficha.

### Tarjetas de visita

Un menú con **Ver tarjetas de visita** y **Añadir tarjeta de visita**.

1. **Añadir**: un **icono de oficio** de la lista (Emergencias, Médico, Seguros,
   Fontanero, Electricista…) o una **foto de la tarjeta**; y la empresa, el titular, hasta
   **dos teléfonos** y un **correo** (todo opcional salvo empresa o titular). Cada teléfono
   se puede **escribir** o **elegir de los contactos del móvil** con el icono de contactos
   del campo: el selector de contactos del sistema entrega solo el número elegido, así que
   la app no pide permiso para leer la agenda.
2. **Ver**: igual que las de fidelización, y también se pueden reordenar arrastrando.
3. **Ficha**: pulsar un teléfono pide confirmación («¿Llamar ahora?») y, si se acepta,
   llama directamente (la primera vez Android pide el permiso de llamadas; si no se da,
   se abre el marcador con el número ya escrito). Pulsar el correo abre el correo con un
   mensaje nuevo para esa dirección.

### Copia y compartir

**Exportar** crea un archivo `.fidelia` (un zip con las tarjetas de los dos tipos, sus
imágenes, códigos y teléfonos) que se comparte o se guarda en el móvil. **Importar** lo lee
y añade las tarjetas, con estas reglas:

- mismo nombre y mismos datos que una existente → se omite;
- mismo nombre pero otro código de barras (fidelización) u otros teléfonos (visita) →
  se añade como «Nombre (importada)»;
- importar dos veces el mismo archivo no duplica nada.

### Todas las pantallas tienen botón «Atrás»

Arriba solo queda «Atrás». El título y el contenido van debajo; en los menús y la
configuración se centran en la pantalla. Los campos de texto llevan la descripción y lo
escrito centrados.

## Notas

- Los «logos» del listado de tiendas no son los oficiales: son tarjetas dibujadas con el
  nombre y un color parecido al de la marca. Para el logo real, cargar la propia imagen.
- El listado incluye las tiendas más habituales y los establecimientos del centro
  comercial Parque Principado (Oviedo), tomados de su web el 26-10-2026. Los colores de
  marca son aproximados; las tiendas sin color conocido usan un color automático.
- Las combinaciones de color salen de `assets/web/assets/css/style.css` de IntCom (bloques
  `body[data-theme=...]`). En Material un solo color «primario» sirve de fondo de botón y de
  texto, así que de los tonos de cada combinación (`primary`, `primary-ink`, `line`) se usa
  el que mejor contraste tiene con su fondo.
- El nombre «Fidelia» de la cabecera usa la tipografía **Inter** (SIL OFL 1.1, Rasmus
  Andersson), tomada de los ficheros que trae Android Studio.
- El estetoscopio del oficio «Médico» está dibujado a mano (la biblioteca de iconos no
  trae ninguno).
- Datos en el almacenamiento privado de la app: `cards.json` y `business_cards.json` (con
  copia `.bak`) e imágenes en `images/`. Se escribe con fichero temporal + renombrado para
  que un corte no deje el fichero a medias.
- Permisos: cámara (leer códigos y hacer fotos) y llamadas (solo al confirmar una llamada).
- Solo se compila para `arm64-v8a`.

## Tests

`gradlew.bat :app:testDebugUnitTest`: reglas de duplicados de los dos tipos de tarjeta,
formato del archivo de copia, teléfonos y correos, listado de tiendas (ids únicos, búsqueda
sin acentos), oficios y combinaciones de color (que todas se lean bien).

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

Se instala como `com.fidelia.app.pruebas` («Fidelia PRUEBAS») junto a la app normal
(desinstalarla al terminar).

## Pila

Kotlin + Jetpack Compose (Material 3), CameraX + ML Kit (lectura del código),
ZXing (dibujo del código), Coil (imágenes).
