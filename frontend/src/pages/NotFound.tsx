import { Link } from "react-router-dom";

export function NotFound() {
  return (
    <section className="shell flex min-h-[60vh] flex-col justify-center py-16">
      <p className="tabnum text-sm text-muted">Error 404</p>
      <h1 className="mt-2 text-[clamp(2.6rem,7vw,5rem)]">Esta página no está en el catálogo.</h1>
      <p className="mt-3 max-w-md text-sm text-muted">
        Puede que el enlace haya cambiado o que la publicación ya no exista.
      </p>
      <div className="mt-7 flex flex-wrap gap-3">
        <Link to="/catalogo" className="btn btn-ink">
          Ir al catálogo
        </Link>
        <Link to="/" className="btn btn-outline">
          Volver al inicio
        </Link>
      </div>
    </section>
  );
}
