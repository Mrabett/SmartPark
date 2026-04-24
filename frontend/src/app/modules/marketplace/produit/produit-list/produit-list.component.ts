import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Produit, Promotion } from '../../models/marketplace.models';
import { OnInit } from '@angular/core';
import { environment } from '../../../../../environments/environment';
import { AuthService } from '../../../../services/auth.service';
import { PanierService } from '../../services/panier.service';
import { PromotionService } from '../../services/promotion.service';

@Component({
  selector: 'app-produit-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './produit-list.component.html',
  styleUrl: './produit-list.component.css'
})
export class ProduitListComponent implements OnInit {
  produits: Produit[] = [];
  filteredProduits: Produit[] = [];
  promotions: Promotion[] = [];
  searchTerm = '';
  selectedCategory = '';
  loading = false;
  categories: string[] = [];
  private readonly apiUrl = `${environment.apiBaseUrl}/produits`;

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private panierService: PanierService,
    private promotionService: PromotionService
  ) {}

  ngOnInit(): void {
    this.loadPromotions();
    this.loadProduits();
  }

  loadPromotions(): void {
    this.promotionService.findAll().subscribe({
      next: (data: Promotion[]) => {
        this.promotions = data;
      },
      error: (err: unknown) => {
        console.error('Erreur loading promotions:', err);
      }
    });
  }

  loadProduits(): void {
    this.loading = true;
    this.http.get<Produit[]>(this.apiUrl).subscribe({
      next: (data: Produit[]) => {
        this.produits = data;
        this.filteredProduits = data;
        this.extractCategories();
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error('Erreur loading produits:', err);
        this.loading = false;
      }
    });
  }

  extractCategories(): void {
    this.categories = [...new Set(this.produits.map(p => p.categorie))];
  }

  filterProduits(): void {
    this.filteredProduits = this.produits.filter(p => {
      const matchSearch = p.nom.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchCategory = !this.selectedCategory || p.categorie === this.selectedCategory;
      return matchSearch && matchCategory;
    });
  }

  getReductionPercentage(produit: Produit): number {
    if (!produit.id) {
      return 0;
    }

    const today = new Date();
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
        return today.getTime() >= debut.getTime() && today.getTime() <= fin.getTime();
      })
      .map((promotion) => promotion.pourcentageReduction || 0);

    return reductions.length > 0 ? Math.max(...reductions) : 0;
  }

  hasPromotion(produit: Produit): boolean {
    return this.getReductionPercentage(produit) > 0;
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

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  get routePrefix(): string {
    return this.isAdmin ? '/marketplace/admin' : '/marketplace/client';
  }

  addToCart(produit: Produit): void {
    if (!this.authService.isClient()) {
      return;
    }
    this.panierService.add(produit, 1);
  }
}
