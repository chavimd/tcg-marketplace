import { useState, type FormEvent } from "react";
import { ErrorNote, Loading } from "../components/ui";
import { api, ApiError } from "../lib/api";
import type { Carta, Juego, Page, Publicacion } from "../lib/types";
import { useResource } from "../lib/useResource";

type Feedback = { kind: "ok" | "error"; text: string } | null;

export function Admin() {
  const juegos = useResource<Juego[]>("/api/games");
  const cartas = useResource<Page<Carta>>("/api/cards?size=100");

  const [juegoFeedback, setJuegoFeedback] = useState<Feedback>(null);
  const [cartaFeedback, setCartaFeedback] = useState<Feedback>(null);
  const [inventarioFeedback, setInventarioFeedback] = useState<Feedback>(null);

  const [enviando, setEnviando] = useState<"juego" | "carta" | "inventario" | null>(null);

  async function crearJuego(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    setEnviando("juego");
    setJuegoFeedback(null);
    try {
      const juego = await api<Juego>("/api/games", {
        method: "POST",
        body: {
          nombre: String(data.get("nombre") ?? "").trim(),
          descripcion: String(data.get("descripcion") ?? "").trim() || null,
        },
      });
      setJuegoFeedback({ kind: "ok", text: `Juego creado: ${juego.nombre}.` });
      form.reset();
      juegos.reload();
    } catch (cause) {
      setJuegoFeedback({
        kind: "error",
        text: cause instanceof ApiError ? cause.message : "No se pudo crear el juego.",
      });
    } finally {
      setEnviando(null);
    }
  }

  async function crearCarta(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    setEnviando("carta");
    setCartaFeedback(null);
    try {
      const carta = await api<Carta>("/api/cards", {
        method: "POST",
        body: {
          juegoId: Number(data.get("juegoId")),
          nombre: String(data.get("nombre") ?? "").trim(),
          numeroSet: String(data.get("numeroSet") ?? "").trim(),
          setNombre: String(data.get("setNombre") ?? "").trim(),
          rareza: String(data.get("rareza") ?? "").trim(),
          tipo: String(data.get("tipo") ?? "").trim() || null,
          color: String(data.get("color") ?? "").trim() || null,
          textoCarta: String(data.get("textoCarta") ?? "").trim() || null,
          imagenUrl: String(data.get("imagenUrl") ?? "").trim() || null,
        },
      });
      setCartaFeedback({ kind: "ok", text: `Carta creada: ${carta.nombre} (#${carta.id}).` });
      form.reset();
      cartas.reload();
    } catch (cause) {
      setCartaFeedback({
        kind: "error",
        text: cause instanceof ApiError ? cause.message : "No se pudo crear la carta.",
      });
    } finally {
      setEnviando(null);
    }
  }

  async function asignarInventario(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    setEnviando("inventario");
    setInventarioFeedback(null);
    try {
      const publicacion = await api<Publicacion>("/api/inventory/grants", {
        method: "POST",
        body: {
          username: String(data.get("username") ?? "").trim(),
          cardId: Number(data.get("cardId")),
          quantity: Number(data.get("quantity")),
          precioUnitario: Number(data.get("precioUnitario")),
          estadoConservacion: String(data.get("estadoConservacion") ?? "").trim() || null,
        },
      });
      setInventarioFeedback({
        kind: "ok",
        text: `Asignadas ${publicacion.stock} unidades a @${publicacion.username}.`,
      });
      form.reset();
    } catch (cause) {
      setInventarioFeedback({
        kind: "error",
        text: cause instanceof ApiError ? cause.message : "No se pudo asignar el inventario.",
      });
    } finally {
      setEnviando(null);
    }
  }

  return (
    <section className="shell py-8 sm:py-12">
      <div className="border-b border-rule pb-4">
        <h1 className="text-[clamp(2.2rem,5vw,3.4rem)]">Administración</h1>
        <p className="mt-2 max-w-xl text-sm text-muted">
          Alta de juegos y cartas del catálogo, y asignación de inventario a una cuenta. El
          inventario nace disponible para intercambio.
        </p>
      </div>

      <div className="mt-8 grid gap-12 lg:grid-cols-3">
        <form className="space-y-4" onSubmit={crearJuego}>
          <div>
            <h2 className="text-2xl">Nuevo juego</h2>
            <p className="hint mt-1">La franquicia que agrupa las cartas.</p>
          </div>
          <div>
            <label className="label" htmlFor="juego-nombre">
              Nombre
            </label>
            <input id="juego-nombre" name="nombre" className="field" required maxLength={50} />
          </div>
          <div>
            <label className="label" htmlFor="juego-descripcion">
              Descripción
            </label>
            <textarea
              id="juego-descripcion"
              name="descripcion"
              className="field h-24 py-2"
              rows={3}
            />
          </div>
          <button type="submit" className="btn btn-ink w-full" disabled={enviando === "juego"}>
            {enviando === "juego" ? "Creando…" : "Crear juego"}
          </button>
          <FeedbackLine feedback={juegoFeedback} />
        </form>

        <form className="space-y-4" onSubmit={crearCarta}>
          <div>
            <h2 className="text-2xl">Nueva carta</h2>
            <p className="hint mt-1">Entra al catálogo, todavía sin dueño.</p>
          </div>

          <div>
            <label className="label" htmlFor="carta-juego">
              Juego
            </label>
            <select id="carta-juego" name="juegoId" className="field" required>
              <option value="">Elige un juego</option>
              {juegos.data?.map((juego) => (
                <option key={juego.id} value={juego.id}>
                  {juego.nombre}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="label" htmlFor="carta-nombre">
              Nombre
            </label>
            <input id="carta-nombre" name="nombre" className="field" required maxLength={100} />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label" htmlFor="carta-set-nombre">
                Set
              </label>
              <input
                id="carta-set-nombre"
                name="setNombre"
                className="field"
                required
                maxLength={100}
              />
            </div>
            <div>
              <label className="label" htmlFor="carta-numero">
                Número
              </label>
              <input
                id="carta-numero"
                name="numeroSet"
                className="field"
                required
                maxLength={30}
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label" htmlFor="carta-rareza">
                Rareza
              </label>
              <input
                id="carta-rareza"
                name="rareza"
                className="field"
                required
                maxLength={20}
              />
            </div>
            <div>
              <label className="label" htmlFor="carta-tipo">
                Tipo
              </label>
              <input id="carta-tipo" name="tipo" className="field" maxLength={50} />
            </div>
          </div>

          <div>
            <label className="label" htmlFor="carta-imagen">
              Imagen (URL)
            </label>
            <input id="carta-imagen" name="imagenUrl" className="field" type="url" />
          </div>

          <button type="submit" className="btn btn-ink w-full" disabled={enviando === "carta"}>
            {enviando === "carta" ? "Creando…" : "Crear carta"}
          </button>
          <FeedbackLine feedback={cartaFeedback} />
        </form>

        <form className="space-y-4" onSubmit={asignarInventario}>
          <div>
            <h2 className="text-2xl">Asignar inventario</h2>
            <p className="hint mt-1">Pone cartas del catálogo en la colección de un usuario.</p>
          </div>

          <div>
            <label className="label" htmlFor="grant-username">
              Usuario
            </label>
            <input
              id="grant-username"
              name="username"
              className="field"
              required
              maxLength={50}
            />
          </div>

          <div>
            <label className="label" htmlFor="grant-carta">
              Carta
            </label>
            <select id="grant-carta" name="cardId" className="field" required>
              <option value="">Elige una carta</option>
              {cartas.data?.content.map((carta) => (
                <option key={carta.id} value={carta.id}>
                  {carta.nombre} · {carta.setNombre} (#{carta.id})
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label" htmlFor="grant-cantidad">
                Cantidad
              </label>
              <input
                id="grant-cantidad"
                name="quantity"
                type="number"
                className="field"
                min={1}
                defaultValue={1}
                required
              />
            </div>
            <div>
              <label className="label" htmlFor="grant-precio">
                Valor ref.
              </label>
              <input
                id="grant-precio"
                name="precioUnitario"
                type="number"
                className="field"
                min={0.01}
                step={0.01}
                defaultValue={1}
                required
              />
            </div>
          </div>

          <div>
            <label className="label" htmlFor="grant-estado">
              Estado de conservación
            </label>
            <input
              id="grant-estado"
              name="estadoConservacion"
              className="field"
              placeholder="Near Mint, Played…"
              maxLength={30}
            />
          </div>

          <button
            type="submit"
            className="btn btn-ink w-full"
            disabled={enviando === "inventario"}
          >
            {enviando === "inventario" ? "Asignando…" : "Asignar inventario"}
          </button>
          <FeedbackLine feedback={inventarioFeedback} />
        </form>
      </div>

      {(juegos.loading || cartas.loading) && <Loading label="Cargando catálogo" />}
      {juegos.error ? <ErrorNote message={juegos.error} onRetry={juegos.reload} /> : null}
      {cartas.error ? <ErrorNote message={cartas.error} onRetry={cartas.reload} /> : null}
    </section>
  );
}

function FeedbackLine({ feedback }: { feedback: Feedback }) {
  if (!feedback) return null;
  return (
    <p className={feedback.kind === "error" ? "form-error" : "text-sm text-ink"} role="status">
      {feedback.text}
    </p>
  );
}
