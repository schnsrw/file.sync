import { Packet } from './Packet';
import { PacketHandler } from './PacketHandler';

export default class WebSocketClient {
  private url: string;
  private socket?: WebSocket;
  private handlers: Record<string, PacketHandler> = {};
  private reconnectDelayMs = 5000;
  private tokenSupplier?: () => Promise<string> | string;
  private disconnectRequested = false;

  constructor(url: string) {
    this.url = url;
  }

  setTokenSupplier(fn: () => Promise<string> | string) {
    this.tokenSupplier = fn;
  }

  async connect(): Promise<void> {
    this.disconnectRequested = false;
    await this.connectInternal();
  }

  private async connectInternal(): Promise<void> {
    let url = this.url;
    if (this.tokenSupplier) {
      const token = await Promise.resolve(this.tokenSupplier());
      if (token) {
        const sep = url.includes('?') ? '&' : '?';
        url += `${sep}token=${encodeURIComponent(token)}`;
      }
    }
    this.socket = new WebSocket(url);
    this.socket.onopen = () => {
      this.send({ type: 'features' });
    };
    this.socket.onmessage = evt => {
      try {
        const packet: Packet = JSON.parse(evt.data);
        const handler = this.handlers[packet.type];
        if (handler) {
          handler(packet, packet.payload);
        }
      } catch (err) {
        console.error(err);
      }
    };
    this.socket.onclose = () => {
      this.socket = undefined;
      if (!this.disconnectRequested) {
        setTimeout(() => this.connectInternal(), this.reconnectDelayMs);
      }
    };
    this.socket.onerror = () => {
      if (!this.disconnectRequested) {
        this.socket?.close();
      }
    };
  }

  registerHandler(type: string, handler: PacketHandler) {
    this.handlers[type] = handler;
  }

  close() {
    this.disconnectRequested = true;
    this.socket?.close(1000, 'bye');
  }

  send(packet: Packet) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(packet));
    }
  }
}
