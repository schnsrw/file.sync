import { AuthRequest, AuthResponse, RefreshTokenRequest, RefreshTokenResponse } from './types';

export default class TokenManager {
  private baseUrl: string;
  private username?: string;
  private password?: string;
  private accessToken?: string;
  private refreshToken?: string;
  private expiresAt = 0;

  constructor(baseUrl: string, username?: string, password?: string) {
    this.baseUrl = baseUrl;
    this.username = username;
    this.password = password;
  }

  async getAccessToken(): Promise<string> {
    if (!this.accessToken || Date.now() >= this.expiresAt) {
      if (this.refreshToken) {
        await this.refresh();
      } else {
        await this.login();
      }
    }
    return this.accessToken as string;
  }

  private async login(): Promise<void> {
    if (!this.username || !this.password) {
      throw new Error('Username/password not configured');
    }
    const req: AuthRequest = { username: this.username, password: this.password };
    const resp = await fetch(`${this.baseUrl}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req)
    });
    if (!resp.ok) throw new Error(`Auth failed: ${resp.status}`);
    const data: AuthResponse = await resp.json();
    this.setTokens(data.accessToken, (data as any).refreshToken || data.refreshTooken);
  }

  private async refresh(): Promise<void> {
    const req: RefreshTokenRequest = { refreshToken: this.refreshToken as string };
    const resp = await fetch(`${this.baseUrl}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(req)
    });
    if (!resp.ok) {
      await this.login();
      return;
    }
    const data: RefreshTokenResponse = await resp.json();
    this.setTokens(data.accessToken, data.refreshToken);
  }

  setTokens(access: string, refresh: string) {
    this.accessToken = access;
    this.refreshToken = refresh;
    this.expiresAt = Date.now() + 14 * 60 * 1000;
  }
}
