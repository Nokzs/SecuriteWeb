import { useState, useEffect } from "react";
import { SyndicCard } from "./syndicCard";
import { SyndicSearch } from "./syndicSearch";
import { ContactSyndicForm } from "./contactSyndicForm";
import { Loader } from "lucide-react";

interface Syndic {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  address?: string;
  buildingsCount?: number;
}

interface ContactFormData {
  firstName: string;
  lastName: string;
  phone: string;
  message: string;
}

export function SyndicsDirectory() {
  const [syndics, setSyndics] = useState<Syndic[]>([]);
  const [filteredSyndics, setFilteredSyndics] = useState<Syndic[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedSyndic, setSelectedSyndic] = useState<Syndic | null>(null);
  const [showContactForm, setShowContactForm] = useState(false);

  useEffect(() => {
    fetchSyndics();
  }, []);

  const fetchSyndics = async () => {
    try {
      setIsLoading(true);
      setError("");
      const response = await fetch(`${import.meta.env.VITE_APIURL}/syndics`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      });

      if (!response.ok) {
        throw new Error("Erreur lors de la récupération des syndics");
      }

      const data = await response.json();
      setSyndics(data);
      setFilteredSyndics(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Une erreur est survenue");
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSearch = (query: string) => {
    const lowerQuery = query.toLowerCase();
    const filtered = syndics.filter(
      (syndic) =>
        syndic.firstName.toLowerCase().includes(lowerQuery) ||
        syndic.lastName.toLowerCase().includes(lowerQuery) ||
        syndic.email.toLowerCase().includes(lowerQuery) ||
        syndic.phone.includes(query)
    );
    setFilteredSyndics(filtered);
  };

  const handleContactClick = (syndic: Syndic) => {
    setSelectedSyndic(syndic);
    setShowContactForm(true);
  };

  const handleFormSubmit = async (data: ContactFormData) => {
    if (!selectedSyndic) return;

    try {
      const response = await fetch(
        `${import.meta.env.VITE_APIURL}/syndics/${selectedSyndic.id}/contact`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            firstName: data.firstName,
            lastName: data.lastName,
            phone: data.phone,
            message: data.message,
          }),
        }
      );

      if (!response.ok) {
        throw new Error("Erreur lors de l'envoi du message");
      }

      return response.json();
    } catch (err) {
      throw err instanceof Error ? err : new Error("Une erreur est survenue");
    }
  };

  return (
    <div className="w-full">
      <SyndicSearch onSearch={handleSearch} placeholder="Chercher par nom, prénom, email ou téléphone..." />

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg mb-6 max-w-4xl mx-auto">
          {error}
        </div>
      )}

      {isLoading ? (
        <div className="flex justify-center items-center py-12">
          <Loader className="animate-spin text-blue-600" size={32} />
        </div>
      ) : (
        <>
          <div className="mb-6 max-w-4xl mx-auto">
            <p className="text-gray-600 text-center">
              {filteredSyndics.length} syndic{filteredSyndics.length !== 1 ? "s" : ""} trouvé
              {filteredSyndics.length !== 1 ? "s" : ""}
            </p>
          </div>

          {filteredSyndics.length === 0 ? (
            <div className="text-center py-12 max-w-4xl mx-auto">
              <p className="text-gray-600 text-lg">Aucun syndic ne correspond à votre recherche.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-6xl mx-auto">
              {filteredSyndics.map((syndic) => (
                <SyndicCard
                  key={syndic.id}
                  id={syndic.id}
                  name={`${syndic.firstName} ${syndic.lastName}`}
                  email={syndic.email}
                  phone={syndic.phone}
                  address={syndic.address}
                  buildingsCount={syndic.buildingsCount}
                  onContact={() => handleContactClick(syndic)}
                />
              ))}
            </div>
          )}
        </>
      )}

      {showContactForm && selectedSyndic && (
        <ContactSyndicForm
          syndicName={`${selectedSyndic.firstName} ${selectedSyndic.lastName}`}
          syndicEmail={selectedSyndic.email}
          onClose={() => {
            setShowContactForm(false);
            setSelectedSyndic(null);
          }}
          onSubmit={handleFormSubmit}
        />
      )}
    </div>
  );
}
