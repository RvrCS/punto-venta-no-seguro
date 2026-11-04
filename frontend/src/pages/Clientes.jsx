import { useEffect, useState } from 'react'
import cliente from '../api/client'

const VACIO = { documento: '', nombre: '', email: '', telefono: '', direccion: '', activo: true }

export default function Clientes() {
  const [clientes, setClientes] = useState([])
  const [form, setForm] = useState(VACIO)
  const [texto, setTexto] = useState('')
  const [editando, setEditando] = useState(null)
  const [error, setError] = useState(null)

  const cargar = () => cliente.get('/clientes').then((r) => setClientes(r.data))

  useEffect(() => {
    cargar().catch((e) => setError(e.response?.data ?? e.message))
  }, [])

  const buscar = async (evento) => {
    evento.preventDefault()
    try {
      const { data } = await cliente.get('/clientes/buscar', { params: { texto } })
      setClientes(data)
    } catch (e) {
      setError(e.response?.data ?? e.message)
    }
  }

  const guardar = async (evento) => {
    evento.preventDefault()
    try {
      // VULNERABILIDAD: no hay validacion de email, telefono ni documento
      if (editando) {
        await cliente.put(`/clientes/${editando}`, form)
      } else {
        await cliente.post('/clientes', form)
      }
      setForm(VACIO)
      setEditando(null)
      cargar()
    } catch (e) {
      setError(e.response?.data ?? e.message)
    }
  }

  const editar = (cliente) => {
    setEditando(cliente.id)
    setForm(cliente)
  }

  const eliminar = async (id) => {
    if (!window.confirm('Eliminar cliente?')) return
    await cliente.delete(`/clientes/${id}`)
    cargar()
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <h1>Clientes</h1>

      <form onSubmit={buscar} style={{ display: 'flex', gap: 8 }}>
        <input placeholder="Buscar por nombre o documento" value={texto} onChange={(e) => setTexto(e.target.value)} />
        <button type="submit">Buscar</button>
      </form>

      <form onSubmit={guardar} style={{ display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'flex-end' }}>
        <label>
          Documento
          <input value={form.documento} onChange={(e) => setForm({ ...form, documento: e.target.value })} />
        </label>
        <label>
          Nombre
          <input value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} />
        </label>
        <label>
          Email
          <input value={form.email ?? ''} onChange={(e) => setForm({ ...form, email: e.target.value })} />
        </label>
        <label>
          Telefono
          <input value={form.telefono ?? ''} onChange={(e) => setForm({ ...form, telefono: e.target.value })} />
        </label>
        <label>
          Direccion
          <input value={form.direccion ?? ''} onChange={(e) => setForm({ ...form, direccion: e.target.value })} />
        </label>
        <button type="submit">{editando ? 'Actualizar' : 'Crear'}</button>
        {editando && (
          <button
            type="button"
            onClick={() => {
              setEditando(null)
              setForm(VACIO)
            }}
          >
            Cancelar
          </button>
        )}
      </form>

      {error && <pre style={{ color: 'crimson' }}>{JSON.stringify(error, null, 2)}</pre>}

      <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
        <thead>
          <tr>
            <th>Documento</th>
            <th>Nombre</th>
            <th>Email</th>
            <th>Telefono</th>
            <th>Direccion</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {clientes.map((c) => (
            <tr key={c.id}>
              <td>{c.documento}</td>
              {/* VULNERABILIDAD (XSS almacenado): el nombre se inserta como HTML */}
              <td dangerouslySetInnerHTML={{ __html: c.nombre }} />
              <td>{c.email}</td>
              <td>{c.telefono}</td>
              <td>{c.direccion}</td>
              <td>
                <button onClick={() => editar(c)}>Editar</button>
                {' | '}
                <button onClick={() => eliminar(c.id)}>Eliminar</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}