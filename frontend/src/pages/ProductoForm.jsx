import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import cliente from '../api/client'

const VACIO = {
  sku: '',
  nombre: '',
  descripcion: '',
  precio: 0,
  costo: 0,
  stock: 0,
  stockMinimo: 5,
  categoriaId: '',
  activo: true,
}

export default function ProductoForm() {
  const { id } = useParams()
  const navegar = useNavigate()
  const editando = Boolean(id)

  const [form, setForm] = useState(VACIO)
  const [categorias, setCategorias] = useState([])
  const [imagen, setImagen] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    cliente.get('/categorias').then((r) => setCategorias(r.data))
    if (editando) {
      cliente
        .get(`/productos/${id}`)
        .then((r) => setForm({ ...VACIO, ...r.data }))
        .catch((e) => setError(e.response?.data ?? e.message))
    }
  }, [id])

  const cambiar = (campo) => (evento) => {
    const valor = evento.target.type === 'checkbox' ? evento.target.checked : evento.target.value
    setForm({ ...form, [campo]: valor })
  }

  const enviar = async (evento) => {
    evento.preventDefault()
    setError(null)
    try {
      if (editando) {
        await cliente.put(`/productos/${id}`, form)
      } else {
        // VULNERABILIDAD: la descripcion puede contener HTML y scripts, se envia sin sanear
        const datos = new FormData()
        datos.append('producto', new Blob([JSON.stringify(form)], { type: 'application/json' }))
        if (imagen) datos.append('imagen', imagen)
        await cliente.post('/productos', datos)
      }
      navegar('/productos')
    } catch (e) {
      setError(e.response?.data ?? e.message)
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, maxWidth: 560 }}>
      <h1>{editando ? 'Editar producto' : 'Nuevo producto'}</h1>

      <form onSubmit={enviar} style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        <label>
          SKU
          <input value={form.sku} onChange={cambiar('sku')} />
        </label>

        <label>
          Nombre
          <input value={form.nombre} onChange={cambiar('nombre')} />
        </label>

        <label>
          Descripcion (admite HTML)
          {/* VULNERABILIDAD: no hay sanitizado ni escapado de la descripcion */}
          <textarea rows={3} value={form.descripcion} onChange={cambiar('descripcion')} />
        </label>

        <label>
          Precio
          <input type="number" step="0.01" value={form.precio} onChange={cambiar('precio')} />
        </label>

        <label>
          Costo
          <input type="number" step="0.01" value={form.costo} onChange={cambiar('costo')} />
        </label>

        <label>
          Stock
          <input type="number" value={form.stock} onChange={cambiar('stock')} />
        </label>

        <label>
          Stock minimo
          <input type="number" value={form.stockMinimo} onChange={cambiar('stockMinimo')} />
        </label>

        <label>
          Categoria
          <select value={form.categoriaId} onChange={cambiar('categoriaId')}>
            <option value="">Sin categoria</option>
            {categorias.map((c) => (
              <option key={c.id} value={c.id}>
                {c.nombre}
              </option>
            ))}
          </select>
        </label>

        {!editando && (
          <label>
            Imagen
            {/* VULNERABILIDAD: no se valida tipo ni tamano del archivo */}
            <input type="file" onChange={(e) => setImagen(e.target.files?.[0] ?? null)} />
          </label>
        )}

        <label>
          <input type="checkbox" checked={form.activo} onChange={cambiar('activo')} /> Activo
        </label>

        {error && <pre style={{ color: 'crimson' }}>{JSON.stringify(error, null, 2)}</pre>}

        <div style={{ display: 'flex', gap: 8 }}>
          <button type="submit">Guardar</button>
          <button type="button" onClick={() => navegar('/productos')}>
            Cancelar
          </button>
        </div>
      </form>
    </div>
  )
}