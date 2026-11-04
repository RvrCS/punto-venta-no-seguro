import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import cliente from '../api/client'

export default function Productos() {
  const [productos, setProductos] = useState([])
  const [texto, setTexto] = useState('')
  const [rango, setRango] = useState({ minimo: '', maximo: '' })
  const [error, setError] = useState(null)

  const cargar = () => cliente.get('/productos').then((r) => setProductos(r.data))

  useEffect(() => {
    cargar().catch((e) => setError(e.response?.data ?? e.message))
  }, [])

  const buscar = async (evento) => {
    evento.preventDefault()
    setError(null)
    try {
      // VULNERABILIDAD (SQLi): el termino de busqueda se envia sin sanear
      const { data } = await cliente.get('/productos/buscar', { params: { texto } })
      setProductos(data)
    } catch (e) {
      setError(e.response?.data ?? e.message)
    }
  }

  const buscarRango = async (evento) => {
    evento.preventDefault()
    setError(null)
    try {
      const { data } = await cliente.get('/productos/rango', { params: rango })
      setProductos(data)
    } catch (e) {
      setError(e.response?.data ?? e.message)
    }
  }

  const eliminar = async (id) => {
    if (!window.confirm('Eliminar producto?')) return
    await cliente.delete(`/productos/${id}`)
    cargar()
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1>Productos</h1>
        <Link to="/productos/nuevo">+ Nuevo producto</Link>
      </div>

      <div style={{ display: 'flex', gap: 24, flexWrap: 'wrap' }}>
        <form onSubmit={buscar} style={{ display: 'flex', gap: 8 }}>
          <input placeholder="Buscar por nombre o SKU" value={texto} onChange={(e) => setTexto(e.target.value)} />
          <button type="submit">Buscar</button>
        </form>

        <form onSubmit={buscarRango} style={{ display: 'flex', gap: 8 }}>
          <input placeholder="Precio min" value={rango.minimo} onChange={(e) => setRango({ ...rango, minimo: e.target.value })} />
          <input placeholder="Precio max" value={rango.maximo} onChange={(e) => setRango({ ...rango, maximo: e.target.value })} />
          <button type="submit">Rango</button>
        </form>
      </div>

      {error && <pre style={{ color: 'crimson' }}>{JSON.stringify(error, null, 2)}</pre>}

      <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
        <thead>
          <tr>
            <th>SKU</th>
            <th>Nombre</th>
            <th>Descripcion</th>
            <th>Precio</th>
            <th>Stock</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {productos.map((p) => (
            <tr key={p.id}>
              <td>{p.sku}</td>
              <td>{p.nombre}</td>
              {/* VULNERABILIDAD (XSS almacenado): descripcion sin escapar */}
              <td dangerouslySetInnerHTML={{ __html: p.descripcion ?? '' }} />
              <td>{p.precio}</td>
              <td>{p.stock}</td>
              <td>
                <Link to={`/productos/${p.id}/editar`}>Editar</Link>
                {' | '}
                {/* VULNERABILIDAD: enlace sin noopener */}
                <a href={`/api/productos/${p.id}`} target="_blank">
                  Ver API
                </a>
                {' | '}
                <button onClick={() => eliminar(p.id)}>Eliminar</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}