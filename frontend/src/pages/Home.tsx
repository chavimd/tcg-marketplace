import { Link } from "react-router-dom";
import { PublicationCard } from "../components/PublicationCard";
import { TradeTicket } from "../components/TradeTicket";
import { ErrorNote, Loading } from "../components/ui";
import { queryString } from "../lib/api";
import type { Juego, Page, Publicacion } from "../lib/types";
import { useResource } from "../lib/useResource";

const steps = [
  {
    title: "Publica tu inventario",
    body: "Cada carta que tienes entra a tu colección con su estado, su stock y su valor de referencia. Tú decides si se ve o no.",
  },
  {
    title: "Propón el cambio",
    body: "Eliges qué ofreces y qué pides de otra colección. La propuesta queda registrada con cartas y cantidades, no con dinero.",
  },
  {
    title: "Cierra el trueque",
    body: "Cuando la otra persona acepta, el stock se mueve entre las dos colecciones en una sola operación. Después se abre el chat.",
  },
];

export function Home() {
  const publicaciones = useResource<Page<Publicacion>>(
    `/api/publications${queryString({ size: 9 })}`,
  );
  const juegos = useResource<Juego[]>("/api/games");

  const destacadas = publicaciones.data?.content ?? [];
  const ofertas = destacadas.slice(0, 2);
  const solicitudes = destacadas.slice(2, 4);

  return (
    <>
      <section className="bg-ink text-paper">
        <div className="shell grid items-center gap-12 py-14 md:grid-cols-[1.05fr_0.95fr] md:gap-16 md:py-20">
          <div className="rise">
            <p className="text-sm text-paper/55">
              Mercado de intercambio · Pokémon, Yu-Gi-Oh!, Magic
            </p>
            <h1 className="mt-4 text-[clamp(2.6rem,6vw,5.2rem)]">
              Cambia cartas.
              <br />
              No las compres.
            </h1>
            <p className="mt-6 max-w-md text-[0.9375rem] leading-relaxed text-paper/70">
              Publica lo que tienes, encuentra lo que te falta y cierra el trueque con stock real.
              Sin pagos, sin saldo y sin comisiones de por medio.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link to="/catalogo" className="btn btn-signal">
                Ver el catálogo
              </Link>
              <a href="#como-funciona" className="btn btn-ghost-dark">
                Cómo funciona
              </a>
            </div>
          </div>

          <div className="rise [animation-delay:120ms]">
            <TradeTicket ofertas={ofertas} solicitudes={solicitudes} />
          </div>
        </div>
      </section>

      <section className="shell py-14 sm:py-16">
        <div className="flex flex-wrap items-end justify-between gap-4 border-b border-rule pb-3">
          <h2 className="text-3xl sm:text-4xl">En la mesa ahora</h2>
          <Link to="/catalogo" className="text-sm font-medium link-underline">
            Ver todo el catálogo
          </Link>
        </div>

        {publicaciones.loading ? <Loading label="Buscando cartas disponibles" /> : null}
        {publicaciones.error ? (
          <div className="mt-6">
            <ErrorNote message={publicaciones.error} onRetry={publicaciones.reload} />
          </div>
        ) : null}

        {!publicaciones.loading && !publicaciones.error ? (
          destacadas.length > 0 ? (
            <div className="mt-7 grid grid-cols-2 gap-x-5 gap-y-9 md:grid-cols-3 xl:grid-cols-4">
              {destacadas.slice(0, 4).map((publicacion) => (
                <PublicationCard key={publicacion.id} publicacion={publicacion} />
              ))}
            </div>
          ) : (
            <p className="mt-6 max-w-md text-sm text-muted">
              Todavía no hay publicaciones visibles. Cuando alguien ponga una carta en la mesa,
              aparecerá aquí.
            </p>
          )
        ) : null}
      </section>

      <section id="como-funciona" className="border-y border-rule bg-paper-2">
        <div className="shell grid gap-8 py-14 md:grid-cols-3 md:gap-10">
          {steps.map((step, index) => (
            <div key={step.title} className="border-t-2 border-ink pt-4">
              <p className="tabnum text-sm font-medium text-muted">{index + 1}</p>
              <h3 className="mt-2 text-2xl">{step.title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-muted">{step.body}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="shell py-14 sm:py-16">
        <div className="grid gap-8 md:grid-cols-[1fr_1.2fr] md:items-start">
          <div>
            <h2 className="text-3xl sm:text-4xl">Un catálogo, varias franquicias</h2>
            <p className="mt-3 max-w-sm text-sm leading-relaxed text-muted">
              El mismo inventario y las mismas reglas para cada juego. Filtra por franquicia, rareza
              o nombre y arma el cambio que te sirva.
            </p>
          </div>
          <div>
            {juegos.loading ? <Loading label="Cargando juegos" /> : null}
            {juegos.error ? <ErrorNote message={juegos.error} onRetry={juegos.reload} /> : null}
            {juegos.data ? (
              <ul className="grid grid-cols-2 gap-x-5 sm:grid-cols-3">
                {juegos.data.map((juego) => (
                  <li key={juego.id} className="border-t border-rule">
                    <Link
                      to={`/catalogo${queryString({ game: juego.nombre })}`}
                      className="flex h-full flex-col justify-between gap-2 py-4 hover:text-signal"
                    >
                      <span className="display text-2xl">{juego.nombre}</span>
                      <span className="text-xs text-muted">
                        {juego.descripcion ?? "Cartas disponibles para intercambio"}
                      </span>
                    </Link>
                  </li>
                ))}
              </ul>
            ) : null}
          </div>
        </div>
      </section>

      <section className="bg-ink text-paper">
        <div className="shell flex flex-col items-start gap-6 py-14 md:flex-row md:items-end md:justify-between">
          <h2 className="max-w-2xl text-[clamp(2rem,5vw,3.6rem)]">
            Lo que te sobra es lo que a otra persona le falta.
          </h2>
          <Link to="/registro" className="btn btn-signal shrink-0">
            Crear cuenta
          </Link>
        </div>
      </section>
    </>
  );
}
