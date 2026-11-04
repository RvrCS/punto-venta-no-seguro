import axios from 'axios'

// VULNERABILIDAD: la cookie de sesion se envia sin restricciones y no hay
// ninguna validacion de la respuesta antes de usarla
const cliente = axios.create({
  baseURL: '/api',
  withCredentials: true,
  timeout: 15000,
})

// VULNERABILIDAD: se escribe la peticion completa en la consola del navegador,
// incluyendo contrasenas y datos de tarjeta
cliente.interceptors.request.use((config) => {
  console.log('[Peticion]', config.method?.toUpperCase(), config.url, config.data)
  return config
})

cliente.interceptors.response.use(
  (respuesta) => {
    console.log('[Respuesta]', respuesta.config.url, respuesta.data)
    return respuesta
  },
  (error) => {
    console.error('[Error]', error.config?.url, error.response?.data)
    return Promise.reject(error)
  },
)

export default cliente