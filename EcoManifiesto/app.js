// cambia entre las secciones del portal (dashboard, manifiestos, etc.)
function cargarVistaWeb(vista) {
  // los ids de las vistas, tienen que coincidir con los web-view-... y nav-... del html
  const vistas = ['dashboard', 'manifestos', 'transportistas', 'expediente', 'usuarios', 'bitacora', 'configuracion'];

  // primero escondemos todas las vistas y les quitamos el estilo de activo a los botones
  vistas.forEach(v => {
    const el = document.getElementById(`web-view-${v}`);
    if (el) el.classList.add('hidden');

    const btn = document.getElementById(`nav-${v}`);
    if (btn) {
      btn.classList.remove('bg-semarnat-light', 'text-semarnat-dark', 'font-bold');
      btn.classList.add('text-gray-600', 'font-semibold');
    }
  });

  // luego mostramos solo la que se eligio
  const vistaActiva = document.getElementById(`web-view-${vista}`);
  if (vistaActiva) vistaActiva.classList.remove('hidden');

  // y le ponemos el estilo verde al boton de esa vista
  const btnActivo = document.getElementById(`nav-${vista}`);
  if (btnActivo) {
    btnActivo.classList.add('bg-semarnat-light', 'text-semarnat-dark', 'font-bold');
    btnActivo.classList.remove('text-gray-600', 'font-semibold');
  }
}

// cuando carga la pagina se dibujan las graficas (los datos son de ejemplo)
document.addEventListener("DOMContentLoaded", () => {
  
  // grafica de barras: volumen mensual de residuos
  const ctxVol = document.getElementById('chartVolumen');
  if (ctxVol) {
    new Chart(ctxVol, {
      type: 'bar',
      data: {
        labels: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct'],
        datasets: [
          { 
            label: 'Aceites Usados (T)', 
            data: [12, 14, 11, 15, 13, 16, 14, 15, 12, 14], 
            backgroundColor: '#0b3c26' 
          },
          { 
            label: 'Lodos Ind. (T)', 
            data: [5, 6, 4, 7, 5, 6, 5, 6, 4, 5], 
            backgroundColor: '#c2410c' 
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { 
          legend: { 
            position: 'bottom', 
            labels: { boxWidth: 10, font: { size: 11 } } 
          } 
        }
      }
    });
  }

  // grafica de dona: distribucion por tipo CRETIB
  const ctxCretib = document.getElementById('chartCretib');
  if (ctxCretib) {
    new Chart(ctxCretib, {
      type: 'doughnut',
      data: {
        labels: ['Tóxico (T)', 'Inflamable (I)', 'Corrosivo (C)', 'Reactivo (R)'],
        datasets: [{
          data: [82.8, 34.3, 17.1, 8.6],
          backgroundColor: ['#7f1d1d', '#c2410c', '#b45309', '#6b21a8']
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } }
      }
    });
  }
});