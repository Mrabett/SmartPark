import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../services/auth.service';
import { Commande, StatutLivraison, UtilisateurLite } from '../../models/marketplace.models';
import { CommandeService } from '../../services/commande.service';

@Component({
  selector: 'app-livraison-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="page-content">
      <div class="section-header">
        <div>
          <h1 class="section-title text-4xl">🚚 Livraison</h1>
          <p class="subtitle">Affectez des agents et suivez les commandes a livrer</p>
        </div>
      </div>

      <div *ngIf="!isAdmin" class="sp-card text-center py-10">
        <p class="text-lg state-text">Acces reserve a l'administration.</p>
        <a routerLink="/marketplace/client/commandes" class="btn btn-ghost mt-4">Retour</a>
      </div>

      <div *ngIf="isAdmin && loading" class="text-center py-10 state-text">Chargement...</div>

      <div *ngIf="isAdmin && !loading && commandes.length === 0" class="sp-card text-center py-10">
        <p class="text-lg state-text">Aucune demande de livraison pour le moment.</p>
      </div>

      <div *ngIf="isAdmin && !loading && commandes.length > 0" class="grid gap-4">
        <div *ngFor="let cmd of commandes" class="sp-card livraison-card">
          <div class="flex flex-wrap justify-between gap-4 items-start mb-4">
            <div>
              <h3 class="section-title text-xl">Commande #{{ cmd.numeroCommande || cmd.id }}</h3>
              <p class="meta-strong">Client: <span class="value-strong">{{ cmd.utilisateurNom || '-' }} {{ cmd.utilisateurPrenom || '' }}</span></p>
              <p class="meta-strong">Lieu: <span class="value-strong">{{ cmd.lieuLivraison || '-' }}</span></p>
            </div>
            <div class="text-right">
              <span class="badge" [ngClass]="badgeLivraison(cmd.statutLivraison)">{{ cmd.statutLivraison || 'EN_ATTENTE_AFFECTATION' }}</span>
              <p class="price-strong mt-2">{{ cmd.montantTotal | number:'1.2-2' }} DT</p>
            </div>
          </div>

          <div class="grid md:grid-cols-3 gap-3 items-end">
            <div>
              <label class="field-label mb-1 block">Agent</label>
              <select class="form-select" [(ngModel)]="assignments[cmd.id || ''].agentId" (ngModelChange)="onAgentChange(cmd)">
                <option value="">Selectionner un agent</option>
                <option *ngFor="let user of agents" [value]="user.id">{{ displayUser(user) }}</option>
              </select>
            </div>

            <div>
              <label class="field-label mb-1 block">Nom agent (manuel)</label>
              <input class="form-input" [(ngModel)]="assignments[cmd.id || ''].agentNom" placeholder="Ex: Agent Karim" />
            </div>

            <button class="btn btn-primary" (click)="assigner(cmd)">Affecter agent</button>
          </div>

          <div class="grid md:grid-cols-2 gap-3 mt-4 items-end">
            <div>
              <label class="field-label mb-1 block">Statut livraison</label>
              <select class="form-select" [(ngModel)]="statusUpdates[cmd.id || '']">
                <option *ngFor="let statut of statutsLivraison" [ngValue]="statut">{{ statut }}</option>
              </select>
            </div>
            <button class="btn btn-ghost" (click)="updateStatut(cmd)">Mettre a jour le statut</button>
          </div>

          <p class="agent-strong mt-3" *ngIf="cmd.agentLivraisonNom">Agent affecte: {{ cmd.agentLivraisonNom }}</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .subtitle {
      color: #d8e4f3;
      margin-top: 0.35rem;
      font-size: 0.95rem;
    }

    .state-text {
      color: #e4eefc;
      font-weight: 600;
    }

    .livraison-card {
      border: 1px solid var(--border2);
      background: linear-gradient(145deg, rgba(18, 30, 45, 0.92), rgba(10, 17, 28, 0.92));
    }

    .meta-strong {
      color: #d9e8fb;
      font-size: 0.88rem;
      font-weight: 700;
      margin-top: 0.2rem;
      letter-spacing: 0.01em;
    }

    .value-strong {
      color: #ffffff;
      font-weight: 800;
    }

    .price-strong {
      color: #7efac5;
      font-size: 1rem;
      font-weight: 900;
      letter-spacing: 0.02em;
    }

    .field-label {
      color: #e6f1ff;
      font-size: 0.78rem;
      font-weight: 800;
      text-transform: uppercase;
      letter-spacing: 0.06em;
    }

    .agent-strong {
      color: #fff3bf;
      font-size: 0.84rem;
      font-weight: 800;
      letter-spacing: 0.02em;
    }
  `]
})
export class LivraisonAdminComponent implements OnInit {
  commandes: Commande[] = [];
  agents: UtilisateurLite[] = [];
  loading = false;
  statutsLivraison = Object.values(StatutLivraison);
  assignments: Record<string, { agentId: string; agentNom: string }> = {};
  statusUpdates: Record<string, StatutLivraison> = {};

  constructor(
    private auth: AuthService,
    private commandeService: CommandeService
  ) {}

  ngOnInit(): void {
    if (!this.isAdmin) {
      return;
    }
    this.loadData();
  }

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  loadData(): void {
    this.loading = true;

    this.commandeService.findLivraisons().subscribe({
      next: (data) => {
        this.commandes = [...data].sort((a, b) => {
          const da = a.dateCommande ? new Date(a.dateCommande).getTime() : 0;
          const db = b.dateCommande ? new Date(b.dateCommande).getTime() : 0;
          return db - da;
        });

        for (const cmd of this.commandes) {
          if (!cmd.id) {
            continue;
          }
          this.assignments[cmd.id] = {
            agentId: cmd.agentLivraisonId || '',
            agentNom: cmd.agentLivraisonNom || ''
          };
          this.statusUpdates[cmd.id] = cmd.statutLivraison || StatutLivraison.EN_ATTENTE_AFFECTATION;
        }

        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement livraisons:', err);
        this.loading = false;
      }
    });

    this.commandeService.getUsers().subscribe({
      next: (users) => {
        this.agents = users.filter((u) => (u.role || '').toUpperCase() !== 'CLIENT');
      },
      error: (err) => console.error('Erreur chargement utilisateurs:', err)
    });
  }

  onAgentChange(cmd: Commande): void {
    const cmdId = cmd.id;
    if (!cmdId) {
      return;
    }
    const assignment = this.assignments[cmdId];
    if (!assignment) {
      return;
    }

    const selected = this.agents.find((a) => a.id === assignment.agentId);
    if (selected) {
      assignment.agentNom = this.displayUser(selected);
    }
  }

  assigner(cmd: Commande): void {
    if (!cmd.id) {
      return;
    }

    const assignment = this.assignments[cmd.id];
    if (!assignment.agentId && !assignment.agentNom.trim()) {
      alert('Veuillez choisir ou saisir un agent.');
      return;
    }

    this.commandeService.assignLivraison(cmd.id, assignment.agentId, assignment.agentNom).subscribe({
      next: () => this.loadData(),
      error: (err) => {
        console.error('Erreur affectation:', err);
        alert('Erreur pendant l\'affectation de l\'agent.');
      }
    });
  }

  updateStatut(cmd: Commande): void {
    if (!cmd.id) {
      return;
    }

    const statut = this.statusUpdates[cmd.id] || StatutLivraison.EN_ATTENTE_AFFECTATION;
    this.commandeService.updateStatutLivraison(cmd.id, statut).subscribe({
      next: () => this.loadData(),
      error: (err) => {
        console.error('Erreur statut livraison:', err);
        alert('Erreur pendant la mise a jour du statut.');
      }
    });
  }

  displayUser(user: UtilisateurLite): string {
    const nom = [user.nom || '', user.prenom || ''].join(' ').trim();
    const fallback = user.email || user.id;
    return nom || fallback;
  }

  badgeLivraison(statut?: string): string {
    if (statut === 'LIVREE') {
      return 'badge-green';
    }
    if (statut === 'EN_COURS' || statut === 'AFFECTEE') {
      return 'badge-amber';
    }
    if (statut === 'ECHEC' || statut === 'ANNULEE') {
      return 'badge-red';
    }
    return 'badge-primary';
  }
}
