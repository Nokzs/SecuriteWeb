En production, vous serez derrière un reverse proxy avec un seul domaine public (ex: https://app.example.com) qui route vers le frontend et la gateway. Le mode dev actuel (localhost + ports) reste inchangé ; en prod, il faudra surtout surcharger des variables et configurer le reverse proxy.

1. Reverse proxy (nginx/traefik) : routes à ajouter

- Router le frontend sur /
- Router la gateway sur les chemins suivants (même host public) :
  - /api/\*\* → gateway (Spring Cloud Gateway)
  - /oauth2/\*\* → gateway (déclenche l’auth)
  - /login/\*\* → gateway (callback login/oauth2/code/...)
  - /auth/\*\* → gateway (/auth/user, /auth/logout, etc.)
- La gateway continuera ensuite à proxy en interne vers le monolithe (http://backend:8080) via MONOLITH_BASE_URI.

2. Frontend : passer en URLs relatives (même domaine)
   Aujourd’hui en dev, le front appelle http://localhost:8082.
   En prod (même domaine public), le front doit appeler le même host, donc on passe en relatif :

- VITE_GATEWAY_BASE=/
- VITE_APIURL=/api
  Concrètement, soit vous mettez ces valeurs dans l’environnement du conteneur frontend, soit dans un .env.production au build.

3. Gateway BFF : issuer OIDC (URL publique)
   Actuellement, en dev, le gateway utilise :

- OAUTH2_ISSUER_URI=http://backend:8080 (ou http://backend:8080 via défaut)
  En prod derrière reverse proxy, le gateway doit utiliser l’URL publique de l’issuer (celle vue par le navigateur), sinon les metadata OIDC (/.well-known/openid-configuration) renverront des URLs incohérentes :
- OAUTH2_ISSUER_URI=https://app.example.com (ou https://app.example.com/<prefix-auth> si vous exposez l’auth sous un sous-chemin)

4. Monolithe (Authorization Server) : redirect URI enregistrée du client gateway-client
   Dans AuthorizationServerConfig, le client gateway-client est enregistré avec un redirect URI. En prod, il doit matcher exactement l’URL publique exposée par le reverse proxy :

- https://app.example.com/login/oauth2/code/gateway-client
  Idem pour le post logout redirect :
- https://app.example.com/
  Si ce n’est pas mis à jour, le provider refusera le callback (mismatch redirect_uri).

5. Cookies / HTTPS
   En prod (HTTPS), il faudra s’assurer que le cookie de session du gateway est compatible HTTPS :

- GATEWAY_SESSION_COOKIE_SECURE=true
- SameSite=LAX est généralement OK puisque tout est sur le même domaine public.
  (Le cas “SameSite=None” est surtout pour le scénario multi-domaines visibles côté navigateur.)

6. Headers X-Forwarded (important)
   Le reverse proxy doit envoyer X-Forwarded-Proto, X-Forwarded-Host, etc. pour que Spring reconstruise correctement les URLs externes.
   Selon le proxy, il peut falloir activer côté gateway :

- server.forward-headers-strategy=framework (ou équivalent), si vous constatez des redirects en http:// au lieu de https://.
  En résumé : en prod, vous ne changez pas l’architecture, vous changez surtout (a) le routage reverse proxy, (b) les variables d’URL côté front, (c) l’issuer/redirect URI publics côté gateway + auth server, et (d) les options HTTPS/cookies.
