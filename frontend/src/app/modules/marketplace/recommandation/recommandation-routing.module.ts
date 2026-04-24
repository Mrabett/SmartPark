import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { RecommandationListComponent } from './recommandation-list/recommandation-list.component';

const routes: Routes = [
  { path: '', component: RecommandationListComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RecommandationRoutingModule { }
