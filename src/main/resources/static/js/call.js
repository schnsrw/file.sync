let ws = null;
let pc = null;
let remoteUser = null;
let localStream = null;

function closeCall() {
  if (pc) {
    pc.close();
    pc = null;
  }
  if (localStream) {
    localStream.getTracks().forEach(t => t.stop());
    localStream = null;
  }
  if (ws) {
    ws.close();
    ws = null;
  }
  window.location.href = '/chat';
}

async function init() {
  const params = new URLSearchParams(window.location.search);
  remoteUser = params.get('user');
  const initiate = params.get('init') === '1';
  if (!remoteUser) {
    alert('No user specified');
    return;
  }

  const token = localStorage.getItem('accessToken');
  const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
  ws = new WebSocket(`${protocol}://${location.host}/ws?token=${encodeURIComponent(token)}`);

  pc = new RTCPeerConnection();

  localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
  document.getElementById('localVideo').srcObject = localStream;
  localStream.getTracks().forEach(track => pc.addTrack(track, localStream));

  pc.ontrack = e => {
    document.getElementById('remoteVideo').srcObject = e.streams[0];
  };

  pc.onicecandidate = e => {
    if (e.candidate && ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({
        type: 'call',
        payload: { to: remoteUser, type: 'candidate', candidate: e.candidate }
      }));
    }
  };

  ws.onmessage = async e => {
    const pkt = JSON.parse(e.data);
    if (pkt.type !== 'call' || pkt.from !== remoteUser) return;
    const data = pkt.payload;
    if (data.type === 'offer') {
      await pc.setRemoteDescription(new RTCSessionDescription(data.offer));
      const answer = await pc.createAnswer();
      await pc.setLocalDescription(answer);
      ws.send(JSON.stringify({
        type: 'call',
        payload: { to: remoteUser, type: 'answer', answer }
      }));
    } else if (data.type === 'answer') {
      await pc.setRemoteDescription(new RTCSessionDescription(data.answer));
    } else if (data.type === 'candidate') {
      if (data.candidate) {
        try {
          await pc.addIceCandidate(new RTCIceCandidate(data.candidate));
        } catch (err) {
          console.error(err);
        }
      }
    } else if (data.type === 'hangup') {
      closeCall();
    }
  };

  if (initiate) {
    const offer = await pc.createOffer();
    await pc.setLocalDescription(offer);
    ws.send(JSON.stringify({
      type: 'call',
      payload: { to: remoteUser, type: 'offer', offer }
    }));
  }

  document.getElementById('hangupBtn').addEventListener('click', () => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 'call', payload: { to: remoteUser, type: 'hangup' } }));
    }
    closeCall();
  });

  window.addEventListener('beforeunload', () => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 'call', payload: { to: remoteUser, type: 'hangup' } }));
    }
  });
}

document.addEventListener('DOMContentLoaded', init);

