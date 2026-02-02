import { Link } from "react-router-dom";
import { Menu, X } from "lucide-react";
import { useState } from "react";

export function PublicNavBar() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  return (
    <nav className="bg-white shadow-md">
      <div className="container mx-auto px-4 py-4 flex justify-between items-center">
        <Link to="/" className="flex items-center gap-2">
          <div className="text-2xl font-bold text-blue-600">🏢</div>
          <span className="text-xl font-bold text-gray-800 hidden sm:inline">
            Annuaire Syndics
          </span>
        </Link>

        <div className="hidden md:flex gap-6">
          <Link
            to="/"
            className="text-gray-700 hover:text-blue-600 font-medium transition"
          >
            Accueil
          </Link>
          <a
            href={`${import.meta.env.VITE_GATEWAY_BASE ?? "http://localhost:8082"}/login?app=appA`}
            className="text-gray-700 hover:text-blue-600 font-medium transition"
          >
            Connexion
          </a>
          <Link
            to="/register"
            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition font-medium"
          >
            S'inscrire
          </Link>
        </div>

        <button
          onClick={() => setIsMenuOpen(!isMenuOpen)}
          className="md:hidden text-gray-700"
        >
          {isMenuOpen ? <X size={24} /> : <Menu size={24} />}
        </button>

        {isMenuOpen && (
          <div className="absolute top-16 left-0 right-0 bg-white shadow-lg md:hidden z-50">
            <div className="flex flex-col p-4 gap-4">
              <Link
                to="/"
                className="text-gray-700 hover:text-blue-600 font-medium transition py-2"
                onClick={() => setIsMenuOpen(false)}
              >
                Accueil
              </Link>
              <a
                href={`${import.meta.env.VITE_GATEWAY_BASE ?? "http://localhost:8082"}/login?app=appA`}
                className="text-gray-700 hover:text-blue-600 font-medium transition py-2"
                onClick={() => setIsMenuOpen(false)}
              >
                Connexion
              </a>
              <Link
                to="/register"
                className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition font-medium text-center"
                onClick={() => setIsMenuOpen(false)}
              >
                S'inscrire
              </Link>
            </div>
          </div>
        )}
      </div>
    </nav>
  );
}
