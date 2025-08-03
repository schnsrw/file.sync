const padBase = 'http://localhost:9001';
let currentNoteId = null;
const iframe = document.getElementById('padFrame');

document.getElementById('notes').addEventListener('click', async (e) => {
    const li = e.target.closest('li[data-note-id]');
    if (!li) return;

    const noteId = li.dataset.noteId;
    if (noteId === currentNoteId) return;

    await saveCurrentNote();
    loadNote(noteId);

    // Highlight selected note
    document.querySelectorAll('#noteList li').forEach(el => el.classList.remove('selected'));
    li.classList.add('selected');
});


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
  api('/note').then(notes => {
    const list = document.getElementById('notes');
    list.innerHTML = ''; // clear old items
    notes.forEach(note => {
      const li = document.createElement('li');
      li.textContent = note.title;
      li.dataset.noteId = note.noteId;
      list.appendChild(li);
    });
  });
}

function loadNote(noteId) {
  currentNoteId = noteId;
  iframe.src = padBase+`/p/note-${noteId}`;
}

async function saveCurrentNote() {
  if (!currentNoteId) return;
  try {
    await api(`/note/${currentNoteId}/save`, 'POST');
    console.log('Note saved:', currentNoteId);
  } catch (e) {
    console.error('Auto-save failed:', e);
  }
}

// Event listeners
document.addEventListener('DOMContentLoaded', () => {
  loadNotes();

  document.getElementById('newNote').addEventListener('click', async () => {
    const title = await showInputModal('Note title');
    if (!title) return;
    api('/note', 'POST', { title }).then(note => {
      loadNotes();
      loadNote(note.noteId);
    });
  });

  document.getElementById('notes').addEventListener('click', async (e) => {
    const li = e.target.closest('li[data-note-id]');
    if (!li) return;

    const noteId = li.dataset.noteId;
    if (noteId === currentNoteId) return;

    await saveCurrentNote();
    loadNote(noteId);
  });

  document.addEventListener('keydown', async (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
      e.preventDefault();
      await saveCurrentNote();
    }
  });

  window.addEventListener('beforeunload', async () => {
    await saveCurrentNote();
  });
});

function showInputModal(message = 'Enter value', defaultValue = '') {
  return new Promise((resolve) => {
    const modal = document.getElementById('inputModal');
    const title = document.getElementById('inputModalTitle');
    const field = document.getElementById('inputModalField');
    const okBtn = document.getElementById('inputModalOk');
    const cancelBtn = document.getElementById('inputModalCancel');

    title.textContent = message;
    field.value = defaultValue;

    modal.style.display = 'flex';
    field.focus();

    function cleanup() {
      modal.style.display = 'none';
      okBtn.removeEventListener('click', handleOk);
      cancelBtn.removeEventListener('click', handleCancel);
    }

    function handleOk() {
      const value = field.value.trim();
      cleanup();
      resolve(value || null); // null if empty
    }

    function handleCancel() {
      cleanup();
      resolve(null);
    }

    okBtn.addEventListener('click', handleOk);
    cancelBtn.addEventListener('click', handleCancel);

    field.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') handleOk();
      if (e.key === 'Escape') handleCancel();
    });
  });
}
