import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { StatutCommande } from '../../models/marketplace.models';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-commande-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page-content">
      <div class="section-header">
        <div>
          <h1 class="section-title text-4xl">Nouvelle Commande</h1>
          <p class="view-subtitle">Creez une commande avec ses lignes et calculez automatiquement les totaux.</p>
        </div>
        <button routerLink="/marketplace/admin/commandes" class="btn btn-ghost">⏎ Retour liste</button>
      </div>

      <div class="admin-grid">
        <div class="sp-card main-card">
          <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-6">
            <div class="head-fields">
              <div>
                <label class="form-label">Utilisateur ID</label>
                <input class="form-input" formControlName="utilisateurId" placeholder="ex. 1" />
              </div>
              <div>
                <label class="form-label">Statut</label>
                <select class="form-select" formControlName="statut">
                  <option *ngFor="let statut of statuts" [value]="statut">{{ statut }}</option>
                </select>
              </div>
            </div>

            <div>
              <div class="flex items-center justify-between mb-3">
                <h2 class="section-title text-xl">Lignes de commande</h2>
                <button type="button" class="btn btn-ghost" (click)="addLigne()">+ Ajouter ligne</button>
              </div>

              <div formArrayName="lignes" class="space-y-4">
                <div *ngFor="let ligne of lignes.controls; let i = index" [formGroupName]="i" class="sp-card line-card">
                  <div class="line-grid">
                    <div class="md:col-span-2">
                      <label class="form-label">Produit ID</label>
                      <input class="form-input" formControlName="produitId" placeholder="ID produit" />
                    </div>
                    <div>
                      <label class="form-label">Nom produit</label>
                      <input class="form-input" formControlName="nomProduit" placeholder="Nom" />
                    </div>
                    <div>
                      <label class="form-label">Qte</label>
                      <input type="number" class="form-input" formControlName="quantite" min="1" (input)="recalculateLigne(i)" />
                    </div>
                    <div>
                      <label class="form-label">Prix unitaire</label>
                      <input type="number" class="form-input" formControlName="prixUnitaire" min="0" step="0.01" (input)="recalculateLigne(i)" />
                    </div>
                  </div>

                  <div class="flex items-center justify-between mt-3">
                    <p class="line-subtotal">Sous-total: <span class="font-semibold">{{ ligne.value.sousTotal || 0 }} DT</span></p>
                    <button type="button" class="text-red-400 hover:underline text-sm" (click)="removeLigne(i)" *ngIf="lignes.length > 1">Supprimer</button>
                  </div>
                </div>
              </div>
            </div>

            <div class="flex gap-3 pt-2">
              <button type="submit" class="btn btn-primary flex-1" [disabled]="loading || form.invalid">
                {{ loading ? 'Enregistrement...' : 'Créer la commande' }}
              </button>
              <button type="button" class="btn btn-ghost flex-1" routerLink="/marketplace/admin/commandes">Annuler</button>
            </div>
          </form>
        </div>

        <div class="sp-card h-fit">
          <h2 class="section-title text-xl mb-4">Apercu</h2>
          <div class="preview-list text-sm">
            <div class="flex justify-between"><span>Total lignes</span><span class="font-semibold">{{ lignes.length }}</span></div>
            <div class="flex justify-between"><span>Montant total</span><span class="font-semibold" style="color: var(--green);">{{ montantTotal }} DT</span></div>
            <div class="flex justify-between"><span>Niveau fidélité</span><span class="font-semibold">{{ niveauClient }}</span></div>
          </div>
        </div>
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

    .admin-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 1.1rem;
    }

    .main-card {
      border-color: var(--border2);
    }

    .head-fields {
      display: grid;
      grid-template-columns: 1fr;
      gap: 0.9rem;
    }

    .line-card {
      background: var(--bg3);
      border-color: var(--border);
      padding: 0.85rem;
    }

    .line-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 0.7rem;
      align-items: end;
    }

    .line-subtotal {
      color: #d9e2ec;
      font-size: 0.9rem;
    }

    .preview-list {
      color: #d9e2ec;
      display: grid;
      gap: 0.55rem;
    }

    @media (min-width: 860px) {
      .head-fields {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }

      .line-grid {
        grid-template-columns: 2fr 1fr 1fr 1fr;
      }
    }

    @media (min-width: 1120px) {
      .admin-grid {
        grid-template-columns: minmax(0, 2fr) minmax(0, 1fr);
        gap: 1.5rem;
      }
    }
  `]
})
export class CommandeFormComponent implements OnInit {
  form: FormGroup;
  loading = false;
  statuts = Object.values(StatutCommande);
  niveauClient = 'BRONZE';
  private readonly apiUrl = `${environment.apiBaseUrl}/commandes`;

  constructor(private fb: FormBuilder, private http: HttpClient) {
    this.form = this.fb.group({
      utilisateurId: ['', Validators.required],
      statut: [StatutCommande.EN_COURS, Validators.required],
      lignes: this.fb.array([this.createLigne()])
    });
  }

  ngOnInit(): void {
    this.recalculateTotal();
  }

  get lignes(): FormArray {
    return this.form.get('lignes') as FormArray;
  }

  get montantTotal(): number {
    return this.lignes.controls.reduce((sum, ligne) => sum + Number(ligne.get('sousTotal')?.value || 0), 0);
  }

  createLigne(): FormGroup {
    return this.fb.group({
      produitId: ['', Validators.required],
      nomProduit: ['', Validators.required],
      quantite: [1, [Validators.required, Validators.min(1)]],
      prixUnitaire: [0, [Validators.required, Validators.min(0)]],
      sousTotal: [0]
    });
  }

  addLigne(): void {
    this.lignes.push(this.createLigne());
  }

  removeLigne(index: number): void {
    this.lignes.removeAt(index);
    this.recalculateTotal();
  }

  recalculateLigne(index: number): void {
    const ligne = this.lignes.at(index);
    const quantite = Number(ligne.get('quantite')?.value || 0);
    const prixUnitaire = Number(ligne.get('prixUnitaire')?.value || 0);
    ligne.get('sousTotal')?.setValue(quantite * prixUnitaire, { emitEvent: false });
  }

  recalculateTotal(): void {
    this.lignes.controls.forEach((_, index) => {
      const ligne = this.lignes.at(index);
      const quantite = Number(ligne.get('quantite')?.value || 0);
      const prixUnitaire = Number(ligne.get('prixUnitaire')?.value || 0);
      ligne.get('sousTotal')?.setValue(quantite * prixUnitaire, { emitEvent: false });
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    const commande = {
      ...this.form.value,
      montantTotal: this.montantTotal,
      dateCommande: new Date().toISOString(),
      lignes: this.lignes.value
    };

    this.http.post(this.apiUrl, commande).subscribe({
      next: () => {
        this.loading = false;
        this.form.reset({ utilisateurId: '', statut: StatutCommande.EN_COURS });
        while (this.lignes.length > 0) {
          this.lignes.removeAt(0);
        }
        this.addLigne();
      },
      error: (error: unknown) => {
        console.error('Erreur création commande:', error);
        this.loading = false;
      }
    });
  }
}
