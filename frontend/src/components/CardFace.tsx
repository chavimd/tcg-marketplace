import { gameGlyph, gameSlug } from "../lib/format";

export interface CardSubject {
  nombre: string;
  setNombre: string | null;
  numeroSet: string | null;
  rareza: string | null;
  juego: string | null;
  imagenUrl: string | null;
}

interface CardFaceProps {
  card: CardSubject;
  className?: string;
  /** Show the printed set line under the name (default true). */
  showSet?: boolean;
  /** "mini" drops the text and keeps only the slab, for thumbnails. */
  variant?: "full" | "mini";
}

/**
 * The card face is the product's visual unit: a catalogue slab, not a fake card.
 * Without artwork it shows an engraved plate, so the grid never looks broken.
 */
export function CardFace({ card, className = "", showSet = true, variant = "full" }: CardFaceProps) {
  const game = card.juego ?? "otro";
  const hasImage = Boolean(card.imagenUrl);

  if (variant === "mini") {
    return (
      <div
        className={`card-face card-face--mini ${hasImage ? "card-face--photo" : ""} ${className}`}
        data-game={gameSlug(game)}
      >
        <div className="card-face__frame">
          {hasImage ? (
            <img className="card-face__image" src={card.imagenUrl ?? ""} alt="" loading="lazy" />
          ) : (
            <div className="card-face__plate">
              <span className="card-face__monogram" aria-hidden="true">
                {gameGlyph(game)}
              </span>
            </div>
          )}
        </div>
      </div>
    );
  }

  return (
    <div
      className={`card-face ${hasImage ? "card-face--photo" : ""} ${className}`}
      data-game={gameSlug(game)}
    >
      <div className="card-face__frame">
        {hasImage ? (
          <>
            <img className="card-face__image" src={card.imagenUrl ?? ""} alt="" loading="lazy" />
            <div className="card-face__caption">
              <p className="card-face__name">{card.nombre}</p>
              {showSet && card.setNombre ? <p className="card-face__set">{card.setNombre}</p> : null}
            </div>
          </>
        ) : (
          <>
            <div className="card-face__top">
              <span>{card.numeroSet ?? "—"}</span>
              <span>{card.rareza ?? game}</span>
            </div>

            <div className="card-face__plate">
              <span className="card-face__monogram" aria-hidden="true">
                {gameGlyph(game)}
              </span>
            </div>

            <div>
              <p className="card-face__name">{card.nombre}</p>
              {showSet && card.setNombre ? <p className="card-face__set">{card.setNombre}</p> : null}
            </div>
          </>
        )}
      </div>
    </div>
  );
}
