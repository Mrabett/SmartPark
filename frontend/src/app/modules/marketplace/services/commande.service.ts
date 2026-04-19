import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Commande, StatutLivraison, UtilisateurLite } from '../models/marketplace.models';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CommandeService {

  private apiUrl = `${environment.apiBaseUrl}/commandes`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<Commande[]> {
    return this.http.get<Commande[]>(this.apiUrl);
  }

  findById(id: string): Observable<Commande> {
    return this.http.get<Commande>(`${this.apiUrl}/${id}`);
  }

  create(commande: Commande): Observable<Commande> {
    return this.http.post<Commande>(this.apiUrl, commande);
  }

  update(id: string, commande: Commande): Observable<Commande> {
    return this.http.put<Commande>(`${this.apiUrl}/${id}`, commande);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  findByClient(utilisateurId: string): Observable<Commande[]> {
    return this.http.get<Commande[]>(`${this.apiUrl}/client/${utilisateurId}`);
  }

  findLivraisons(): Observable<Commande[]> {
    return this.http.get<Commande[]>(`${this.apiUrl}/livraisons`);
  }

  requestLivraison(id: string, lieuLivraison: string): Observable<Commande> {
    return this.http.put<Commande>(`${this.apiUrl}/${id}/livraison/request`, { lieuLivraison });
  }

  assignLivraison(id: string, agentId: string, agentNom: string): Observable<Commande> {
    return this.http.put<Commande>(`${this.apiUrl}/${id}/livraison/assign`, { agentId, agentNom });
  }

  updateStatutLivraison(id: string, statutLivraison: StatutLivraison): Observable<Commande> {
    return this.http.put<Commande>(`${this.apiUrl}/${id}/livraison/status`, { statutLivraison });
  }

  getUsers(): Observable<UtilisateurLite[]> {
    return this.http.get<UtilisateurLite[]>(`${environment.apiBaseUrl}/utilisateurs`);
  }
}
