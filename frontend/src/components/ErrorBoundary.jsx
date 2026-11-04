import { Component } from 'react'

// VULNERABILIDAD: el error boundary muestra la respuesta cruda del backend,
// que incluye el stack trace completo del servidor, la clase de la excepcion
// y el mensaje original de MySQL
export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { error: null }
  }

  static getDerivedStateFromError(error) {
    return { error }
  }

  componentDidCatch(error, info) {
    // VULNERABILIDAD: se registra el error con datos sensibles en la consola
    console.error('Error capturado:', error, info)
  }

  render() {
    const { error } = this.state
    if (!error) return this.props.children

    const detalleServidor = error.response?.data

    return (
      <div style={{ padding: '2rem', fontFamily: 'system-ui, sans-serif' }}>
        <h1>Ocurrio un error</h1>

        {detalleServidor ? (
          <>
            <p>
              El servidor respondio con: {error.response.status} {error.response.statusText}
            </p>
            <pre style={{ whiteSpace: 'pre-wrap', background: '#fee2e2', padding: '1rem' }}>
              {typeof detalleServidor === 'string' ? detalleServidor : JSON.stringify(detalleServidor, null, 2)}
            </pre>
          </>
        ) : (
          <pre style={{ whiteSpace: 'pre-wrap', background: '#f4f4f5', padding: '1rem' }}>{error.stack}</pre>
        )}

        <button onClick={() => window.location.reload()}>Recargar</button>
      </div>
    )
  }
}