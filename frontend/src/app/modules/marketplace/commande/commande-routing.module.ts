import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CommandeListComponent } from './commande-list/commande-list.component';
import { CommandeFormComponent } from './commande-form/commande-form.component';
import { CommandeDetailComponent } from './commande-detail/commande-detail.component';
import { LivraisonAdminComponent } from './livraison-admin/livraison-admin.component';

const routes: Routes = [
  { path: '', component: CommandeListComponent },
  { path: 'livraisons', component: LivraisonAdminComponent },
  { path: 'nouvelle', component: CommandeFormComponent },
  { path: ':id', component: CommandeDetailComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CommandeRoutingModule { }
