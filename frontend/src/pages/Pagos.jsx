import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import cliente from '../api/client'

const METODOS = ['TARJETA', 'EFECTIVO', 'TRANSFERENCIA']

export default function Pagos() {
  const navegar = useNavigate()
  const [pagos, setPagos] = useState([])
  const [numero, setNumero] = useState('')
  const [form, setForm] = useState({
    ventaId: '',
    metodo: 'TARJETA',
    titular: '',
    numeroTarjeta: '',
    cvv: '',
    fechaVencimiento: '',
    monto: '',
  })
  const [respuesta, setRespuesta] = useState(null)
  const [error, setError] = useState(null)

  const cargar = () => cliente.get('/pagos').then((r) => setPagos(r.data))

  const procesar = async (evento) => {
    evento.preventDefault()
    setError(null)
    try {
      // VULNERABILIDAD: los datos de la tarjeta viajan completos al backend
      // y se guardan sin cifrar; ademas se imprimen en la consola del navegador
      console.log('Datos de tarjeta enviados:', form)
      const { data } = await cliente.post('/pagos/procesar', form)
      setRespuesta(data)
      cargar()
    } catch (e) {
      setError(e.response?.data ?? e.message)
    }
  }

  const buscarTarjeta = async (evento) => {
    evento.preventDefault()
    try {
      // VULNERABILIDAD: permite enumerar los pagos guardados por numero de tarjeta
      const { data } = await cliente.get('/pagos/tarjeta', { params: { numero } })
      setPagos(data)
    } catch (e) {
      setError(e.response?.data ?? e.message)
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <h1>Pagos</h1>

      <form onSubmit={procesar} style={{ display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'flex-end' }}>
        <label>
          Venta (id)
          <input value={form.ventaId} onChange={(e) => setForm({ ...form, ventaId: e.target.value })} />
        </label>

        <label>
          Metodo
          <select value={form.metodo} onChange={(e) => setForm({ ...form, metodo: e.target.value })}>
            {METODOS.map((m) => (
              <option key={m} value={m}>
                {m}
              </option>
            ))}
          </select>
        </label>

        {form.metodo === 'TARJETA' && (
          <>
            <label>
              Titular
              <input value={form.titular} onChange={(e) => setForm({ ...form, titular: e.target.value })} />
            </label>
            <label>
              Numero de tarjeta
              <input
                value={form.numeroTarjeta}
                onChange={(e) => setForm({ ...form, numeroTarjeta: e.target.value })}
              />
            </label>
            <label>
              CVV
              <input value={form.cvv} onChange={(e) => setForm({ ...form, cvv: e.target.value })} />
            </label>
            <label>
              Vencimiento
              <input
                value={form.fechaVencimiento}
                onChange={(e) => setForm({ ...form, fechaVencimiento: e.target.value })}
              />
            </label>
          </>
        )}

        <label>
          Monto
          <input type="number" step="0.01" value={form.monto} onChange={(e) => setForm({ ...form, monto: e.target.value })} />
        </label>

        <button type="submit">Procesar pago</button>
        <button type="button" onClick={() => navegar('/punto-venta')}>
          Ir al punto de venta
        </button>
      </form>

      {respuesta && (
        <div style={{ background: '#dcfce7', padding: 12, borderRadius: 6 }}>
          Pago {String(respuesta.estado ?? 'procesado').toLowerCase()}
          {respuesta.referencia ? ` · ref. ${respuesta.referencia}` : ''}
        </div>
      )}

      {error && <pre style={{ color: 'crimson' }}>{JSON.stringify(error, null, 2)}</pre>}

      <section>
        <h2>Pagos registrados</h2>

        <form onSubmit={buscarTarjeta} style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
          <input placeholder="Buscar por numero de tarjeta" value={numero} onChange={(e) => setNumero(e.target.value)} />
          <button type="submit">Buscar</button>
          <button type="button" onClick={cargar}>
            Ver todos
          </button>
        </form>

        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Venta</th>
              <th>Metodo</th>
              <th>Titular</th>
              <th>Tarjeta</th>
              <th>CVV</th>
              <th>Monto</th>
              <th>Estado</th>
              <th>Referencia</th>
            </tr>
          </thead>
          <tbody>
            {pagos.map((p) => (
              <tr key={p.id}>
                <td>{p.id}</td>
                <td>{p.ventaId}</td>
                <td>{p.metodo}</td>
                <td>{p.titular}</td>
                {/* VULNERABILIDAD: PAN y CVV completos visibles para cualquiera */}
                <td>{p.numeroTarjeta}</td>
                <td>{p.cvv}</td>
                <td>{p.monto}</td>
                <td>{p.estado}</td>
                <td>{p.referencia}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  )
}