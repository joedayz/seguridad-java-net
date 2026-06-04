import { AuthConfig } from 'angular-oauth2-oidc';

/** Valores alineados con Keycloak realm demo y client angular-pkce-client */
export const authConfig: AuthConfig = {
  issuer: 'http://localhost:8092/realms/demo',
  redirectUri: window.location.origin,
  clientId: 'angular-pkce-client',
  responseType: 'code',
  scope: 'openid profile email',
  showDebugInformation: true,
  requireHttps: false,
  useSilentRefresh: false
};

export const apiBaseUrl = 'http://localhost:8088';
