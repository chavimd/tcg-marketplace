import type { ReactNode } from "react";

interface AuthShellProps {
  title: string;
  intro: string;
  children: ReactNode;
  footer: ReactNode;
}

export function AuthShell({ title, intro, children, footer }: AuthShellProps) {
  return (
    <div className="grid min-h-[calc(100vh-3.5rem)] md:grid-cols-2">
      <div className="hidden flex-col justify-between bg-ink p-10 text-paper md:flex lg:p-14">
        <p className="display text-2xl">Sobrecambio</p>
        <div>
          <p className="display max-w-sm text-[clamp(2rem,3.4vw,3.2rem)] leading-[0.95]">
            Tu colección vale por lo que puede cambiar.
          </p>
          <p className="mt-5 max-w-sm text-sm leading-relaxed text-paper/60">
            Aquí no se compra ni se vende: se publica lo que tienes, se pide lo que falta y se
            cierra el trueque cuando las dos partes aceptan.
          </p>
        </div>
        <p className="text-xs text-paper/40">Sin pagos · sin saldo · solo trueques</p>
      </div>

      <div className="flex items-center px-5 py-12 sm:px-8 md:justify-center md:py-16">
        <div className="w-full max-w-sm">
          <h1 className="text-4xl">{title}</h1>
          <p className="mt-2 text-sm leading-relaxed text-muted">{intro}</p>
          <div className="mt-8">{children}</div>
          <div className="mt-7 border-t border-rule pt-4 text-sm text-muted">{footer}</div>
        </div>
      </div>
    </div>
  );
}
