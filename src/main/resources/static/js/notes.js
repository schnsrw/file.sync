const padBase = 'http://localhost:9001/p/';
let currentNote = null;

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
  let resp = await fetch(path, opts);
  if (resp.status === 401) {
    if (await refreshTokens()) {
      token = localStorage.getItem('accessToken');
      opts.headers['Authorization'] = 'Bearer ' + token;
      resp = await fetch(path, opts);
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

function loadNotes() {
  api('/notes').then(notes => {
    const list = document.getElementById('notes');
    list.innerHTML = '';
    notes.forEach(n => {
      const li = document.createElement('li');
      li.textContent = n.title;
      li.onclick = () => openNote(n);
      list.appendChild(li);
    });
  });
}

function openNote(note) {
  currentNote = note;
  document.getElementById('padFrame').src = padBase + note.padId;
}

document.getElementById('newNote').addEventListener('click', () => {
  const title = prompt('Note title');
  if (!title) return;
  api('/notes', 'POST', { title }).then(note => {
    loadNotes();
    openNote(note);
  });
});

document.getElementById('saveNote').addEventListener('click', () => {
  if (!currentNote) return;
  api(`/notes/${currentNote.noteId}/save`, 'POST').then(() => alert('Saved'));
});

document.addEventListener('DOMContentLoaded', loadNotes);
