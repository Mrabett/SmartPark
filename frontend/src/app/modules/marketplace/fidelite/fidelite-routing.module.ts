import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FideliteDashboardComponent } from './fidelite-dashboard/fidelite-dashboard.component';

const routes: Routes = [
  { path: '', component: FideliteDashboardComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FideliteRoutingModule { }
