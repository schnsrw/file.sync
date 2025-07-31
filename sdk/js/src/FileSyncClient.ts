import TokenManager from './TokenManager';
import WebSocketClient from './WebSocketClient';
import { Packet } from './Packet';
import { PacketHandler } from './PacketHandler';
import { RegisterRequest } from './types';

export default class FileSyncClient {
  private baseUrl: string;
  private tokenManager: TokenManager;
  private wsClient?: WebSocketClient;
  private features = new Set<string>();
  private handlers: Record<string, PacketHandler> = {};

  private constructor(baseUrl: string, username?: string, password?: string) {
    this.baseUrl = baseUrl;
    this.tokenManager = new TokenManager(baseUrl, username, password);
  }

  static builder() {
    return new Builder();
  }

  private async get(path: string): Promise<any> {
    const token = await this.tokenManager.getAccessToken();
    const resp = await fetch(`${this.baseUrl}${path}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    if (!resp.ok) throw new Error(`Request failed: ${resp.status}`);
    return resp.text();
  }

  private async post(path: string): Promise<any> {
    const token = await this.tokenManager.getAccessToken();
    const resp = await fetch(`${this.baseUrl}${path}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` }
    });
    if (!resp.ok) throw new Error(`Request failed: ${resp.status}`);
    return resp.text();
  }

  async register(req: RegisterRequest): Promise<void> {
    const resp = await fetch(`${this.baseUrl}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req)
    });
    if (!resp.ok) throw new Error(`Registration failed: ${resp.status}`);
  }

  async verifyOtp(userId: string, code: string): Promise<void> {
    const resp = await fetch(`${this.baseUrl}/auth/${userId}/verify?userId=${userId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ verificationCode: code })
    });
    if (!resp.ok) throw new Error(`Verification failed: ${resp.status}`);
    const data = await resp.json();
    this.tokenManager.setTokens(data.accessToken, data.refreshTooken);
  }

  async connectWebSocket(): Promise<void> {
    if (!this.wsClient) {
      this.wsClient = new WebSocketClient(this.baseUrl.replace(/^http/, 'ws') + '/ws');
      this.wsClient.setTokenSupplier(() => this.tokenManager.getAccessToken());
      this.wsClient.registerHandler('features', (_p, payload) => {
        if (Array.isArray(payload)) {
          for (const f of payload) {
            this.features.add(f);
            const h = this.handlers[f];
            if (h) this.wsClient!.registerHandler(f, h);
          }
        }
      });
    }
    await this.wsClient.connect();
  }

  registerHandler(type: string, handler: PacketHandler) {
    this.handlers[type] = handler;
    if (this.wsClient && this.features.has(type)) {
      this.wsClient.registerHandler(type, handler);
    }
  }

  sendChatMessage(to: string, text: string) {
    if (!this.wsClient) return;
    const packet: Packet = { type: 'chat', payload: { to, text } };
    this.wsClient.send(packet);
  }

  sendPing() {
    this.wsClient?.send({ type: 'ping' });
  }

  requestRecent(user: string, before?: number) {
    if (!this.wsClient) return;
    const payload: any = { user };
    if (before) payload.before = before;
    this.wsClient.send({ type: 'recent', payload });
  }

  requestFeatures() {
    this.wsClient?.send({ type: 'features' });
  }

  async requestConnection(username: string): Promise<any> {
    return this.post(`/connections/request/${username}`);
  }

  async acceptConnection(id: string): Promise<any> {
    return this.post(`/connections/${id}/accept`);
  }

  async rejectConnection(id: string): Promise<any> {
    return this.post(`/connections/${id}/reject`);
  }

  async listPendingConnections(): Promise<any> {
    return this.get('/connections/pending');
  }

  async listConnectedUsers(page: number, size: number): Promise<any> {
    return this.get(`/users/connected?page=${page}&size=${size}`);
  }

  async disconnect(username: string): Promise<any> {
    return this.post(`/users/${username}/disconnect`);
  }

  disconnectWebSocket() {
    this.wsClient?.close();
    this.wsClient = undefined;
  }
}

class Builder {
  private baseUrl = '';
  private username?: string;
  private password?: string;

  baseUrl(url: string): Builder {
    this.baseUrl = url;
    return this;
  }

  username(u: string): Builder {
    this.username = u;
    return this;
  }

  password(p: string): Builder {
    this.password = p;
    return this;
  }

  build(): FileSyncClient {
    return new FileSyncClient(this.baseUrl, this.username, this.password);
  }
}
