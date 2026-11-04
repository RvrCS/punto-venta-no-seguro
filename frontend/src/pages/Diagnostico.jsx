import { useEffect, useState } from 'react'
import cliente from '../api/client'

// Convierte las filas [{clave, valor}] de /configuracion en un mapa simple.
function mapaConfig(filas) {
  const mapa = {}
  for (const fila of filas ?? []) mapa[fila.clave] = fila.valor
  return mapa
}

// VULNERABILIDAD: "cachea" un secreto en IndexedDB (la base de datos local del
// navegador). No se ve en pantalla: hay que abrir DevTools > Application >
// IndexedDB para encontrarlo.
function guardarEnIndexedDB(clave, valor) {
  try {
    const solicitud = indexedDB.open('pv_cache', 1)
    solicitud.onupgradeneeded = (e) => {
      const db = e.target.result
      if (!db.objectStoreNames.contains('ajustes')) {
        db.createObjectStore('ajustes', { keyPath: 'id' })
      }
    }
    solicitud.onsuccess = (e) => {
      const db = e.target.result
      const tx = db.transaction('ajustes', 'readwrite')
      tx.objectStore('ajustes').put({ id: clave, valor })
    }
  } catch {
    /* ignorar */
  }
}

export default function Diagnostico() {
  const [salud, setSalud] = useState(null)
  const [cacheado, setCacheado] = useState(false)

  useEffect(() => {
    cliente.get('/salud').then((r) => setSalud(r.data)).catch(() => {})

    // La app "cachea" la configuración en el navegador para acelerar el checkout.
    // VULNERABILIDAD: cada secreto queda guardado del lado del cliente, repartido
    // en varios almacenes, en vez de mantenerse solo en el servidor.
    cliente
      .get('/configuracion')
      .then((r) => {
        const cfg = mapaConfig(r.data)

        // 1) localStorage: visible en DevTools > Application > Local Storage.
        localStorage.setItem(
          'pv_cfg',
          JSON.stringify({ jwt: cfg['jwt.secret'], correo: cfg['correo.admin'] }),
        )

        // 2) IndexedDB: base de datos local del navegador.
        guardarEnIndexedDB('pasarela', cfg['pasarela.secret'])

        // 3) Cookie NO protegida: legible desde JS (document.cookie), por lo que
        //    cualquier XSS puede robarla. El valor va en base64, hay que decodificarlo.
        const correo = cfg['correo.password'] ?? ''
        document.cookie = `pv_mail=${btoa(correo)}; path=/; SameSite=Lax`

        setCacheado(true)
      })
      .catch(() => {})

    // 4) Cookie protegida (HttpOnly): la emite el backend al pedir la config de la
    //    pasarela. No se puede leer con document.cookie; hay que interceptar la
    //    respuesta con un proxy (Burp Suite) para ver la cabecera Set-Cookie.
    cliente.get('/pagos/configuracion').catch(() => {})
  }, [])

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <h1>Diagnostico del sistema</h1>

      <section>
        <h2>Estado del servicio</h2>
        {salud && <pre>{JSON.stringify(salud, null, 2)}</pre>}
      </section>

      <section>
        <h2>Configuracion</h2>
        <p style={{ color: '#6b7280' }}>
          La configuracion de la tienda se cacheo localmente para acelerar el
          checkout {cacheado ? '✓' : '…'}.
        </p>
      </section>

      <section>
        <h2>Endpoints de Spring Boot Actuator</h2>
        {/* VULNERABILIDAD: actuator expone todos los endpoints sin autenticacion */}
        <p>
          <a href="/actuator" target="_blank" rel="noreferrer">
            /actuator
          </a>
          {' | '}
          <a href="/actuator/env" target="_blank" rel="noreferrer">
            /actuator/env
          </a>
          {' | '}
          <a href="/actuator/configprops" target="_blank" rel="noreferrer">
            /actuator/configprops
          </a>
          {' | '}
          <a href="/actuator/heapdump" target="_blank" rel="noreferrer">
            /actuator/heapdump
          </a>
        </p>
      </section>
    </div>
  )
}
