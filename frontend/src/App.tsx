import type { ReactNode } from "react";
import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import { Layout } from "./components/Layout";
import { Loading } from "./components/ui";
import { useAuth } from "./lib/auth";
import { Admin } from "./pages/Admin";
import { Catalog } from "./pages/Catalog";
import { Collection } from "./pages/Collection";
import { Home } from "./pages/Home";
import { Login } from "./pages/Login";
import { NewTrade } from "./pages/NewTrade";
import { NotFound } from "./pages/NotFound";
import { PublicationDetail } from "./pages/PublicationDetail";
import { Register } from "./pages/Register";
import { TradeDetail } from "./pages/TradeDetail";
import { Trades } from "./pages/Trades";

function RequireAuth({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="shell py-16">
        <Loading label="Comprobando tu sesión" />
      </div>
    );
  }

  if (!user) {
    const next = `${location.pathname}${location.search}`;
    return <Navigate to={`/entrar?next=${encodeURIComponent(next)}`} replace />;
  }

  return <>{children}</>;
}

function RequireAdmin({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  if (user?.rol !== "ADMIN") {
    return <Navigate to="/" replace />;
  }
  return <>{children}</>;
}

export function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Home />} />
        <Route path="catalogo" element={<Catalog />} />
        <Route path="catalogo/:id" element={<PublicationDetail />} />
        <Route path="entrar" element={<Login />} />
        <Route path="registro" element={<Register />} />
        <Route
          path="coleccion"
          element={
            <RequireAuth>
              <Collection />
            </RequireAuth>
          }
        />
        <Route
          path="trueques"
          element={
            <RequireAuth>
              <Trades />
            </RequireAuth>
          }
        />
        <Route
          path="trueques/nuevo"
          element={
            <RequireAuth>
              <NewTrade />
            </RequireAuth>
          }
        />
        <Route
          path="trueques/:id"
          element={
            <RequireAuth>
              <TradeDetail />
            </RequireAuth>
          }
        />
        <Route
          path="admin"
          element={
            <RequireAuth>
              <RequireAdmin>
                <Admin />
              </RequireAdmin>
            </RequireAuth>
          }
        />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
}
