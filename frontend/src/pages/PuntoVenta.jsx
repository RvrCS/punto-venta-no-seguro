import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import cliente from '../api/client'

export default function PuntoVenta() {
  const navegar = useNavigate()
  const [productos, setProductos] = useState([])
  const [carrito, setCarrito] = useState([])
  const [busqueda, setBusqueda] = useState('')
  const [checkout, setCheckout] = useState({ clienteId: '', usuarioId: '', descuento: 0, observaciones: '' })
  const [cantidadesSel, setCantidadesSel] = useState({})
  const [error, setError] = useState(null)

  const cargarCarrito = () => cliente.get('/ventas/carrito').then((r) => setCarrito(r.data))

  useEffect(() => {
    cliente.get('/productos').then((r) => setProductos(r.data))
    cargarCarrito()
  }, [])

  const buscar = async (evento) => {
    evento.preventDefault()
    const { data } = await cliente.get('/productos/buscar', { params: { texto: busqueda } })
    setProductos(data)
  }

  const agregar = async (producto, cantidad = 1) => {
    try {
      // VULNERABILIDAD: el precio lo manda el navegador y el backend lo acepta sin
      // compararlo con la base de datos, por lo que se puede alterar el precio de venta
      const cantidadNum = Number(cantidad)
      const { data } = await cliente.post('/ventas/carrito', null, {
        params: { productoId: producto.id, cantidad: cantidadNum, precio: producto.precio },
      })
      setCarrito(data)
    } catch (e) {
      setError(e.response?.data ?? e.message)
    }
  }

  const quitar = async (indice) => {
    const { data } = await cliente.delete(`/ventas/carrito/${indice}`)
    setCarrito(data)
  }

  const actualizarCantidad = async (indice, valor) => {
    const cantidad = Number(valor)
    if (!Number.isFinite(cantidad) || cantidad <= 0) {
      const { data } = await cliente.delete(`/ventas/carrito/${indice}`)
      setCarrito(data)
      return
    }
    const { data } = await cliente.put(`/ventas/carrito/${indice}`, null, {
      params: { cantidad },
    })
    setCarrito(data)
  }

  const finalizar = async (evento) => {
    evento.preventDefault()
    setError(null)
    try {
      // VULNERABILIDAD: el descuento y el usuario los decide el cliente
      const { data } = await cliente.post('/ventas/checkout', {
        clienteId: checkout.clienteId || null,
        usuarioId: checkout.usuarioId || null,
        descuento: Number(checkout.descuento) || 0,
        observaciones: checkout.observaciones,
      })
      if (data.error) {
        setError(data.error)
        return
      }
      setCarrito([])
      navegar(`/facturas/${data.venta.id}`)
    } catch (e) {
      setError(e.response?.data ?? e.message)
    }
  }

  const total = carrito.reduce((suma, linea) => suma + Number(linea.subtotal ?? 0), 0)

  return (
    <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 24 }}>
      <section style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        <h1>Punto de venta</h1>

        <form onSubmit={buscar} style={{ display: 'flex', gap: 8 }}>
          <input
            placeholder="Escanear o buscar producto (SKU / nombre)"
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
          />
          <button type="submit">Buscar</button>
        </form>

        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead>
            <tr>
              <th>SKU</th>
              <th>Producto</th>
              <th>Precio</th>
              <th>Stock</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {productos.slice(0, 40).map((p) => (
              <tr key={p.id}>
                <td>{p.sku}</td>
                <td>{p.nombre}</td>
                <td>{p.precio}</td>
                <td>{p.stock}</td>
                    <td>
                      <input
                        type="number"
                        step="1"
                        min="0"
                        value={cantidadesSel[p.id] ?? 1}
                        onChange={(e) =>
                          setCantidadesSel((s) => ({ ...s, [p.id]: e.target.value }))
                        }
                        style={{ width: '60px' }}
                      />
                      <button onClick={() => agregar(p, cantidadesSel[p.id] ?? 1)}>Agregar</button>
                    </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <aside style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        <h2>Carrito</h2>

        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <tbody>
                {carrito.map((linea, indice) => (
                  <tr key={indice}>
                    <td>#{linea.productoId}</td>
                    <td>
                      <input
                        type="number"
                        step="1"
                        value={linea.cantidad}
                        onChange={(e) => actualizarCantidad(indice, e.target.value)}
                        style={{ width: '60px' }}
                      /> x {linea.precioUnit}
                    </td>
                    <td>{linea.subtotal}</td>
                <td>
                  <button onClick={() => quitar(indice)}>x</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <div style={{ fontWeight: 600 }}>Total: {total.toFixed(2)}</div>

        <form onSubmit={finalizar} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          <label>
            Cliente (id)
            <input
              value={checkout.clienteId}
              onChange={(e) => setCheckout({ ...checkout, clienteId: e.target.value })}
            />
          </label>

          <label>
            Vendedor (id)
            <input
              value={checkout.usuarioId}
              onChange={(e) => setCheckout({ ...checkout, usuarioId: e.target.value })}
            />
          </label>

          <label>
            Descuento
            <input
              type="number"
              step="0.01"
              value={checkout.descuento}
              onChange={(e) => setCheckout({ ...checkout, descuento: e.target.value })}
            />
          </label>

          <label>
            Observaciones
            <textarea
              rows={2}
              value={checkout.observaciones}
              onChange={(e) => setCheckout({ ...checkout, observaciones: e.target.value })}
            />
          </label>

          {error && <pre style={{ color: 'crimson' }}>{JSON.stringify(error, null, 2)}</pre>}

          <button type="submit" disabled={carrito.length === 0}>
            Cobrar
          </button>
        </form>
      </aside>
    </div>
  )
}