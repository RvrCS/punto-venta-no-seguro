import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import cliente from '../api/client'

export default function Facturas() {
  const [ventas, setVentas] = useState([])
  const [filtros, setFiltros] = useState({ estado: '', desde: '' })
  const [error, setError] = useState(null)

  useEffect(() => {
    cliente.get('/ventas').then((r) => setVentas(r.data)).catch(() => {})
  }, [])

  const buscar = async (evento) => {
    evento.preventDefault()
    setError(null)
    try {
      // VULNERABILIDAD (SQLi): los filtros viajan sin sanear al backend
      const { data } = await cliente.get('/ventas/buscar', { params: filtros })
      setVentas(data)
    } catch (e) {
      setError(e.response?.data ?? e.message)
    }
  }

  const anular = async (id) => {
    if (!window.confirm('Anular esta venta?')) return
    // VULNERABILIDAD (BAC): cualquier usuario puede anular ventas ajenas
    await cliente.post(`/ventas/${id}/anular`, null, { params: { motivo: 'Anulada desde la interfaz' } })
    cliente.get('/ventas').then((r) => setVentas(r.data))
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <h1>Facturas</h1>

      <form onSubmit={buscar} style={{ display: 'flex', gap: 8, alignItems: 'flex-end', flexWrap: 'wrap' }}>
        <label>
          Estado
          <select
            value={filtros.estado}
            onChange={(e) => setFiltros({ ...filtros, estado: e.target.value })}
          >
            <option value="">Todos</option>
            <option value="COMPLETADA">Completada</option>
            <option value="ANULADA">Anulada</option>
            <option value="PENDIENTE">Pendiente</option>
          </select>
        </label>

        <label>
          Desde
          <input
            placeholder="2024-01-01T00:00:00"
            value={filtros.desde}
            onChange={(e) => setFiltros({ ...filtros, desde: e.target.value })}
          />
        </label>

        <button type="submit">Filtrar</button>
      </form>

      {error && <pre style={{ color: 'crimson' }}>{JSON.stringify(error, null, 2)}</pre>}

      <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
        <thead>
          <tr>
            <th>Folio</th>
            <th>Cliente</th>
            <th>Subtotal</th>
            <th>Descuento</th>
            <th>Total</th>
            <th>Estado</th>
            <th>Fecha</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {ventas.map((v) => (
            <tr key={v.id}>
              <td>{v.folio}</td>
              <td>{v.clienteId ?? 'Consumidor final'}</td>
              <td>{v.subtotal}</td>
              <td>{v.descuento}</td>
              <td>{v.total}</td>
              <td>{v.estado}</td>
              <td>{v.creadaEn?.replace?.('T', ' ')}</td>
              <td>
                <Link to={`/facturas/${v.id}`}>Detalle</Link>
                {v.estado !== 'ANULADA' && (
                  <>
                    {' | '}
                    <button onClick={() => anular(v.id)}>Anular</button>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}