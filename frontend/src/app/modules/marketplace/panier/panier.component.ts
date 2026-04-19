import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { Commande, StatutCommande, StatutLivraison, PointsFidelite, Produit, Promotion } from '../models/marketplace.models';
import { PanierService } from '../services/panier.service';
import { CommandeService } from '../services/commande.service';
import { FideliteService } from '../services/fidelite.service';
import { PromotionService } from '../services/promotion.service';

@Component({
  selector: 'app-panier',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="page-content">
      <div class="section-header">
        <div>
          <h1 class="section-title text-4xl">🧺 Mon Panier</h1>
          <p class="cart-subtitle">Finalisez vos achats et profitez de vos avantages</p>
        </div>
        <button routerLink="/marketplace/client/produits" class="btn btn-ghost">Continuer mes achats</button>
      </div>

      <div *ngIf="panier.items.length === 0" class="sp-card text-center py-20">
        <div class="text-6xl mb-6">🛒</div>
        <p class="cart-empty-text text-xl mb-8">Votre panier est actuellement vide.</p>
        <button routerLink="/marketplace/client/produits" class="btn btn-primary px-8 py-3">Parcourir la boutique</button>
      </div>

      <div *ngIf="panier.items.length > 0" class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="lg:col-span-2 space-y-4">
          <div *ngFor="let item of panier.items" class="sp-card hover:border-border2 transition-all">
            <div class="flex items-center justify-between gap-6 flex-wrap">
              <div class="flex items-center gap-4">
                <div class="w-16 h-16 rounded-xl overflow-hidden bg-bg3">
                  <img [src]="item.produit.image" class="w-full h-full object-cover" onerror="this.src='https://via.placeholder.com/64?text=P'"/>
                </div>
                <div>
                  <h3 class="section-title text-lg" style="color: var(--text);">{{ item.produit.nom }}</h3>
                  <p class="text-xs category-soft uppercase tracking-wider">{{ item.produit.categorie }}</p>
                  <p class="text-sm font-semibold" style="color: var(--green);">{{ getDiscountedPrice(item.produit) | number:'1.2-2' }} DT / unite</p>
                </div>
              </div>
              <div class="flex items-center gap-3 bg-bg3 p-1 rounded-xl">
                <button class="btn btn-ghost p-1 min-w-[32px]" (click)="decrease(item.produit.id, item.quantite)">-</button>
                <span class="w-8 text-center font-bold text-white">{{ item.quantite }}</span>
                <button class="btn btn-ghost p-1 min-w-[32px]" (click)="increase(item.produit.id, item.quantite)">+</button>
              </div>
              <div class="text-right min-w-[100px]">
                <p class="text-xl font-bold" style="color: var(--text);">{{ getLineTotal(item.produit, item.quantite) | number:'1.2-2' }} DT</p>
                <button class="remove-item-btn" (click)="panier.remove(item.produit.id)">
                  Retirer l'article
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="sp-card h-fit sticky top-6">
          <h2 class="section-title text-xl mb-6 pb-4 border-b border-white/10">Résumé de commande</h2>
          <div class="space-y-4 text-sm mb-8">
            <div class="flex justify-between">
              <span class="summary-label">Nombre d'articles</span>
              <span class="text-white font-semibold">{{ panier.count }}</span>
            </div>
            <div class="flex justify-between text-xl pt-4 border-t border-white/10">
              <span style="color: var(--text);">Total</span>
              <span class="font-black" style="color: var(--green);">{{ totalPanierAvecPromotions | number:'1.2-2' }} DT</span>
            </div>
          </div>
          <button class="btn btn-primary w-full py-4 text-lg" [disabled]="loading" (click)="handleBuyClick()">
            {{ loading ? '⏳ Traitement...' : '💳 Acheter maintenant' }}
          </button>
        </div>
      </div>

      <!-- Modal de confirmation de paiement (Glassmorphism) -->
      <div *ngIf="showPaymentConfirmation" class="fixed inset-0 flex items-center justify-center z-[1000] p-4">
        <div class="absolute inset-0 bg-black/80 backdrop-blur-sm" (click)="cancelPayment()"></div>
        <div class="sp-card w-full max-w-lg relative z-10 border-border2 shadow-2xl animate-scaleUp">
          <div class="flex items-center justify-between mb-8">
            <h2 class="section-title text-2xl" style="color: var(--text);">Validation du paiement</h2>
            <button class="btn btn-ghost p-2" (click)="cancelPayment()">✕</button>
          </div>

          <div class="space-y-3 mb-8 max-h-48 overflow-y-auto pr-2 custom-scrollbar">
            <div *ngFor="let item of panier.items" class="flex justify-between text-sm py-2 border-b border-white/5">
              <span class="modal-soft">{{ item.produit.nom }} <span class="text-white">× {{ item.quantite }}</span></span>
              <span class="font-semibold">{{ getLineTotal(item.produit, item.quantite) | number:'1.2-2' }} DT</span>
            </div>
          </div>

          <div class="bg-bg3 p-6 rounded-2xl mb-8">
            <div class="flex justify-between text-sm mb-3">
              <span class="modal-soft">Sous-total principal:</span>
              <span class="text-white">{{ totalPanierAvecPromotions | number:'1.2-2' }} DT</span>
            </div>
            <div *ngIf="reductionPercentage > 0" class="flex justify-between text-sm mb-3" style="color: var(--green);">
              <span>Avantage Fidélité (-{{ reductionPercentage }}%):</span>
              <span>-{{ (totalPanierAvecPromotions * reductionPercentage / 100).toFixed(2) }} DT</span>
            </div>
            <div class="flex justify-between font-black text-2xl border-t border-white/10 pt-4 mt-2">
              <span style="color: var(--text);">Montant final</span>
              <span style="color: var(--green);">{{ totalApresReduction }} DT</span>
            </div>
          </div>

          <div class="bg-bg3 p-5 rounded-2xl mb-8">
            <label class="flex items-center gap-3 text-white font-semibold">
              <input type="checkbox" [(ngModel)]="demanderLivraison" />
              <span>Livraison locale dans le parc</span>
            </label>

            <div *ngIf="demanderLivraison" class="mt-3">
              <label class="text-sm field-label-soft mb-2 block">Lieu dans le parc</label>
              <input
                class="form-input w-full"
                [(ngModel)]="lieuLivraison"
                placeholder="Ex: Zone padel, terrasse centrale, gradin B"
              />
            </div>
          </div>

          <div class="flex gap-4">
            <button class="btn btn-ghost flex-1 py-3" [disabled]="loading" (click)="cancelPayment()">
              Modifier le panier
            </button>
            <button class="btn btn-primary flex-1 py-3" [disabled]="loading" (click)="confirmPayment()">
              {{ loading ? '...' : 'Valider mon achat' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .cart-subtitle {
      color: #e8f0fa;
      margin-top: 0.35rem;
      font-size: 1rem;
      line-height: 1.5;
      font-weight: 500;
    }

    .cart-empty-text {
      color: #e7eef8;
      font-weight: 600;
      letter-spacing: -0.2px;
    }

    .remove-item-btn {
      color: #ffd1d8;
      background: rgba(255, 77, 106, 0.22);
      border: 1px solid rgba(255, 77, 106, 0.45);
      border-radius: 10px;
      padding: 0.3rem 0.6rem;
      font-size: 0.78rem;
      font-weight: 700;
      margin-top: 0.35rem;
      transition: all 0.2s ease;
    }

    .remove-item-btn:hover {
      color: #fff;
      background: rgba(255, 77, 106, 0.3);
      border-color: rgba(255, 77, 106, 0.7);
    }

    .summary-label {
      color: #e2e8f0;
      font-size: 0.95rem;
      font-weight: 600;
    }

    .category-soft {
      color: #dce9fb;
      font-weight: 700;
    }

    .modal-soft {
      color: #e3eefc;
      font-weight: 600;
    }

    .field-label-soft {
      color: #e5f0ff;
      font-weight: 700;
      letter-spacing: 0.01em;
    }
  `]
})
export class PanierComponent implements OnInit {
  loading = false;
  showPaymentConfirmation = false;
  demanderLivraison = false;
  lieuLivraison = '';
  reductionPercentage = 0;
  fidelitePoints: PointsFidelite | null = null;
  promotions: Promotion[] = [];

  constructor(
    public panier: PanierService,
    private authService: AuthService,
    private router: Router,
    private commandeService: CommandeService,
    private fideliteService: FideliteService,
    private promotionService: PromotionService
  ) {}

  ngOnInit(): void {
    this.loadFideliteInfo();
    this.loadPromotions();
  }

  private loadPromotions(): void {
    this.promotionService.findAll().subscribe({
      next: (data: Promotion[]) => {
        this.promotions = data;
      },
      error: (err: unknown) => {
        console.error('Erreur promotions:', err);
      }
    });
  }

  private loadFideliteInfo(): void {
    const userId = this.authService.getCurrentUserId();

    this.fideliteService.getPointsForUser(userId).subscribe({
      next: (data: PointsFidelite) => {
        this.fidelitePoints = data;
      },
      error: (err: unknown) => {
        console.error('Erreur fidélité:', err);
      }
    });

    this.fideliteService.getReduction(userId).subscribe({
      next: (data: any) => {
        this.reductionPercentage = Number(data?.pourcentageReduction || 0);
      },
      error: (err: unknown) => {
        console.error('Erreur réduction:', err);
        this.reductionPercentage = 0;
      }
    });
  }

  get totalApresReduction(): string {
    const reduction = (this.totalPanierAvecPromotions * this.reductionPercentage) / 100;
    return (this.totalPanierAvecPromotions - reduction).toFixed(2);
  }

  get totalPanierAvecPromotions(): number {
    return this.panier.items.reduce(
      (sum, item) => sum + this.getLineTotal(item.produit, item.quantite),
      0
    );
  }

  getDiscountedPrice(produit: Produit): number {
    const reduction = this.getReductionPercentage(produit);
    const discounted = produit.prix * (1 - reduction / 100);
    return Math.round(discounted * 100) / 100;
  }

  getLineTotal(produit: Produit, quantite: number): number {
    return this.getDiscountedPrice(produit) * quantite;
  }

  private getReductionPercentage(produit: Produit): number {
    if (!produit.id) {
      return 0;
    }

    const now = new Date();
    const reductions = this.promotions
      .filter((promotion) => {
        if (!promotion.active || !promotion.produitIds?.includes(produit.id!)) {
          return false;
        }
        const debut = this.parsePromotionDateTime(promotion.dateDebut, promotion.heureDebut, false);
        const fin = this.parsePromotionDateTime(promotion.dateFin, promotion.heureFin, true);
        if (!debut || !fin) {
          return false;
        }
        return now.getTime() >= debut.getTime() && now.getTime() <= fin.getTime();
      })
      .map((promotion) => promotion.pourcentageReduction || 0);

    return reductions.length > 0 ? Math.max(...reductions) : 0;
  }

  private parsePromotionDateTime(rawDate: unknown, rawTime: unknown, endOfDay: boolean): Date | null {
    if (rawDate == null) {
      return null;
    }

    const fallbackTime = endOfDay ? '23:59:59.999' : '00:00:00.000';
    const parsedTime = typeof rawTime === 'string' && /^\d{2}:\d{2}$/.test(rawTime)
      ? `${rawTime}${endOfDay ? ':59.999' : ':00.000'}`
      : fallbackTime;

    if (rawDate instanceof Date) {
      return rawDate;
    }

    if (Array.isArray(rawDate) && rawDate.length >= 3) {
      const parsed = new Date(Number(rawDate[0]), Number(rawDate[1]) - 1, Number(rawDate[2]));
      const [h, m, sMs] = parsedTime.split(':');
      const [s, ms] = sMs.split('.');
      parsed.setHours(Number(h), Number(m), Number(s), Number(ms));
      return Number.isNaN(parsed.getTime()) ? null : parsed;
    }

    const raw = String(rawDate);
    const parsed = /^\d{4}-\d{2}-\d{2}$/.test(raw)
      ? new Date(`${raw}T${parsedTime}`)
      : new Date(raw);

    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }

  increase(id: string | undefined, qte: number): void {
    if (!id) return;
    this.panier.setQuantite(id, qte + 1);
  }

  decrease(id: string | undefined, qte: number): void {
    if (!id) return;
    this.panier.setQuantite(id, Math.max(1, qte - 1));
  }

  handleBuyClick(): void {
    if (this.panier.items.length === 0) {
      return;
    }
    this.showPaymentConfirmation = true;
  }

  cancelPayment(): void {
    this.showPaymentConfirmation = false;
    this.demanderLivraison = false;
    this.lieuLivraison = '';
  }

  confirmPayment(): void {
    if (this.panier.items.length === 0) {
      return;
    }

    if (this.demanderLivraison && !this.lieuLivraison.trim()) {
      alert('Veuillez indiquer votre lieu dans le parc pour la livraison.');
      return;
    }

    const userId = this.authService.getCurrentUserId();
    const clientInfo = this.authService.getClientInfo();
    
    console.log('=== DEBUG CRÉATION COMMANDE ===');
    console.log('UtilisateurId:', userId);
    console.log('Client Info:', clientInfo);
    console.log('Nombre articles:', this.panier.items.length);
    
    const lignes = this.panier.items.map(item => ({
      produitId: item.produit.id ?? '',
      nomProduit: item.produit.nom,
      quantite: item.quantite,
      prixUnitaire: this.getDiscountedPrice(item.produit),
      sousTotal: this.getLineTotal(item.produit, item.quantite),
      image: item.produit.image // 👈 AJOUTÉ
    }));

    const commande: Commande = {
      utilisateurId: userId,
      utilisateurNom: clientInfo.nom,
      utilisateurPrenom: clientInfo.prenom,
      statut: StatutCommande.PAYEE,
      dateCommande: new Date().toISOString(),
      montantTotal: parseFloat(this.totalApresReduction),
      livraisonDemandee: this.demanderLivraison,
      lieuLivraison: this.demanderLivraison ? this.lieuLivraison.trim() : undefined,
      statutLivraison: this.demanderLivraison ? StatutLivraison.EN_ATTENTE_AFFECTATION : undefined,
      lignes
    };

    console.log('Commande à envoyer:', commande);
    console.log('Réduction appliquée:', this.reductionPercentage + '%');
    this.loading = true;

    this.commandeService.create(commande).subscribe({
      next: (createdCommande) => {
        console.log('✅ Commande créée avec succès:', createdCommande);
        // Les points sont ajoutés automatiquement par le backend lors de la création de la commande
        this.panier.clear();
        this.loading = false;
        this.showPaymentConfirmation = false;
        this.demanderLivraison = false;
        this.lieuLivraison = '';
        // Attendre un bit avant de naviguer pour que les données s'actualisent
        setTimeout(() => {
          this.router.navigate(['/marketplace/client/commandes']);
        }, 500);
      },
      error: (err: unknown) => {
        console.error('❌ Erreur achat:', err);
        this.loading = false;
        this.showPaymentConfirmation = false;
        alert('Erreur lors de la création de la commande. Veuillez réessayer.');
      }
    });
  }
}
