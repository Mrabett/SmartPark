import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { PanierService } from '../modules/marketplace/services/panier.service';

@Component({
  selector: 'app-marketplace-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="marketplace-wrapper">
      <!-- 💎 SUB-NAVBAR PREMIUM -->
      <div class="marketplace-header sp-card">
        <div class="nav-container">
          <div class="main-info">
            <h1 class="title">Boutique & Marketplace</h1>
            <p class="subtitle">{{ isAdmin ? 'Administration des ventes' : 'Espace Achats & Fidélité' }}</p>
          </div>

          <!-- 📑 TABS CLIENT -->
          <nav class="marketplace-tabs" *ngIf="!isAdmin">
            <a routerLink="produits" routerLinkActive="active" class="market-tab">📦 Catalogue</a>
            <a routerLink="objectifs" routerLinkActive="active" class="market-tab">🎯 Objectif</a>
            <a routerLink="panier" routerLinkActive="active" class="market-tab relative">
              🧺 Panier
              <span class="badge badge-red count-badge" *ngIf="panier.count > 0">{{ panier.count }}</span>
            </a>
            <a routerLink="commandes" routerLinkActive="active" class="market-tab">🛒 Mes Commandes</a>
            <a routerLink="fidelite" routerLinkActive="active" class="market-tab">⭐ Fidélité</a>
            <a routerLink="chater" routerLinkActive="active" class="market-tab">💬 Chater</a>
            <a routerLink="recommandations" routerLinkActive="active" class="market-tab">💡 Pour vous</a>
          </nav>

          <!-- 📑 TABS ADMIN -->
          <nav class="marketplace-tabs" *ngIf="isAdmin">
            <a routerLink="produits" routerLinkActive="active" class="market-tab">📦 Produits</a>
            <a routerLink="commandes" routerLinkActive="active" class="market-tab">🛒 Commandes</a>
            <a routerLink="commandes/livraisons" routerLinkActive="active" class="market-tab">🚚 Livraison</a>
            <a routerLink="objectifs" routerLinkActive="active" class="market-tab">🎯 Objectifs</a>
            <a routerLink="promotions" routerLinkActive="active" class="market-tab">🏷️ Promotions</a>
            <a routerLink="alertes" routerLinkActive="active" class="market-tab">🚨 Alertes</a>
            <a routerLink="fidelite" routerLinkActive="active" class="market-tab">⭐ Fidélité</a>
            <a routerLink="chater" routerLinkActive="active" class="market-tab">💬 Chater</a>
            <a routerLink="ai-suggest" routerLinkActive="active" class="market-tab ai-tab">🤖 IA Produit</a>
          </nav>
        </div>
      </div>

      <!-- 📄 CONTENU DÉFILANT -->
      <main class="marketplace-content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .marketplace-wrapper {
      padding: 1.5rem;
      animation: fadeIn 0.4s ease-out;
    }

    .marketplace-header {
      margin-bottom: 2rem;
      padding: 1.25rem 2rem !important;
      background: var(--card) !important;
      border-color: var(--border2) !important;
    }

    .nav-container {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }

    @media (min-width: 768px) {
      .nav-container {
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
      }
    }

    .title {
      font-size: 1.25rem;
      font-weight: 800;
      color: var(--text);
      margin: 0;
    }

    .subtitle {
      font-size: 0.75rem;
      color: var(--muted);
      text-transform: uppercase;
      letter-spacing: 0.1em;
      margin: 0;
    }

    .marketplace-tabs {
      display: flex;
      gap: 0.5rem;
      background: var(--bg3);
      padding: 0.4rem;
      border-radius: 12px;
      overflow-x: auto;
    }

    .market-tab {
      padding: 0.6rem 1.2rem;
      border-radius: 10px;
      font-size: 0.85rem;
      font-weight: 600;
      color: var(--muted);
      text-decoration: none;
      transition: all 0.2s;
      white-space: nowrap;
      
      &:hover {
        color: var(--text);
        background: rgba(255,255,255,0.05);
      }

      &.active {
        color: #000;
        background: var(--green);
        box-shadow: 0 4px 15px var(--green-glow);
      }
    }

    .count-badge {
      position: absolute;
      top: -5px;
      right: -5px;
      font-size: 0.65rem;
      padding: 2px 6px;
    }

    .ai-tab {
      background: linear-gradient(135deg, rgba(0,201,127,0.12), rgba(168,85,247,0.12)) !important;
      border: 1px solid rgba(0,201,127,0.25) !important;
      color: #00c97f !important;
      position: relative;
      overflow: hidden;
    }

    .ai-tab::before {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(90deg, transparent, rgba(0,201,127,0.06), transparent);
      animation: aiTabShine 2.5s ease-in-out infinite;
    }

    @keyframes aiTabShine {
      0% { transform: translateX(-100%); }
      100% { transform: translateX(200%); }
    }

    .ai-tab.active {
      background: linear-gradient(135deg, #00c97f, #a855f7) !important;
      color: #000 !important;
      border-color: transparent !important;
      box-shadow: 0 4px 18px rgba(0,201,127,0.35) !important;
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(-5px); }
      to { opacity: 1; transform: translateY(0); }
    }
  `]
})
export class MarketplaceLayoutComponent {
  constructor(
    private auth: AuthService,
    public panier: PanierService
  ) {}

  get isAdmin(): boolean {
    const isAdmin = this.auth.isAdmin();
    console.log('[MarketplaceLayout] Is user admin?', isAdmin, 'Role:', this.auth.getRole());
    return isAdmin;
  }
}
