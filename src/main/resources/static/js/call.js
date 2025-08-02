let ws = null;
let pc = null;
let remoteUser = null;
let localStream = null;
let remoteStream = null;
let pendingCandidates = [];

function closeCall() {
  if (pc) {
    pc.close();
    pc = null;
  }
  if (localStream) {
    localStream.getTracks().forEach(t => t.stop());
    localStream = null;
  }
  if (remoteStream) {
    remoteStream.getTracks().forEach(t => t.stop());
    remoteStream = null;
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

  pc = new RTCPeerConnection({
    iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
  });

  localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
  document.getElementById('localVideo').srcObject = localStream;
  localStream.getTracks().forEach(track => pc.addTrack(track, localStream));

  const remoteVideoEl = document.getElementById('remoteVideo');

  pc.ontrack = e => {
    if (!remoteStream) {
      remoteStream = new MediaStream();
      remoteVideoEl.srcObject = remoteStream;
    }
    remoteStream.addTrack(e.track);
    remoteVideoEl.play().catch(() => {});
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
      for (const c of pendingCandidates) {
        try { await pc.addIceCandidate(c); } catch (err) { console.error(err); }
      }
      pendingCandidates = [];
    } else if (data.type === 'answer') {
      await pc.setRemoteDescription(new RTCSessionDescription(data.answer));
      for (const c of pendingCandidates) {
        try { await pc.addIceCandidate(c); } catch (err) { console.error(err); }
      }
      pendingCandidates = [];
    } else if (data.type === 'candidate') {
      if (data.candidate) {
        const candidate = new RTCIceCandidate(data.candidate);
        if (pc.remoteDescription) {
          try {
            await pc.addIceCandidate(candidate);
          } catch (err) {
            console.error(err);
          }
        } else {
          pendingCandidates.push(candidate);
        }
      }
    } else if (data.type === 'hangup') {
      closeCall();
    }
  };

  const sendOffer = async () => {
    const offer = await pc.createOffer();
    await pc.setLocalDescription(offer);
    ws.send(JSON.stringify({
      type: 'call',
      payload: { to: remoteUser, type: 'offer', offer }
    }));
  };

  if (initiate) {
    if (ws.readyState === WebSocket.OPEN) {
      await sendOffer();
    } else {
      ws.addEventListener('open', sendOffer);
    }
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

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', init);
} else {
  init();
}

const sendOffer = async () => {
  const offer = await pc.createOffer();
  await pc.setLocalDescription(offer);
  console.log('[CALL] Sending offer to', remoteUser); // add this log
  ws.send(JSON.stringify({
    type: 'call',
    payload: { to: remoteUser, type: 'offer', offer }
  }));
};

