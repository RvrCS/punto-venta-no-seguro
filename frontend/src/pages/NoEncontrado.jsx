import { Link } from 'react-router-dom'

export default function NoEncontrado() {
  return (
    <div style={{ padding: '3rem', fontFamily: 'system-ui, sans-serif' }}>
      <h1>404</h1>
      <p>La pagina solicitada no existe.</p>
      <Link to="/">Volver al inicio</Link>
    </div>
  )
}