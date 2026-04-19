import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterLink } from "@angular/router";
import { RecommandationService } from "../../services/recommandation.service";
import { ProduitRecommande } from "../../models/marketplace.models";
import { AuthService } from "../../../../services/auth.service";
import { PanierService } from "../../services/panier.service";

@Component({
  selector: "app-recommandation-list",
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page-content">
      <div class="section-header">
        <div>
          <h1 class="section-title text-4xl">✨ Pour Vous</h1>
          <p class="view-subtitle">Des produits selectionnes selon vos habitudes d'achat</p>
        </div>
        <button class="btn btn-ghost" (click)="loadRecommendedProducts()">🔄 Actualiser</button>
      </div>
      
      <div *ngIf="loading" class="text-center py-20">
        <p class="state-text text-lg animate-pulse">Analyse de vos préférences en cours...</p>
      </div>
      
      <div *ngIf="!loading && recommendedProducts.length > 0" class="recommend-grid">
        <div *ngFor="let produit of recommendedProducts" class="sp-card group recommend-card overflow-hidden flex flex-col">
          <div class="relative h-56 -mx-6 -mt-6 mb-6 overflow-hidden">
            <img
              [src]="produit.image"
              alt="{{ produit.nom }}"
              class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
              onerror="this.src='https://via.placeholder.com/400?text=Produit'"
            />
            <div class="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
            <span class="absolute top-4 right-4 badge badge-green">{{ produit.categorie }}</span>
          </div>

          <h3 class="section-title text-xl mb-2" style="color: var(--text);">{{ produit.nom }}</h3>
          <p class="recommend-description text-sm mb-6 line-clamp-2 flex-grow">{{ produit.description }}</p>
          
          <div class="flex items-center justify-between mb-6">
            <p class="text-2xl font-black" style="color: var(--green);">{{ produit.prix }} <span class="text-sm">DT</span></p>
          </div>

          <div class="flex gap-3">
            <button [routerLink]="['/marketplace/client/produits', produit.id]" class="btn btn-ghost flex-1 py-3 text-sm">Voir Détails</button>
            <button class="btn btn-primary flex-1 py-3 text-sm" (click)="addToCart(produit)">🛒 Ajouter</button>
          </div>
        </div>
      </div>
      
      <div *ngIf="!loading && recommendedProducts.length === 0" class="sp-card text-center py-20 bg-bg3/30 border-white/5">
        <div class="text-6xl mb-6 opacity-20">✨</div>
        <h3 class="section-title text-xl mb-4">Plus vous achetez, plus nous apprenons !</h3>
        <p class="state-text max-w-md mx-auto mb-8">
          Faites vos premiers achats dans la boutique pour débloquer des recommandations personnalisées basées sur vos goûts.
        </p>
        <button routerLink="/marketplace/client/produits" class="btn btn-primary px-8 py-3">Explorer le catalogue catalogue</button>
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

    .recommend-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 1.1rem;
    }

    .recommend-card {
      border-color: var(--border);
      transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
    }

    .recommend-card:hover {
      transform: translateY(-3px);
      border-color: var(--border2);
      box-shadow: 0 14px 30px rgba(0, 0, 0, 0.26);
    }

    .recommend-description {
      color: #d9e2ec;
      line-height: 1.5;
    }

    .state-text {
      color: #e4eefc;
      font-weight: 600;
    }

    @media (min-width: 860px) {
      .recommend-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 1.25rem;
      }
    }

    @media (min-width: 1220px) {
      .recommend-grid {
        grid-template-columns: repeat(3, minmax(0, 1fr));
      }
    }
  `]
})
export class RecommandationListComponent implements OnInit {
  recommendedProducts: ProduitRecommande[] = [];
  loading = false;

  constructor(
    private recommandationService: RecommandationService,
    private authService: AuthService,
    private panierService: PanierService
  ) {}

  ngOnInit(): void {
    this.loadRecommendedProducts();
  }

  loadRecommendedProducts(): void {
    this.loading = true;
    this.recommandationService.getRecommendedProducts(this.authService.getCurrentUserId()).subscribe({
      next: (data) => {
        this.recommendedProducts = data;
        this.loading = false;
      },
      error: (err) => {
        console.error("Erreur:", err);
        this.loading = false;
      }
    });
  }

  addToCart(produit: ProduitRecommande): void {
    this.panierService.add(produit as any, 1);
  }
}
