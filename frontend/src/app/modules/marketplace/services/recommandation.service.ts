import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProduitRecommande, Recommandation } from '../models/marketplace.models';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class RecommandationService {

  private apiUrl = `${environment.apiBaseUrl}/recommandations`;

  constructor(private http: HttpClient) {}

  generer(clientId: string): Observable<Recommandation[]> {
    return this.http.post<Recommandation[]>(`${this.apiUrl}/generer/${clientId}`, {});
  }

  generateRecommandations(clientId: string): Observable<Recommandation[]> {
    return this.generer(clientId);
  }

  getRecommandations(clientId: string): Observable<Recommandation[]> {
    return this.http.get<Recommandation[]>(`${this.apiUrl}/client/${clientId}`);
  }

  getRecommandationsForUser(clientId: string): Observable<Recommandation[]> {
    return this.getRecommandations(clientId);
  }

  getRecommendedProducts(clientId: string): Observable<ProduitRecommande[]> {
    return this.http.get<ProduitRecommande[]>(`${this.apiUrl}/produits/${clientId}`);
  }

  marquerVue(id: string): Observable<Recommandation> {
    return this.http.put<Recommandation>(`${this.apiUrl}/${id}/vue`, {});
  }

  markAsViewed(id: string): Observable<Recommandation> {
    return this.marquerVue(id);
  }

  markAsPurchased(id: string): Observable<Recommandation> {
    return this.http.put<Recommandation>(`${this.apiUrl}/${id}/achetee`, {});
  }
}
