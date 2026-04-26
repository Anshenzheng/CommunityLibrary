import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface ToastMessage {
  id: number;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private messagesSubject = new BehaviorSubject<ToastMessage[]>([]);
  public messages$: Observable<ToastMessage[]> = this.messagesSubject.asObservable();
  
  private nextId = 0;

  constructor() { }

  showSuccess(message: string): void {
    this.addMessage('success', message);
  }

  showError(message: string): void {
    this.addMessage('error', message);
  }

  showWarning(message: string): void {
    this.addMessage('warning', message);
  }

  showInfo(message: string): void {
    this.addMessage('info', message);
  }

  private addMessage(type: ToastMessage['type'], message: string): void {
    const id = this.nextId++;
    const messages = [...this.messagesSubject.value, { id, type, message }];
    this.messagesSubject.next(messages);
    
    setTimeout(() => {
      this.removeMessage(id);
    }, 5000);
  }

  removeMessage(id: number): void {
    const messages = this.messagesSubject.value.filter(m => m.id !== id);
    this.messagesSubject.next(messages);
  }
}
