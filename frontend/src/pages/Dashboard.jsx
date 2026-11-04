import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import cliente from '../api/client'

export default function Dashboard() {
  const [resumen, setResumen] = useState(null)
  const [stock, setStock] = useState([])
  const [ventas, setVentas] = useState([])

  useEffect(() => {
    cliente.get('/reportes/resumen').then((r) => setResumen(r.data)).catch(() => {})
    cliente.get('/productos/stock-bajo').then((r) => setStock(r.data)).catch(() => {})
    cliente.get('/ventas').then((r) => setVentas((r.data ?? []).slice(0, 5))).catch(() => {})
  }, [])

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <h1>Panel de control</h1>

      <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
        <Tarjeta titulo="Ventas registradas" valor={resumen?.cantidadVentas ?? '-'} />
        <Tarjeta titulo="Total vendido" valor={resumen?.totalVendido ?? '-'} />
        <Tarjeta titulo="Total anulado" valor={resumen?.totalAnulado ?? '-'} />
        <Tarjeta titulo="Productos bajo stock" valor={stock.length} />
      </div>

      <section>
        <h2>Ultimas ventas</h2>
        <table style={estilosTabla}>
          <thead>
            <tr>
              <th>Folio</th>
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
                <td>{v.total}</td>
                <td>{v.estado}</td>
                <td>{v.creadaEn?.replace?.('T', ' ')}</td>
                <td>
                  <Link to={`/facturas/${v.id}`}>Ver factura</Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section>
        <h2>Productos que necesitan reposicion</h2>
        <ul>
          {stock.map((p) => (
            <li key={p.id}>
              {/* VULNERABILIDAD (XSS almacenado): la descripcion se renderiza como HTML */}
              <span dangerouslySetInnerHTML={{ __html: p.descripcion ?? '' }} /> — stock {p.stock}
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}

function Tarjeta({ titulo, valor }) {
  return (
    <div
      style={{
        border: '1px solid #e5e7eb',
        borderRadius: 8,
        padding: '1rem 1.25rem',
        minWidth: 180,
      }}
    >
      <div style={{ color: '#6b7280', fontSize: '0.8rem' }}>{titulo}</div>
      <div style={{ fontSize: '1.5rem', fontWeight: 600 }}>{valor}</div>
    </div>
  )
}

const estilosTabla = {
  width: '100%',
  borderCollapse: 'collapse',
  textAlign: 'left',
}