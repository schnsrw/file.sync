(function() {
  function parseJwt(token) {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64.padEnd(base64.length + (4 - base64.length % 4) % 4, '=');
      const json = atob(padded);
      return JSON.parse(json);
    } catch (e) {
      return null;
    }
  }

  function isTokenExpired(token) {
    const payload = parseJwt(token);
    if (!payload || !payload.exp) return true;
    return payload.exp * 1000 < Date.now();
  }

  function clearTokens() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }

  function logout() {
    clearTokens();
    window.location.href = '/login';
  }

  function ensureAuth() {
    const path = window.location.pathname;
    if (path.endsWith('/login')) {
      const token = localStorage.getItem('accessToken');
      if (token && isTokenExpired(token)) clearTokens();
      return;
    }
    const at = localStorage.getItem('accessToken');
    const rt = localStorage.getItem('refreshToken');
    if (!at || !rt || isTokenExpired(at)) {
      clearTokens();
      window.location.href = '/login';
    }
  }

  document.addEventListener('DOMContentLoaded', ensureAuth);
  window.logout = logout;
})();
