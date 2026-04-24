import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProduitExpirationAlert } from '../models/marketplace.models';
import { ProduitService } from '../services/produit.service';

@Component({
  selector: 'app-expiration-alerts',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-content">
      <div class="section-header">
        <div>
          <h1 class="section-title text-4xl">🚨 Alertes d'expiration</h1>
          <p class="view-subtitle">Produits proches de la date limite avec priorite d'action admin</p>
        </div>
      </div>

      <div *ngIf="loading" class="sp-card text-center py-10">
        <p class="state-text">Chargement des alertes...</p>
      </div>

      <div *ngIf="!loading && alerts.length === 0" class="sp-card text-center py-12">
        <p class="section-title text-xl" style="color: var(--green);">Aucune alerte</p>
        <p class="view-subtitle">Aucun produit n'expire dans les 10 prochains jours.</p>
      </div>

      <div *ngIf="!loading && alerts.length > 0" class="alerts-grid">
        <div *ngFor="let alert of alerts" class="sp-card alert-card" [ngClass]="severityClass(alert.joursRestants)">
          <div class="flex items-start justify-between gap-3 mb-2">
            <h3 class="section-title text-xl" style="color: var(--text);">{{ alert.nomProduit }}</h3>
            <span class="badge" [ngClass]="badgeClass(alert.joursRestants)">{{ badgeText(alert.joursRestants) }}</span>
          </div>

          <p class="alert-category">Categorie: {{ alert.categorie || 'Non definie' }}</p>

          <div class="details-wrap space-y-2">
            <div class="info-line">
              <span class="label">Date d'expiration</span>
              <span class="value">{{ alert.dateExpiration | date:'dd/MM/yyyy' }}</span>
            </div>
            <div class="info-line">
              <span class="label">Jours restants</span>
              <span class="value">{{ alert.joursRestants }}</span>
            </div>
            <div class="info-line">
              <span class="label">Stock disponible</span>
              <span class="value">{{ alert.quantiteDisponible ?? 0 }}</span>
            </div>
          </div>

          <button (click)="creerPromotion(alert.produitId)" class="btn-promotion mt-4">
            🎁 Faire une promotion
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .view-subtitle {
      color: #f1f5f9;
      margin-top: 0.35rem;
      font-size: 1.05rem;
      line-height: 1.55;
      font-weight: 500;
    }

    .alerts-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 1rem;
    }

    .alert-card {
      border-color: var(--border);
      transition: transform 0.2s ease, border-color 0.2s ease;
    }

    .alert-card:hover {
      transform: translateY(-2px);
      border-color: var(--border2);
    }

    .alert-card.warning {
      border-left: 4px solid var(--amber);
    }

    .alert-card.critical {
      border-left: 4px solid var(--red);
    }

    .info-line {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      font-size: 1rem;
    }

    .label {
      color: #e2e8f0;
      font-weight: 600;
    }

    .value {
      color: var(--text);
      font-weight: 700;
    }

    .alert-category {
      color: #dbe7f5;
      font-size: 0.95rem;
      margin-bottom: 0.9rem;
    }

    .state-text {
      color: #e4eefc;
      font-weight: 600;
    }

    .details-wrap {
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid rgba(255, 255, 255, 0.08);
      border-radius: 12px;
      padding: 0.85rem;
    }

    @media (min-width: 920px) {
      .alerts-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
    }

    @media (min-width: 1240px) {
      .alerts-grid {
        grid-template-columns: repeat(3, minmax(0, 1fr));
      }
    }

    .btn-promotion {
      width: 100%;
      padding: 0.75rem 1.25rem;
      background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
      color: white;
      border: none;
      border-radius: 8px;
      font-weight: 600;
      font-size: 0.95rem;
      cursor: pointer;
      transition: all 0.3s ease;
      margin-top: 0.5rem;
    }

    .btn-promotion:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
    }

    .btn-promotion:active {
      transform: translateY(0);
    }
  `]
})
export class ExpirationAlertsComponent implements OnInit {
  alerts: ProduitExpirationAlert[] = [];
  loading = false;

  constructor(private produitService: ProduitService, private router: Router) {}

  ngOnInit(): void {
    this.loadAlerts();
  }

  loadAlerts(): void {
    this.loading = true;
    this.produitService.getExpirationAlerts().subscribe({
      next: (data) => {
        this.alerts = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement alertes expiration:', err);
        this.loading = false;
      }
    });
  }

  badgeText(daysLeft: number): string {
    if (daysLeft <= 0) {
      return 'Expire aujourd\'hui';
    }
    if (daysLeft <= 3) {
      return 'Urgent';
    }
    return `J-${daysLeft}`;
  }

  badgeClass(daysLeft: number): string {
    return daysLeft <= 3 ? 'badge-red' : 'badge-amber';
  }

  severityClass(daysLeft: number): string {
    return daysLeft <= 3 ? 'critical' : 'warning';
  }

  creerPromotion(produitId: string | undefined): void {
    if (produitId) {
      this.router.navigate(['/marketplace/admin/promotions/nouvelle'], {
        queryParams: { produitId: produitId }
      });
    }
  }
}
