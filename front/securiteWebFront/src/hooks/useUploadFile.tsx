export function useUploadFile() {
  const upload = async (file: File, signedUrl: string) => {
    try {
      const response = await fetch(signedUrl, {
        method: "PUT",
        body: file,
      });
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Erreur MinIO (${response.status}): ${errorText}`);
      }

      return true;
    } catch (error: unknown) {
      if (error instanceof Error) {
        console.error("Erreur lors de l'upload du fichier :", error.message);
      }
      throw error;
    }
  };

  return upload;
}
