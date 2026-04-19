import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { environment } from '../../../../../environments/environment';
import { Commande, StatutCommande } from '../../models/marketplace.models';
import { AuthService } from '../../../../services/auth.service';
import { ProduitService } from '../../services/produit.service';

@Component({
  selector: 'app-commande-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="page-content" *ngIf="commande">
      <button [routerLink]="[routePrefix, 'commandes']" class="btn btn-ghost mb-4">← Retour aux commandes</button>
      <div class="sp-card detail-card">
        <div class="header-row">
          <div>
            <h1 class="section-title text-3xl">Commande Ndeg {{ commande.numeroCommande || commande.id }}</h1>
            <p class="meta-text">{{ commande.dateCommande | date:'medium' }}</p>
          </div>
          <span class="badge" [ngClass]="statusClass(commande.statut)">{{ commande.statut }}</span>
        </div>

        <div class="sp-card panel mb-6">
          <h2 class="section-title text-xl mb-3">Informations client</h2>
          <div class="panel-grid">
            <div>
              <p class="field-label">Nom</p>
              <p class="field-value" *ngIf="!isAdmin">{{ commande.utilisateurNom || '-' }}</p>
              <input *ngIf="isAdmin" class="form-input" [(ngModel)]="commande.utilisateurNom" />
            </div>
            <div>
              <p class="field-label">Prenom</p>
              <p class="field-value" *ngIf="!isAdmin">{{ commande.utilisateurPrenom || '-' }}</p>
              <input *ngIf="isAdmin" class="form-input" [(ngModel)]="commande.utilisateurPrenom" />
            </div>
          </div>
        </div>

        <div class="sp-card panel mb-6" *ngIf="isAdmin">
          <h2 class="section-title text-xl mb-3">Mise a jour commande</h2>
          <div class="panel-grid actions-grid">
            <div>
              <label class="field-label">Statut</label>
              <select class="form-select" [(ngModel)]="commande.statut">
                <option *ngFor="let statut of statuts" [ngValue]="statut">{{ statut }}</option>
              </select>
            </div>
            <button class="btn btn-primary" (click)="saveAdminChanges()" [disabled]="saving">{{ saving ? 'Enregistrement...' : 'Enregistrer modifications' }}</button>
          </div>
          <button class="btn btn-ghost mt-3" style="color: var(--red); border-color: rgba(255,77,106,0.25);" (click)="deleteCommande()">Supprimer cette commande</button>
        </div>

        <div class="items-list mb-6">
          <div *ngFor="let ligne of commande.lignes" class="item-row">
            <div class="item-main">
              <div class="thumb">
                <img [src]="resolveLineImage(ligne.produitId, ligne.image)" class="w-full h-full object-cover" onerror="this.src='https://via.placeholder.com/48?text=P'"/>
              </div>
              <div>
                <p class="item-name">{{ ligne.nomProduit }}</p>
                <p class="item-ref">Ref: {{ ligne.produitId }}</p>
              </div>
            </div>
            <div class="item-prices">
              <p class="item-line">{{ ligne.quantite }} x {{ ligne.prixUnitaire }} DT</p>
              <p class="item-total">{{ ligne.sousTotal }} DT</p>
            </div>
          </div>
        </div>

        <div class="summary-row">
          <p class="text-lg summary-label">Montant total</p>
          <p class="text-2xl font-bold" style="color: var(--green);">{{ commande.montantTotal }} DT</p>
        </div>
      </div>
    </div>

    <div *ngIf="loading" class="text-center py-12 state-text">Chargement...</div>
  `,
  styles: [`
    .detail-card {
      border-color: var(--border);
    }

    .header-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 1rem;
      margin-bottom: 1.25rem;
      flex-wrap: wrap;
    }

    .meta-text {
      color: #dbe8f9;
      font-size: 0.88rem;
      font-weight: 600;
      margin-top: 0.25rem;
    }

    .panel {
      background: var(--bg3);
      border-color: var(--border2);
      padding: 1rem;
    }

    .panel-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 0.85rem;
    }

    .actions-grid {
      align-items: end;
    }

    .field-label {
      color: #e5f0ff;
      font-size: 0.78rem;
      font-weight: 800;
      margin-bottom: 0.3rem;
      text-transform: uppercase;
      letter-spacing: 0.06em;
    }

    .field-value {
      color: var(--text);
      font-weight: 600;
    }

    .items-list {
      display: grid;
      gap: 0.65rem;
    }

    .item-row {
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 0.9rem;
      background: rgba(255, 255, 255, 0.02);
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 0.7rem;
      flex-wrap: wrap;
    }

    .item-main {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }

    .thumb {
      width: 48px;
      height: 48px;
      border-radius: 10px;
      overflow: hidden;
      border: 1px solid var(--border);
    }

    .item-name {
      color: var(--text);
      font-weight: 600;
    }

    .item-ref {
      color: #dde9fb;
      font-size: 0.78rem;
      font-weight: 600;
    }

    .summary-label {
      color: #e4eefc;
      font-weight: 700;
    }

    .state-text {
      color: #e4eefc;
      font-weight: 600;
    }

    .item-prices {
      text-align: right;
    }

    .item-line {
      color: #dbe5ef;
      font-size: 0.88rem;
    }

    .item-total {
      color: var(--text);
      font-weight: 700;
    }

    .summary-row {
      border-top: 1px solid var(--border);
      padding-top: 1rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    @media (min-width: 900px) {
      .panel-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
    }
  `]
})
export class CommandeDetailComponent implements OnInit {
  commande: Commande | null = null;
  lineImageMap: Record<string, string> = {};
  loading = false;
  saving = false;
  statuts = Object.values(StatutCommande);
  private readonly apiUrl = `${environment.apiBaseUrl}/commandes`;

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router,
    private produitService: ProduitService
  ) {}

  ngOnInit(): void {
    this.loading = true;
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.loading = false;
      return;
    }

    this.http.get<Commande>(`${this.apiUrl}/${id}`).subscribe({
      next: (data) => {
        this.commande = data;
        this.hydrateMissingLineImages();
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error('Erreur chargement commande:', err);
        this.loading = false;
      }
    });
  }

  get routePrefix(): string {
    return this.authService.isAdmin() ? '/marketplace/admin' : '/marketplace/client';
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  resolveLineImage(produitId: string, lineImage?: string): string {
    if (lineImage && lineImage.trim().length > 0) {
      return lineImage;
    }

    if (produitId && this.lineImageMap[produitId]) {
      return this.lineImageMap[produitId];
    }

    return 'https://via.placeholder.com/48?text=P';
  }

  private hydrateMissingLineImages(): void {
    if (!this.commande?.lignes?.length) {
      return;
    }

    const missingIds = this.commande.lignes
      .filter((ligne) => (!ligne.image || !ligne.image.trim()) && !!ligne.produitId)
      .map((ligne) => ligne.produitId)
      .filter((id, index, arr) => arr.indexOf(id) === index);

    for (const produitId of missingIds) {
      this.produitService.findById(produitId).subscribe({
        next: (produit) => {
          if (produit?.image) {
            this.lineImageMap[produitId] = produit.image;
          }
        },
        error: () => {
          // Keep default placeholder when product lookup fails
        }
      });
    }
  }

  saveAdminChanges(): void {
    if (!this.isAdmin || !this.commande?.id) {
      return;
    }

    this.saving = true;
    this.http.put<Commande>(`${this.apiUrl}/${this.commande.id}`, this.commande).subscribe({
      next: (updated) => {
        this.commande = updated;
        this.saving = false;
      },
      error: (err: unknown) => {
        console.error('Erreur mise a jour commande:', err);
        this.saving = false;
      }
    });
  }

  deleteCommande(): void {
    if (!this.isAdmin || !this.commande?.id) {
      return;
    }

    if (!confirm('Supprimer cette commande ?')) {
      return;
    }

    this.http.delete<void>(`${this.apiUrl}/${this.commande.id}`).subscribe({
      next: () => this.router.navigate(['/marketplace/admin/commandes']),
      error: (err: unknown) => console.error('Erreur suppression commande:', err)
    });
  }

  statusClass(status: string): string {
    if (status === 'LIVREE') return 'badge-success';
    if (status === 'PAYEE') return 'badge-primary';
    if (status === 'ANNULEE') return 'badge-danger';
    return 'badge-warning';
  }
}
