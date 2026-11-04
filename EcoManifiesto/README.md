# EcoManifiesto

Equipo **TrazaMx** · Minihackatón 2026 · Ingeniería en TIID, grupo 4-B

| Integrante | Matrícula |
|---|---|
| Alberto Carlos Martienz Roa | 25022100014 |
| Jesus Adrian Barron Celio | 25022100026 |

Contacto: jesusbarroncelio21@gmail.com

---

## 1. De qué trata el proyecto

En México los residuos peligrosos tienen que poder rastrearse desde la empresa que los genera hasta donde se tratan o se disponen. Hoy eso se hace con manifiestos en papel que se pierden, se llenan mal o nunca regresan al generador.

EcoManifiesto es una app móvil Android que digitaliza ese manifiesto:

1. El **generador** crea el manifiesto y se genera un folio con su código QR.
2. El **transportista** revisa el manifiesto y firma con el dedo para recibir la custodia (estado `EN_TRANSITO`).
3. El **destino final** escanea el QR, firma de recibido y el manifiesto queda `ENTREGADO`.
4. Cualquier manifiesto se puede exportar a PDF con la estructura del formato oficial de SEMARNAT (19 campos).

El **administrador** tiene una pantalla para entrar al portal web desde la computadora (QR con un token que dura 5 minutos).

## 2. Qué trae el paquete

| Carpeta / archivo | Qué es |
|---|---|
| `EcoManifiesto-debug.apk` | App ya compilada, lista para instalar en un celular Android |
| `app/` | Código fuente de la app (Kotlin + Jetpack Compose) |
| `firestore.rules` | Reglas de seguridad de la base de datos |
| `DATA_MODEL.md` | Colecciones y campos de Firestore |
| `index.html`, `app.js`, `styles.css` | Prototipo del portal web del administrador |

## 3. Qué se necesita para probarlo

Hay tres formas, de la más rápida a la más completa.

### Opción A: instalar el APK (la más rápida, ~5 minutos)

- Un celular Android 8.0 o superior.
- Conexión a internet (la app usa Firebase).
- Pasar `EcoManifiesto-debug.apk` al celular e instalarlo (hay que permitir "instalar apps de orígenes desconocidos").
- Para escanear el QR conviene un celular físico; en emulador se puede usar el botón **Folio Manual**.

### Opción B: abrir el código en Android Studio

- Android Studio Ladybug o más nuevo.
- JDK 17 (el que trae Android Studio sirve) y Android SDK 35.
- Conexión a internet para que Gradle descargue las dependencias.
- Abrir la carpeta `EcoManifiesto` y esperar a que sincronice Gradle.
- El archivo `app/google-services.json` ya viene incluido y apunta a nuestro proyecto de Firebase (`ecomanifiesto`), así que no hay que configurar nada.
- Correr en un dispositivo o emulador con API 26 o superior.

### Opción C: ver el portal web

Abrir `index.html` en cualquier navegador. Necesita internet porque carga Tailwind y Chart.js desde CDN. Es un prototipo visual con datos de ejemplo (no está conectado a Firebase).

## 4. Cuentas de prueba

La app pide correo, contraseña y que se escoja el rol en la pantalla de inicio. El rol que se escoge tiene que ser el de la cuenta, si no la app lo rechaza.

| Rol | Correo | Contraseña |
|---|---|---|
| Generador | generador@test.com | 123890 |
| Transportista | chofer@test.com | 123890 |
| Destino Final | destino@test.com | 123890 |
| Administrador | admin@test.com | 123890 |

## 5. Recorrido sugerido para la demo (unos 5 minutos)

El orden importa porque cada rol trabaja sobre el estado que dejó el anterior. Para cambiar de usuario se toca el avatar de arriba a la derecha y se elige **Cerrar sesión**.

**Paso 1. Generador**
1. Entrar con el rol *Generador*.
2. Elegir el tipo de residuo, la cantidad (máximo 1,200 L) y el transportista (el botón de las flechas cambia al siguiente).
3. Tocar **Generar Manifiesto y Código QR**. Aparece el folio (`EM-AAAA-0042` en adelante), el sello y el QR.
4. Probar **Imprimir PDF** para ver el manifiesto con el formato oficial.
5. Probar con más de 1,200 litros: debe marcar error.

**Paso 2. Transportista**
1. Cerrar sesión y entrar con el rol *Transportista*.
2. Aparece el manifiesto más reciente que está en estado `CREADO`.
3. Dibujar la firma en el recuadro y tocar **Firmar Custodia**. El botón no se activa hasta que haya firma.
4. La pantalla de confirmación muestra el avance de la ruta y el estado `EN_TRANSITO`.

**Paso 3. Destino final**
1. Cerrar sesión y entrar con el rol *Destino Final*.
2. Escanear el QR del manifiesto (se puede mostrar en otra pantalla) o usar **Folio Manual** y escribir el folio.
3. Si el manifiesto todavía no tiene firma de custodia, la app avisa y no deja recibirlo. Si ya se entregó, también avisa.
4. Firmar y tocar **Confirmar Recepción**. El estado pasa a `ENTREGADO`.
5. **Descargar Acuse Certificado PDF** genera el PDF ya con las firmas y los sellos.

**Paso 4. Administrador**
1. Entrar con el rol *Administrador*.
2. Se muestra el QR de acceso a PC con cuenta regresiva de 5 minutos. Cuando llega a cero se genera uno nuevo.
3. **Cambiar a Chofer** deja al admin probar el flujo del transportista sin cerrar sesión.



## 6. Limitaciones conocidas

- Los catálogos (residuos, transportistas, destino) y los números de autorización son **datos de ejemplo**, no reales.
- El sello SHA-256 es una huella de integridad, **no** es una firma electrónica con validez legal (NOM-151).
- Las reglas de Firestore para `manifiestos` son de desarrollo: cualquier usuario con sesión puede leerlos y editarlos. Antes de usarlo en serio hay que restringir por rol y por empresa.
- El rol se guarda la primera vez que alguien inicia sesión; en producción debería asignarlo solo el administrador.
- La evidencia fotográfica y las notificaciones son solo visuales por ahora.
- El enlace de acceso a PC ya genera su token en Firestore, pero el portal web todavía no lo valida.
- El portal web es un prototipo con datos fijos.

## 7. Tecnologías

- Android nativo con Kotlin y Jetpack Compose (Material 3)
- Firebase Authentication y Cloud Firestore
- CameraX y ML Kit para escanear QR, ZXing para generarlos
- `PdfDocument` de Android para el PDF
- Portal web: HTML, Tailwind (CDN) y Chart.js

Nota: en la documentación técnica del 29 de octubre se planteó React Native con Node y PostgreSQL. La versión que se entrega para evaluar es la de Android nativo con Firebase que se describe en este archivo.


## 12. Link de la pagina del Administrador
Ya no alcanzamos a hacer el hosting pipi


