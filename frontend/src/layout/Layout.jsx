import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/ContextoAuth.jsx'

const ENLACES = [
  { a: '/', texto: 'Inicio' },
  { a: '/punto-venta', texto: 'Punto de venta' },
  { a: '/productos', texto: 'Productos' },
  { a: '/categorias', texto: 'Categorias' },
  { a: '/clientes', texto: 'Clientes' },
  { a: '/facturas', texto: 'Facturas' },
  { a: '/pagos', texto: 'Pagos' },
  { a: '/reportes', texto: 'Reportes' },
  { a: '/usuarios', texto: 'Usuarios', soloAdmin: true },
  { a: '/diagnostico', texto: 'Diagnostico' },
]

export default function Layout() {
  const { usuario, logout, esAdmin } = useAuth()
  const navegar = useNavigate()

  // VULNERABILIDAD (BAC): los enlaces se filtran en el frontend, pero las rutas
  // siguen siendo accesibles y la API no comprueba el rol en ningun momento
  const visibles = ENLACES.filter((e) => !e.soloAdmin || esAdmin())

  return (
    <div style={{ display: 'flex', minHeight: '100vh', fontFamily: 'system-ui, sans-serif' }}>
      <aside style={{ width: 210, background: '#111827', color: '#e5e7eb', padding: '1rem' }}>
        <h2 style={{ fontSize: '1rem' }}>Punto de Venta</h2>

        <nav style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
          {visibles.map((enlace) => (
            <NavLink
              key={enlace.a}
              to={enlace.a}
              end={enlace.a === '/'}
              style={({ isActive }) => ({
                color: isActive ? '#fff' : '#9ca3af',
                textDecoration: 'none',
                padding: '6px 8px',
                borderRadius: 4,
                background: isActive ? '#1f2937' : 'transparent',
              })}
            >
              {enlace.texto}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        <header
          style={{
            padding: '0.75rem 1.25rem',
            borderBottom: '1px solid #e5e7eb',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <strong>Supermercado El Vendedor</strong>

          <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
            {/* VULNERABILIDAD: el nombre de usuario se inyecta como HTML sin escapar */}
            <span dangerouslySetInnerHTML={{ __html: usuario?.username ?? '' }} />
            <button
              onClick={async () => {
                await logout()
                navegar('/login')
              }}
            >
              Cerrar sesion
            </button>
          </div>
        </header>

        <main style={{ flex: 1, padding: '1.25rem' }}>
          <Outlet />
        </main>

        <footer style={{ padding: '0.75rem 1.25rem', borderTop: '1px solid #e5e7eb', color: '#6b7280' }}>
          <Link to="/diagnostico">Diagnostico del sistema</Link>
        </footer>
      </div>
    </div>
  )
}