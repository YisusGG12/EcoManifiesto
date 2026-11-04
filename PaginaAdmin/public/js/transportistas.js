/* TRANSPORTISTAS: registro de transportistas y vehículos (RF-07) */

// RF-07: formulario de nuevo transportista
function newT() { modal(`<h3 style="margin-top:0">Registrar transportista</h3><label>Empresa</label><input id="tn"><label>Conductor</label><input id="tr"><label>Vehículo</label><input id="tv"><label>Placas</label><input id="tp"><label>Autorización SEMARNAT</label><input id="ta"><label>Permiso SCT</label><input id="ts2"><label>Fecha de vigencia</label><input id="tf" type="date"><div class="err" id="te"></div><div style="display:flex;gap:10px;margin-top:20px"><button onclick="close_()">Cancelar</button><button class="p" style="flex:1" onclick="saveT()">Registrar</button></div>`); }

// RF-07: guarda el transportista
function saveT() { const n = $("#tn").value, f = $("#tf").value; if (!n || !f || !$("#tp").value) {
    $("#te").textContent = "Complete empresa, placas y fecha de vigencia.";
    return;
} const d = new Date(f), days = (d - Date.now()) / 864e5; T.unshift({ n, rfc: $("#tr").value || "—", v: $("#tv").value || "—", pl: $("#tp").value, a: "SEMARNAT-NUEVO-" + (Math.random() * 999 | 0), f: d.toLocaleDateString("es-MX", { dateStyle: "medium" }), s: days < 0 ? "VENCIDO" : days < 30 ? "POR_VENCER" : "VIGENTE" }); close_(); go("tra"); toast("Transportista registrado"); }
