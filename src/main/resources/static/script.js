const API_BASE = "http://localhost:8080/api";

// ---------- Tab switching ----------
document.querySelectorAll(".tab-btn").forEach(btn => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
    document.querySelectorAll(".tab-panel").forEach(p => p.classList.remove("active"));
    btn.classList.add("active");
    document.getElementById(btn.dataset.tab).classList.add("active");

    if (btn.dataset.tab === "dashboard") loadDashboard();
    if (btn.dataset.tab === "tickets") loadTickets();
    if (btn.dataset.tab === "users") loadUsers();
    if (btn.dataset.tab === "create-ticket") loadEmployeesIntoSelect();
  });
});

// ---------- Helpers ----------
async function apiRequest(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options
  });
  const isJson = res.headers.get("content-type")?.includes("application/json");
  const body = isJson ? await res.json() : null;
  if (!res.ok) {
    const message = body?.message || `Request failed (${res.status})`;
    throw new Error(message);
  }
  return body;
}

function formatDate(dateStr) {
  if (!dateStr) return "-";
  const d = new Date(dateStr);
  return d.toLocaleString();
}

// ---------- Dashboard ----------
async function loadDashboard() {
  try {
    const stats = await apiRequest("/tickets/stats");
    document.getElementById("statTotal").textContent = stats.totalTickets;
    document.getElementById("statOpen").textContent = stats.byStatus.OPEN || 0;
    document.getElementById("statInProgress").textContent = stats.byStatus.IN_PROGRESS || 0;
    document.getElementById("statResolved").textContent = stats.byStatus.RESOLVED || 0;
    document.getElementById("statClosed").textContent = stats.byStatus.CLOSED || 0;
    document.getElementById("statBreached").textContent = stats.breachedCount;

    document.getElementById("statLow").textContent = stats.byPriority.LOW || 0;
    document.getElementById("statMedium").textContent = stats.byPriority.MEDIUM || 0;
    document.getElementById("statHigh").textContent = stats.byPriority.HIGH || 0;
    document.getElementById("statCritical").textContent = stats.byPriority.CRITICAL || 0;
  } catch (err) {
    console.error(err);
  }
}

// ---------- Create Ticket ----------
async function loadEmployeesIntoSelect() {
  const select = document.getElementById("ticketCreatedBy");
  select.innerHTML = "<option value=''>Loading...</option>";
  try {
    const users = await apiRequest("/users");
    const employees = users.filter(u => u.role === "EMPLOYEE");
    if (employees.length === 0) {
      select.innerHTML = "<option value=''>No employees yet - add one in Users tab</option>";
      return;
    }
    select.innerHTML = employees
      .map(u => `<option value="${u.id}">${u.name} (${u.email})</option>`)
      .join("");
  } catch (err) {
    select.innerHTML = "<option value=''>Failed to load employees</option>";
  }
}

document.getElementById("createTicketForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const msg = document.getElementById("createTicketMsg");
  msg.textContent = "";
  msg.className = "form-msg";

  const payload = {
    title: document.getElementById("ticketTitle").value,
    description: document.getElementById("ticketDescription").value,
    category: document.getElementById("ticketCategory").value,
    priority: document.getElementById("ticketPriority").value,
    createdById: Number(document.getElementById("ticketCreatedBy").value)
  };

  try {
    await apiRequest("/tickets", { method: "POST", body: JSON.stringify(payload) });
    msg.textContent = "Ticket created successfully!";
    msg.className = "form-msg success";
    document.getElementById("createTicketForm").reset();
  } catch (err) {
    msg.textContent = err.message;
    msg.className = "form-msg error";
  }
});

// ---------- Ticket List ----------
document.getElementById("refreshTicketsBtn").addEventListener("click", loadTickets);
document.getElementById("filterStatus").addEventListener("change", loadTickets);
document.getElementById("filterPriority").addEventListener("change", loadTickets);

async function loadTickets() {
  const container = document.getElementById("ticketListContainer");
  container.innerHTML = "<p>Loading tickets...</p>";

  const status = document.getElementById("filterStatus").value;
  const priority = document.getElementById("filterPriority").value;

  let path = "/tickets";
  if (status) path = `/tickets/status/${status}`;
  else if (priority) path = `/tickets/priority/${priority}`;

  try {
    let tickets = await apiRequest(path);
    if (status && priority) {
      tickets = tickets.filter(t => t.priority === priority);
    }

    if (tickets.length === 0) {
      container.innerHTML = "<p>No tickets found.</p>";
      return;
    }

    container.innerHTML = tickets.map(ticketCardHtml).join("");

    container.querySelectorAll(".ticket-card").forEach(card => {
      card.addEventListener("click", () => openTicketModal(card.dataset.id));
    });
  } catch (err) {
    container.innerHTML = `<p class="form-msg error">${err.message}</p>`;
  }
}

function ticketCardHtml(t) {
  return `
    <div class="ticket-card" data-id="${t.id}">
      <div class="ticket-main">
        <div class="ticket-title">#${t.id} - ${t.title}</div>
        <div class="ticket-sub">${t.category} • Created by ${t.createdBy?.name || "-"} • Agent: ${t.assignedAgent?.name || "Unassigned"}</div>
      </div>
      <div class="badge-row">
        <span class="badge ${t.status}">${t.status}</span>
        <span class="badge ${t.priority}">${t.priority}</span>
        <span class="badge ${t.slaStatus}">${t.slaStatus.replace("_", " ")}</span>
      </div>
    </div>
  `;
}

// ---------- Ticket Details Modal ----------
const modal = document.getElementById("ticketModal");
document.getElementById("closeModalBtn").addEventListener("click", () => modal.classList.add("hidden"));
modal.addEventListener("click", (e) => { if (e.target === modal) modal.classList.add("hidden"); });

async function openTicketModal(ticketId) {
  const content = document.getElementById("ticketModalContent");
  content.innerHTML = "<p>Loading...</p>";
  modal.classList.remove("hidden");

  try {
    const [ticket, comments, agents] = await Promise.all([
      apiRequest(`/tickets/${ticketId}`),
      apiRequest(`/tickets/${ticketId}/comments`),
      apiRequest(`/users/agents`)
    ]);

    content.innerHTML = renderTicketDetail(ticket, comments, agents);
    attachModalHandlers(ticket, agents);
  } catch (err) {
    content.innerHTML = `<p class="form-msg error">${err.message}</p>`;
  }
}

function renderTicketDetail(t, comments, agents) {
  const agentOptions = agents.map(a => `<option value="${a.id}" ${t.assignedAgent?.id === a.id ? "selected" : ""}>${a.name}</option>`).join("");

  const statusOptions = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"]
    .map(s => `<option value="${s}" ${t.status === s ? "selected" : ""}>${s}</option>`).join("");

  const priorityOptions = ["LOW", "MEDIUM", "HIGH", "CRITICAL"]
    .map(p => `<option value="${p}" ${t.priority === p ? "selected" : ""}>${p}</option>`).join("");

  const commentsHtml = comments.length
    ? comments.map(c => `
        <div class="comment-item">
          <div class="comment-author">${c.author.name} <span class="comment-date">• ${formatDate(c.createdDate)}</span></div>
          <div>${c.message}</div>
        </div>`).join("")
    : "<p class='ticket-sub'>No comments yet.</p>";

  return `
    <h2>#${t.id} - ${t.title}</h2>
    <div class="badge-row" style="margin-bottom:16px;">
      <span class="badge ${t.status}">${t.status}</span>
      <span class="badge ${t.priority}">${t.priority}</span>
      <span class="badge ${t.slaStatus}">${t.slaStatus.replace("_", " ")}</span>
    </div>

    <div class="detail-row"><strong>Description:</strong> ${t.description}</div>
    <div class="detail-row"><strong>Category:</strong> ${t.category}</div>
    <div class="detail-row"><strong>Created By:</strong> ${t.createdBy?.name || "-"} (${t.createdBy?.email || "-"})</div>
    <div class="detail-row"><strong>Assigned Agent:</strong> ${t.assignedAgent?.name || "Unassigned"}</div>
    <div class="detail-row"><strong>Created:</strong> ${formatDate(t.createdDate)}</div>
    <div class="detail-row"><strong>Last Updated:</strong> ${formatDate(t.updatedDate)}</div>
    <div class="detail-row"><strong>Resolution Deadline:</strong> ${formatDate(t.resolutionDeadline)}</div>

    <div class="section-title">Assign Agent</div>
    <div class="inline-form">
      <select id="agentSelect">
        <option value="">Select agent...</option>
        ${agentOptions}
      </select>
      <button class="btn-small" id="assignBtn">Assign</button>
    </div>

    <div class="section-title">Update Status</div>
    <div class="inline-form">
      <select id="statusSelect">${statusOptions}</select>
      <button class="btn-small" id="statusBtn">Update Status</button>
    </div>

    <div class="section-title">Update Priority</div>
    <div class="inline-form">
      <select id="prioritySelect">${priorityOptions}</select>
      <button class="btn-small" id="priorityBtn">Update Priority</button>
    </div>

    <p class="form-msg" id="modalMsg"></p>

    <div class="section-title">Resolution Notes / Comments</div>
    <div id="commentsContainer">${commentsHtml}</div>

    <div class="inline-form" style="flex-direction:column; align-items:stretch;">
      <select id="commentAuthorSelect">
        <option value="">Select support agent...</option>
        ${agentOptions}
      </select>
      <textarea id="commentMessage" placeholder="Add a comment or resolution note..." rows="3" style="margin-top:6px;"></textarea>
      <button class="btn-small" id="addCommentBtn" style="margin-top:6px; width: fit-content;">Add Comment</button>
    </div>
  `;
}

function attachModalHandlers(ticket, agents) {
  const msgEl = document.getElementById("modalMsg");

  function showMsg(text, isError) {
    msgEl.textContent = text;
    msgEl.className = "form-msg " + (isError ? "error" : "success");
  }

  document.getElementById("assignBtn").addEventListener("click", async () => {
    const agentId = document.getElementById("agentSelect").value;
    if (!agentId) { showMsg("Please select an agent", true); return; }
    try {
      await apiRequest(`/tickets/${ticket.id}/assign`, {
        method: "PUT",
        body: JSON.stringify({ agentId: Number(agentId) })
      });
      showMsg("Agent assigned successfully", false);
      refreshAfterChange(ticket.id);
    } catch (err) {
      showMsg(err.message, true);
    }
  });

  document.getElementById("statusBtn").addEventListener("click", async () => {
    const status = document.getElementById("statusSelect").value;
    try {
      await apiRequest(`/tickets/${ticket.id}/status`, {
        method: "PUT",
        body: JSON.stringify({ status })
      });
      showMsg("Status updated successfully", false);
      refreshAfterChange(ticket.id);
    } catch (err) {
      showMsg(err.message, true);
    }
  });

  document.getElementById("priorityBtn").addEventListener("click", async () => {
    const priority = document.getElementById("prioritySelect").value;
    try {
      await apiRequest(`/tickets/${ticket.id}/priority`, {
        method: "PUT",
        body: JSON.stringify({ priority })
      });
      showMsg("Priority updated successfully", false);
      refreshAfterChange(ticket.id);
    } catch (err) {
      showMsg(err.message, true);
    }
  });

  document.getElementById("addCommentBtn").addEventListener("click", async () => {
    const authorId = document.getElementById("commentAuthorSelect").value;
    const message = document.getElementById("commentMessage").value.trim();
    if (!authorId || !message) { showMsg("Select an agent and enter a message", true); return; }
    try {
      await apiRequest(`/tickets/${ticket.id}/comments`, {
        method: "POST",
        body: JSON.stringify({ authorId: Number(authorId), message })
      });
      showMsg("Comment added", false);
      refreshAfterChange(ticket.id);
    } catch (err) {
      showMsg(err.message, true);
    }
  });
}

async function refreshAfterChange(ticketId) {
  await openTicketModal(ticketId);
  loadTickets();
  loadDashboard();
}

// ---------- Users ----------
document.getElementById("createUserForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const msg = document.getElementById("createUserMsg");
  msg.textContent = "";
  msg.className = "form-msg";

  const payload = {
    name: document.getElementById("userName").value,
    email: document.getElementById("userEmail").value,
    role: document.getElementById("userRole").value
  };

  try {
    await apiRequest("/users", { method: "POST", body: JSON.stringify(payload) });
    msg.textContent = "User created successfully!";
    msg.className = "form-msg success";
    document.getElementById("createUserForm").reset();
    loadUsers();
  } catch (err) {
    msg.textContent = err.message;
    msg.className = "form-msg error";
  }
});

async function loadUsers() {
  const container = document.getElementById("userListContainer");
  container.innerHTML = "<p>Loading users...</p>";
  try {
    const users = await apiRequest("/users");
    if (users.length === 0) {
      container.innerHTML = "<p>No users yet.</p>";
      return;
    }
    container.innerHTML = users.map(u => `
      <div class="user-card">
        <div>
          <div class="ticket-title">${u.name}</div>
          <div class="ticket-sub">${u.email}</div>
        </div>
        <span class="badge ${u.role === 'SUPPORT_AGENT' ? 'IN_PROGRESS' : 'OPEN'}">${u.role}</span>
      </div>
    `).join("");
  } catch (err) {
    container.innerHTML = `<p class="form-msg error">${err.message}</p>`;
  }
}

// ---------- Init ----------
loadDashboard();
