import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/ContextoAuth.jsx'

export default function Login() {
  const { login } = useAuth()
  const navegar = useNavigate()
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState(null)

  const enviar = async (evento) => {
    evento.preventDefault()
    setError(null)
    try {
      // VULNERABILIDAD: los datos se envian sin sanear ni validar en el cliente
      await login(form.username, form.password)
      navegar('/')
    } catch (e) {
      // VULNERABILIDAD: se muestra el detalle crudo devuelto por el servidor
      const detalle = e.response?.data
      setError(typeof detalle === 'string' ? detalle : JSON.stringify(detalle, null, 2))
    }
  }

  return (
    <div style={estilos.centrado}>
      <form onSubmit={enviar} style={estilos.tarjeta}>
        <h1>Punto de Venta</h1>
        <label>
          Usuario
          <input
            value={form.username}
            onChange={(e) => setForm({ ...form, username: e.target.value })}
            autoComplete="username"
          />
        </label>

        <label>
          Contrasena
          <input
            type="password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            autoComplete="current-password"
          />
        </label>

        {error && <pre style={estilos.error}>{error}</pre>}

        <button type="submit">Ingresar</button>

        <p>
          No tenes cuenta? <Link to="/registro">Registrate</Link>
        </p>

        <details>
          <summary>Usuarios de prueba</summary>
          <ul>
            <li>admin / admin</li>
            <li>vendedor / vendedor</li>
          </ul>
        </details>
      </form>
    </div>
  )
}

const estilos = {
  centrado: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontFamily: 'system-ui, sans-serif',
  },
  tarjeta: {
    display: 'flex',
    flexDirection: 'column',
    gap: '0.75rem',
    width: 320,
    padding: '1.5rem',
    border: '1px solid #e5e7eb',
    borderRadius: 8,
  },
  error: {
    color: 'crimson',
    fontSize: '0.75rem',
    whiteSpace: 'pre-wrap',
  },
}