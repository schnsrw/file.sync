export interface Packet {
  type: string;
  to?: string;
  from?: string;
  payload?: any;
}
