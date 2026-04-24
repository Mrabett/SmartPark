import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PromotionRoutingModule } from './promotion-routing.module';
import { PromotionListComponent } from './promotion-list/promotion-list.component';
import { PromotionFormComponent } from './promotion-form/promotion-form.component';


@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    PromotionRoutingModule,
    PromotionListComponent,
    PromotionFormComponent
  ]
})
export class PromotionModule { }
