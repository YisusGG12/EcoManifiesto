/* NAVEGACIÓN Y SESIÓN: login, menú, búsqueda y notificaciones */

// Cambia entre pestañas de inicio de sesión (credenciales / QR)
function lt(n) { $("#lc").style.display = n == 1 ? "" : "none"; $("#lq").style.display = n == 2 ? "" : "none"; $("#t1").className = n == 1 ? "on" : ""; $("#t2").className = n == 2 ? "on" : ""; if (n == 2)
    $("#lqr").innerHTML = qr("LOGIN-QR"); }

// Inicio de sesión
function doLogin() { const e = $("#em").value.trim(); if (!e) {
    $("#le").textContent = "Escribe tu correo o RFC para continuar.";
    return;
} $("#login").style.display = "none"; $("#app").classList.add("on"); go("dash"); toast("Sesión iniciada · rol: Administrador"); }

// Cerrar sesión
function out() { $("#app").classList.remove("on"); $("#login").style.display = "flex"; }

// Navega a una pantalla y la dibuja
function go(p) { page = p; q = ""; flt = "Todos"; $("#nav").innerHTML = NAV.map(([k, l]) => `<a class="${k == p ? "on" : ""}" onclick="go('${k}')">${l}</a>`).join(""); $("#v").innerHTML = V[p](); scrollTo(0, 0); }

// Búsqueda global de la barra superior
function gs(v) { q = v.toLowerCase(); if (page != "man" && page != "tra")
    go("man");
else
    go2(); }

// Redibuja la pantalla conservando el foco del buscador
function go2() { $("#v").innerHTML = V[page](); const i = $("#fq"); if (i) {
    i.focus();
} }

// Muestra u oculta el panel de notificaciones
function tn() { const d = $("#nd"); d.style.display = d.style.display == "block" ? "none" : "block"; $("#dot").style.display = "none"; d.innerHTML = ns.length ? ns.map(n => `<div>🔔 ${n}</div>`).join("") : "<div>Sin notificaciones nuevas.</div>"; }

document.addEventListener("click", e => { if (e.target.id == "md")
    close_(); });
