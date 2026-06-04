import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { OAuthService } from 'angular-oauth2-oidc';

import { apiBaseUrl, authConfig } from './auth.config';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  private readonly oauth = inject(OAuthService);
  private readonly http = inject(HttpClient);

  readonly apiBaseUrl = apiBaseUrl;
  readonly keycloakUrl = authConfig.issuer;

  loggedIn = false;
  username = '';
  accessTokenPreview = '';
  lastResponse = '';
  lastStatus = '';
  loading = false;

  ngOnInit(): void {
    this.oauth.configure(authConfig);
    this.oauth.loadDiscoveryDocumentAndTryLogin().then(() => {
      this.syncSession();
    });
  }

  login(): void {
    this.oauth.initCodeFlow();
  }

  logout(): void {
    this.oauth.logOut();
    this.syncSession();
    this.lastResponse = '';
    this.lastStatus = '';
  }

  callApi(path: string, needsAuth: boolean): void {
    this.loading = true;
    this.lastResponse = '';
    this.lastStatus = '';

    const url = `${apiBaseUrl}${path}`;
    this.http.get<unknown>(url).subscribe({
      next: (body) => {
        this.lastStatus = '200 OK';
        this.lastResponse = JSON.stringify(body, null, 2);
        this.loading = false;
      },
      error: (err) => {
        this.lastStatus = `${err.status} ${err.statusText || 'Error'}`;
        this.lastResponse = err.error
          ? JSON.stringify(err.error, null, 2)
          : String(err.message ?? err);
        this.loading = false;
      }
    });

    if (!needsAuth && !this.loggedIn) {
      // public endpoint works without login
    }
  }

  private syncSession(): void {
    this.loggedIn = this.oauth.hasValidAccessToken();
    const claims = this.oauth.getIdentityClaims() as Record<string, string> | null;
    this.username = claims?.['preferred_username'] ?? '';
    const token = this.oauth.getAccessToken();
    this.accessTokenPreview = token
      ? `${token.slice(0, 40)}… (${token.length} chars)`
      : '';
  }
}
