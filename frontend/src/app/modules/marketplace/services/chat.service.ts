import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ChatMessage } from '../models/marketplace.models';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly apiUrl = `${environment.apiBaseUrl}/chat`;

  constructor(private http: HttpClient) {}

  getMessages(): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.apiUrl}/messages`);
  }

  sendMessage(payload: ChatMessage): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(`${this.apiUrl}/messages`, payload);
  }

  updateMessage(messageId: string, payload: { content: string; requesterId: string; requesterEmail: string }): Observable<ChatMessage> {
    return this.http.put<ChatMessage>(`${this.apiUrl}/messages/${messageId}`, payload);
  }

  deleteMessage(messageId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/messages/${messageId}`);
  }
}
