const API_BASE = 'http://localhost:8080';

const Auth = {
  save(token, role, name) {
    localStorage.setItem('sf_token', token);
    localStorage.setItem('sf_role', role);
    localStorage.setItem('sf_name', name);
  },
  token() { return localStorage.getItem('sf_token'); },
  role() { return localStorage.getItem('sf_role'); },
  name() { return localStorage.getItem('sf_name'); },
  clear() { localStorage.removeItem('sf_token'); localStorage.removeItem('sf_role'); localStorage.removeItem('sf_name'); },
  requireLogin(allowedRoles) {
    const token = this.token();
    const role = this.role();
    if (!token) { window.location.href = 'login.html'; return false; }
    if (allowedRoles && !allowedRoles.includes(role)) {
      window.location.href = 'login.html';
      return false;
    }
    return true;
  },
  logout() { this.clear(); window.location.href = 'login.html'; }
};

async function api(path, options = {}) {
  const headers = Object.assign({ 'Content-Type': 'application/json' }, options.headers || {});
  const token = Auth.token();
  if (token) headers['Authorization'] = 'Bearer ' + token;

  let response;
  try {
    response = await fetch(API_BASE + path, Object.assign({}, options, { headers }));
  } catch (err) {
    throw new Error('Could not reach the server. Is the backend running on localhost:8080?');
  }

  if (response.status === 401 || response.status === 403) {
    Auth.clear();
    window.location.href = 'login.html';
    throw new Error('Session expired. Please log in again.');
  }

  const text = await response.text();
  let data;
  try { data = text ? JSON.parse(text) : null; } catch (e) { data = text; }

  if (!response.ok) {
    const message = typeof data === 'string' ? data : (data && data.message) || 'Something went wrong.';
    throw new Error(message);
  }
  return data;
}

function initTopbar(el) {
  if (!el) return;
  const nameEl = el.querySelector('.who');
  if (nameEl) nameEl.textContent = Auth.name() + ' \u2014 ' + Auth.role();
  const logoutBtn = el.querySelector('[data-logout]');
  if (logoutBtn) logoutBtn.addEventListener('click', () => Auth.logout());
}

function stampClass(status) {
  return (status || '').toLowerCase();
}

function stampLabel(status) {
  return (status || '').replace(/_/g, ' ');
}
// Voice Input using Web Speech API
function initVoiceInput(inputId, btnId) {
  const input = document.getElementById(inputId);
  const btn = document.getElementById(btnId);
  
  // Check for browser support
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  
  if (!SpeechRecognition) {
    btn.style.display = 'none'; // Hide mic if browser doesn't support it
    return;
  }

  const recognition = new SpeechRecognition();
  recognition.continuous = false;
  recognition.interimResults = false;

  btn.addEventListener('click', () => {
    recognition.start();
    btn.textContent = '🎙️'; // Visual feedback that it's listening
    btn.style.background = 'var(--amber)';
    btn.style.color = '#fff'; // Make text readable on amber background
  });

  recognition.onresult = (event) => {
    const transcript = event.results[0][0].transcript;
    input.value = transcript;
    btn.textContent = '🎤';
    btn.style.background = 'transparent';
    btn.style.color = 'var(--green)';
  };

  recognition.onerror = () => {
    btn.textContent = '🎤';
    btn.style.background = 'transparent';
    btn.style.color = 'var(--green)';
    alert('Microphone access denied or not available.');
  };
  
  recognition.onend = () => {
    // Reset button if it ends without a result
    btn.textContent = '🎤';
    btn.style.background = 'transparent';
    btn.style.color = 'var(--green)';
  };
}