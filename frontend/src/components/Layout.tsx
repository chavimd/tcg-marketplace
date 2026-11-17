import { useEffect } from "react";
import { Link, NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../lib/auth";

const links = [
  { to: "/catalogo", label: "Catálogo" },
  { to: "/coleccion", label: "Mi colección" },
  { to: "/trueques", label: "Trueques" },
];

function navClass({ isActive }: { isActive: boolean }): string {
  return `relative flex shrink-0 items-center whitespace-nowrap py-4 text-sm font-medium transition-colors ${
    isActive ? "text-ink" : "text-muted hover:text-ink"
  }`;
}

export function Layout() {
  const { user, loading, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: "instant" as ScrollBehavior });
  }, [location.pathname]);

  return (
    <div className="flex min-h-screen flex-col">
      <a
        href="#contenido"
        className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-3 focus:z-50 focus:bg-ink focus:px-4 focus:py-2 focus:text-sm focus:text-white"
      >
        Saltar al contenido
      </a>

      <header className="sticky top-0 z-40 border-b border-rule bg-paper/95 backdrop-blur">
        <div className="shell flex flex-wrap items-center gap-x-6">
          <Link to="/" className="order-1 flex shrink-0 items-baseline gap-2 py-3">
            <span className="display text-[1.6rem] leading-none">Sobrecambio</span>
            <span className="hidden text-[0.6875rem] font-medium text-muted lg:inline">
              solo intercambios
            </span>
          </Link>

          <nav
            aria-label="Principal"
            className="order-3 -mx-5 flex w-full items-stretch gap-6 overflow-x-auto px-5 md:order-2 md:mx-0 md:w-auto md:min-w-0 md:flex-1 md:px-0"
          >
            {links.map((link) => (
              <NavLink key={link.to} to={link.to} className={navClass}>
                {({ isActive }) => (
                  <>
                    {link.label}
                    <span
                      className={`absolute inset-x-0 bottom-0 h-0.5 ${isActive ? "bg-signal" : "bg-transparent"}`}
                      aria-hidden="true"
                    />
                  </>
                )}
              </NavLink>
            ))}
            {user?.rol === "ADMIN" ? (
              <NavLink to="/admin" className={navClass}>
                {({ isActive }) => (
                  <>
                    Admin
                    <span
                      className={`absolute inset-x-0 bottom-0 h-0.5 ${isActive ? "bg-signal" : "bg-transparent"}`}
                      aria-hidden="true"
                    />
                  </>
                )}
              </NavLink>
            ) : null}
          </nav>

          <div className="order-2 ml-auto flex shrink-0 items-center gap-3 md:order-3">
            {loading ? (
              <span className="h-8 w-24 animate-pulse rounded bg-paper-2" aria-hidden="true" />
            ) : user ? (
              <>
                <Link to="/coleccion" className="hidden text-sm font-medium sm:inline">
                  @{user.username}
                </Link>
                <button
                  type="button"
                  className="btn btn-outline btn-sm"
                  onClick={() => {
                    logout();
                    navigate("/");
                  }}
                >
                  Salir
                </button>
              </>
            ) : (
              <>
                <Link to="/entrar" className="hidden text-sm font-medium sm:inline">
                  Entrar
                </Link>
                <Link to="/registro" className="btn btn-ink btn-sm">
                  Crear cuenta
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      <main id="contenido" className="flex-1">
        <Outlet />
      </main>

      <footer className="mt-16 bg-ink text-paper sm:mt-24">
        <div className="shell grid gap-10 py-12 md:grid-cols-[1.6fr_1fr_1fr]">
          <div>
            <p className="display text-3xl">Sobrecambio</p>
            <p className="mt-3 max-w-sm text-sm leading-relaxed text-paper/60">
              Cartas que cambian de manos, no de precio. Publicas lo que tienes, pides lo que te
              falta y el stock se mueve cuando ambos aceptan.
            </p>
          </div>
          <nav aria-label="Explorar">
            <p className="text-sm font-semibold text-paper/50">Explorar</p>
            <ul className="mt-3 space-y-2 text-sm">
              <li>
                <Link to="/catalogo" className="hover:text-signal">
                  Catálogo
                </Link>
              </li>
              <li>
                <Link to="/trueques" className="hover:text-signal">
                  Trueques
                </Link>
              </li>
              <li>
                <Link to="/coleccion" className="hover:text-signal">
                  Mi colección
                </Link>
              </li>
            </ul>
          </nav>
          <div>
            <p className="text-sm font-semibold text-paper/50">Cuenta</p>
            <ul className="mt-3 space-y-2 text-sm">
              {user ? (
                <li className="text-paper/70">Sesión como @{user.username}</li>
              ) : (
                <>
                  <li>
                    <Link to="/entrar" className="hover:text-signal">
                      Entrar
                    </Link>
                  </li>
                  <li>
                    <Link to="/registro" className="hover:text-signal">
                      Crear cuenta
                    </Link>
                  </li>
                </>
              )}
            </ul>
          </div>
        </div>
        <div className="border-t border-rule-dark">
          <div className="shell flex flex-col gap-2 py-4 text-xs text-paper/45 sm:flex-row sm:items-center sm:justify-between">
            <p>Proyecto académico · sin pagos ni saldo</p>
            <p>Pokémon, Yu-Gi-Oh! y Magic son marcas de sus respectivos titulares</p>
          </div>
        </div>
      </footer>
    </div>
  );
}
