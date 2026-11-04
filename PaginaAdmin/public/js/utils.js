/* UTILIDADES: componentes visuales reutilizables */

// Etiqueta de color para un estado
const st = s => `<span class="st ${s}">${s.replace("_", " ")}</span>`;

// Genera un código QR (versión de ejemplo; firebase-app.js la reemplaza por un QR real)
function qr(seed) {
    let h = 0, s = "";
    for (const ch of seed)
        h = (h * 31 + ch.charCodeAt(0)) >>> 0;
    const N = 21;
    let r = "";
    const fd = (x, y) => (x < 8 && y < 8) || (x > 12 && y < 8) || (x < 8 && y > 12);
    for (let y = 0; y < N; y++)
        for (let x = 0; x < N; x++) {
            let on;
            if (fd(x, y)) {
                const lx = x > 12 ? x - 14 : x, ly = y > 12 ? y - 14 : y;
                on = lx == 0 || lx == 6 || ly == 0 || ly == 6 || (lx >= 2 && lx <= 4 && ly >= 2 && ly <= 4) && lx < 7 && ly < 7;
                if (lx == 7 || ly == 7)
                    on = false;
            }
            else {
                h = (h * 1103515245 + 12345) >>> 0;
                on = (h >> 16) & 1;
            }
            if (on)
                r += `<rect x="${x}" y="${y}" width="1" height="1"/>`;
        }
    return `<svg viewBox="-1 -1 23 23" style="background:#fff;border-radius:8px;width:100%" fill="#000">${r}</svg>`;
}

// Mensaje flotante temporal
function toast(m) { const t = $("#ts"); t.textContent = m; t.style.display = "block"; clearTimeout(t.h); t.h = setTimeout(() => t.style.display = "none", 3500); }

// Agrega un evento a la bitácora local
function log(f, e, u) { const n = new Date(), p = x => String(x).padStart(2, "0"); L.unshift([`${p(n.getDate())}/${p(n.getMonth() + 1)}/${n.getFullYear()} ${p(n.getHours())}:${p(n.getMinutes())}:${p(n.getSeconds())}`, f, e, u]); }

// Muestra y guarda una notificación (campana)
function notify(m) { ns.unshift(m); $("#dot").style.display = "block"; toast("🔔 " + m); }

// Encabezado de pantalla
function hd(tag, t, s, btns) { return `<div class="hd"><div><span class="pill">${tag}</span><h2>${t}</h2><div class="sub">${s}</div></div><div style="display:flex;gap:10px;flex-wrap:wrap">${btns || ""}</div></div>`; }

// Tarjeta de indicador (KPI)
function kp(l, v, s) { return `<div class="card"><div class="kl">${l}</div><div class="kv">${v}</div><div class="sub" style="font-size:13px">${s}</div></div>`; }

// Filas de la tabla de manifiestos
function mrows(a, act) { return a.map(m => `<tr><td class="mono"><b>#${m.f}</b></td><td>${m.d}</td><td>${m.r} <span class="pill" style="background:#ffd9cc;color:#7a2e0a">${m.c}</span></td><td>${m.q}</td><td>${m.t}</td><td>${m.x}</td><td>${st(m.s)}</td>${act ? `<td>${m.s == "CREADO" ? `<button class="p" onclick="custody('${m.f}')">Firmar custodia</button>` : m.s == "EN_TRANSITO" ? `<button class="p" onclick="scan('${m.f}')">Escanear QR</button>` : ""} <button onclick="showQR('${m.f}')">QR</button></td>` : ""}</tr>`).join(""); }

// Encabezados de la tabla de manifiestos
const mh = a => `<th>Folio</th><th>Fecha</th><th>Residuo y CRETIB</th><th>Cantidad</th><th>Transportista</th><th>Destino final</th><th>Estatus</th>${a ? "<th>Acción</th>" : ""}`;

// Abre una ventana modal con el HTML dado
function modal(h) { $("#mb").innerHTML = h; $("#md").classList.add("on"); }

// Cierra la ventana modal
function close_() { $("#md").classList.remove("on"); }
