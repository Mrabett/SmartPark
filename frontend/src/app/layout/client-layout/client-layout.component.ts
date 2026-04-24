import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Location } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { PanierService } from '../../modules/marketplace/services/panier.service';

@Component({
  selector: 'app-client-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="client-shell">
      <header class="client-header sp-card">
        <div class="client-header-left">
          <button class="btn btn-ghost" (click)="goBack()">← Retour</button>
          <div>
            <p class="client-kicker">SmartPark Marketplace</p>
            <h1 class="client-title">Espace Client</h1>
            <p class="client-subtitle">{{ clientName }}</p>
          </div>
        </div>

        <div class="client-header-right">
          <a routerLink="/marketplace/client/panier" class="btn btn-primary panier-btn">
            🧺 Mon panier
            <span class="badge badge-red panier-count" *ngIf="panier.count > 0">{{ panier.count }}</span>
          </a>
          <button class="btn btn-ghost" (click)="logout()">Se déconnecter</button>
        </div>
      </header>

      <div class="client-body">
        <aside class="client-sidebar sp-card">
          <div class="sidebar-intro">
            <div class="intro-overline">Interface client</div>
            <div class="intro-title">Navigation rapide</div>
          </div>

          <nav class="sidebar-nav">
            <a routerLink="/marketplace/client/produits" routerLinkActive="active" class="sidebar-link">📦 Produits</a>
            <a routerLink="/marketplace/client/panier" routerLinkActive="active" class="sidebar-link">🧺 Mon panier</a>
            <a routerLink="/marketplace/client/commandes" routerLinkActive="active" class="sidebar-link">🛒 Mes commandes</a>
            <a routerLink="/marketplace/client/fidelite" routerLinkActive="active" class="sidebar-link">⭐ Fidélité</a>
            <a routerLink="/marketplace/client/recommandations" routerLinkActive="active" class="sidebar-link">💡 Recommandations</a>
          </nav>

          <button class="btn btn-ghost back-app-btn" (click)="backToApp()">↩ Retour SmartPark</button>
        </aside>

        <main class="client-content">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `,
  styles: [`
    .client-shell {
      position: relative;
      z-index: 1;
      padding: 1.5rem;
      min-height: calc(100vh - 90px);
    }

    .client-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 1.25rem;
      border-color: var(--border2) !important;
    }

    .client-header-left,
    .client-header-right {
      display: flex;
      align-items: center;
      gap: 0.85rem;
      flex-wrap: wrap;
    }

    .client-kicker {
      margin: 0;
      font-size: 0.72rem;
      color: var(--muted);
      text-transform: uppercase;
      letter-spacing: 0.12em;
    }

    .client-title {
      margin: 0;
      font-size: 1.35rem;
      color: var(--text);
      font-weight: 800;
    }

    .client-subtitle {
      margin: 0;
      font-size: 0.82rem;
      color: var(--muted);
    }

    .panier-btn {
      display: inline-flex;
      align-items: center;
      gap: 0.55rem;
    }

    .panier-count {
      font-weight: 800;
      box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.22);
    }

    .client-body {
      display: grid;
      grid-template-columns: 290px minmax(0, 1fr);
      gap: 1.25rem;
      align-items: start;
    }

    .client-sidebar {
      position: sticky;
      top: 1rem;
      background: linear-gradient(180deg, var(--card) 0%, var(--bg2) 100%);
      border-color: var(--border) !important;
    }

    .sidebar-intro {
      background: rgba(255, 255, 255, 0.04);
      border: 1px solid var(--border);
      border-radius: 14px;
      padding: 0.85rem;
      margin-bottom: 1rem;
    }

    .intro-overline {
      color: var(--muted);
      font-size: 0.78rem;
    }

    .intro-title {
      color: var(--text);
      font-size: 1rem;
      font-weight: 700;
      margin-top: 0.2rem;
    }

    .sidebar-nav {
      display: grid;
      gap: 0.5rem;
    }

    .sidebar-link {
      text-decoration: none;
      color: var(--muted);
      border: 1px solid transparent;
      border-radius: 12px;
      padding: 0.75rem 0.9rem;
      font-weight: 600;
      transition: all 0.2s;
      background: rgba(255, 255, 255, 0.01);
    }

    .sidebar-link:hover {
      color: var(--text);
      border-color: var(--border);
      background: rgba(255, 255, 255, 0.03);
    }

    .sidebar-link.active {
      color: #000;
      background: var(--green);
      border-color: var(--green);
      box-shadow: 0 8px 20px var(--green-glow);
    }

    .back-app-btn {
      margin-top: 1rem;
      width: 100%;
      justify-content: center;
    }

    .client-content {
      min-width: 0;
    }

    @media (max-width: 980px) {
      .client-body {
        grid-template-columns: 1fr;
      }

      .client-sidebar {
        position: static;
      }
    }
  `]
})
export class ClientLayoutComponent {
  constructor(
    private authService: AuthService,
    private router: Router,
    private location: Location,
    public panier: PanierService
  ) {}

  get clientName(): string {
    const info = this.authService.getClientInfo();
    return `${info.prenom} ${info.nom}`;
  }

  goBack(): void {
    this.location.back();
  }

  backToApp(): void {
    this.router.navigate(['/user/terrains']);
  }

  logout(): void {
    this.authService.logout();
    this.panier.clear();
    this.router.navigate(['/login']);
  }
}
