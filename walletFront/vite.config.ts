import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    host: true, // Permet l'accès externe au conteneur
    port: 5173,
    watch: {
      usePolling: true, // Nécessaire sur certains systèmes (Windows) pour détecter les changements de fichiers
    },
  },
});
