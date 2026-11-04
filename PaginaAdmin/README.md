# EcoManifiesto – Web de administración (TrazaMx)

## Estructura
```
ecomanifiesto/
├── firebase.json        (configuración de Hosting y reglas)
├── .firebaserc          (proyecto: ecomanifiesto)
├── firestore.rules      (reglas de su base de datos)
├── README.md
└── public/
├── index.html            (estructura de la página y orden de scripts)
├── css/
│   └── styles.css        (estilos)
└── js/
    ├── state.js          (variables globales y datos de ejemplo)
    ├── utils.js          (toast, modal, QR, tarjetas)
    ├── views.js          (HTML de cada pantalla)
    ├── navegacion.js     (login, menú, búsqueda, notificaciones)
    ├── manifiestos.js    (crear, firmar, escanear QR)
    ├── transportistas.js (registro de transportistas)
    ├── firebase-config.js (sus llaves de Firebase)
    └── firebase-app.js   (conexión con Auth y Firestore)
```
