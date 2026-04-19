import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import { Objectif, Produit } from '../models/marketplace.models';
import { ObjectifService } from '../services/objectif.service';
import { PanierService } from '../services/panier.service';
import { ProduitService } from '../services/produit.service';

@Component({
  selector: 'app-parcours-objectif',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './parcours-objectif.component.html',
  styleUrl: './parcours-objectif.component.scss'
})
export class ParcoursObjectifComponent implements OnInit {
  loading = false;
  errorMessage = '';
  allProducts: Produit[] = [];
  objectifs: Objectif[] = [];
  selectedObjectiveId = '';

  constructor(
    private objectifService: ObjectifService,
    private produitService: ProduitService,
    private panierService: PanierService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  get selectedConfig(): Objectif | null {
    return this.objectifs.find(o => o.id === this.selectedObjectiveId) || this.objectifs[0] || null;
  }

  get filteredProducts(): Produit[] {
    const target = this.selectedConfig;
    if (!target) {
      return [];
    }

    const ids = new Set((target.produitIds || []).filter(Boolean));
    if (ids.size === 0) {
      return [];
    }

    return this.allProducts
      .filter(p => p.actif && !!p.id && ids.has(p.id))
      .slice(0, 12);
  }

  selectObjective(id?: string): void {
    if (id) {
      this.selectedObjectiveId = id;
    }
  }

  addToCart(product: Produit): void {
    this.panierService.add(product, 1);
  }

  trackByProduitId(_: number, produit: Produit): string {
    return produit.id || produit.nom;
  }

  private loadProducts(): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      products: this.produitService.findAll(),
      objectifs: this.objectifService.findActifs()
    }).subscribe({
      next: ({ products, objectifs }) => {
        this.allProducts = products || [];
        this.objectifs = objectifs || [];
        this.selectedObjectiveId = this.objectifs[0]?.id || '';
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Impossible de charger les objectifs et les produits.';
      }
    });
  }
}
