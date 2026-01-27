import { PublicNavBar } from "../component/PublicNavBar";
import { SyndicsDirectory } from "../syndic/component/syndicsDirectory";

export function Home() {
  return (
    <div className="min-h-screen bg-gray-50">
      <PublicNavBar />
      
      <main className="container mx-auto px-4 py-12">
        {/* Hero Section */}
        <section className="mb-16 text-center">
          <h1 className="text-4xl md:text-5xl font-bold text-gray-800 mb-4">
            Bienvenue dans notre annuaire de syndics
          </h1>
          <p className="text-xl text-gray-600 max-w-3xl mx-auto mb-8">
            Trouvez et contactez directement les syndics disponibles. Utilisez la barre de recherche ci-dessous pour trouver le syndic qui vous convient.
          </p>
        </section>

        {/* Syndics Directory Section */}
        <section className="bg-white rounded-lg shadow-lg p-8 mb-12">
          <h2 className="text-3xl font-bold text-gray-800 mb-8 text-center">
            Annuaire des syndics
          </h2>
          <SyndicsDirectory />
        </section>

        {/* Info Section */}
        <section className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-12">
          <div className="bg-blue-50 rounded-lg p-6 border border-blue-200">
            <div className="text-3xl font-bold text-blue-600 mb-3">📋</div>
            <h3 className="text-lg font-semibold text-gray-800 mb-2">Large sélection</h3>
            <p className="text-gray-600">
              Consultez notre annuaire complet de syndics professionnels
            </p>
          </div>
          <div className="bg-green-50 rounded-lg p-6 border border-green-200">
            <div className="text-3xl font-bold text-green-600 mb-3">✉️</div>
            <h3 className="text-lg font-semibold text-gray-800 mb-2">Contact direct</h3>
            <p className="text-gray-600">
              Envoyez des messages directement aux syndics de votre choix
            </p>
          </div>
          <div className="bg-purple-50 rounded-lg p-6 border border-purple-200">
            <div className="text-3xl font-bold text-purple-600 mb-3">🔍</div>
            <h3 className="text-lg font-semibold text-gray-800 mb-2">Recherche facile</h3>
            <p className="text-gray-600">
              Recherchez par nom, prénom, téléphone ou email en quelques secondes
            </p>
          </div>
        </section>

        {/* CTA Section */}
        <section className="bg-gradient-to-r from-blue-600 to-blue-800 rounded-lg text-white p-12 text-center">
          <h2 className="text-3xl font-bold mb-4">Vous êtes propriétaire ou locataire?</h2>
          <p className="text-lg mb-8 opacity-90">
            Connectez-vous pour accéder à plus de fonctionnalités et gérer vos propriétés
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <a
              href="/login"
              className="bg-white text-blue-600 font-semibold py-3 px-8 rounded-lg hover:bg-gray-100 transition inline-block"
            >
              Se connecter
            </a>
            <a
              href="/register"
              className="border-2 border-white text-white font-semibold py-3 px-8 rounded-lg hover:bg-white hover:text-blue-600 transition inline-block"
            >
              S'inscrire
            </a>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="bg-gray-800 text-white py-8 mt-16">
        <div className="container mx-auto px-4 text-center">
          <p>&copy; 2026 Annuaire des Syndics. Tous droits réservés.</p>
        </div>
      </footer>
    </div>
  );
}
