import { Packet } from './Packet';

export type PacketHandler = (packet: Packet, payload: any) => void;
