import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PointsFidelite, HistoriquePoints, AbonnementConfig } from '../models/marketplace.models';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class FideliteService {

  private apiUrl = `${environment.apiBaseUrl}/marketplace/fidelite`;

  constructor(private http: HttpClient) {}

  getPoints(utilisateurId: string): Observable<PointsFidelite> {
    return this.http.get<PointsFidelite>(`${this.apiUrl}/client/${utilisateurId}`);
  }

  getPointsForUser(utilisateurId: string): Observable<PointsFidelite> {
    return this.getPoints(utilisateurId);
  }

  getReduction(utilisateurId: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/reduction/${utilisateurId}`);
  }

  ajouterPoints(data: any): Observable<PointsFidelite> {
    return this.http.post<PointsFidelite>(`${this.apiUrl}/ajouter-points`, data);
  }

  acheterAbonnement(utilisateurId: string, abonnement: string, points: number): Observable<PointsFidelite> {
    return this.http.post<PointsFidelite>(`${this.apiUrl}/acheter-abonnement`, {
      utilisateurId,
      abonnement,
      points
    });
  }

  getAbonnements(actifsSeulement = false): Observable<AbonnementConfig[]> {
    return this.http.get<AbonnementConfig[]>(`${this.apiUrl}/abonnements?actifsSeulement=${actifsSeulement}`);
  }

  createAbonnement(payload: AbonnementConfig): Observable<AbonnementConfig> {
    return this.http.post<AbonnementConfig>(`${this.apiUrl}/abonnements`, payload);
  }

  updateAbonnement(id: string, payload: AbonnementConfig): Observable<AbonnementConfig> {
    return this.http.put<AbonnementConfig>(`${this.apiUrl}/abonnements/${id}`, payload);
  }

  deleteAbonnement(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/abonnements/${id}`);
  }

  getHistorique(utilisateurId: string): Observable<HistoriquePoints[]> {
    return this.http.get<HistoriquePoints[]>(`${this.apiUrl}/historique/${utilisateurId}`);
  }

  getHistoriqueForUser(utilisateurId: string): Observable<HistoriquePoints[]> {
    return this.getHistorique(utilisateurId);
  }

  addPointsForPurchase(utilisateurId: string, nombreProduits: number, commandeId?: string): Observable<PointsFidelite> {
    return this.http.post<PointsFidelite>(`${this.apiUrl}/ajouter-points`, {
      utilisateurId,
      commandeId,
      nombreProduits
    });
  }
}
