import { useEffect, useState } from 'react'
import cliente from '../api/client'

const VACIA = { nombre: '', descripcion: '' }

export default function Categorias() {
  const [categorias, setCategorias] = useState([])
  const [form, setForm] = useState(VACIA)
  const [error, setError] = useState(null)

  const cargar = () => cliente.get('/categorias').then((r) => setCategorias(r.data))

  useEffect(() => {
    cargar().catch((e) => setError(e.response?.data ?? e.message))
  }, [])

  const crear = async (evento) => {
    evento.preventDefault()
    try {
      // VULNERABILIDAD: el nombre y la descripcion se envian sin validar
      await cliente.post('/categorias', form)
      setForm(VACIA)
      cargar()
    } catch (e) {
      setError(e.response?.data ?? e.message)
    }
  }

  const eliminar = async (id) => {
    await cliente.delete(`/categorias/${id}`)
    cargar()
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <h1>Categorias</h1>

      <form onSubmit={crear} style={{ display: 'flex', gap: 8, alignItems: 'flex-end' }}>
        <label>
          Nombre
          <input value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} />
        </label>
        <label>
          Descripcion
          <input value={form.descripcion} onChange={(e) => setForm({ ...form, descripcion: e.target.value })} />
        </label>
        <button type="submit">Crear</button>
      </form>

      {error && <pre style={{ color: 'crimson' }}>{JSON.stringify(error, null, 2)}</pre>}

      <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Nombre</th>
            <th>Descripcion</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {categorias.map((c) => (
            <tr key={c.id}>
              <td>{c.id}</td>
              <td>{c.nombre}</td>
              {/* VULNERABILIDAD (XSS): la descripcion de la categoria se renderiza como HTML */}
              <td dangerouslySetInnerHTML={{ __html: c.descripcion ?? '' }} />
              <td>
                <button onClick={() => eliminar(c.id)}>Eliminar</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}