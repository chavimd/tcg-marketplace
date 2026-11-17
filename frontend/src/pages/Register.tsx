import { useState, type FormEvent } from "react";
import { Link, Navigate, useNavigate, useSearchParams } from "react-router-dom";
import { AuthShell } from "../components/AuthShell";
import { ApiError } from "../lib/api";
import { useAuth } from "../lib/auth";

export function Register() {
  const { user, register } = useAuth();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const next = params.get("next") ?? "/coleccion";

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
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
      await register(username.trim(), email.trim(), password);
      navigate(next, { replace: true });
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : "No se pudo crear la cuenta.");
    } finally {
      setSending(false);
    }
  }

  return (
    <AuthShell
      title="Crear cuenta"
      intro="Con una cuenta puedes publicar tu inventario, proponer trueques y hablar con la otra parte."
      footer={
        <>
          ¿Ya tienes cuenta?{" "}
          <Link to="/entrar" className="font-medium link-underline">
            Entrar
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
            className="field"
            autoComplete="username"
            minLength={3}
            maxLength={50}
            required
            value={username}
            onChange={(event) => setUsername(event.target.value)}
          />
          <p className="hint mt-1">Entre 3 y 50 caracteres. Es el nombre que verán los demás.</p>
        </div>

        <div>
          <label className="label" htmlFor="email">
            Correo
          </label>
          <input
            id="email"
            type="email"
            className="field"
            autoComplete="email"
            required
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
        </div>

        <div>
          <label className="label" htmlFor="password">
            Contraseña
          </label>
          <input
            id="password"
            type="password"
            className="field"
            autoComplete="new-password"
            minLength={8}
            maxLength={72}
            required
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
          <p className="hint mt-1">Mínimo 8 caracteres.</p>
        </div>

        {error ? (
          <p className="form-error" role="alert">
            {error}
          </p>
        ) : null}

        <button type="submit" className="btn btn-ink w-full" disabled={sending}>
          {sending ? "Creando cuenta…" : "Crear cuenta"}
        </button>
      </form>
    </AuthShell>
  );
}
