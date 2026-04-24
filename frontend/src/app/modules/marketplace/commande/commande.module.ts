import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CommandeRoutingModule } from './commande-routing.module';
import { CommandeListComponent } from './commande-list/commande-list.component';
import { CommandeFormComponent } from './commande-form/commande-form.component';
import { CommandeDetailComponent } from './commande-detail/commande-detail.component';
import { LivraisonAdminComponent } from './livraison-admin/livraison-admin.component';


@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    CommandeRoutingModule,
    CommandeListComponent,
    CommandeFormComponent,
    CommandeDetailComponent,
    LivraisonAdminComponent
  ]
})
export class CommandeModule { }
