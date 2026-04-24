import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { FideliteService } from "../../services/fidelite.service";
import { AbonnementConfig, PointsFidelite } from "../../models/marketplace.models";
import { AuthService } from "../../../../services/auth.service";

@Component({
  selector: "app-fidelite-dashboard",
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-content">
      <div class="section-header">
        <div>
          <h1 class="section-title text-4xl">⭐ Programme de Fidélité</h1>
          <p class="view-subtitle">Gerez vos points et debloquez des avantages exclusifs</p>
        </div>
      </div>

      <div *ngIf="isClient" class="stats-grid mb-8">
        <div class="sp-card stat-card text-center border-l-4" style="border-color: #3B9EFF;">
          <div class="text-5xl mb-3">⭐</div>
          <p class="stat-label">Votre Solde</p>
          <p class="stat-description">Points cumulés au total</p>
          <p class="text-4xl font-black" style="color: var(--text);">{{ fidelite?.pointsTotal || 0 }}</p>
        </div>
        
        <div class="sp-card stat-card text-center border-l-4" style="border-color: var(--green);">
          <div class="text-5xl mb-3">💰</div>
          <p class="stat-label">À Utiliser</p>
          <p class="stat-description">Points prêts à être échangés</p>
          <p class="text-4xl font-black" style="color: var(--green);">{{ fidelite?.pointsDisponibles || 0 }}</p>
        </div>
        
        <div class="sp-card stat-card text-center border-l-4" style="border-color: var(--amber);">
          <div class="text-5xl mb-3">🏆</div>
          <p class="stat-label">Votre Statut</p>
          <p class="stat-description">Niveau d'adhésion actuel</p>
          <p class="text-3xl font-black" style="color: var(--amber);">{{ fidelite?.niveau || "Bronze" }}</p>
        </div>
        
        <div class="sp-card stat-card text-center border-l-4" style="border-color: var(--red);">
          <div class="text-5xl mb-3">🎁</div>
          <p class="stat-label">Échangés</p>
          <p class="stat-description">Points utilisés jusqu'à présent</p>
          <p class="text-4xl font-black" style="color: var(--red);">{{ fidelite?.pointsUtilises || 0 }}</p>
        </div>
      </div>

      <div *ngIf="isClient" class="sp-card mb-8">
        <h2 class="section-title text-2xl mb-6">Niveaux et Avantages</h2>
        <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div *ngFor="let niveau of niveaux" 
            class="p-6 border-2 rounded-2xl text-center transition-all bg-bg3"
            [ngStyle]="niveauStyle(niveau)"
          >
            <p class="font-black text-xl mb-2">{{ niveau }}</p>
            <span class="badge badge-green">
              {{ getReduction(niveau) }}% de réduction
            </span>
          </div>
        </div>
      </div>

      <div *ngIf="isClient" class="sp-card">
        <h2 class="section-title text-2xl mb-2">Acheter un abonnement</h2>
        <p class="view-subtitle mb-8">
          Utilisez vos points pour augmenter votre pourcentage de réduction sur vos futurs achats.
        </p>

        <div class="subs-grid">
          <div *ngFor="let option of abonnementOptions" class="sp-card sub-card border-white/10 bg-bg3/50">
            <p class="section-title text-lg mb-3" style="color: var(--text);">{{ option.label }}</p>
            <div class="space-y-3 mb-6 px-3">
              <div class="flex justify-between items-center">
                <span class="subscription-label">Réduction appliquée:</span>
                <span class="font-bold text-lg" style="color: var(--green);">{{ option.pourcentageReduction }}%</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="subscription-label">Coût en points:</span>
                <span class="font-bold text-lg text-white">{{ option.pointsRequis }} pts</span>
              </div>
            </div>
            <button
              class="btn btn-primary w-full"
              [disabled]="loadingAbonnement || !fidelite || fidelite.pointsDisponibles < option.pointsRequis"
              (click)="acheterAbonnement(option.code, option.pointsRequis)"
            >
              {{ loadingAbonnement ? '⏳...' : '✨ Débloquer' }}
            </button>
          </div>
        </div>
      </div>

      <div *ngIf="isAdmin" class="sp-card">
        <h2 class="section-title text-2xl mb-6">Gestion des abonnements (Admin)</h2>

        <div class="grid grid-cols-1 md:grid-cols-5 gap-4 mb-8 bg-bg3 p-6 rounded-2xl">
          <div class="form-group">
            <label class="form-label">Code</label>
            <input class="form-input" placeholder="BRONZE" [(ngModel)]="newAbonnement.code" />
          </div>
          <div class="form-group">
            <label class="form-label">Label</label>
            <input class="form-input" placeholder="Niveau Bronze" [(ngModel)]="newAbonnement.label" />
          </div>
          <div class="form-group">
            <label class="form-label">Points</label>
            <input class="form-input" type="number" [(ngModel)]="newAbonnement.pointsRequis" />
          </div>
          <div class="form-group">
            <label class="form-label">Réduction %</label>
            <input class="form-input" type="number" [(ngModel)]="newAbonnement.pourcentageReduction" />
          </div>
          <div class="flex items-end">
            <button class="btn btn-primary w-full" (click)="addAbonnement()">➕ Ajouter</button>
          </div>
        </div>

        <div class="admin-tarif-table-wrap overflow-x-auto">
          <table class="admin-tarif-table">
            <thead>
              <tr>
                <th>Code</th>
                <th>Label</th>
                <th>Coût pts</th>
                <th>Reduc %</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let option of abonnementOptions">
                <td><input class="form-input text-xs p-1" [(ngModel)]="option.code" /></td>
                <td><input class="form-input text-xs p-1" [(ngModel)]="option.label" /></td>
                <td><input class="form-input text-xs p-1 w-16" type="number" [(ngModel)]="option.pointsRequis" /></td>
                <td><input class="form-input text-xs p-1 w-16" type="number" [(ngModel)]="option.pourcentageReduction" /></td>
                <td>
                  <span class="badge" [ngClass]="option.actif ? 'badge-green' : 'badge-red'">
                    {{ option.actif ? 'ACTIF' : 'INACTIF' }}
                  </span>
                </td>
                <td>
                  <div class="flex gap-2">
                    <button class="btn btn-sm btn-edit" (click)="updateAbonnement(option)">💾</button>
                    <button class="btn btn-sm btn-delete" (click)="deleteAbonnement(option)">🗑️</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div *ngIf="errorMessage" class="mt-6 p-4 badge-red rounded-xl">
          ⚠️ {{ errorMessage }}
        </div>
        <div *ngIf="successMessage" class="mt-6 p-4 badge-green rounded-xl">
          ✅ {{ successMessage }}
        </div>
      </div>

      <div *ngIf="!isAdmin && !isClient" class="sp-card mt-8">
        <p class="state-text">Role inconnu, impossible de charger l'interface fidelite.</p>
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

    .stat-label {
      color: #e2e8f0;
      font-size: 1rem;
      font-weight: 600;
      letter-spacing: -0.3px;
      margin-bottom: 0.25rem;
    }

    .stat-description {
      color: #a8b8cc;
      font-size: 0.95rem;
      margin-bottom: 0.75rem;
      line-height: 1.4;
    }

    .subscription-label {
      color: #e0ecfb;
      font-size: 0.95rem;
      font-weight: 700;
    }

    .state-text {
      color: #e4eefc;
      font-weight: 600;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 1rem;
    }

    .subs-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 1rem;
    }

    .stat-card,
    .sub-card {
      border-color: var(--border);
      transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
    }

    .stat-card:hover,
    .sub-card:hover {
      transform: translateY(-2px);
      border-color: var(--border2) !important;
      box-shadow: 0 12px 26px rgba(0, 0, 0, 0.24);
    }

    @media (min-width: 820px) {
      .stats-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }

      .subs-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
    }

    @media (min-width: 1220px) {
      .stats-grid {
        grid-template-columns: repeat(4, minmax(0, 1fr));
      }

      .subs-grid {
        grid-template-columns: repeat(4, minmax(0, 1fr));
      }
    }
  `]
})
export class FideliteDashboardComponent implements OnInit {
  fidelite: PointsFidelite | null = null;
  loading = false;
  loadingAbonnement = false;
  loadingAbonnements = false;
  niveaux = ["BRONZE", "SILVER", "GOLD", "PLATINUM"];
  errorMessage = "";
  successMessage = "";
  abonnementOptions: AbonnementConfig[] = [];
  newAbonnement: AbonnementConfig = {
    code: "",
    label: "",
    pointsRequis: 10,
    pourcentageReduction: 2,
    actif: true
  };

  constructor(
    private fideliteService: FideliteService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadAbonnements();
    if (this.isClient) {
      this.loadFidelite();
    }
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  get isClient(): boolean {
    return this.authService.isClient();
  }

  loadFidelite(): void {
    this.loading = true;
    this.fideliteService.getPointsForUser(this.authService.getCurrentUserId()).subscribe({
      next: (data) => {
        this.fidelite = data;
        this.loading = false;
      },
      error: (err) => {
        console.error("Erreur:", err);
        this.loading = false;
      }
    });
  }

  acheterAbonnement(abonnement: string, pointsAUtiliser: number): void {
    if (!this.fidelite || this.fidelite.pointsDisponibles < pointsAUtiliser) {
      this.errorMessage = `Points insuffisants. Vous avez ${this.fidelite?.pointsDisponibles || 0} points, il faut ${pointsAUtiliser}.`;
      this.successMessage = "";
      return;
    }

    this.loadingAbonnement = true;
    this.errorMessage = "";
    this.successMessage = "";

    this.fideliteService.acheterAbonnement(this.authService.getCurrentUserId(), abonnement, pointsAUtiliser).subscribe({
      next: (data) => {
        this.fidelite = data;
        this.loadingAbonnement = false;
        this.successMessage = `Abonnement ${abonnement} achete avec succes. Votre prochain achat appliquera ${this.getReduction(abonnement)}% de reduction.`;
        this.loadFidelite();
      },
      error: (err) => {
        console.error('Erreur abonnement:', err);
        this.loadingAbonnement = false;
        this.errorMessage = err?.error?.message || "Erreur lors de l'achat de l'abonnement.";
      }
    });
  }

  loadAbonnements(): void {
    this.loadingAbonnements = true;
    this.fideliteService.getAbonnements(this.isClient).subscribe({
      next: (data) => {
        this.abonnementOptions = data;
        this.loadingAbonnements = false;
      },
      error: (err) => {
        console.error("Erreur abonnements:", err);
        this.loadingAbonnements = false;
      }
    });
  }

  addAbonnement(): void {
    this.errorMessage = "";
    this.successMessage = "";
    this.fideliteService.createAbonnement(this.newAbonnement).subscribe({
      next: () => {
        this.successMessage = "Abonnement ajoute avec succes.";
        this.newAbonnement = {
          code: "",
          label: "",
          pointsRequis: 10,
          pourcentageReduction: 2,
          actif: true
        };
        this.loadAbonnements();
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || "Erreur lors de l'ajout de l'abonnement.";
      }
    });
  }

  updateAbonnement(option: AbonnementConfig): void {
    if (!option.id) {
      return;
    }
    this.errorMessage = "";
    this.successMessage = "";
    this.fideliteService.updateAbonnement(option.id, option).subscribe({
      next: () => {
        this.successMessage = `Abonnement ${option.label} mis a jour.`;
        this.loadAbonnements();
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || "Erreur lors de la mise a jour.";
      }
    });
  }

  deleteAbonnement(option: AbonnementConfig): void {
    if (!option.id) {
      return;
    }

    const confirmed = window.confirm(`Confirmer la suppression de l'abonnement ${option.label} ?`);
    if (!confirmed) {
      return;
    }

    this.errorMessage = "";
    this.successMessage = "";
    this.fideliteService.deleteAbonnement(option.id).subscribe({
      next: () => {
        this.successMessage = `Abonnement ${option.label} supprime.`;
        this.loadAbonnements();
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || "Erreur lors de la suppression.";
      }
    });
  }

  getReduction(niveau: string): number {
    const found = this.abonnementOptions.find(a => a.code === niveau);
    return found ? found.pourcentageReduction : 0;
  }

  niveauStyle(niveau: string): any {
    switch (niveau) {
      case 'BRONZE': return { 'border-color': '#CD7F32', 'background': 'rgba(205, 127, 50, 0.1)', 'color': '#CD7F32' };
      case 'SILVER': return { 'border-color': '#C0C0C0', 'background': 'rgba(192, 192, 192, 0.1)', 'color': '#C0C0C0' };
      case 'GOLD': return { 'border-color': '#FFD700', 'background': 'rgba(255, 215, 0, 0.1)', 'color': '#FFD700' };
      case 'PLATINUM': return { 'border-color': '#A78BFA', 'background': 'rgba(167, 139, 250, 0.1)', 'color': '#A78BFA' };
      default: return { 'border-color': 'rgba(255,255,255,0.1)', 'background': 'var(--bg3)', 'color': 'var(--text)' };
    }
  }
}
