const state = {
  page: 0,
  size: 10,
  totalPages: 1,
  users: [],
  filter: "",
  isLoggedIn: false,
  config: {
    apiBase: "http://localhost:8080",
    token: "",
  },
};

const qs = (id) => document.getElementById(id);

const loginOverlay = qs("loginOverlay");
const loginForm = qs("loginForm");
const loginUsername = qs("loginUsername");
const loginPassword = qs("loginPassword");
const loginApiBase = qs("loginApiBase");
const loginNote = qs("loginNote");

const apiBaseInput = qs("apiBase");
const tokenInput = qs("token");
const saveConfigBtn = qs("saveConfig");
const testApiBtn = qs("testApi");
const searchInput = qs("search");
const refreshBtn = qs("refresh");
const prevBtn = qs("prev");
const nextBtn = qs("next");
const pageInfo = qs("pageInfo");
const userRows = qs("userRows");
const rowTemplate = document.getElementById("rowTemplate");
const detail = qs("detail");
const clearDetail = qs("clearDetail");
const form = qs("userForm");
const formNote = qs("formNote");
const modeLabel = qs("mode");
const resetBtn = qs("reset");
const toast = qs("toast");
const apiStatus = qs("apiStatus");
const screenTitle = qs("screenTitle");
const navButtons = Array.from(document.querySelectorAll(".nav-btn"));
const screens = Array.from(document.querySelectorAll(".screen"));
const openSettingsBtn = qs("openSettings");
const goUsersBtn = qs("goUsers");
const goSettingsBtn = qs("goSettings");
const clearTokenBtn = qs("clearToken");
const totalUsers = qs("totalUsers");
const pageSize = qs("pageSize");
const lastUpdated = qs("lastUpdated");

const fields = {
  id: qs("id"),
  username: qs("username"),
  fullName: qs("fullName"),
  email: qs("email"),
  password: qs("password"),
  role: qs("role"),
  enabled: qs("enabled"),
};

const loadConfig = () => {
  const saved = localStorage.getItem("admin-ui-config");
  if (!saved) {
    return;
  }
  try {
    const config = JSON.parse(saved);
    state.config = { ...state.config, ...config };
  } catch (error) {
    console.warn("Failed to parse config", error);
  }
};

const syncConfigUI = () => {
  apiBaseInput.value = state.config.apiBase;
  tokenInput.value = state.config.token;
};

const saveConfig = () => {
  state.config.apiBase = apiBaseInput.value.trim() || state.config.apiBase;
  state.config.token = tokenInput.value.trim();
  localStorage.setItem("admin-ui-config", JSON.stringify(state.config));
  notify("Da luu cau hinh.");
  fetchUsers();
};

const notify = (message) => {
  formNote.textContent = message;
  if (!message) {
    return;
  }
  toast.textContent = message;
  toast.classList.add("show");
  setTimeout(() => toast.classList.remove("show"), 2400);
};

const setApiStatus = (stateLabel, status) => {
  apiStatus.textContent = stateLabel;
  apiStatus.classList.remove("ok", "warn");
  if (status) {
    apiStatus.classList.add(status);
  }
};

const showScreen = (name) => {
  screens.forEach((screen) => {
    screen.classList.toggle("active", screen.dataset.screen === name);
  });
  navButtons.forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.screen === name);
  });
  const titleMap = {
    welcome: "Tong quan",
    users: "Nguoi dung",
    settings: "Cau hinh",
  };
  screenTitle.textContent = titleMap[name] || "Admin";
};

const setMode = (mode) => {
  modeLabel.textContent = mode;
};

const resetForm = () => {
  form.reset();
  fields.id.value = "";
  setMode("Create");
  notify("");
};

const makeHeaders = () => {
  const headers = { "Content-Type": "application/json" };
  if (state.config.token) {
    headers.Authorization = `Bearer ${state.config.token}`;
  }
  return headers;
};

const request = async (path, options = {}) => {
  const url = `${state.config.apiBase}${path}`;
  console.log('Request:', url, options);
  try {
    const response = await fetch(url, {
      ...options,
      headers: { ...makeHeaders(), ...options.headers },
    });
    console.log('Response status:', response.status);
    if (!response.ok) {
      const text = await response.text();
      console.error('Response error:', text);
      throw new Error(text || "Request failed");
    }
    return response.json();
  } catch (error) {
    console.error('Fetch error:', error);
    throw error;
  }
};

const normalizeUser = (item) => ({
  id: item.id ?? item.userId ?? item.idUser ?? "-",
  name: item.fullName ?? item.username ?? "-",
  email: item.email ?? "-",
  role: item.role ?? item.roles?.[0] ?? "-",
  status: item.enabled !== undefined ? (item.enabled ? "Hoat dong" : "Khoa") : "-",
  raw: item,
});

const fetchUsers = async () => {
  try {
    console.log('Fetching users with token:', state.config.token ? 'Present' : 'Missing');
    const data = await request(`/api/admin/users?page=${state.page}&size=${state.size}`);
    console.log('Received data:', data);
    const page = data.data || data;
    state.totalPages = page.totalPages ?? 1;
    state.users = (page.content || page || []).map(normalizeUser);
    totalUsers.textContent = page.totalElements ?? state.users.length;
    pageSize.textContent = state.size;
    lastUpdated.textContent = new Date().toLocaleTimeString();
    setApiStatus("Connected", "ok");
    renderTable();
  } catch (error) {
    console.error('fetchUsers error:', error);
    
    // If unauthorized, show login screen
    if (error.message.includes('401') || error.message.includes('Unauthorized') || error.message.includes('Failed to fetch')) {
      logout();
      notify('Phien dang nhap het han. Vui long dang nhap lai.');
    } else {
      setApiStatus("No access", "warn");
      notify(`Loi tai danh sach: ${error.message}`);
    }
  }
};

const renderTable = () => {
  userRows.innerHTML = "";
  const filter = state.filter.toLowerCase();
  const filtered = state.users.filter((item) => {
    if (!filter) return true;
    return [item.name, item.email, String(item.id), String(item.role)]
      .filter(Boolean)
      .some((value) => value.toLowerCase().includes(filter));
  });

  filtered.forEach((item) => {
    const row = rowTemplate.content.cloneNode(true);
    row.querySelector("[data-id]").textContent = item.id;
    row.querySelector("[data-name]").textContent = item.name;
    row.querySelector("[data-email]").textContent = item.email;
    row.querySelector("[data-role]").textContent = item.role;
    row.querySelector("[data-status]").textContent = item.status;

    row.querySelector("[data-view]").addEventListener("click", () => {
      detail.textContent = JSON.stringify(item.raw, null, 2);
    });

    row.querySelector("[data-edit]").addEventListener("click", () => {
      setMode("Update");
      fields.id.value = item.id;
      fields.username.value = item.raw.username ?? "";
      fields.fullName.value = item.raw.fullName ?? "";
      fields.email.value = item.email === "-" ? "" : item.email;
      fields.role.value = item.role === "-" ? "USER" : item.role;
      fields.enabled.value = item.raw.enabled ? "true" : "false";
    });

    row.querySelector("[data-delete]").addEventListener("click", async () => {
      if (!confirm(`Xoa user ${item.id}?`)) return;
      try {
        await request(`/api/admin/users/${item.id}`, { method: "DELETE" });
        notify("Da xoa user.");
        fetchUsers();
      } catch (error) {
        notify(`Loi xoa: ${error.message}`);
      }
    });

    userRows.appendChild(row);
  });

  pageInfo.textContent = `Page ${state.page + 1} / ${state.totalPages}`;
};

const createUser = async () => {
  const email = fields.email.value.trim();
  if (!email.endsWith('@gmail.com')) {
    throw new Error('Email phai la @gmail.com khi tao moi user');
  }
  
  const payload = {
    username: fields.username.value.trim(),
    fullName: fields.fullName.value.trim() || undefined,
    email: email,
    password: fields.password.value,
    role: fields.role.value,
    enabled: fields.enabled.value === "true",
  };

  return request("/api/admin/users", {
    method: "POST",
    body: JSON.stringify(payload),
  });
};

const updateUser = async (id) => {
  const payload = {
    username: fields.username.value.trim(),
    fullName: fields.fullName.value.trim() || undefined,
    email: fields.email.value.trim(),
    password: fields.password.value || undefined,
    role: fields.role.value,
    enabled: fields.enabled.value === "true",
  };

  return request(`/api/admin/users/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
};

const submitForm = async (event) => {
  event.preventDefault();
  const id = fields.id.value.trim();
  try {
    if (id) {
      await updateUser(id);
      notify("Cap nhat user thanh cong.");
    } else {
      await createUser();
      notify("Tao user thanh cong.");
    }
    resetForm();
    fetchUsers();
  } catch (error) {
    notify(`Loi luu: ${error.message}`);
  }
};

const testApi = async () => {
  try {
    await request(`/api/admin/users?page=0&size=1`);
    setApiStatus("Connected", "ok");
    notify("Ket noi thanh cong.");
  } catch (error) {
    setApiStatus("No access", "warn");
    notify(`Ket noi that bai: ${error.message}`);
  }
};

const login = async (username, password, apiBase) => {
  try {
    state.config.apiBase = apiBase;
    const response = await fetch(`${apiBase}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });
    
    if (!response.ok) {
      const text = await response.text();
      throw new Error(text || "Dang nhap that bai");
    }
    
    const data = await response.json();
    const token = data.data?.token || data.token;
    
    if (!token) {
      throw new Error("Khong nhan duoc token");
    }
    
    state.config.token = token;
    state.isLoggedIn = true;
    localStorage.setItem("admin-ui-config", JSON.stringify(state.config));
    
    loginOverlay.classList.add("hidden");
    syncConfigUI();
    setApiStatus("Connected", "ok");
    fetchUsers();
    loginNote.textContent = "";
  } catch (error) {
    loginNote.textContent = `Loi: ${error.message}`;
    loginNote.style.color = "var(--danger)";
  }
};

const logout = () => {
  state.isLoggedIn = false;
  state.config.token = "";
  localStorage.removeItem("admin-ui-config");
  loginOverlay.classList.remove("hidden");
  setApiStatus("Disconnected", "warn");
  state.users = [];
  renderTable();
  loginForm.reset();
};

const init = () => {
  loadConfig();
  syncConfigUI();
  showScreen("welcome");
  setApiStatus("Disconnected", "warn");
  
  if (state.config.token) {
    state.isLoggedIn = true;
    loginOverlay.classList.add("hidden");
    fetchUsers();
  } else {
    loginOverlay.classList.remove("hidden");
  }
  
  loginForm.addEventListener("submit", (e) => {
    e.preventDefault();
    login(loginUsername.value.trim(), loginPassword.value, loginApiBase.value.trim());
  });

  saveConfigBtn.addEventListener("click", saveConfig);
  testApiBtn.addEventListener("click", testApi);
  refreshBtn.addEventListener("click", fetchUsers);
  resetBtn.addEventListener("click", resetForm);
  clearDetail.addEventListener("click", () => {
    detail.textContent = "";
  });
  openSettingsBtn.addEventListener("click", () => showScreen("settings"));
  goUsersBtn.addEventListener("click", () => showScreen("users"));
  goSettingsBtn.addEventListener("click", () => showScreen("settings"));
  clearTokenBtn.addEventListener("click", () => {
    logout();
    notify("Da dang xuat.");
  });
  navButtons.forEach((btn) => {
    btn.addEventListener("click", () => showScreen(btn.dataset.screen));
  });
  prevBtn.addEventListener("click", () => {
    if (state.page > 0) {
      state.page -= 1;
      fetchUsers();
    }
  });
  nextBtn.addEventListener("click", () => {
    if (state.page + 1 < state.totalPages) {
      state.page += 1;
      fetchUsers();
    }
  });
  searchInput.addEventListener("input", (event) => {
    state.filter = event.target.value;
    renderTable();
  });
  form.addEventListener("submit", submitForm);
};

init();
