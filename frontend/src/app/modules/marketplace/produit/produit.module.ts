import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ProduitRoutingModule } from './produit-routing.module';
import { ProduitListComponent } from './produit-list/produit-list.component';
import { ProduitFormComponent } from './produit-form/produit-form.component';
import { ProduitDetailComponent } from './produit-detail/produit-detail.component';

@NgModule({
  imports: [
    CommonModule,
    ProduitRoutingModule,
    ProduitListComponent,
    ProduitFormComponent,
    ProduitDetailComponent
  ]
})
export class ProduitModule { }
