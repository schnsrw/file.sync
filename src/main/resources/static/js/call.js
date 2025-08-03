let callWs;
let pc;
let localStream;
let remoteUser;
let incomingOffer;

function connectCallWs() {
  const token = localStorage.getItem('accessToken');
  if (!token) return;
  const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
  callWs = new WebSocket(`${protocol}://${location.host}/ws?token=${encodeURIComponent(token)}`);
  callWs.onmessage = e => {
    const pkt = JSON.parse(e.data);
    if (pkt.type === 'call') handleSignal(pkt);
  };
}

function createUi() {
  if (document.getElementById('callOverlay')) return;
  const overlay = document.createElement('div');
  overlay.id = 'callOverlay';
  overlay.style.display = 'none';
  overlay.style.position = 'fixed';
  overlay.style.top = '10px';
  overlay.style.left = '10px';
  overlay.style.width = '240px';
  overlay.style.height = '180px';
  overlay.style.background = '#000';
  overlay.style.zIndex = '1000';
  overlay.style.borderRadius = '8px';
  overlay.style.boxShadow = '0 2px 8px rgba(0,0,0,0.3)';
  overlay.style.resize = 'both';
  overlay.style.overflow = 'hidden';
  overlay.style.cursor = 'move';

  const remoteVideo = document.createElement('video');
  remoteVideo.id = 'remoteVideo';
  remoteVideo.autoplay = true;
  remoteVideo.style.width = '100%';
  remoteVideo.style.height = '100%';
  overlay.appendChild(remoteVideo);

  const localVideo = document.createElement('video');
  localVideo.id = 'localVideo';
  localVideo.autoplay = true;
  localVideo.muted = true;
  localVideo.style.position = 'absolute';
  localVideo.style.width = '80px';
  localVideo.style.height = '60px';
  localVideo.style.bottom = '5px';
  localVideo.style.right = '5px';
  overlay.appendChild(localVideo);

  const endBtn = document.createElement('button');
  endBtn.innerHTML = '📴';
  endBtn.title = 'End Call';
  endBtn.style.position = 'absolute';
  endBtn.style.top = '5px';
  endBtn.style.right = '5px';
  endBtn.style.background = '#dc3545';
  endBtn.style.color = '#fff';
  endBtn.style.border = 'none';
  endBtn.style.borderRadius = '50%';
  endBtn.style.width = '32px';
  endBtn.style.height = '32px';
  endBtn.style.cursor = 'pointer';
  endBtn.addEventListener('click', endCall);
  overlay.appendChild(endBtn);
  const controls = document.createElement('div');
  controls.id = 'callControls';
  controls.style.position = 'absolute';
  controls.style.bottom = '5px';
  controls.style.left = '50%';
  controls.style.transform = 'translateX(-50%)';
  controls.style.display = 'flex';
  controls.style.gap = '8px';

  controls.innerHTML = `
    <button id="toggleMic" title="Toggle Mic">🎙️</button>
    <button id="toggleCam" title="Toggle Camera">🎥</button>
    <button id="fullscreenBtn" title="Fullscreen">⛶</button>
  `;

  [...controls.children].forEach(btn => {
    btn.style.background = 'rgba(0,0,0,0.6)';
    btn.style.color = '#fff';
    btn.style.border = 'none';
    btn.style.borderRadius = '50%';
    btn.style.width = '36px';
    btn.style.height = '36px';
    btn.style.cursor = 'pointer';
  });

  overlay.appendChild(controls);


  document.body.appendChild(overlay);

  let isDragging = false;
  let dragOffsetX = 0;
  let dragOffsetY = 0;
  overlay.addEventListener('mousedown', e => {
    if (e.target.tagName === 'BUTTON') return;
    isDragging = true;
    dragOffsetX = e.clientX - overlay.offsetLeft;
    dragOffsetY = e.clientY - overlay.offsetTop;
    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', stopDrag);
  });

  function onMouseMove(e) {
    if (!isDragging) return;
    overlay.style.left = `${e.clientX - dragOffsetX}px`;
    overlay.style.top = `${e.clientY - dragOffsetY}px`;
  }

  function stopDrag() {
    isDragging = false;
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', stopDrag);
  }

  const incoming = document.createElement('div');
  incoming.id = 'incomingCall';
  incoming.style.display = 'none';
  incoming.style.position = 'fixed';
  incoming.style.top = '20px';
  incoming.style.right = '20px';
  incoming.style.background = '#fff';
  incoming.style.padding = '10px';
  incoming.style.border = '1px solid #333';
  incoming.style.zIndex = '1000';
  incoming.innerHTML = `
    <div style="font-size: 1.2em; font-weight: bold; margin-bottom: 10px;">
      <span id="callerName"></span> is calling...
    </div>
    <div style="display: flex; justify-content: center; gap: 10px;">
      <button id="acceptCall" title="Accept" style="background:#28a745;color:white;border:none;border-radius:50%;width:40px;height:40px;cursor:pointer;">📞</button>
      <button id="declineCall" title="Decline" style="background:#dc3545;color:white;border:none;border-radius:50%;width:40px;height:40px;cursor:pointer;">✖</button>
    </div>`;

  document.body.appendChild(incoming);
  document.getElementById('acceptCall').addEventListener('click', acceptCall);
  document.getElementById('declineCall').addEventListener('click', declineCall);
}

function createPeer() {
  pc = new RTCPeerConnection();
  pc.ontrack = e => {
    const video = document.getElementById('remoteVideo');
    video.srcObject = e.streams[0];
  };
  pc.onicecandidate = e => {
    if (e.candidate && remoteUser) {
      callWs.send(JSON.stringify({type:'call', payload:{to:remoteUser, action:'candidate', candidate:e.candidate}}));
    }
  };
}

async function startCall(user) {
  remoteUser = user;
  createPeer();
  localStream = await navigator.mediaDevices.getUserMedia({video:true, audio:true});
  document.getElementById('localVideo').srcObject = localStream;
  localStream.getTracks().forEach(t => pc.addTrack(t, localStream));
  const offer = await pc.createOffer();
  await pc.setLocalDescription(offer);
  callWs.send(JSON.stringify({type:'call', payload:{to:user, action:'offer', sdp:offer}}));
  document.getElementById('callOverlay').style.display = 'block';
  showToast(`Calling ${user}...`);
}
window.startCall = startCall;

async function acceptCall() {
  document.getElementById('incomingCall').style.display = 'none';
  if (!remoteUser || !incomingOffer) return;
  createPeer();
  localStream = await navigator.mediaDevices.getUserMedia({video:true, audio:true});
  document.getElementById('localVideo').srcObject = localStream;
  localStream.getTracks().forEach(t => pc.addTrack(t, localStream));
  await pc.setRemoteDescription(new RTCSessionDescription(incomingOffer));
  const answer = await pc.createAnswer();
  await pc.setLocalDescription(answer);
  callWs.send(JSON.stringify({type:'call', payload:{to:remoteUser, action:'answer', sdp:answer}}));
  document.getElementById('callOverlay').style.display = 'block';
  incomingOffer = null;
}

function declineCall() {
  document.getElementById('incomingCall').style.display = 'none';
  if (remoteUser) {
    callWs.send(JSON.stringify({type:'call', payload:{to:remoteUser, action:'decline'}}));
    remoteUser = null;
  }
  incomingOffer = null;
}

function handleSignal(pkt) {
  const {action, sdp, candidate} = pkt.payload;
  remoteUser = pkt.from;
  switch(action) {
    case 'offer':
      incomingOffer = sdp;
      document.getElementById('callerName').textContent = pkt.from;
      document.getElementById('incomingCall').style.display = 'block';
      break;
    case 'answer':
      if (pc) pc.setRemoteDescription(new RTCSessionDescription(sdp));
      break;
    case 'candidate':
      if (pc) pc.addIceCandidate(new RTCIceCandidate(candidate));
      break;
    case 'end':
    case 'decline':
      endCall();
      break;
  }
}

function endCall() {
  if (pc) { pc.close(); pc = null; }
  if (localStream) { localStream.getTracks().forEach(t => t.stop()); localStream = null; }
  document.getElementById('callOverlay').style.display = 'none';
  if (remoteUser) {
    callWs.send(JSON.stringify({type:'call', payload:{to:remoteUser, action:'end'}}));
  }
  remoteUser = null;
}

document.addEventListener('DOMContentLoaded', () => {
  createUi();
  document.getElementById('toggleMic').onclick = () => {
    const audioTrack = localStream?.getAudioTracks()[0];
    if (audioTrack) audioTrack.enabled = !audioTrack.enabled;
  };

  document.getElementById('toggleCam').onclick = () => {
    const videoTrack = localStream?.getVideoTracks()[0];
    if (videoTrack) videoTrack.enabled = !videoTrack.enabled;
  };

  document.getElementById('fullscreenBtn').onclick = () => {
    const overlay = document.getElementById('callOverlay');
    if (!document.fullscreenElement) {
      overlay.requestFullscreen().catch(console.error);
    } else {
      document.exitFullscreen();
    }
  };

  connectCallWs();
});

function showToast(msg) {
  const toast = document.createElement('div');
  toast.textContent = msg;
  toast.style.position = 'fixed';
  toast.style.bottom = '20px';
  toast.style.left = '50%';
  toast.style.transform = 'translateX(-50%)';
  toast.style.background = '#333';
  toast.style.color = '#fff';
  toast.style.padding = '10px 20px';
  toast.style.borderRadius = '8px';
  toast.style.zIndex = 9999;
  document.body.appendChild(toast);
  setTimeout(() => toast.remove(), 3000);
}
