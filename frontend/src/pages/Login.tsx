import { useState, type FormEvent } from "react";
import { Link, Navigate, useNavigate, useSearchParams } from "react-router-dom";
import { AuthShell } from "../components/AuthShell";
import { ApiError } from "../lib/api";
import { useAuth } from "../lib/auth";

export function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const next = params.get("next") ?? "/catalogo";

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [sending, setSending] = useState(false);

  if (user) {
    return <Navigate to={next} replace />;
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSending(true);
    try {
      await login(username.trim(), password);
      navigate(next, { replace: true });
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : "No se pudo iniciar sesión.");
    } finally {
      setSending(false);
    }
  }

  return (
    <AuthShell
      title="Entrar"
      intro="Accede para ver tu colección, proponer intercambios y responder los que te lleguen."
      footer={
        <>
          ¿Todavía no tienes cuenta?{" "}
          <Link
            to={`/registro${next !== "/catalogo" ? `?next=${encodeURIComponent(next)}` : ""}`}
            className="font-medium link-underline"
          >
            Crear una cuenta
          </Link>
        </>
      }
    >
      <form className="space-y-4" onSubmit={onSubmit}>
        <div>
          <label className="label" htmlFor="username">
            Usuario
          </label>
          <input
            id="username"
            name="username"
            className="field"
            autoComplete="username"
            required
            value={username}
            onChange={(event) => setUsername(event.target.value)}
          />
        </div>

        <div>
          <label className="label" htmlFor="password">
            Contraseña
          </label>
          <input
            id="password"
            name="password"
            type="password"
            className="field"
            autoComplete="current-password"
            required
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
        </div>

        {error ? (
          <p className="form-error" role="alert">
            {error}
          </p>
        ) : null}

        <button type="submit" className="btn btn-ink w-full" disabled={sending}>
          {sending ? "Entrando…" : "Entrar"}
        </button>
      </form>
    </AuthShell>
  );
}
