import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Objectif, Produit } from '../models/marketplace.models';
import { ObjectifService } from '../services/objectif.service';
import { ProduitService } from '../services/produit.service';

@Component({
  selector: 'app-objectif-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './objectif-admin.component.html',
  styleUrl: './objectif-admin.component.scss'
})
export class ObjectifAdminComponent implements OnInit {
  objectifs: Objectif[] = [];
  produits: Produit[] = [];
  loading = false;
  submitting = false;
  errorMessage = '';
  successMessage = '';

  editId: string | null = null;
  form: Objectif = {
    titre: '',
    description: '',
    icon: '🎯',
    actif: true,
    produitIds: []
  };

  constructor(
    private objectifService: ObjectifService,
    private produitService: ProduitService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';

    this.objectifService.findAll().subscribe({
      next: (objectifs) => {
        this.objectifs = objectifs || [];
      },
      error: () => {
        this.errorMessage = 'Impossible de charger les objectifs.';
      }
    });

    this.produitService.findAll().subscribe({
      next: (produits) => {
        this.produits = (produits || []).filter(p => p.actif);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Impossible de charger les produits.';
      }
    });
  }

  isSelected(produitId?: string): boolean {
    if (!produitId) return false;
    return this.form.produitIds.includes(produitId);
  }

  toggleProduit(produitId?: string): void {
    if (!produitId) return;

    if (this.isSelected(produitId)) {
      this.form.produitIds = this.form.produitIds.filter(id => id !== produitId);
    } else {
      this.form.produitIds = [...this.form.produitIds, produitId];
    }
  }

  editObjectif(objectif: Objectif): void {
    this.editId = objectif.id || null;
    this.form = {
      titre: objectif.titre,
      description: objectif.description,
      icon: objectif.icon || '🎯',
      actif: objectif.actif,
      produitIds: [...(objectif.produitIds || [])]
    };
    this.successMessage = '';
    this.errorMessage = '';
  }

  resetForm(): void {
    this.editId = null;
    this.form = {
      titre: '',
      description: '',
      icon: '🎯',
      actif: true,
      produitIds: []
    };
  }

  submit(): void {
    if (!this.form.titre.trim() || !this.form.description.trim()) {
      this.errorMessage = 'Le titre et la description sont obligatoires.';
      return;
    }

    this.submitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload: Objectif = {
      ...this.form,
      titre: this.form.titre.trim(),
      description: this.form.description.trim(),
      icon: (this.form.icon || '🎯').trim() || '🎯'
    };

    const request = this.editId
      ? this.objectifService.update(this.editId, payload)
      : this.objectifService.create(payload);

    request.subscribe({
      next: () => {
        this.submitting = false;
        this.successMessage = this.editId ? 'Objectif mis à jour avec succès.' : 'Objectif ajouté avec succès.';
        this.resetForm();
        this.loadData();
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage = err?.error?.message || 'Erreur lors de l\'enregistrement de l\'objectif.';
      }
    });
  }

  deleteObjectif(objectif: Objectif): void {
    if (!objectif.id) return;
    if (!confirm(`Supprimer l'objectif "${objectif.titre}" ?`)) return;

    this.objectifService.delete(objectif.id).subscribe({
      next: () => {
        this.successMessage = 'Objectif supprimé.';
        this.errorMessage = '';
        if (this.editId === objectif.id) {
          this.resetForm();
        }
        this.loadData();
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Erreur lors de la suppression de l\'objectif.';
      }
    });
  }
}
