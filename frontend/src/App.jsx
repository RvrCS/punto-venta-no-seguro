import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { ProveedorAuth, useAuth } from './auth/ContextoAuth.jsx'
import Layout from './layout/Layout.jsx'
import ErrorBoundary from './components/ErrorBoundary.jsx'
import Login from './pages/Login.jsx'
import Registro from './pages/Registro.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Productos from './pages/Productos.jsx'
import ProductoForm from './pages/ProductoForm.jsx'
import Categorias from './pages/Categorias.jsx'
import Clientes from './pages/Clientes.jsx'
import PuntoVenta from './pages/PuntoVenta.jsx'
import Pagos from './pages/Pagos.jsx'
import Facturas from './pages/Facturas.jsx'
import FacturaDetalle from './pages/FacturaDetalle.jsx'
import Reportes from './pages/Reportes.jsx'
import Usuarios from './pages/Usuarios.jsx'
import Diagnostico from './pages/Diagnostico.jsx'
import NoEncontrado from './pages/NoEncontrado.jsx'

// VULNERABILIDAD (BAC): el guard solo mira el rol guardado en localStorage,
// que el usuario puede editar desde el navegador. La API tampoco valida nada,
// asi que la proteccion es puramente cosmetica.
function RutaProtegida({ children, soloAdmin = false }) {
  const { usuario, cargando, esAdmin } = useAuth()

  if (cargando) return <p>Cargando...</p>
  if (!usuario) return <Navigate to="/login" replace />
  if (soloAdmin && !esAdmin()) return <Navigate to="/" replace />

  return children
}

function App() {
  return (
    <BrowserRouter>
      <ErrorBoundary>
        <ProveedorAuth>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/registro" element={<Registro />} />

            <Route
              element={
                <RutaProtegida>
                  <Layout />
                </RutaProtegida>
              }
            >
              <Route path="/" element={<Dashboard />} />
              <Route path="/productos" element={<Productos />} />
              <Route path="/productos/nuevo" element={<ProductoForm />} />
              <Route path="/productos/:id/editar" element={<ProductoForm />} />
              <Route path="/categorias" element={<Categorias />} />
              <Route path="/clientes" element={<Clientes />} />
              <Route path="/punto-venta" element={<PuntoVenta />} />
              <Route path="/pagos" element={<Pagos />} />
              <Route path="/facturas" element={<Facturas />} />
              <Route path="/facturas/:id" element={<FacturaDetalle />} />
              <Route path="/reportes" element={<Reportes />} />
              <Route path="/diagnostico" element={<Diagnostico />} />
              <Route
                path="/usuarios"
                element={
                  <RutaProtegida soloAdmin>
                    <Usuarios />
                  </RutaProtegida>
                }
              />
            </Route>

            <Route path="*" element={<NoEncontrado />} />
          </Routes>
        </ProveedorAuth>
      </ErrorBoundary>
    </BrowserRouter>
  )
}

export default App