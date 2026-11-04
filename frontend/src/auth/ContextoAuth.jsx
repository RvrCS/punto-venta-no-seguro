import { createContext, useContext, useEffect, useState } from 'react'
import cliente from '../api/client'

const ContextoAuth = createContext(null)

// VULNERABILIDAD: la sesion y las credenciales se guardan en localStorage,
// accesible desde cualquier script inyectado en la pagina (XSS -> robo de sesion)
const CLAVE_USUARIO = 'pv_usuario'

export function ProveedorAuth({ children }) {
  const [usuario, setUsuario] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem(CLAVE_USUARIO))
    } catch {
      return null
    }
  })
  const [cargando, setCargando] = useState(true)

  useEffect(() => {
    cliente
      .get('/auth/yo')
      .then((r) => {
        setUsuario(r.data)
        localStorage.setItem(CLAVE_USUARIO, JSON.stringify(r.data))
      })
      .catch(() => {
        setUsuario(null)
        localStorage.removeItem(CLAVE_USUARIO)
      })
      .finally(() => setCargando(false))
  }, [])

  const login = async (username, password) => {
    const { data } = await cliente.post('/auth/login', { username, password })
    // VULNERABILIDAD: el backend devuelve las credenciales y se guardan en el navegador
    const sesion = {
      username: data.usuario?.username ?? username,
      authorities: data.autoridades,
      credenciales: data.credenciales,
    }
    setUsuario(sesion)
    localStorage.setItem(CLAVE_USUARIO, JSON.stringify(sesion))
    return sesion
  }

  const logout = async () => {
    try {
      await cliente.post('/auth/logout')
    } finally {
      setUsuario(null)
      localStorage.removeItem(CLAVE_USUARIO)
    }
  }

  const esAdmin = () => (usuario?.authorities ?? []).some((a) => a.authority === 'ROL_ADMIN')

  return (
    <ContextoAuth.Provider value={{ usuario, cargando, login, logout, esAdmin }}>
      {children}
    </ContextoAuth.Provider>
  )
}

export function useAuth() {
  return useContext(ContextoAuth)
}