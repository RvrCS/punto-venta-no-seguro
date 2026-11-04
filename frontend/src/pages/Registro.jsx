import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import cliente from '../api/client'

const ROLES = ['VENDEDOR', 'ADMIN', 'CLIENTE']

export default function Registro() {
  const navegar = useNavigate()
  const [form, setForm] = useState({
    username: '',
    password: '',
    nombre: '',
    email: '',
    // VULNERABILIDAD: el rol viaja como campo libre en el formulario
    rol: 'VENDEDOR',
  })
  const [resultado, setResultado] = useState(null)

  const enviar = async (evento) => {
    evento.preventDefault()
    try {
      // VULNERABILIDAD: se envia el objeto tal cual, sin validacion de campos
      const { data } = await cliente.post('/auth/registro', form)
      setResultado(data)
    } catch (e) {
      setResultado(e.response?.data ?? e.message)
    }
  }

  return (
    <div style={{ minHeight: '100vh', display: 'grid', placeItems: 'center', fontFamily: 'system-ui, sans-serif' }}>
      <form onSubmit={enviar} style={{ display: 'flex', flexDirection: 'column', gap: 12, width: 340 }}>
        <h1>Crear cuenta</h1>

        <label>
          Usuario
          <input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
        </label>

        <label>
          Contrasena
          <input
            type="password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
          />
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
            {ROLES.map((r) => (
              <option key={r} value={r}>
                {r}
              </option>
            ))}
          </select>
        </label>

        <button type="submit">Registrarme</button>

        {resultado && <pre>{JSON.stringify(resultado, null, 2)}</pre>}

        <button type="button" onClick={() => navegar('/login')}>
          Volver al login
        </button>
      </form>
    </div>
  )
}