const baseUrl = '';
let ws = null;
let currentChat = null;

async function api(path, method = 'GET', body) {
  const token = localStorage.getItem('accessToken');
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (token) opts.headers['Authorization'] = 'Bearer ' + token;
  if (body) opts.body = JSON.stringify(body);
  const resp = await fetch(baseUrl + path, opts);
  if (!resp.ok) throw new Error('Request failed');
  return resp.json();
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
    }
  };
}

async function loadUsers() {
  try {
    const users = await api('/users/connected?page=0&size=50');
    const list = document.getElementById('users');
    list.innerHTML = '';
    users.forEach(u => {
      const btn = document.createElement('button');
      btn.textContent = u.username;
      btn.onclick = () => { currentChat = u.username; document.getElementById('messages').innerHTML = ''; };
      list.appendChild(btn);
      list.appendChild(document.createElement('br'));
    });
  } catch (e) {
    console.error(e);
  }
}

function showMessage(from, text, id) {
  const msgDiv = document.createElement('div');
  msgDiv.className = 'msg' + (from === 'me' ? ' me' : '');
  msgDiv.id = id ? 'msg-' + id : '';
  msgDiv.innerHTML = `<span>${text}</span> <span class="status"></span>`;
  document.getElementById('messages').appendChild(msgDiv);
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
    loginForm.addEventListener('submit', async e => {
      e.preventDefault();
      const username = document.getElementById('username').value;
      const password = document.getElementById('password').value;
      try {
        const resp = await fetch('/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username, password }) });
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
    document.getElementById('sendBtn').addEventListener('click', sendMessage);
    loadUsers();
    connectWs();
  }
});
