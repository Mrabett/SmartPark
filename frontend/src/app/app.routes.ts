import { Routes } from '@angular/router';
import { authGuard, adminGuard } from './guards/auth.guard';

import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { TerrainListComponent } from './pages/terrains/terrain-list/terrain-list.component';
import { TerrainFormComponent } from './pages/terrains/terrain-form/terrain-form.component';
import { ReservationListComponent } from './pages/reservations/reservation-list/reservation-list.component';
import { UserTerrainsComponent } from './pages/user/user-terrains/user-terrains.component';
import { UserReserverComponent } from './pages/user/user-reserver/user-reserver.component';
import { UserMesReservationsComponent } from './pages/user/user-mes-reservations/user-mes-reservations.component';
import { UserPlanningComponent } from './pages/user/user-planning/user-planning.component';
import { ChatbotComponent } from './pages/chatbot/chatbot.component';
import { UserParametresComponent } from './pages/user/user-parametres/user-parametres.component';
import { TarificationComponent } from './pages/tarification/tarification.component'; // ✅ AJOUTÉ
//import { ChatbotComponent } from
  './pages/ia/chatbot/chatbot.component';
//import { IaDashboardComponent } from
  './pages/admin/ia-dashboard/ia-dashboard.component';
  import { UserMatchsComponent } from
  './pages/user/user-matchs/user-matchs.component';
import { UserCreerMatchComponent } from
  './pages/user/user-creer-match/user-creer-match.component';
import { UserFideliteComponent } from
  './pages/user/user-fidelite/user-fidelite.component';

import { PanierComponent } from './modules/marketplace/panier/panier.component';
import { MarketplaceLayoutComponent } from './layout/marketplace-layout.component';
import { ClientLayoutComponent } from './layout/client-layout/client-layout.component';
import { ExpirationAlertsComponent } from './modules/marketplace/alerts/expiration-alerts.component';
import { AiSuggestComponent } from './modules/marketplace/ai/ai-suggest.component';
import { ParcoursObjectifComponent } from './modules/marketplace/objectifs/parcours-objectif.component';
import { ObjectifAdminComponent } from './modules/marketplace/objectifs/objectif-admin.component';
import { ChaterComponent } from './pages/chater/chater.component';
import { roleGuard } from './auth/role.guard';



export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  // ── Publiques ──────────────────────────────────────
  { path: 'login',    component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  // ── ADMIN ──────────────────────────────────────────
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'terrains',
    component: TerrainListComponent,
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'terrains/nouveau',
    component: TerrainFormComponent,
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'terrains/modifier/:id',
    component: TerrainFormComponent,
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'reservations',
    component: ReservationListComponent,
    canActivate: [authGuard, adminGuard]
  },
  {
  path: 'chatbot',
  component: ChatbotComponent,
  canActivate: [authGuard]
},

  {
    path: 'tarification',           // ✅ AJOUTÉ
    component: TarificationComponent,
    canActivate: [authGuard, adminGuard]
  },

  // ── USER ───────────────────────────────────────────
  {
    path: 'user/terrains',
    component: UserTerrainsComponent,
    canActivate: [authGuard]
  },
  {
    path: 'user/reserver/:id',
    component: UserReserverComponent,
    canActivate: [authGuard]
  },
  {
    path: 'user/reservations',
    component: UserMesReservationsComponent,
    canActivate: [authGuard]
  },
  {
    path: 'user/calendrier',
    component: UserPlanningComponent,
    canActivate: [authGuard]
  },
  {
  path: 'user/matchs',
  component: UserMatchsComponent,
  canActivate: [authGuard]
},
{
  path: 'user/creer-match',
  component: UserCreerMatchComponent,
  canActivate: [authGuard]
},

  {
    path: 'user/parametres',
    component: UserParametresComponent,
    canActivate: [authGuard]
  },
  {
  path: 'user/fidelite',
  component: UserFideliteComponent,
  canActivate: [authGuard]
},


  // ── Chatbot ────────────────────────────────────────
  {
    path: 'chatbot',
    component: ChatbotComponent,
    canActivate: [authGuard]
  },

  // ── MARKETPLACE ADMIN ──────────────────────────────
  {
    path: 'marketplace/admin',
    component: MarketplaceLayoutComponent,
    canActivate: [roleGuard],
    data: { role: 'admin' },
    children: [
      { path: '', redirectTo: 'produits', pathMatch: 'full' },
      { path: 'produits', loadChildren: () => import('./modules/marketplace/produit/produit.module').then(m => m.ProduitModule) },
      { path: 'commandes', loadChildren: () => import('./modules/marketplace/commande/commande.module').then(m => m.CommandeModule) },
      { path: 'objectifs', component: ObjectifAdminComponent },
      { path: 'promotions', loadChildren: () => import('./modules/marketplace/promotion/promotion.module').then(m => m.PromotionModule) },
      { path: 'alertes', component: ExpirationAlertsComponent },
      { path: 'ai-suggest', component: AiSuggestComponent },
      { path: 'chater', component: ChaterComponent },
      { path: 'fidelite', loadChildren: () => import('./modules/marketplace/fidelite/fidelite.module').then(m => m.FideliteModule) }
    ]
  },

  // ── MARKETPLACE CLIENT ─────────────────────────────
  {
    path: 'marketplace/client',
    component: MarketplaceLayoutComponent,
    canActivate: [roleGuard],
    data: { role: 'client' },
    children: [
      { path: '', redirectTo: 'produits', pathMatch: 'full' },
      { path: 'produits', loadChildren: () => import('./modules/marketplace/produit/produit.module').then(m => m.ProduitModule) },
      { path: 'objectifs', component: ParcoursObjectifComponent },
      { path: 'panier', component: PanierComponent },
      { path: 'commandes', loadChildren: () => import('./modules/marketplace/commande/commande.module').then(m => m.CommandeModule) },
      { path: 'fidelite', loadChildren: () => import('./modules/marketplace/fidelite/fidelite.module').then(m => m.FideliteModule) },
      { path: 'chater', component: ChaterComponent },
      { path: 'recommandations', loadChildren: () => import('./modules/marketplace/recommandation/recommandation.module').then(m => m.RecommandationModule) }
    ]
  },

  // ── Wildcard ───────────────────────────────────────
  { path: '**', redirectTo: 'login' }
];