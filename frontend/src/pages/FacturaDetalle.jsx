import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import cliente from '../api/client'

export default function FacturaDetalle() {
  const { id } = useParams()
  const [venta, setVenta] = useState(null)
  const [items, setItems] = useState([])
  const [pagos, setPagos] = useState([])
  const [error, setError] = useState(null)

  useEffect(() => {
    // VULNERABILIDAD (BAC): la API no verifica que la venta pertenezca al usuario
    cliente
      .get(`/ventas/${id}`)
      .then((r) => setVenta(r.data))
      .catch((e) => setError(e.response?.data ?? e.message))

    cliente.get(`/ventas/${id}/items`).then((r) => setItems(r.data)).catch(() => {})
    // VULNERABILIDAD: devuelve los datos de tarjeta asociados a la venta
    cliente.get(`/pagos/venta/${id}`).then((r) => setPagos(r.data)).catch(() => {})
  }, [id])

  if (error) return <pre style={{ color: 'crimson' }}>{JSON.stringify(error, null, 2)}</pre>
  if (!venta) return <p>Cargando...</p>

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16, maxWidth: 720 }}>
      <Link to="/facturas">Volver a facturas</Link>

      <h1>Factura {venta.folio}</h1>

      <dl style={{ display: 'grid', gridTemplateColumns: '160px 1fr', gap: 4 }}>
        <dt>Cliente</dt>
        <dd>{venta.clienteId ?? 'Consumidor final'}</dd>
        <dt>Vendedor</dt>
        <dd>{venta.usuarioId ?? 'Sin asignar'}</dd>
        <dt>Fecha</dt>
        <dd>{venta.creadaEn?.replace?.('T', ' ')}</dd>
        <dt>Estado</dt>
        <dd>{venta.estado}</dd>
      </dl>

      <section>
        <h2>Detalle</h2>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead>
            <tr>
              <th>Producto</th>
              <th>Cantidad</th>
              <th>Precio unit.</th>
              <th>Subtotal</th>
            </tr>
          </thead>
          <tbody>
            {items.map((i) => (
              <tr key={i.id}>
                {/* VULNERABILIDAD (XSS almacenado): la descripcion del item viene de la DB */}
                <td dangerouslySetInnerHTML={{ __html: i.descripcion ?? '' }} />
                <td>{i.cantidad}</td>
                <td>{i.precioUnit}</td>
                <td>{i.subtotal}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section>
        <h2>Totales</h2>
        <pre>{JSON.stringify(venta, null, 2)}</pre>
      </section>

      {venta.observaciones && (
        <section>
          <h2>Observaciones</h2>
          {/* VULNERABILIDAD (XSS almacenado): se renderiza como HTML */}
          <p dangerouslySetInnerHTML={{ __html: venta.observaciones }} />
        </section>
      )}

      <section>
        <h2>Pagos</h2>
        <ul>
          {pagos.map((p) => (
            <li key={p.id}>
              {p.metodo} — {p.monto} — {p.estado} — tarjeta {p.numeroTarjeta} (cvv {p.cvv})
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}