import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FideliteRoutingModule } from './fidelite-routing.module';
import { FideliteDashboardComponent } from './fidelite-dashboard/fidelite-dashboard.component';


@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    FideliteRoutingModule,
    FideliteDashboardComponent
  ]
})
export class FideliteModule { }
