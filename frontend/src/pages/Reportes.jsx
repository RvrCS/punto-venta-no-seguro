import { useState } from 'react'
import cliente from '../api/client'

export default function Reportes() {
  const [ventas, setVentas] = useState([])
  const [resumen, setResumen] = useState(null)
  const [tablas, setTablas] = useState([])
  const [filtros, setFiltros] = useState({ desde: '2000-01-01', hasta: '2099-12-31', estado: 'COMPLETADA' })
  const [url, setUrl] = useState('http://169.254.169.254/latest/meta-data/')
  const [salida, setSalida] = useState(null)
  const [error, setError] = useState(null)

  const cargarResumen = () => cliente.get('/reportes/resumen').then((r) => setResumen(r.data))

  const consultarVentas = async () => {
    setError(null)
    try {
      // VULNERABILIDAD (SQLi): los filtros se interpolan en la consulta nativa del servidor
      const { data } = await cliente.get('/reportes/ventas', { params: filtros })
      setVentas(data)
    } catch (e) {
      setError(e.response?.data ?? e.message)
    }
  }

  const verEstructura = () => cliente.get('/reportes/base-datos').then((r) => setTablas(r.data))

  const exportar = async (formato) => {
    const ruta = formato === 'html' ? '/reportes/exportar/html' : '/reportes/exportar/csv'
    const { data } = await cliente.get(ruta, { params: { estado: filtros.estado, nombreCliente: 'Todos' } })
    setSalida(data)
  }

  // VULNERABILIDAD (SSRF): el usuario decide a que URL accede el servidor
  const consultarUrl = async (evento) => {
    evento.preventDefault()
    setError(null)
    try {
      const { data } = await cliente.post('/integraciones/consultar-url', null, { params: { url } })
      setSalida(data)
    } catch (e) {
      setError(e.response?.data ?? e.message)
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
      <h1>Reportes</h1>

      <section>
        <h2>Resumen</h2>
        <button onClick={cargarResumen}>Calcular</button>
        {resumen && <pre>{JSON.stringify(resumen, null, 2)}</pre>}
      </section>

      <section>
        <h2>Ventas por periodo</h2>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 8 }}>
          <label>
            Desde
            <input value={filtros.desde} onChange={(e) => setFiltros({ ...filtros, desde: e.target.value })} />
          </label>
          <label>
            Hasta
            <input value={filtros.hasta} onChange={(e) => setFiltros({ ...filtros, hasta: e.target.value })} />
          </label>
          <label>
            Estado
            <input value={filtros.estado} onChange={(e) => setFiltros({ ...filtros, estado: e.target.value })} />
          </label>
          <button onClick={consultarVentas}>Consultar</button>
          <button onClick={() => exportar('html')}>Exportar HTML</button>
          <button onClick={() => exportar('csv')}>Exportar CSV</button>
        </div>

        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead>
            <tr>
              <th>Folio</th>
              <th>Total</th>
              <th>Estado</th>
              <th>Fecha</th>
            </tr>
          </thead>
          <tbody>
            {ventas.map((v, i) => (
              <tr key={v.id ?? i}>
                <td>{v.folio ?? JSON.stringify(v)}</td>
                <td>{v.total ?? '-'}</td>
                <td>{v.estado ?? '-'}</td>
                <td>{v.fecha ?? '-'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section>
        <h2>Estructura de la base de datos</h2>
        <button onClick={verEstructura}>Ver tablas</button>
        {tablas.length > 0 && <pre>{JSON.stringify(tablas, null, 2)}</pre>}
      </section>

      <section>
        <h2>Verificar URL de proveedor</h2>
        <form onSubmit={consultarUrl} style={{ display: 'flex', gap: 8 }}>
          <input style={{ flex: 1 }} value={url} onChange={(e) => setUrl(e.target.value)} />
          <button type="submit">Consultar</button>
        </form>
      </section>

      {salida && <pre style={{ background: '#e0f2fe', padding: 12 }}>{String(salida)}</pre>}
      {error && <pre style={{ color: 'crimson' }}>{JSON.stringify(error, null, 2)}</pre>}
    </div>
  )
}