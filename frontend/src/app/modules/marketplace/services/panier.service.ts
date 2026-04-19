import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Produit } from '../models/marketplace.models';

export interface PanierItem {
  produit: Produit;
  quantite: number;
}

@Injectable({ providedIn: 'root' })
export class PanierService {
  private readonly storageKey = 'smartpark_panier_items';
  private readonly itemsSubject = new BehaviorSubject<PanierItem[]>(this.readFromStorage());
  items$ = this.itemsSubject.asObservable();

  get items(): PanierItem[] {
    return this.itemsSubject.value;
  }

  get count(): number {
    return this.items.reduce((sum, item) => sum + item.quantite, 0);
  }

  get total(): number {
    return this.items.reduce((sum, item) => sum + (item.produit.prix * item.quantite), 0);
  }

  add(produit: Produit, quantite = 1): void {
    const items = [...this.items];
    const index = items.findIndex(i => i.produit.id === produit.id);

    if (index >= 0) {
      items[index] = { ...items[index], quantite: items[index].quantite + quantite };
    } else {
      items.push({ produit, quantite });
    }

    this.update(items);
  }

  remove(produitId?: string): void {
    if (!produitId) {
      return;
    }
    this.update(this.items.filter(i => i.produit.id !== produitId));
  }

  setQuantite(produitId: string | undefined, quantite: number): void {
    if (!produitId) {
      return;
    }
    const next = this.items
      .map(item => item.produit.id === produitId ? { ...item, quantite: Math.max(1, quantite) } : item);
    this.update(next);
  }

  clear(): void {
    this.update([]);
  }

  private update(items: PanierItem[]): void {
    this.itemsSubject.next(items);
    localStorage.setItem(this.storageKey, JSON.stringify(items));
  }

  private readFromStorage(): PanierItem[] {
    try {
      const raw = localStorage.getItem(this.storageKey);
      return raw ? JSON.parse(raw) as PanierItem[] : [];
    } catch {
      return [];
    }
  }
}
