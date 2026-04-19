import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PromotionListComponent } from './promotion-list/promotion-list.component';
import { PromotionFormComponent } from './promotion-form/promotion-form.component';

const routes: Routes = [
  { path: '', component: PromotionListComponent },
  { path: 'nouvelle', component: PromotionFormComponent },
  { path: ':id/edit', component: PromotionFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PromotionRoutingModule { }
