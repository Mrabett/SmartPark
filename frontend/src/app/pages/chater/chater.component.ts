import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ChatMessage } from '../../modules/marketplace/models/marketplace.models';
import { ChatService } from '../../modules/marketplace/services/chat.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-chater',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chater.component.html',
  styleUrl: './chater.component.scss'
})
export class ChaterComponent implements OnInit, OnDestroy {
  messages: ChatMessage[] = [];
  draft = '';
  editDraft = '';
  editingMessageId: string | null = null;
  loading = false;
  sending = false;
  updating = false;
  errorMessage = '';
  private pollTimer: ReturnType<typeof setInterval> | null = null;

  constructor(
    private chatService: ChatService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadMessages(true);
    this.pollTimer = setInterval(() => {
      this.loadMessages(false);
    }, 4000);
  }

  ngOnDestroy(): void {
    if (this.pollTimer) {
      clearInterval(this.pollTimer);
      this.pollTimer = null;
    }
  }

  get currentUserId(): string {
    return this.authService.getCurrentUserId();
  }

  get currentUserName(): string {
    const user = this.authService.getUser();
    return `${user?.prenom || ''} ${user?.nom || ''}`.trim() || user?.email || 'Client';
  }

  get currentUserEmail(): string {
    return this.authService.getUser()?.email || '';
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  loadMessages(withLoader: boolean): void {
    if (withLoader) {
      this.loading = true;
    }

    this.chatService.getMessages().subscribe({
      next: (messages) => {
        this.messages = messages;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Impossible de charger les messages.';
      }
    });
  }

  send(): void {
    if (this.sending) {
      return;
    }

    const content = this.draft.trim();
    if (!content) {
      return;
    }

    this.sending = true;
    this.errorMessage = '';

    const payload: ChatMessage = {
      senderId: this.currentUserId,
      senderName: this.currentUserName,
      content
    };

    this.chatService.sendMessage(payload).subscribe({
      next: (created) => {
        this.messages = [...this.messages, created];
        this.draft = '';
        this.sending = false;
      },
      error: (err) => {
        this.sending = false;
        this.errorMessage = err?.error?.message || 'Impossible d\'envoyer le message.';
      }
    });
  }

  removeMessage(messageId: string | undefined): void {
    if (!this.isAdmin || !messageId) {
      return;
    }

    this.chatService.deleteMessage(messageId).subscribe({
      next: () => {
        this.messages = this.messages.filter(m => m.id !== messageId);
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Suppression impossible.';
      }
    });
  }

  isMine(msg: ChatMessage): boolean {
    return msg.senderId === this.currentUserId;
  }

  canEdit(msg: ChatMessage): boolean {
    return this.isAdmin || this.isMine(msg);
  }

  startEdit(msg: ChatMessage): void {
    if (!msg.id || !this.canEdit(msg)) {
      return;
    }
    this.editingMessageId = msg.id;
    this.editDraft = msg.content;
    this.errorMessage = '';
  }

  cancelEdit(): void {
    this.editingMessageId = null;
    this.editDraft = '';
    this.updating = false;
  }

  saveEdit(msg: ChatMessage): void {
    if (!msg.id || !this.canEdit(msg) || this.updating) {
      return;
    }

    const content = this.editDraft.trim();
    if (!content) {
      this.errorMessage = 'Le message ne peut pas être vide.';
      return;
    }

    this.updating = true;
    this.errorMessage = '';

    this.chatService.updateMessage(msg.id, {
      content,
      requesterId: this.currentUserId,
      requesterEmail: this.currentUserEmail
    }).subscribe({
      next: (updated) => {
        this.messages = this.messages.map(m => m.id === updated.id ? updated : m);
        this.cancelEdit();
      },
      error: (err) => {
        this.updating = false;
        if (err?.status === 403) {
          this.errorMessage = 'Vous ne pouvez modifier que vos propres messages.';
          return;
        }
        if (err?.status === 404) {
          this.errorMessage = 'Le message n\'existe plus.';
          return;
        }
        this.errorMessage = err?.error?.message || 'Impossible de modifier le message.';
      }
    });
  }
}
