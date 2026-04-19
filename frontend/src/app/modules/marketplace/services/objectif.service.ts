import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Objectif } from '../models/marketplace.models';

@Injectable({ providedIn: 'root' })
export class ObjectifService {
  private readonly apiUrl = `${environment.apiBaseUrl}/objectifs`;

  constructor(private http: HttpClient) {}

  findAll(): Observable<Objectif[]> {
    return this.http.get<Objectif[]>(this.apiUrl);
  }

  findActifs(): Observable<Objectif[]> {
    return this.http.get<Objectif[]>(`${this.apiUrl}/actifs`);
  }

  create(objectif: Objectif): Observable<Objectif> {
    return this.http.post<Objectif>(this.apiUrl, objectif);
  }

  update(id: string, objectif: Objectif): Observable<Objectif> {
    return this.http.put<Objectif>(`${this.apiUrl}/${id}`, objectif);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
