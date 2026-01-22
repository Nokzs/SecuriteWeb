# Étape 1 : Build
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
# Vite va injecter les variables de .env.production ici
RUN npm run build 

# Étape 2 : Serveur de production
FROM nginx:stable-alpine
# Vite génère par défaut dans le dossier 'dist'
COPY --from=build /app/dist /usr/share/nginx/html
# Config pour gérer le routage (indispensable pour les Single Page Apps)
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
