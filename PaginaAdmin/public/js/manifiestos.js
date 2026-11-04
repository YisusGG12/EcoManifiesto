/* MANIFIESTOS: crear, firma de custodia, escáner QR y firma de recibido (RF-02 a RF-06) */

// RF-02: formulario de nuevo manifiesto
function newM() { modal(`<h3 style="margin-top:0">Nuevo manifiesto</h3><label>Tipo de residuo</label><select id="nr"><option>Aceite lubricante usado|I-T</option><option>Solventes gastados|I-R</option><option>Lodos de tratamiento químico|C-T</option><option>Baterías ácido-plomo|C-T</option></select><label>Cantidad</label><div style="display:flex;gap:8px"><input id="nq" type="number" min="1" placeholder="Ej. 500"><select id="nu" style="width:90px"><option>L</option><option>kg</option></select></div><label>Transportista y vehículo</label><select id="nt" onchange="chkT()">${T.map((t, i) => `<option value="${i}">${t.n} · ${t.pl} (${t.s.replace("_", " ")})</option>`).join("")}</select><div id="ne"></div><label>Destino final</label><select id="nx"><option>Recuperadora Ecológica Querétaro</option><option>Termodestructora Monterrey</option><option>Confinamiento Mina Verde</option></select><div class="err" id="me"></div><div style="display:flex;gap:10px;margin-top:20px"><button onclick="close_()">Cancelar</button><button class="p" id="nb" style="flex:1" onclick="saveM()">Guardar y generar QR</button></div>`); chkT(); }

// RF-03: valida la vigencia del transportista elegido
function chkT() { const t = T[$("#nt").value], e = $("#ne"); e.innerHTML = t.s == "VENCIDO" ? "<div class='err'>⛔ Autorización vencida: el retiro está bloqueado. Elija otro transportista.</div>" : t.s == "POR_VENCER" ? "<div class='err' style='color:#8a5a00'>⚠ La autorización vence pronto (permitido).</div>" : "<div style='color:var(--g);font-size:14px;margin-top:8px'>✔ Autorización vigente</div>"; $("#nb").disabled = t.s == "VENCIDO"; }

// RF-02: guarda el manifiesto (estado CREADO)
function saveM() { const [r, c] = $("#nr").value.split("|"), n = $("#nq").value; if (!n || n <= 0) {
    $("#me").textContent = "Escriba una cantidad mayor a cero.";
    return;
} const f = "EM-2026-0" + (349 + cn++); M.unshift({ f, d: new Date().toLocaleString("es-MX", { dateStyle: "medium", timeStyle: "short" }), r, c, q: n + " " + $("#nu").value, t: T[$("#nt").value].n, x: $("#nx").value, s: "CREADO" }); log(f, "Manifiesto creado (CREADO)", "Ing. Jorge Soto · Generador"); go("man"); showQR(f, 1); }

// Muestra el QR del manifiesto
function showQR(f, nw) { const m = M.find(x => x.f == f); modal(`<h3 style="margin-top:0">${nw ? "✅ Manifiesto creado" : "Código QR"}</h3><p class="mono"><b>#${f}</b></p><div style="width:210px;margin:auto">${qr(f)}</div><p style="text-align:center">${st(m.s)}</p><p class="sub" style="text-align:center">${m.r} · ${m.q}</p><button class="p" style="width:100%" onclick="close_()">Listo</button>`); }

// Ventana con recuadro para dibujar la firma
function sig(title, f, cb) {
    modal(`<h3 style="margin-top:0">${title}</h3><p class="mono"><b>#${f}</b></p><p class="sub">Dibuje su firma:</p><canvas id="cv" width="420" height="140"></canvas><div class="err" id="se"></div><div style="display:flex;gap:10px;margin-top:16px"><button onclick="close_()">Cancelar</button><button onclick="clr()">Borrar</button><button class="p" style="flex:1" id="sb">Confirmar firma</button></div>`);
    const c = $("#cv"), x = c.getContext("2d");
    x.lineWidth = 2.5;
    x.lineCap = "round";
    x.strokeStyle = "#0f4a26";
    let d = 0, ink = 0;
    const p = e => { const r = c.getBoundingClientRect(); return [(e.clientX - r.left) * c.width / r.width, (e.clientY - r.top) * c.height / r.height]; };
    c.onpointerdown = e => { d = 1; const [a, b] = p(e); x.beginPath(); x.moveTo(a, b); };
    c.onpointermove = e => { if (!d)
        return; const [a, b] = p(e); x.lineTo(a, b); x.stroke(); ink++; };
    c.onpointerup = () => d = 0;
    window.clr = () => { x.clearRect(0, 0, c.width, c.height); ink = 0; };
    $("#sb").onclick = () => { if (ink < 8) {
        $("#se").textContent = "Falta la firma: dibújela en el recuadro.";
        return;
    } cb(); };
}

// RF-04: firma de custodia (EN_TRANSITO)
function custody(f) { sig("Firma de custodia (transportista)", f, () => { M.find(m => m.f == f).s = "EN_TRANSITO"; log(f, "Retiro y firma de chofer (EN_TRANSITO)", "Carlos Ruiz Miranda · Transportista"); close_(); go("man"); toast("Custodia firmada · estado EN_TRANSITO"); }); }

// RF-05: escáner de QR del destino final
function scan(f) { modal(`<h3 style="margin-top:0">Escáner de QR (destino final)</h3><div class="scan">Apunte la cámara al código QR…</div><button class="p" style="width:100%;margin-top:16px" onclick="sig('Firma de recibido','${f}',()=>recv('${f}'))">Simular lectura del QR #${f}</button><button style="width:100%;margin-top:10px" onclick="close_()">Cancelar</button>`); }

// RF-05: firma de recibido (ENTREGADO)
function recv(f) { M.find(m => m.f == f).s = "ENTREGADO"; log(f, "Recepción y firma de recibido (ENTREGADO)", "Lic. Patricia Lozano · Destino Final"); close_(); go("man"); notify(`Entrega cerrada: el manifiesto #${f} fue ENTREGADO con firmas de fecha, hora y usuario.`); }
