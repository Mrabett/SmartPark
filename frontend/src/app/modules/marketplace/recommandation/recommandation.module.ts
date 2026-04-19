import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { RecommandationRoutingModule } from './recommandation-routing.module';
import { RecommandationListComponent } from './recommandation-list/recommandation-list.component';


@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    RecommandationRoutingModule,
    RecommandationListComponent
  ]
})
export class RecommandationModule { }
