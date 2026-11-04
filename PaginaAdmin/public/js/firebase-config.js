/* CONFIGURACIÓN DE FIREBASE (Consola Firebase > Configuración del proyecto > Tus apps > Web).
   La apiKey web no es secreta: la seguridad la dan las reglas de Firestore (firestore.rules). */

/* Conexión con Firebase (Auth + Firestore). Se carga DESPUÉS de app.js y sobrescribe sus funciones de datos. */
/* ====== CONFIGURACIÓN FIREBASE: pegue aquí los datos de Consola Firebase > Configuración del proyecto > Tus apps > Web ====== */
const FB = { apiKey: "AIzaSyCUXj3d5YSx4KmLgzT0dHQ7ThStMz8NE-w", authDomain: "ecomanifiesto.firebaseapp.com", projectId: "ecomanifiesto", storageBucket: "ecomanifiesto.firebasestorage.app", messagingSenderId: "1074226436855", appId: "1:1074226436855:web:71259e4b66f72ccbe4d1f2" };
