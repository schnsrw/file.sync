const baseUrl = '';
let ws = null;
let currentChat = null;
let myUsername = null;

async function refreshTokens() {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) return false;
  const resp = await fetch('/auth/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken })
  });
  if (!resp.ok) return false;
  const data = await resp.json();
  localStorage.setItem('accessToken', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshTooken || data.refreshToken);
  return true;
}

async function api(path, method = 'GET', body) {
  let token = localStorage.getItem('accessToken');
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (token) opts.headers['Authorization'] = 'Bearer ' + token;
  if (body) opts.body = JSON.stringify(body);
  let resp = await fetch(baseUrl + path, opts);
  if (resp.status === 401 && await refreshTokens()) {
    token = localStorage.getItem('accessToken');
    opts.headers['Authorization'] = 'Bearer ' + token;
    resp = await fetch(baseUrl + path, opts);
  }
  if (!resp.ok) throw new Error('Request failed');
  return resp.status === 204 ? null : resp.json();
}

function connectWs() {
  const token = localStorage.getItem('accessToken');
  if (!token) return;
  const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
  ws = new WebSocket(`${protocol}://${location.host}/ws?token=${encodeURIComponent(token)}`);
  ws.onmessage = e => {
    const pkt = JSON.parse(e.data);
    if (pkt.type === 'chat') {
      showMessage(pkt.from, pkt.payload.text);
    } else if (pkt.type === 'receipt') {
      const el = document.getElementById('msg-' + pkt.payload.id);
      if (el) el.querySelector('.status').textContent = pkt.payload.status;
    } else if (pkt.type === 'last-seen') {
      if (pkt.from === currentChat) {
        const header = document.getElementById('chatWith');
        if (pkt.payload.status === 'ONLINE') {
          header.textContent = `${currentChat} (online)`;
        } else if (pkt.payload['last-seen'] && pkt.payload['last-seen'] > 0) {
          const d = new Date(pkt.payload['last-seen']);
          header.textContent = `${currentChat} (last seen ${d.toLocaleString()})`;
        } else {
          header.textContent = `${currentChat} (offline)`;
        }
      }
    }
  };
}

async function loadUsers() {
  try {
    const users = await api('/users/connected?page=0&size=50');

    const enriched = await Promise.all(users.map(async u => {
      let lastMsg = '';
      let lastTime = 0;
      try {
        const msgs = await api(`/chat/${u.username}?timestamp=${Date.now()}&size=1`);
        if (msgs && msgs.length > 0) {
          lastMsg = msgs[0].content;
          lastTime = new Date(msgs[0].timestamp).getTime();
        }
      } catch (e) {}
      return { ...u, lastMsg, lastTime };
    }));

    enriched.sort((a, b) => b.lastTime - a.lastTime);

    const list = document.getElementById('users');
    list.innerHTML = '';

    enriched.forEach(u => {
      const isOnline = u.active || u.online;
      const statusText = isOnline ? 'online' : (u.lastSeen ? `last seen ${new Date(u.lastSeen).toLocaleString()}` : 'offline');
      const btn = document.createElement('div');
      btn.className = 'user';
      btn.innerHTML = `
        <div class="user-pic">${u.username.charAt(0).toUpperCase()}</div>
        <div class="user-info">
          <div class="name">${u.username}</div>
          <div class="status-text">${statusText}</div>
          <div class="last-msg">${u.lastMsg || ''}</div>
        </div>
        <div class="status-dot ${isOnline ? 'online' : 'offline'}"></div>
      `;

      btn.onclick = async () => {
        currentChat = u.username;
        document.getElementById('messages').innerHTML = '';
        document.getElementById('chatWith').textContent = `Chatting with ${u.username}`;

        // Set active class
        document.querySelectorAll('#users .user').forEach(el => el.classList.remove('active'));
        btn.classList.add('active');

        await loadHistory(u.username);
        requestLastSeen(u.username);
      };
      list.appendChild(btn);
    });
  } catch (e) {
    console.error(e);
  }
}

function showMessage(from, text, id) {
  const wrapper = document.createElement('div');
  const isSelf = from === 'me';

  wrapper.className = 'msg-wrapper' + (isSelf ? ' me' : '');

  const avatarLetter = isSelf ? myUsername.charAt(0).toUpperCase() : from.charAt(0).toUpperCase();
  const profileDiv = `<div class="msg-profile">${avatarLetter}</div>`;

  const msgDiv = document.createElement('div');
  msgDiv.className = 'msg';
  msgDiv.id = id ? 'msg-' + id : '';
  msgDiv.innerHTML = `<span>${text}</span><div class="status"></div>`;

  // Arrange elements: avatar on right for self, left for others
  wrapper.innerHTML = isSelf
    ? msgDiv.outerHTML + profileDiv  // Right-side icon for self
    : profileDiv + msgDiv.outerHTML; // Left-side icon for others

  document.getElementById('messages').appendChild(wrapper);
  document.getElementById('messages').scrollTop = document.getElementById('messages').scrollHeight;
}



async function loadHistory(user) {
  try {
    const msgs = await api(`/chat/${user}?timestamp=${Date.now()}&size=50`);
    if (!msgs) return;
    msgs.reverse().forEach(m => {
      const from = m.from === myUsername ? 'me' : m.from;
      showMessage(from, m.content, m.id);
    });
  } catch (e) {
    console.error(e);
  }
}

function requestLastSeen(user) {
  if (ws) {
    ws.send(JSON.stringify({ type: 'last-seen', payload: { to: user } }));
  }
}

async function sendMessage() {
  const text = document.getElementById('messageText').value.trim();
  if (!text || !currentChat || !ws) return;
  document.getElementById('messageText').value = '';
  const payload = { type: 'chat', payload: { to: currentChat, text } };
  ws.send(JSON.stringify(payload));
  const id = Date.now().toString();
  showMessage('me', text, id);
}

document.addEventListener('DOMContentLoaded', () => {
  const loginForm = document.getElementById('loginForm');
  if (loginForm) {
    const token = localStorage.getItem('accessToken');
    if (token) {
      api('/users/me').then(() => {
        window.location.href = '/chat';
      }).catch(() => {});
    }
    loginForm.addEventListener('submit', async e => {
      e.preventDefault();
      const username = document.getElementById('username').value;
      const password = document.getElementById('password').value;
      try {
        const resp = await fetch('/auth/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username, password })
        });
        if (!resp.ok) throw new Error('login failed');
        const data = await resp.json();
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshTooken || data.refreshToken);
        window.location.href = '/chat';
      } catch (err) {
        document.getElementById('error').textContent = 'Login failed';
      }
    });
  }

  if (document.getElementById('sendBtn')) {
    api('/users/me').then(u => { myUsername = u.username; }).catch(() => {});
    document.getElementById('sendBtn').addEventListener('click', sendMessage);
    loadUsers();
    connectWs();
  }
});
