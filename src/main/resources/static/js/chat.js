const baseUrl = '';
let ws = null;
let currentChat = null;
let myUsername = null;
let oldestTimestamp = null;
let loadingHistory = false;

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
  if (resp.status === 401) {
    if (await refreshTokens()) {
      token = localStorage.getItem('accessToken');
      opts.headers['Authorization'] = 'Bearer ' + token;
      resp = await fetch(baseUrl + path, opts);
    } else {
      logout();
      throw new Error('Unauthorized');
    }
  }
  if (!resp.ok) {
    if (resp.status === 401) logout();
    throw new Error('Request failed');
  }
  return resp.status === 204 ? null : resp.json();
}

function connectWs() {
  const token = localStorage.getItem('accessToken');
  if (!token) return;
  const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
  ws = new WebSocket(`${protocol}://${location.host}/ws?token=${encodeURIComponent(token)}`);
  ws.onopen = () => {
      document.querySelectorAll('#users .user').forEach(el => {
        const uname = el.dataset.username;
        if (uname) requestLastSeen(uname);
      });
    };
  ws.onmessage = e => {
    const pkt = JSON.parse(e.data);
    if (pkt.type === 'chat') {
      if (pkt.from === currentChat) {
        showMessage(pkt.from, pkt.payload.text, pkt.payload.id, Date.now());
      } else {
        const userItem = document.querySelector(`#users .user[data-username="${pkt.from}"]`);
        if (userItem) {
          userItem.classList.add('unread');
          const lastMsgEl = userItem.querySelector('.last-msg');
          if (lastMsgEl) lastMsgEl.textContent = pkt.payload.text;
        }
      }
    } else if (pkt.type === 'receipt') {
      const el = document.getElementById('msg-' + pkt.payload.id);
      if (el) el.querySelector('.status').textContent = pkt.payload.status;
    } else if (pkt.type === 'last-seen') {
      const userItem = document.querySelector(`#users .user[data-username="${pkt.from}"]`);
      if (userItem) {
        const dot = userItem.querySelector('.status-dot');
        if (pkt.payload.status === 'ONLINE') {
          dot.classList.add('online');
          dot.classList.remove('offline');
        } else {
          dot.classList.add('offline');
          dot.classList.remove('online');
        }
      }
      if (pkt.from === currentChat) {
        const statusEl = document.getElementById('userStatus');
        if (pkt.payload.status === 'ONLINE') {
          statusEl.textContent = 'online';
        } else if (pkt.payload['last-seen'] && pkt.payload['last-seen'] > 0) {
          const d = new Date(pkt.payload['last-seen']);
          statusEl.textContent = `last seen ${d.toLocaleString()}`;
        } else {
          statusEl.textContent = 'offline';
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
      const statusText = u.lastSeen ? `last seen ${new Date(u.lastSeen).toLocaleString()}` : 'offline';
            const btn = document.createElement('div');
            btn.className = 'user';
            btn.dataset.username = u.username;
            btn.innerHTML = `
              <div class="user-pic">${u.username.charAt(0).toUpperCase()}</div>
        <div class="user-info">
          <div class="name">${u.username}</div>
            <div class="last-msg">${u.lastMsg || ''}</div>
        </div>
        <div class="status-dot offline"></div>

      `;

      btn.onclick = async () => {
        currentChat = u.username;
        document.getElementById('messages').innerHTML = '';
        document.getElementById('chatWith').textContent = u.username;
        document.getElementById('userStatus').textContent = '';

        btn.classList.remove('unread');

        // Set active class
        document.querySelectorAll('#users .user').forEach(el => el.classList.remove('active'));
        btn.classList.add('active');

        await loadHistory(u.username);
        requestLastSeen(u.username);
      };
      list.appendChild(btn);
      requestLastSeen(u.username);
    });
  } catch (e) {
    console.error(e);
  }
}

function showMessage(from, text, id, timestamp = Date.now(), prepend = false) {
  const wrapper = document.createElement('div');
  const isSelf = from === 'me';

  wrapper.className = 'msg-wrapper' + (isSelf ? ' me' : '');

  const avatarLetter = isSelf ? myUsername?.charAt(0).toUpperCase() : from.charAt(0).toUpperCase();
  const profileDiv = document.createElement('div');
  profileDiv.className = 'msg-profile';
  profileDiv.textContent = avatarLetter;

  const msgDiv = document.createElement('div');
  msgDiv.className = 'msg';
  msgDiv.id = id ? 'msg-' + id : '';
  const timeStr = new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  msgDiv.innerHTML = `<span>${text}</span><div class="meta"><span class="timestamp">${timeStr}</span><span class="status"></span></div>`;

  wrapper.appendChild(profileDiv);
  wrapper.appendChild(msgDiv);

  const messagesEl = document.getElementById('messages');
  if (prepend) {
    messagesEl.insertBefore(wrapper, messagesEl.firstChild);
  } else {
    messagesEl.appendChild(wrapper);
    messagesEl.scrollTop = messagesEl.scrollHeight;
  }
}

async function loadHistory(user) {
  try {
    oldestTimestamp = Date.now();
    const msgs = await api(`/chat/${user}?timestamp=${oldestTimestamp}&size=50`);
    if (!msgs) return;
    msgs.reverse().forEach(m => {
      const from = m.from === myUsername ? 'me' : m.from;
      showMessage(from, m.content, m.id, m.timestamp);
    });
    if (msgs.length > 0) {
      oldestTimestamp = msgs[0].timestamp;
    }
  } catch (e) {
    console.error(e);
  }
}

async function loadMoreHistory() {
  if (loadingHistory || !currentChat || !oldestTimestamp) return;
  loadingHistory = true;
  try {
    const msgs = await api(`/chat/${currentChat}?timestamp=${oldestTimestamp}&size=50`);
    if (msgs && msgs.length > 0) {
      msgs.reverse().forEach(m => {
        const from = m.from === myUsername ? 'me' : m.from;
        showMessage(from, m.content, m.id, m.timestamp, true);
      });
      oldestTimestamp = msgs[0].timestamp;
    }
  } catch (e) {
    console.error(e);
  } finally {
    loadingHistory = false;
  }
}

function requestLastSeen(user) {
  if (ws && ws.readyState === WebSocket.OPEN) {
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
  showMessage('me', text, id, Date.now());
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
        window.location.href = '/dashboard';
      } catch (err) {
        document.getElementById('error').textContent = 'Login failed';
      }
    });
  }

  if (document.getElementById('sendBtn')) {
    api('/users/me').then(u => { myUsername = u.username; }).catch(() => {});
    document.getElementById('sendBtn').addEventListener('click', sendMessage);
    document.getElementById('messageText').addEventListener('keydown', e => {
          if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault(); // Prevent newline
            sendMessage();
          }
        });
    connectWs();
    loadUsers();
    const msgBox = document.getElementById('messages');
    msgBox.addEventListener('scroll', async () => {
      if (msgBox.scrollTop === 0) {
        const prevHeight = msgBox.scrollHeight;
        await loadMoreHistory();
        msgBox.scrollTop = msgBox.scrollHeight - prevHeight;
      }
    });
  }
});
