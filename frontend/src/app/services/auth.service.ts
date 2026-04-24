import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';

export interface AuthUser {
  id?:        string;
  email:      string;
  nom:        string;
  prenom?:    string;
  role:       'ADMIN' | 'USER';
  telephone?: string;
  token:      string;
}

export interface ClientInfo {
  id: string;
  nom: string;
  prenom: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private apiUrl = 'http://localhost:8081/api/auth';
  private userSubject = new BehaviorSubject<AuthUser | null>(
    this.getUserFromStorage()
  );

  user$ = this.userSubject.asObservable();

  constructor(
    private http:   HttpClient,
    private router: Router
  ) {}

  // ✅ Inscription
  register(data: {
    email: string; password: string;
    telephone: string; nom: string;
  }): Observable<AuthUser> {
    return this.http.post<AuthUser>(
      `${this.apiUrl}/register`, data
    ).pipe(tap(res => this.saveUser(res)));
  }

  // ✅ Connexion
  login(email: string, password: string): Observable<AuthUser> {
    return this.http.post<AuthUser>(
      `${this.apiUrl}/login`, { email, password }
    ).pipe(tap(res => this.saveUser(res)));
  }

  // ✅ Déconnexion
  logout() {
    localStorage.removeItem('smartpark_user');
    this.userSubject.next(null);
    this.router.navigate(['/login']);
  }

  // ✅ Modifier profil
  updateProfile(data: {
    nom: string; telephone: string;
  }): Observable<AuthUser> {
    return this.http.put<AuthUser>(
      `${this.apiUrl}/profile`, data
    ).pipe(tap(res => this.saveUser(res)));
  }

  // ✅ Changer mot de passe
  changePassword(data: {
    ancienPassword: string; nouveauPassword: string;
  }): Observable<any> {
    return this.http.put(
      `${this.apiUrl}/change-password`, data
    );
  }

  // ✅ Getters
  getUser(): AuthUser | null { return this.userSubject.value; }
  getToken(): string | null  { return this.getUser()?.token || null; }
  isLoggedIn(): boolean      { return this.getUser() !== null; }
  isAdmin(): boolean {
    const role = (this.getUser()?.role || '').toUpperCase();
    return role === 'ADMIN';
  }
  isUser(): boolean {
    const role = (this.getUser()?.role || '').toUpperCase();
    return role === 'USER';
  }

  // ✅ Méthodes de compatibilité pour les composants Marketplace
  isClient(): boolean {
    return this.isUser();
  }

  getRole(): string | null {
    const role = (this.getUser()?.role || '').toUpperCase();
    if (!role) return null;
    return role === 'ADMIN' ? 'admin' : 'client';
  }

  getCurrentUserId(): string {
    const user = this.getUser();
    return user?.id || user?.email || '1';
  }

  getClientInfo(): ClientInfo {
    const user = this.getUser();
    return {
      id: this.getCurrentUserId(),
      nom: user?.nom || 'Client',
      prenom: user?.prenom || ''
    };
  }

  private saveUser(user: AuthUser) {
    localStorage.setItem('smartpark_user', JSON.stringify(user));
    this.userSubject.next(user);
  }

  private getUserFromStorage(): AuthUser | null {
    const stored = localStorage.getItem('smartpark_user');
    return stored ? JSON.parse(stored) : null;
  }
}