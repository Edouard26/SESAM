import { Link } from "react-router-dom";

export default function NotFound() {
    return (
        <main className="d-flex align-items-center justify-content-center"
              style={{ height: "75vh" }}>
            <div className="text-center">
                <h1 className="display-1 fw-bold">404</h1>
                <h2 className="mb-3"> Page introuvable </h2>
                <p className="text-body-secondary mb-4"> Cette page n'existe pas ou n'est plus disponible. </p>
                <Link to="/" className="btn btn-primary"> Retour à l'accueil </Link>
            </div>
        </main>
    );
}
