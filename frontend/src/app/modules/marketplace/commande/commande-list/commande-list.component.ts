import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { HttpClient } from "@angular/common/http";
import { RouterLink } from "@angular/router";
import { Commande } from "../../models/marketplace.models";
import { environment } from '../../../../../environments/environment';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: "app-commande-list",
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page-content">
      <div class="section-header">
        <div>
          <h1 class="section-title text-4xl">{{ isAdmin ? '📦 Gestion des Commandes' : '🛒 Mes Commandes' }}</h1>
          <p class="view-subtitle">Suivez l'etat de vos transactions et livraisons</p>
        </div>
        <a *ngIf="isAdmin" routerLink="nouvelle" class="btn btn-primary">+ Nouvelle commande</a>
      </div>

      <div *ngIf="isAdmin && !loading" class="sp-card revenue-card mb-4">
        <p class="revenue-label">Total de revenue</p>
        <p class="revenue-value">{{ totalRevenue | number:'1.2-2' }} DT</p>
      </div>
      
      <div *ngIf="loading" class="text-center py-12">
        <p class="state-text">Chargement de vos commandes...</p>
      </div>
      
      <div *ngIf="!loading && commandes.length > 0" class="orders-list">
        <div *ngFor="let cmd of commandes" class="sp-card order-card">
          <div class="order-row">
            <div class="order-main">
              <div class="order-icon">📄</div>
              <div>
                <h3 class="section-title text-lg" style="color: var(--text);">Commande #{{ cmd.numeroCommande || cmd.id }}</h3>
                <p class="order-date">{{ cmd.dateCommande | date:'mediumDate' }}</p>
                <p class="order-date" *ngIf="cmd.livraisonDemandee">
                  🚚 Livraison: {{ cmd.statutLivraison || 'EN_ATTENTE_AFFECTATION' }}
                </p>
                <p class="order-date" *ngIf="cmd.livraisonDemandee && cmd.lieuLivraison">
                  📍 {{ cmd.lieuLivraison }}
                </p>
              </div>
            </div>
            <div>
              <span [ngClass]="{
                'badge-green': cmd.statut === 'LIVREE' || cmd.statut === 'PAYEE',
                'badge-amber': cmd.statut === 'EN_COURS',
                'badge-red': cmd.statut === 'ANNULEE'
              }" class="badge">
                {{ cmd.statut }}
              </span>
            </div>
            <div class="order-total">
              <p class="text-2xl font-bold" style="color: var(--green);">{{ cmd.montantTotal }} <span class="text-sm">DT</span></p>
              <p class="order-items">{{ cmd.lignes.length }} article(s)</p>
            </div>
            <div class="flex gap-3">
              <a [routerLink]="['/marketplace', isAdmin ? 'admin' : 'client', 'commandes', cmd.id]" class="btn btn-ghost">
                Détails
              </a>
              <button *ngIf="isAdmin" class="btn btn-ghost" style="color: var(--red); border-color: rgba(255,77,106,0.2);" (click)="deleteCommande(cmd.id)">
                🗑️
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <div *ngIf="!loading && commandes.length === 0" class="sp-card text-center py-16">
        <p class="view-subtitle text-lg">Aucune commande n'a ete trouvee dans l'historique.</p>
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

    .orders-list {
      display: grid;
      gap: 1rem;
    }

    .order-card {
      border-color: var(--border);
      transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
    }

    .order-card:hover {
      transform: translateY(-2px);
      border-color: var(--border2);
      box-shadow: 0 12px 28px rgba(0, 0, 0, 0.24);
    }

    .order-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .order-main {
      display: flex;
      align-items: center;
      gap: 0.85rem;
    }

    .order-icon {
      background: var(--bg3);
      padding: 0.7rem;
      border-radius: 0.75rem;
      font-size: 1.35rem;
    }

    .order-date,
    .order-items {
      color: #deebfb;
      font-size: 0.86rem;
      font-weight: 600;
    }

    .state-text {
      color: #e4eefc;
      font-weight: 600;
    }

    .order-total {
      text-align: right;
    }

    .revenue-card {
      border-color: var(--border2);
      background: linear-gradient(135deg, rgba(0, 201, 127, 0.08), rgba(20, 28, 38, 0.8));
      padding: 1rem 1.2rem;
    }

    .revenue-label {
      color: #dbe7f5;
      font-size: 0.9rem;
      margin-bottom: 0.25rem;
      text-transform: uppercase;
      letter-spacing: 0.08em;
    }

    .revenue-value {
      color: var(--green);
      font-size: 2rem;
      font-weight: 900;
      line-height: 1.1;
    }
  `]
})
export class CommandeListComponent implements OnInit {
  commandes: Commande[] = [];
  loading = false;
  private readonly apiUrl = `${environment.apiBaseUrl}/commandes`;

  constructor(private http: HttpClient, private authService: AuthService) {}

  ngOnInit(): void {
    this.loadCommandes();
  }

  loadCommandes(): void {
    this.loading = true;
    const url = this.isAdmin
      ? this.apiUrl
      : `${this.apiUrl}/client/${this.authService.getCurrentUserId()}`;

    this.http.get<Commande[]>(url).subscribe({
      next: (data: Commande[]) => {
        this.commandes = [...data].sort((a, b) => {
          const da = a.dateCommande ? new Date(a.dateCommande).getTime() : 0;
          const db = b.dateCommande ? new Date(b.dateCommande).getTime() : 0;
          return db - da;
        });
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error("Erreur:", err);
        this.loading = false;
      }
    });
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  get routePrefix(): string {
    return this.isAdmin ? '/marketplace/admin' : '/marketplace/client';
  }

  get totalRevenue(): number {
    return this.commandes.reduce((sum, cmd) => sum + (cmd.montantTotal || 0), 0);
  }

  deleteCommande(id?: string): void {
    if (!this.isAdmin || !id) {
      return;
    }

    if (!confirm('Supprimer cette commande ?')) {
      return;
    }

    this.http.delete<void>(`${this.apiUrl}/${id}`).subscribe({
      next: () => this.loadCommandes(),
      error: (err: unknown) => console.error('Erreur suppression commande:', err)
    });
  }
}
