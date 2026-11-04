/* CONEXIÓN CON FIREBASE: Auth + Firestore. Se carga al final y sobrescribe las funciones de ejemplo de los otros archivos. */

/* =================================================================================================================== */
const ok = FB.apiKey.startsWith("AIza");

let db, au, me = null, CAT = { r: [], d: [] }, uns = [], first = 1;

if (ok) {
    firebase.initializeApp(FB);
    db = firebase.firestore();
    au = firebase.auth();
}

// Datos de ejemplo: manifiestos
M = [];

// Datos de ejemplo (se reemplazan con Firestore al iniciar sesión): transportistas
T = [];

// Datos de ejemplo: bitácora
L = [];

// Datos de ejemplo: usuarios
U = [];

$("#pw").value = "";

const fmt = ms => ms ? new Date(ms).toLocaleString("es-MX", { dateStyle: "medium", timeStyle: "short" }) : "";

const sha = async (s) => [...new Uint8Array(await crypto.subtle.digest("SHA-256", new TextEncoder().encode(s)))].map(b => b.toString(16).padStart(2, "0")).join("");

const tState = t => { if (t.activo === false)
    return "VENCIDO"; if (t.vigencia) {
    const d = (t.vigencia - Date.now()) / 864e5;
    return d < 0 ? "VENCIDO" : d < 30 ? "POR_VENCER" : "VIGENTE";
} return "VIGENTE"; };

const err = e => toast("Firestore: " + e.message);

const oq = qr;

// Genera un código QR (versión de ejemplo; firebase-app.js la reemplaza por un QR real)
qr = seed => { try {
    const q = qrcode(0, "M");
    q.addData(seed);
    q.make();
    return `<div style="background:#fff;padding:6px;border-radius:8px">${q.createSvgTag({ cellSize: 4, margin: 1, scalable: true })}</div>`;
}
catch (e) {
    return oq(seed);
} };

// Inicio de sesión
doLogin = async () => {
    const e = $("#em").value.trim(), p = $("#pw").value;
    if (!ok) {
        $("#le").textContent = "Falta pegar apiKey y appId de Firebase en el archivo (bloque CONFIGURACIÓN FIREBASE).";
        return;
    }
    if (!e || !p) {
        $("#le").textContent = "Escriba correo y contraseña.";
        return;
    }
    try {
        const c = await au.signInWithEmailAndPassword(e, p), s = await db.collection("users").doc(c.user.uid).get();
        if (!s.exists) {
            $("#le").textContent = "Su usuario no tiene perfil en la colección users.";
            await au.signOut();
            return;
        }
        me = { uid: c.user.uid, ...s.data() };
        $("#login").style.display = "none";
        $("#app").classList.add("on");
        $(".top div b").textContent = me.nombre || me.email;
        $(".top div div").textContent = (me.rol || "").toUpperCase();
        subs();
        go("dash");
        toast("Sesión iniciada · rol: " + me.rol);
    }
    catch (x) {
        $("#le").textContent = "No se pudo ingresar: " + x.message;
    }
};

const oo = out;

// Cerrar sesión
out = () => { uns.forEach(f => f()); uns = []; me = null; if (au)
    au.signOut(); oo(); };

const oc = close_;

// Cierra la ventana modal
close_ = () => { oc(); if (me)
    go(page); };

function subs() {
    uns.forEach(f => f());
    uns = [];
    first = 1;
    const rf = () => { if (me && !$(".md.on"))
        go(page); }, c = n => db.collection(n);
    uns.push(c("transportistas").onSnapshot(s => { T = s.docs.map(d => { const t = d.data(); return { raw: t, n: t.empresa, rfc: t.conductor || "—", v: (t.vehiculo || "") + " · " + (t.tipoVehiculo || ""), pl: t.placas || "—", a: (t.autorizacion || "—") + " / " + (t.permisoSct || "—"), f: t.vigencia ? new Date(t.vigencia).toLocaleDateString("es-MX", { dateStyle: "medium" }) : "Sin fecha", s: tState(t) }; }); rf(); }, err));
    uns.push(c("residuos").onSnapshot(s => { CAT.r = s.docs.map(d => d.data()).filter(x => x.activo !== false); }, err));
    uns.push(c("destinatarios").onSnapshot(s => { CAT.d = s.docs.map(d => d.data()).filter(x => x.activo !== false).sort((a, b) => (a.empresa || "").localeCompare(b.empresa || "")); }, err));
    uns.push(c("users").onSnapshot(s => { U = s.docs.map(d => { const u = d.data(); return [(u.nombre || u.email || "?").slice(0, 2).toUpperCase(), u.nombre || "—", u.email || "", u.rol || "", u.empresa || u.cargo || "—", u.registro || "—", "—"]; }); rf(); }, () => { }));
    uns.push(c("manifiestos").orderBy("creadoEn", "desc").onSnapshot(s => {
        s.docChanges().forEach(ch => { const m = ch.doc.data(); if (!first && ch.type == "modified" && m.estado == "ENTREGADO")
            notify(`Entrega cerrada: el manifiesto #${m.folio} fue ENTREGADO con firmas con fecha, hora y usuario.`); });
        M = s.docs.map(d => { const m = d.data(); return { raw: m, f: m.folio || d.id, d: fmt(m.creadoEn), r: m.residuo, c: (m.cretib || []).join("-"), q: m.cantidad + " " + (m.unidad || ""), t: m.transportista || "—", x: m.destinatario || "—", s: m.estado }; });
        L = [];
        M.forEach(x => { const m = x.raw; L.push([m.creadoEn, x.f, "Manifiesto creado (CREADO)", m.generadorFirmante || ""]); if (m.enTransitoEn)
            L.push([m.enTransitoEn, x.f, "Retiro y firma de transportista (EN_TRANSITO)", m.transportistaFirmante || ""]); if (m.entregadoEn)
            L.push([m.entregadoEn, x.f, "Recepción y firma de recibido (ENTREGADO)", (m.receptorNombre || "") + " · " + (m.receptorCargo || "")]); });
        L.sort((a, b) => b[0] - a[0]);
        L = L.map(l => [fmt(l[0]), ...l.slice(1)]);
        first = 0;
        rf();
    }, err));
}

// RF-02: formulario de nuevo manifiesto
newM = () => {
    if (!CAT.r.length || !T.length || !CAT.d.length) {
        toast("Faltan datos en Firestore: residuos, transportistas o destinatarios.");
        return;
    }
    modal(`<h3 style="margin-top:0">Nuevo manifiesto</h3><label>Tipo de residuo</label><select id="nr">${CAT.r.map((r, i) => `<option value="${i}">${r.nombre} (${(r.cretib || []).join("")})</option>`).join("")}</select><label>Cantidad</label><div style="display:flex;gap:8px"><input id="nq" type="number" min="1" placeholder="Ej. 500"><select id="nu" style="width:90px"><option>L</option><option>kg</option></select></div><label>Transportista y vehículo</label><select id="nt" onchange="chkT()">${T.map((t, i) => `<option value="${i}">${t.n} · ${t.pl} (${t.s.replace("_", " ")})</option>`).join("")}</select><div id="ne"></div><label>Destino final</label><select id="nx">${CAT.d.map((d, i) => `<option value="${i}">${d.empresa}</option>`).join("")}</select><div class="err" id="me"></div><div style="display:flex;gap:10px;margin-top:20px"><button onclick="close_()">Cancelar</button><button class="p" id="nb" style="flex:1" onclick="saveM()">Guardar y generar QR</button></div>`);
    chkT();
};

// RF-02: guarda el manifiesto (estado CREADO)
saveM = async () => {
    const r = CAT.r[$("#nr").value], t = T[$("#nt").value].raw, d = CAT.d[$("#nx").value], n = +$("#nq").value;
    if (!n || n <= 0) {
        $("#me").textContent = "Escriba una cantidad mayor a cero.";
        return;
    }
    $("#nb").disabled = true;
    try {
        const ref = db.collection("counters").doc("manifiestos");
        let folio;
        await db.runTransaction(async (tx) => { const s = await tx.get(ref), k = (s.exists ? s.data().n : 0) + 1; tx.set(ref, { n: k }); folio = "EM-" + new Date().getFullYear() + "-" + String(k).padStart(4, "0"); });
        const ts = Date.now(), u = $("#nu").value;
        await db.collection("manifiestos").doc(folio).set({ folio, estado: "CREADO", registroAmbiental: me.registro || "", generador: me.empresa || "", instalacion: me.instalacion || "", generadorDomicilio: me.direccion || "", residuo: r.nombre, residuoDetalle: r.detalle || "", claveResiduo: r.clave || "", cretib: r.cretib || [], envase: r.envase || "", capacidadEnvase: r.capacidad || "", cantidad: n, unidad: u, etiquetado: true, instrucciones: r.instrucciones || "", generadorFirmante: me.nombre || "", creadoEn: ts, hash: await sha([folio, r.nombre, n, ts].join("|")), transportista: t.empresa, autorizacionTransportista: t.autorizacion || "", permisoSct: t.permisoSct || "", tipoVehiculo: t.tipoVehiculo || "", vehiculo: t.vehiculo || "", placas: t.placas || "", conductor: t.conductor || "", destinatario: d.empresa, destinoDomicilio: d.domicilio || "", autorizacionDestinatario: d.autorizacion || "", generadorUid: me.uid });
        close_();
        setTimeout(() => showQR(folio, 1), 250);
    }
    catch (x) {
        $("#me").textContent = "Error: " + x.message;
        $("#nb").disabled = false;
    }
};

// Ventana con recuadro para dibujar la firma
sig = (title, f, cb) => {
    modal(`<h3 style="margin-top:0">${title}</h3><p class="mono"><b>#${f}</b></p><p class="sub">Dibuje su firma:</p><canvas id="cv" width="420" height="140"></canvas><div class="err" id="se"></div><div style="display:flex;gap:10px;margin-top:16px"><button onclick="close_()">Cancelar</button><button onclick="clr()">Borrar</button><button class="p" style="flex:1" id="sb">Confirmar firma</button></div>`);
    const c = $("#cv"), x = c.getContext("2d");
    x.lineWidth = 2.5;
    x.lineCap = "round";
    x.strokeStyle = "#0f4a26";
    let d = 0, S = [];
    const p = e => { const r = c.getBoundingClientRect(); return [(e.clientX - r.left) * c.width / r.width, (e.clientY - r.top) * c.height / r.height]; };
    c.onpointerdown = e => { d = 1; const q = p(e); S.push([q]); x.beginPath(); x.moveTo(...q); };
    c.onpointermove = e => { if (!d)
        return; const q = p(e); S[S.length - 1].push(q); x.lineTo(...q); x.stroke(); };
    c.onpointerup = () => d = 0;
    window.clr = () => { x.clearRect(0, 0, c.width, c.height); S = []; };
    $("#sb").onclick = async () => {
        if (S.reduce((a, s) => a + s.length, 0) < 8) {
            $("#se").textContent = "Falta la firma: dibújela en el recuadro.";
            return;
        }
        $("#sb").disabled = true;
        await cb(S.map(s => s.map(q => (q[0] / c.width).toFixed(3) + "," + (q[1] / c.height).toFixed(3)).join(";")).join("|"));
    };
};

const wr = async (f, o, okm) => { try {
    await db.collection("manifiestos").doc(f).update(o);
    oc();
    toast(okm);
}
catch (x) {
    $("#se").textContent = x.message;
    $("#sb").disabled = false;
} };

// RF-04: firma de custodia (EN_TRANSITO)
custody = f => sig("Firma de custodia (transportista)", f, s => wr(f, { estado: "EN_TRANSITO", firmaTransportista: s, transportistaFirmante: me.nombre || "", enTransitoEn: Date.now() }, "Custodia firmada · estado EN_TRANSITO"));

// RF-05: escáner de QR del destino final
scan = f => modal(`<h3 style="margin-top:0">Escáner de QR (destino final)</h3><div class="scan">Apunte la cámara al código QR…</div><button class="p" style="width:100%;margin-top:16px" onclick="sig('Firma de recibido','${f}',s=>recv('${f}',s))">Simular lectura del QR #${f}</button><button style="width:100%;margin-top:10px" onclick="close_()">Cancelar</button>`);

// RF-05: firma de recibido (ENTREGADO)
recv = async (f, s) => { const ts = Date.now(); wr(f, { estado: "ENTREGADO", firmaReceptor: s, receptorNombre: me.nombre || "", receptorCargo: me.cargo || "", observaciones: "", entregadoEn: ts, hashEntrega: await sha([f, s, ts].join("|")) }, "Entrega firmada · ENTREGADO"); };

// RF-07: guarda el transportista
saveT = async () => {
    const n = $("#tn").value, f = $("#tf").value;
    if (!n || !f || !$("#tp").value) {
        $("#te").textContent = "Complete empresa, placas y fecha de vigencia.";
        return;
    }
    try {
        await db.collection("transportistas").add({ empresa: n, conductor: $("#tr").value, vehiculo: $("#tv").value, tipoVehiculo: "", placas: $("#tp").value, autorizacion: $("#ta").value, permisoSct: $("#ts2").value, vigencia: new Date(f).getTime(), activo: true });
        close_();
        toast("Transportista registrado");
    }
    catch (x) {
        $("#te").textContent = x.code == "permission-denied" ? "Solo el administrador puede registrar transportistas." : x.message;
    }
};
