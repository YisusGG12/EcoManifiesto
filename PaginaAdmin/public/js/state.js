/* ESTADO: variables globales y datos de ejemplo */

// Atajo para document.querySelector
/* Lógica de la interfaz: navegación, vistas, modales (datos de ejemplo hasta que firebase-app.js los reemplaza) */
const $ = s => document.querySelector(s);

// Opciones del menú lateral [id, etiqueta]
const NAV = [["dash", "▦ Dashboard"], ["man", "▤ Manifiestos"], ["tra", "🚚 Transportistas y Vehículos"], ["coa", "🗂 Expediente Anual COA"], ["usr", "👥 Usuarios"], ["bit", "📜 Bitácora y Auditoría"], ["cfg", "⚙ Configuración"]];

// Estado de la interfaz: pantalla actual, búsqueda, filtro, notificaciones y consecutivo local
let page = "dash", q = "", flt = "Todos", ns = [], cn = 1;

// Datos de ejemplo (se reemplazan con Firestore al iniciar sesión): transportistas
let T = [{ n: "Transportes Químicos del Norte S.A. de C.V.", rfc: "TQN-140523-9T4", v: "Tractocamión Kenworth T680", pl: "45-AF-9X", a: "SEMARNAT-09-028-PS-I-2024", f: "15 Mar 2027", s: "VIGENTE" }, { n: "Fluidos y Residuos Especiales del Centro", rfc: "FRE-080315-P21", v: "Pipa Volvo VNL 760", pl: "91-EZ-3L", a: "SEMARNAT-15-099-PS-I-2021", f: "20 Nov 2026", s: "POR_VENCER" }, { n: "Auto-Líquidos de Occidente S.A.", rfc: "ALO-110202-V19", v: "Torton International DuraStar", pl: "54-DX-7K", a: "SEMARNAT-14-118-PS-I-2019 (REVOCADO)", f: "03 Ene 2025", s: "VENCIDO" }, { n: "EcoLimpieza y Logística S.A.", rfc: "ELL-120610-QA3", v: "Tracto TR-09", pl: "88-AF-3X", a: "SEMARNAT-15-IV-02", f: "12 Sep 2027", s: "VIGENTE" }];

// Datos de ejemplo: manifiestos
let M = [{ f: "EM-2026-0348", d: "18 Abr 2026 09:30", r: "Aceite lubricante usado", c: "I-T", q: "4,200 L", t: "EcoLimpieza y Logística S.A.", x: "Termodestructora Monterrey", s: "ENTREGADO" }, { f: "EM-2026-0346", d: "19 Abr 2026 06:10", r: "Solventes gastados (tolueno)", c: "I-R", q: "1,100 L", t: "Transportes Químicos del Norte S.A. de C.V.", x: "Recuperadora Ecológica Querétaro", s: "EN_TRANSITO" }, { f: "EM-2026-0344", d: "19 Abr 2026 11:00", r: "Residuos de pintura y solventes", c: "I-T", q: "560 kg", t: "EcoLimpieza y Logística S.A.", x: "Recuperadora Ecológica Querétaro", s: "CREADO" }];

// Datos de ejemplo: bitácora
let L = [["15/02/2026 14:32:08", "MAN-2026-00482", "Retiro y firma de chofer", "Ing. Jorge Soto · Supervisor de Planta"], ["15/02/2026 11:15:42", "MAN-2026-00478", "Validación báscula destino final", "Lic. Patricia Lozano · Destino Final"], ["14/02/2026 17:50:11", "BIT-2026-01194", "Almacenamiento temporal inicial", "Ing. Roberto Vega · Responsable Técnico"], ["14/02/2026 09:20:33", "MAN-2026-00465", "Emisión de acuse digital certificado", "Dra. Marina Delgado · Perito Ambiental"]];

// Datos de ejemplo: usuarios
let U = [["RV", "Ing. Roberto Vega", "r.vega@ecomanifiesto.mx", "Administrador del Sistema", "Sede Corporativa Bajío", "Firma SHA-256 activa", "Hace 12 min"], ["JS", "Ing. Jorge Soto", "j.soto@quimicabajio.com", "Generador de Residuos", "Planta Celaya #04", "Emisión QR y manifiestos", "Hoy, 10:20"], ["PL", "Lic. Patricia Lozano", "p.lozano@rimsa.com.mx", "Destino Final (Receptor)", "Planta RIMSA Mina N.L.", "Cierre y recepción final", "Ayer, 18:45"], ["CR", "Carlos Ruiz Miranda", "c.ruiz@ecolimpieza.mx", "Operador Transportista", "EcoLimpieza y Logística", "Biometría en app móvil", "Hoy, 08:15"], ["MD", "Dra. Marina Delgado", "m.delgado@auditoria-fed.gob.mx", "Auditor Externo / PROFEPA", "Despacho Pericial", "Solo lectura de bitácoras", "Hace 3 días"]];
