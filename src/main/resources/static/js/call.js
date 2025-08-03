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
  overlay.style.bottom = '10px';
  overlay.style.right = '10px';
  overlay.style.width = '240px';
  overlay.style.height = '180px';
  overlay.style.background = '#000';
  overlay.style.zIndex = '1000';

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
  endBtn.textContent = '✖';
  endBtn.style.position = 'absolute';
  endBtn.style.top = '5px';
  endBtn.style.right = '5px';
  endBtn.addEventListener('click', endCall);
  overlay.appendChild(endBtn);

  document.body.appendChild(overlay);

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
  incoming.innerHTML = '<span id="callerName"></span> is calling... ' +
    '<button id="acceptCall">Accept</button> <button id="declineCall">Decline</button>';
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
  connectCallWs();
});
