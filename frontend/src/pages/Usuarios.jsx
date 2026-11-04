import { useEffect, useState } from 'react'
import cliente from '../api/client'

const VACIO = { username: '', password: '', nombre: '', email: '', rol: 'VENDEDOR', activo: true }

export default function Usuarios() {
  const [usuarios, setUsuarios] = useState([])
  const [form, setForm] = useState(VACIO)
  const [editando, setEditando] = useState(null)
  const [texto, setTexto] = useState('')
  const [error, setError] = useState(null)

  const cargar = () => cliente.get('/usuarios').then((r) => setUsuarios(r.data))

  useEffect(() => {
    cargar().catch((e) => setError(e.response?.data ?? e.message))
  }, [])

  const guardar = async (evento) => {
    evento.preventDefault()
    setError(null)
    try {
      // VULNERABILIDAD (BAC): la API acepta el rol y la contrasena desde el cliente
      if (editando) {
        await cliente.put(`/usuarios/${editando}`, form)
      } else {
        await cliente.post('/usuarios', form)
      }
      setForm(VACIO)
      setEditando(null)
      cargar()
    } catch (e) {
      setError(e.response?.data ?? e.message)
    }
  }

  const buscar = async (evento) => {
    evento.preventDefault()
    // VULNERABILIDAD (SQLi): el texto se concatena en la consulta
    const { data } = await cliente.get('/usuarios/buscar', { params: { texto } })
    setUsuarios(data)
  }

  const eliminar = async (id) => {
    if (!window.confirm('Eliminar usuario?')) return
    await cliente.delete(`/usuarios/${id}`)
    cargar()
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <h1>Usuarios</h1>

      <form onSubmit={buscar} style={{ display: 'flex', gap: 8 }}>
        <input placeholder="Buscar usuario" value={texto} onChange={(e) => setTexto(e.target.value)} />
        <button type="submit">Buscar</button>
      </form>

      <form onSubmit={guardar} style={{ display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'flex-end' }}>
        <label>
          Usuario
          <input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
        </label>
        <label>
          {/* VULNERABILIDAD: el campo de contrasena se envia en texto plano */}
          Contrasena
          <input value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
        </label>
        <label>
          Nombre
          <input value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} />
        </label>
        <label>
          Email
          <input value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
        </label>
        <label>
          Rol
          <select value={form.rol} onChange={(e) => setForm({ ...form, rol: e.target.value })}>
            <option value="ADMIN">ADMIN</option>
            <option value="VENDEDOR">VENDEDOR</option>
          </select>
        </label>
        <button type="submit">{editando ? 'Actualizar' : 'Crear'}</button>
        {editando && (
          <button type="button" onClick={() => { setEditando(null); setForm(VACIO) }}>
            Cancelar
          </button>
        )}
      </form>

      {error && <pre style={{ color: 'crimson' }}>{JSON.stringify(error, null, 2)}</pre>}

      <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
        <thead>
          <tr>
            <th>ID</th>
            <th>Usuario</th>
            {/* VULNERABILIDAD: la contrasena se lista en la tabla de usuarios */}
            <th>Contrasena</th>
            <th>Nombre</th>
            <th>Email</th>
            <th>Rol</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {usuarios.map((u) => (
            <tr key={u.id}>
              <td>{u.id}</td>
              <td>{u.username}</td>
              <td>{u.password}</td>
              <td>{u.nombre}</td>
              <td>{u.email}</td>
              <td>{u.rol}</td>
              <td>
                <button onClick={() => { setEditando(u.id); setForm(u) }}>Editar</button>
                {' | '}
                <button onClick={() => eliminar(u.id)}>Eliminar</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}