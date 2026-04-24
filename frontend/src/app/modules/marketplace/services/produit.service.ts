import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Produit, ProduitExpirationAlert } from '../models/marketplace.models';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProduitService {

  private apiUrl = `${environment.apiBaseUrl}/produits`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<Produit[]> {
    return this.http.get<Produit[]>(this.apiUrl);
  }

  findById(id: string): Observable<Produit> {
    return this.http.get<Produit>(`${this.apiUrl}/${id}`);
  }

  create(produit: Produit): Observable<Produit> {
    return this.http.post<Produit>(this.apiUrl, produit);
  }

  update(id: string, produit: Produit): Observable<Produit> {
    return this.http.put<Produit>(`${this.apiUrl}/${id}`, produit);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  findByCategorie(categorie: string): Observable<Produit[]> {
    return this.http.get<Produit[]>(`${this.apiUrl}/categorie/${categorie}`);
  }

  findActifs(): Observable<Produit[]> {
    return this.http.get<Produit[]>(`${this.apiUrl}/actifs`);
  }

  decreaseStock(produitId: string, quantite: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${produitId}/stock/decrease`, { quantite });
  }

  getExpirationAlerts(jours: number = 10): Observable<ProduitExpirationAlert[]> {
    return this.http.get<ProduitExpirationAlert[]>(`${this.apiUrl}/alertes/expiration?jours=${jours}`);
  }
}
