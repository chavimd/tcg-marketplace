import { useEffect, useState, type FormEvent } from "react";
import { useSearchParams } from "react-router-dom";
import { PublicationCard } from "../components/PublicationCard";
import { Empty, ErrorNote, Loading, Pagination } from "../components/ui";
import { queryString } from "../lib/api";
import type { Juego, Page, Publicacion } from "../lib/types";
import { useResource } from "../lib/useResource";

const PAGE_SIZE = 12;

export function Catalog() {
  const [params, setParams] = useSearchParams();
  const game = params.get("game") ?? "";
  const rarity = params.get("rarity") ?? "";
  const term = params.get("q") ?? "";
  const page = Math.max(0, Number.parseInt(params.get("page") ?? "0", 10) || 0);

  const [draft, setDraft] = useState(term);

  useEffect(() => {
    setDraft(term);
  }, [term]);

  const juegos = useResource<Juego[]>("/api/games");
  const publicaciones = useResource<Page<Publicacion>>(
    `/api/publications${queryString({
      game,
      rarity,
      q: term,
      page,
      size: PAGE_SIZE,
    })}`,
  );

  function update(next: Record<string, string | number | null>) {
    const merged = new URLSearchParams(params);
    for (const [key, value] of Object.entries(next)) {
      if (value === null || value === "") {
        merged.delete(key);
      } else {
        merged.set(key, String(value));
      }
    }
    setParams(merged, { replace: true });
  }

  function onSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    update({ q: draft.trim(), page: null });
  }

  const hasFilters = Boolean(game || rarity || term);
  const total = publicaciones.data?.content.length ?? 0;
  const totalElements = publicaciones.data?.totalElements ?? 0;

  return (
    <>
      <section className="bg-ink text-paper">
        <div className="shell flex flex-wrap items-end justify-between gap-4 py-9 sm:py-11">
          <div>
            <h1 className="text-[clamp(2.5rem,6vw,4rem)]">Catálogo</h1>
            <p className="mt-2 max-w-lg text-sm leading-relaxed text-paper/60">
              Cada publicación es una carta real con dueño y stock. Si algo te sirve, propón el
              cambio.
            </p>
          </div>
          <p className="tabnum text-sm text-paper/60">
            {publicaciones.loading
              ? "Contando…"
              : `${totalElements} ${totalElements === 1 ? "publicación" : "publicaciones"}`}
          </p>
        </div>
      </section>

      <div className="border-b border-rule bg-paper">
        <form className="shell flex flex-wrap items-end gap-3 py-4" onSubmit={onSearch}>
          <label className="min-w-[14rem] flex-1">
            <span className="sr-only">Buscar carta</span>
            <input
              className="field"
              type="search"
              placeholder="Buscar por nombre de carta"
              value={draft}
              onChange={(event) => setDraft(event.target.value)}
            />
          </label>

          <label className="w-full sm:w-48">
            <span className="sr-only">Juego</span>
            <select
              className="field"
              value={game}
              onChange={(event) => update({ game: event.target.value, page: null })}
            >
              <option value="">Todos los juegos</option>
              {juegos.data?.map((juego) => (
                <option key={juego.id} value={juego.nombre}>
                  {juego.nombre}
                </option>
              ))}
            </select>
          </label>

          <label className="w-full sm:w-40">
            <span className="sr-only">Rareza</span>
            <input
              className="field"
              placeholder="Rareza"
              value={rarity}
              onChange={(event) => update({ rarity: event.target.value, page: null })}
            />
          </label>

          <button type="submit" className="btn btn-ink">
            Buscar
          </button>

          {hasFilters ? (
            <button
              type="button"
              className="btn btn-outline"
              onClick={() => setParams(new URLSearchParams(), { replace: true })}
            >
              Limpiar
            </button>
          ) : null}
        </form>
      </div>

      <section className="shell py-8 sm:py-10">
        {publicaciones.loading ? <Loading label="Cargando publicaciones" /> : null}

        {publicaciones.error ? (
          <ErrorNote message={publicaciones.error} onRetry={publicaciones.reload} />
        ) : null}

        {!publicaciones.loading && !publicaciones.error ? (
          total > 0 ? (
            <>
              <div className="grid grid-cols-2 gap-x-5 gap-y-9 md:grid-cols-3 lg:grid-cols-4">
                {publicaciones.data?.content.map((publicacion) => (
                  <PublicationCard key={publicacion.id} publicacion={publicacion} />
                ))}
              </div>
              <Pagination
                page={page}
                totalPages={publicaciones.data?.totalPages ?? 1}
                onChange={(next) => update({ page: next })}
              />
            </>
          ) : (
            <Empty
              title="No hay cartas con esos filtros"
              action={
                hasFilters ? (
                  <button
                    type="button"
                    className="btn btn-outline"
                    onClick={() => setParams(new URLSearchParams(), { replace: true })}
                  >
                    Quitar filtros
                  </button>
                ) : undefined
              }
            >
              Prueba con otro juego, otra rareza o busca por el nombre exacto de la carta.
            </Empty>
          )
        ) : null}
      </section>
    </>
  );
}
