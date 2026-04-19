import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { HttpClient } from "@angular/common/http";
import { RouterLink } from "@angular/router";
import { Promotion } from "../../models/marketplace.models";
import { environment } from '../../../../../environments/environment';

@Component({
  selector: "app-promotion-list",
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page-content">
      <div class="section-header">
        <div>
          <h1 class="section-title text-4xl">🏷️ Gestion des Promotions</h1>
          <p class="view-subtitle">Creez et gerez les offres speciales de la boutique</p>
        </div>
        <button routerLink="nouvelle" class="btn btn-primary">+ Ajouter une promotion</button>
      </div>

      <div *ngIf="loading" class="text-center py-20">
        <p class="state-text text-lg animate-pulse">Chargement des offres en cours...</p>
      </div>

      <div *ngIf="!loading && promotions.length > 0" class="promotions-grid">
        <div *ngFor="let promo of promotions" class="sp-card promo-card group">
          <div class="flex justify-between items-start mb-4">
            <h3 class="section-title text-xl" style="color: var(--text);">{{ promo.titre }}</h3>
            <span [ngClass]="promo.active ? 'badge-green' : 'badge-red'" class="badge">
              {{ promo.active ? "ACTIF" : "INACTIF" }}
            </span>
          </div>
          
          <p class="promo-description text-sm mb-6 line-clamp-2">{{ promo.description }}</p>
          
          <div class="flex justify-between items-end border-t border-white/5 pt-4">
            <div>
              <p class="text-xs font-semibold uppercase tracking-widest mb-1" style="color: white;">Réduction appliquée</p>
              <p class="text-4xl font-black" style="color: var(--green);">-{{ promo.pourcentageReduction }}%</p>
            </div>
            <div class="flex gap-2">
              <button [routerLink]="[promo.id, 'edit']" class="btn btn-ghost p-2 opacity-50 hover:opacity-100 hover:bg-blue-500/20 transition-all" title="Modifier">✏️</button>
              <button (click)="deletePromotion(promo.id!)" class="btn btn-ghost p-2 opacity-50 hover:opacity-100 hover:bg-red-500/20 transition-all text-red-500" title="Supprimer">🗑️</button>
            </div>
          </div>
        </div>
      </div>

      <div *ngIf="!loading && promotions.length === 0" class="sp-card text-center py-16">
        <div class="text-5xl mb-6 opacity-30">🏷️</div>
        <p class="view-subtitle text-xl mb-8">Aucune promotion n'est configuree pour le moment.</p>
        <button routerLink="nouvelle" class="btn btn-primary px-8 py-3">Creer la premiere offre</button>
      </div>
    </div>
  `,
  styles: [`
    .view-subtitle {
      color: #e2e8f0;
      margin-top: 0.35rem;
      font-size: 1rem;
      line-height: 1.5;
    }

    .promotions-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 1.1rem;
    }

    .promo-card {
      border-color: var(--border);
      transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
    }

    .promo-card:hover {
      transform: translateY(-2px);
      border-color: var(--border2);
      box-shadow: 0 12px 28px rgba(0, 0, 0, 0.24);
    }

    .promo-description {
      color: #d9e2ec;
      line-height: 1.45;
    }

    .state-text {
      color: #e4eefc;
      font-weight: 600;
    }

    @media (min-width: 860px) {
      .promotions-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
    }

    @media (min-width: 1220px) {
      .promotions-grid {
        grid-template-columns: repeat(3, minmax(0, 1fr));
      }
    }
  `]
})
export class PromotionListComponent implements OnInit {
  promotions: Promotion[] = [];
  loading = false;

  private readonly apiUrl = `${environment.apiBaseUrl}/promotions`;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadPromotions();
  }

  private loadPromotions(): void {
    this.loading = true;
    this.http.get<Promotion[]>(this.apiUrl).subscribe({
      next: (data: Promotion[]) => {
        // Filtrer les promotions expirées (dateFin < aujourd'hui)
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        
        this.promotions = data.filter(promo => {
          if (!promo.dateFin) return true; // Si pas de dateFin, garder
          const dateFin = new Date(promo.dateFin);
          dateFin.setHours(0, 0, 0, 0);
          return dateFin >= today; // Garder les promotions non expirées
        });
        
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error("Erreur:", err);
        this.loading = false;
      }
    });
  }

  deletePromotion(promotionId: string): void {
    const confirmation = confirm('⚠️ Êtes-vous sûr de vouloir supprimer cette promotion ?\n\nCette action est irréversible.');
    
    if (!confirmation) return;

    this.http.delete(`${this.apiUrl}/${promotionId}`).subscribe({
      next: () => {
        // Supprimer de la liste
        this.promotions = this.promotions.filter(p => p.id !== promotionId);
        console.log('Promotion supprimée avec succès');
      },
      error: (err: unknown) => {
        console.error('Erreur suppression promotion:', err);
        alert('❌ Erreur lors de la suppression de la promotion.');
      }
    });
  }
}
