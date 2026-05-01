// CONFIG
const API = window.API_BASE || '/api';

// Current session state
const session = {
  role: 'client',
  page: 'dashboard',
  clientId: 'cl001',
  consultantId: 'c001',
};


// API HELPERS
async function api(path, method='GET', body=null) {
  const opts = { method, headers: {'Content-Type':'application/json'} };
  if (body) opts.body = JSON.stringify(body);
  try {
    const r = await fetch(API + path, opts);
    const data = await r.json();
    if (!r.ok) throw new Error(data.error || 'Request failed');
    return data;
  } catch(e) {
    throw e;
  }
}

async function apiSafe(path, method='GET', body=null) {
  try { return await api(path, method, body); }
  catch(e) { toast(e.message, 'error'); return null; }
}


// NAVIGATION

const navConfig = {
  client: [
    {id:'dashboard',  label:'Dashboard',         icon:'◧'},
    {id:'book',       label:'Book a Session',     icon:'＋'},
    {id:'my-bookings',label:'My Bookings',        icon:'📋'},
    {id:'payments',   label:'Payments',           icon:'💳'},
    {id:'pay-methods',label:'Payment Methods',    icon:'🔑'},
    {id:'consultants',label:'Browse Consultants', icon:'👤'},
    {id:'chatbot',    label:'AI Assistant',       icon:'🤖'},
  ],
  consultant: [
    {id:'dashboard',       label:'Dashboard',         icon:'◧'},
    {id:'my-schedule',     label:'My Schedule',        icon:'📅'},
    {id:'incoming',        label:'Booking Requests',   icon:'📥'},
    {id:'manage-bookings', label:'Manage Bookings',    icon:'📋'},
    {id:'availability',    label:'Availability',       icon:'🗓'},
  ],
  admin: [
    {id:'dashboard',       label:'Dashboard',          icon:'◧'},
    {id:'policies',        label:'System Policies',    icon:'⚙'},
    {id:'all-consultants', label:'Consultant Review',  icon:'👥'},
    {id:'all-bookings',    label:'All Bookings',       icon:'📋'},
    {id:'all-payments',    label:'Payments Overview',  icon:'💳'},
  ],
};

const userMeta = {
  client:     {name:'Alex Chen',        initials:'AC', role:'client · cl001'},
  consultant: {name:'Dr. S. Mitchell',  initials:'SM', role:'consultant · c001'},
  admin:      {name:'Admin',            initials:'AU', role:'administrator'},
};

function switchRole(r) {
  session.role = r;
  document.querySelectorAll('.role-btn').forEach(b=>b.classList.remove('active'));
  document.getElementById('role-'+r).classList.add('active');
  const u = userMeta[r];
  document.getElementById('user-name').textContent  = u.name;
  document.getElementById('user-role').textContent  = u.role;
  document.getElementById('user-avatar').textContent = u.initials;
  navigate('dashboard');
}

function renderNav() {
  const nav = document.getElementById('sidebar-nav');
  const items = navConfig[session.role]||[];
  nav.innerHTML = `<div class="nav-label">Navigation</div>` +
    items.map(item=>`<div class="nav-item${item.id===session.page?' active':''}" onclick="navigate('${item.id}')">
      <span class="nav-icon">${item.icon}</span>${item.label}
    </div>`).join('');
}

const pageTitles = {
  dashboard:'Dashboard <span>/ Overview</span>',
  book:'Book a Session <span>/ New Booking</span>',
  'my-bookings':'My Bookings <span>/ History & Active</span>',
  payments:'Payments <span>/ Transactions</span>',
  'pay-methods':'Payment Methods <span>/ Saved Methods</span>',
  consultants:'Browse Consultants <span>/ Find Experts</span>',
  chatbot:'AI Assistant <span>/ Customer Support</span>',
  'my-schedule':'My Schedule <span>/ Sessions</span>',
  incoming:'Booking Requests <span>/ Pending</span>',
  'manage-bookings':'Manage Bookings <span>/ All</span>',
  availability:'Availability <span>/ Time Slots</span>',
  policies:'System Policies <span>/ Configuration</span>',
  'all-consultants':'Consultant Review <span>/ Approvals</span>',
  'all-bookings':'All Bookings <span>/ Platform</span>',
  'all-payments':'Payments Overview <span>/ Revenue</span>',
};

async function navigate(pageId) {
  session.page = pageId;
  renderNav();
  document.getElementById('topbar-title').innerHTML = pageTitles[pageId]||pageId;
  const el = document.getElementById('main-content');
  el.innerHTML = '<div class="empty-state"><div class="empty-icon">⏳</div><div class="empty-title">Loading…</div></div>';
  el.className = 'content';

  const pages = {
    dashboard: renderDashboard,
    book: renderBook,
    'my-bookings': renderMyBookings,
    payments: renderPayments,
    'pay-methods': renderPayMethods,
    consultants: renderConsultants,
    chatbot: renderChatbot,
    'my-schedule': renderMySchedule,
    incoming: renderIncoming,
    'manage-bookings': renderManageBookings,
    availability: renderAvailability,
    policies: renderPolicies,
    'all-consultants': renderAllConsultants,
    'all-bookings': renderAllBookings,
    'all-payments': renderAllPayments,
  };

  try {
    await (pages[pageId]||renderDashboard)(el);
    el.className = 'content';
  } catch(e) {
    el.innerHTML = `<div class="empty-state"><div class="empty-icon">⚠️</div><div class="empty-title">Error loading page</div><div class="text-dim">${e.message}</div></div>`;
  }
}


// HELPERS
function fmtDate(d){if(!d)return'—';const dt=new Date(d);return dt.toLocaleDateString('en-CA',{month:'short',day:'numeric',year:'numeric'})+' '+dt.toLocaleTimeString('en-CA',{hour:'2-digit',minute:'2-digit'})}
function fmtMoney(n){return'$'+Number(n).toLocaleString('en-CA',{minimumFractionDigits:2})}
function badge(s){return`<span class="badge badge-${(s||'').toLowerCase().replace(' ','_')}">${s||''}</span>`}

function toast(msg, type='info') {
  const t = document.createElement('div');
  t.className = `toast toast-${type}`;
  t.innerHTML = `<span>${{success:'✓', error:'✕', info:'ℹ'}[type]||'•'}</span><span>${msg}</span>`;
  document.getElementById('toast-container').appendChild(t);
  setTimeout(()=>t.remove(), 3500);
}

function openModal(title, bodyHtml, footerHtml='') {
  document.getElementById('modal-title').textContent = title;
  document.getElementById('modal-body').innerHTML = bodyHtml;
  document.getElementById('modal-footer').innerHTML = footerHtml;
  document.getElementById('modal-overlay').classList.add('open');
}
function closeModal() { document.getElementById('modal-overlay').classList.remove('open'); }
document.getElementById('modal-overlay').addEventListener('click', e=>{if(e.target===e.currentTarget) closeModal()});

function setLoading(v) { document.getElementById('loading-overlay').classList.toggle('active', v); }

function bookingTimeline(state) {
  const steps = [
    {k:'REQUESTED',      l:'Requested', i:'1'},
    {k:'CONFIRMED',      l:'Confirmed', i:'2'},
    {k:'PENDING_PAYMENT',l:'Pending Payment', i:'3'},
    {k:'PAID',           l:'Paid',      i:'4'},
    {k:'COMPLETED',      l:'Completed', i:'5'},
  ];
  const order = ['REQUESTED','CONFIRMED','PENDING_PAYMENT','PAID','COMPLETED','REJECTED','CANCELLED'];
  const ci = order.indexOf(state);
  const terminal = ['REJECTED','CANCELLED'].includes(state);
  let h = '<div class="state-timeline">';
  steps.forEach((s, i) => {
    const si = order.indexOf(s.k);
    let cls = si < ci ? 'done' : s.k === state ? 'active' : '';
    if (i > 0) h += `<div class="state-connector${order.indexOf(steps[i-1].k) < ci ? ' done' : ''}"></div>`;
    h += `<div class="state-step ${cls}"><div class="state-dot">${cls==='done'?'✓':s.i}</div><div class="state-label">${s.l}</div></div>`;
  });
  if (terminal) {
    h += `<div class="state-connector"></div><div class="state-step skip"><div class="state-dot">✕</div><div class="state-label">${state}</div></div>`;
  }
  return h + '</div>';
}

function tableOf(cols, rows, emptyMsg='No records') {
  if (!rows||!rows.length) return `<div class="empty-state"><div class="empty-icon">📋</div><div class="empty-title">${emptyMsg}</div></div>`;
  return `<div class="table-wrap"><table>
    <thead><tr>${cols.map(c=>`<th>${c}</th>`).join('')}</tr></thead>
    <tbody>${rows.join('')}</tbody>
  </table></div>`;
}


// DASHBOARD
async function renderDashboard(el) {
  if (session.role==='client') await renderClientDashboard(el);
  else if (session.role==='consultant') await renderConsultantDashboard(el);
  else await renderAdminDashboard(el);
}

async function renderClientDashboard(el) {
  const [bookings, services] = await Promise.all([
    apiSafe(`/bookings/client/${session.clientId}`)||[],
    apiSafe('/services')||[],
  ]);
  const active    = (bookings||[]).filter(b=>['REQUESTED','CONFIRMED','PENDING_PAYMENT','PAID'].includes(b.state));
  const completed = (bookings||[]).filter(b=>b.state==='COMPLETED');

  el.innerHTML=`
  <div class="stats-grid">
    <div class="stat-card"><div class="stat-label">Active Bookings</div><div class="stat-value">${active.length}</div></div>
    <div class="stat-card"><div class="stat-label">Completed</div><div class="stat-value">${completed.length}</div></div>
    <div class="stat-card"><div class="stat-label">Services Available</div><div class="stat-value">${(services||[]).length}</div></div>
    <div class="stat-card"><div class="stat-label">Platform</div><div class="stat-value" style="font-size:18px">ConsultHub</div><div class="stat-sub">Phase 2</div></div>
  </div>
  <div class="card mb-4">
    <div class="card-header"><div class="card-title">Recent Bookings</div>
      <button class="btn btn-sm btn-ghost" onclick="navigate('my-bookings')">View all →</button></div>
    ${tableOf(
      ['ID','Consultant','Service','Date','Amount','State','Actions'],
      (bookings||[]).slice(0,5).map(b=>`<tr>
        <td class="td-mono">#${b.id}</td>
        <td>${b.consultantName}</td>
        <td>${b.serviceName}</td>
        <td class="td-mono text-sm">${fmtDate(b.slotStart)}</td>
        <td class="td-mono font-bold">${fmtMoney(b.amount)}</td>
        <td>${badge(b.state)}</td>
        <td class="flex gap-2">
          <button class="btn btn-xs btn-ghost" onclick="viewBooking('${b.id}')">Detail</button>
          ${b.state==='PENDING_PAYMENT'?`<button class="btn btn-xs btn-accent" onclick="openPayModal('${b.id}')">Pay</button>`:''}
          ${['REQUESTED','CONFIRMED'].includes(b.state)?`<button class="btn btn-xs btn-danger" onclick="cancelBooking('${b.id}')">Cancel</button>`:''}
        </td>
      </tr>`),
      'No bookings yet'
    )}
  </div>
  <div class="mt-6">
    <div class="section-header">
      <div><div class="section-title">Available Services</div></div>
      <button class="btn btn-accent" onclick="navigate('book')">＋ Book Session</button>
    </div>
    <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(200px,1fr));gap:12px">
      ${(services||[]).map(s=>`<div class="card" style="padding:16px;cursor:pointer" onclick="navigate('book')">
        <div style="font-weight:bold;font-size:14px;margin-bottom:4px">${s.name}</div>
        <div class="text-dim text-sm">${s.duration} min</div>
        <div style="font-size:22px;font-weight:bold;color:#c0392b;margin-top:8px">${fmtMoney(s.price)}</div>
      </div>`).join('')}
    </div>
  </div>`;
}

async function renderConsultantDashboard(el) {
  const bookings = await apiSafe(`/bookings/consultant/${session.consultantId}`)||[];
  const requested = bookings.filter(b=>b.state==='REQUESTED');
  const upcoming  = bookings.filter(b=>['CONFIRMED','PENDING_PAYMENT','PAID'].includes(b.state));
  el.innerHTML=`
  <div class="stats-grid">
    <div class="stat-card"><div class="stat-label">Pending Requests</div><div class="stat-value">${requested.length}</div></div>
    <div class="stat-card"><div class="stat-label">Upcoming</div><div class="stat-value">${upcoming.length}</div></div>
    <div class="stat-card"><div class="stat-label">Completed</div><div class="stat-value">${bookings.filter(b=>b.state==='COMPLETED').length}</div></div>
    <div class="stat-card"><div class="stat-label">Total Bookings</div><div class="stat-value">${bookings.length}</div></div>
  </div>
  ${requested.length?`<div class="card mb-4">
    <div class="card-header"><div class="card-title">🔔 New Requests</div></div>
    ${tableOf(['Client','Service','Date','Amount','Actions'],requested.map(b=>`<tr>
      <td>${b.clientName}</td><td>${b.serviceName}</td>
      <td class="td-mono text-sm">${fmtDate(b.slotStart)}</td>
      <td class="td-mono">${fmtMoney(b.amount)}</td>
      <td class="flex gap-2">
        <button class="btn btn-xs btn-success" onclick="acceptBooking('${b.id}')">Accept</button>
        <button class="btn btn-xs btn-danger" onclick="rejectBooking('${b.id}')">Reject</button>
      </td>
    </tr>`))}
  </div>`:''}
  <div class="card">
    <div class="card-header"><div class="card-title">My Schedule</div></div>
    ${tableOf(['ID','Client','Service','Date','State','Actions'],
      bookings.filter(b=>!['CANCELLED','REJECTED'].includes(b.state)).map(b=>`<tr>
        <td class="td-mono">#${b.id}</td><td>${b.clientName}</td><td>${b.serviceName}</td>
        <td class="td-mono text-sm">${fmtDate(b.slotStart)}</td><td>${badge(b.state)}</td>
        <td>${b.state==='PAID'?`<button class="btn btn-xs btn-success" onclick="completeBooking('${b.id}')">Complete</button>`:''}</td>
      </tr>`),'No bookings')}
  </div>`;
}

async function renderAdminDashboard(el) {
  const [stats, consultants] = await Promise.all([
    apiSafe('/admin/stats')||{},
    apiSafe('/consultants')||[],
  ]);
  const pending  = (consultants||[]).filter(c=>c.status==='PENDING');
  const policies = await apiSafe('/admin/policies')||{};

  el.innerHTML=`
  <div class="stats-grid">
    <div class="stat-card"><div class="stat-label">Total Bookings</div><div class="stat-value">${stats.totalBookings||0}</div></div>
    <div class="stat-card"><div class="stat-label">Consultants (Approved)</div><div class="stat-value">${stats.approvedConsultants||0}</div></div>
    <div class="stat-card"><div class="stat-label">Pending Review</div><div class="stat-value">${stats.pendingConsultants||0}</div></div>
    <div class="stat-card"><div class="stat-label">Clients</div><div class="stat-value">${stats.totalClients||0}</div></div>
  </div>
  ${pending.length?`<div class="card mb-4">
    <div class="card-header"><div class="card-title">⏳ Pending Approvals</div>
      <button class="btn btn-sm btn-ghost" onclick="navigate('all-consultants')">View all →</button></div>
    ${tableOf(['Name','Email','Actions'],pending.map(c=>`<tr>
      <td>${c.name}</td><td class="td-mono text-sm">${c.email}</td>
      <td class="flex gap-2">
        <button class="btn btn-xs btn-success" onclick="approveConsultant('${c.id}')">Approve</button>
        <button class="btn btn-xs btn-danger" onclick="rejectConsultant('${c.id}')">Reject</button>
      </td>
    </tr>`))}
  </div>`:''}
  <div class="card">
    <div class="card-header"><div class="card-title">System Policies</div>
      <button class="btn btn-sm btn-ghost" onclick="navigate('policies')">Configure →</button></div>
    <div class="card-body">
      <div class="policy-grid">
        <div class="policy-card"><div class="policy-icon">⏱</div><div class="policy-name">Cancellation Window</div>
          <div class="policy-value">${policies.cancellationHours}h</div></div>
        <div class="policy-card"><div class="policy-icon">💵</div><div class="policy-name">Pricing Range</div>
          <div class="policy-value">${fmtMoney(policies.priceMin||0)} – ${fmtMoney(policies.priceMax||1000)}</div></div>
        <div class="policy-card"><div class="policy-icon">🔔</div><div class="policy-name">Notifications</div>
          <div class="policy-value" style="font-size:18px;color:${policies.notificationsEnabled?'#27ae60':'#c0392b'}">${policies.notificationsEnabled?'Enabled':'Disabled'}</div></div>
        <div class="policy-card"><div class="policy-icon">↩</div><div class="policy-name">Refund Policy</div>
          <div class="policy-value" style="font-size:18px">${policies.refundPolicy}</div></div>
      </div>
    </div>
  </div>`;
}


// CLIENT PAGES

let bookState = {consultantId:null, slotId:null};

async function renderBook(el) {
  const [consultants, services] = await Promise.all([
    apiSafe('/consultants/approved')||[],
    apiSafe('/services')||[],
  ]);
  el.innerHTML=`<div style="max-width:680px">
  <div class="card">
    <div class="card-header"><div class="card-title">Create a Booking</div></div>
    <div class="card-body">
      <div class="form-grid">
        <div class="form-group">
          <label>Consultant</label>
          <select id="bk-con" onchange="loadSlots()">
            <option value="">— Select consultant —</option>
            ${consultants.map(c=>`<option value="${c.id}">${c.name}</option>`).join('')}
          </select>
        </div>
        <div class="form-group">
          <label>Service</label>
          <select id="bk-svc">
            <option value="">— Select service —</option>
            ${services.map(s=>`<option value="${s.id}">${s.name} (${s.duration}min) — ${fmtMoney(s.price)}</option>`).join('')}
          </select>
        </div>
      </div>
      <div class="mt-4">
        <label style="display:block;margin-bottom:8px">Available Slots</label>
        <div id="slots-area"><div class="text-dim text-sm">Select a consultant first</div></div>
      </div>
      <div class="divider"></div>
      <div class="flex gap-3" style="justify-content:flex-end">
        <button class="btn btn-ghost" onclick="navigate('dashboard')">Cancel</button>
        <button class="btn btn-accent" onclick="submitBooking()">＋ Request Booking</button>
      </div>
    </div>
  </div></div>`;
}

async function loadSlots() {
  const cid = document.getElementById('bk-con').value;
  const el  = document.getElementById('slots-area');
  bookState.consultantId = cid;
  bookState.slotId = null;
  if (!cid) { el.innerHTML='<div class="text-dim text-sm">Select a consultant first</div>'; return; }
  el.innerHTML='<div class="text-dim text-sm">Loading slots…</div>';
  const slots = await apiSafe(`/consultants/${cid}/slots`)||[];
  const avail = slots.filter(s=>s.available);
  if (!avail.length) { el.innerHTML='<div class="text-dim text-sm">No available slots for this consultant</div>'; return; }
  el.innerHTML=`<div class="slot-grid">${avail.map(s=>`
    <div class="slot-card" id="slot-${s.id}" onclick="selectSlot('${s.id}',this)">
      <div class="slot-time">${new Date(s.start).toLocaleDateString('en-CA',{month:'short',day:'numeric'})}</div>
      <div class="slot-time">${new Date(s.start).toLocaleTimeString('en-CA',{hour:'2-digit',minute:'2-digit'})} – ${new Date(s.end).toLocaleTimeString('en-CA',{hour:'2-digit',minute:'2-digit'})}</div>
      <div class="slot-avail free">Available</div>
    </div>`).join('')}</div>`;
}

function selectSlot(id, el) {
  bookState.slotId = id;
  document.querySelectorAll('.slot-card').forEach(c=>c.classList.remove('selected'));
  el.classList.add('selected');
}

async function submitBooking() {
  const cid = document.getElementById('bk-con')?.value;
  const sid = document.getElementById('bk-svc')?.value;
  if (!cid) { toast('Select a consultant','error'); return; }
  if (!sid) { toast('Select a service','error'); return; }
  if (!bookState.slotId) { toast('Select a time slot','error'); return; }
  setLoading(true);
  const result = await apiSafe('/bookings','POST',{
    clientId: session.clientId,
    consultantId: cid,
    serviceId: sid,
    slotId: bookState.slotId,
  });
  setLoading(false);
  if (result) { toast(`Booking #${result.id} requested!`,'success'); navigate('my-bookings'); }
}

async function renderMyBookings(el) {
  const bookings = await apiSafe(`/bookings/client/${session.clientId}`)||[];
  el.innerHTML=`<div class="filter-bar">
    <div class="search-input"><input type="text" placeholder="Search…" oninput="filterTbl(this,'bk-tbl')"/></div>
    <select onchange="filterState(this,'bk-tbl')">
      <option value="">All states</option>
      ${['REQUESTED','CONFIRMED','PENDING_PAYMENT','PAID','COMPLETED','CANCELLED','REJECTED'].map(s=>`<option>${s}</option>`).join('')}
    </select>
  </div>
  <div class="card">
    ${tableOf(['ID','Consultant','Service','Date','Amount','State','Actions'],
      bookings.map(b=>`<tr data-state="${b.state}">
        <td class="td-mono">#${b.id}</td><td>${b.consultantName}</td>
        <td>${b.serviceName}</td>
        <td class="td-mono text-sm">${fmtDate(b.slotStart)}</td>
        <td class="td-mono font-bold">${fmtMoney(b.amount)}</td>
        <td>${badge(b.state)}</td>
        <td class="flex gap-2">
          <button class="btn btn-xs btn-ghost" onclick="viewBooking('${b.id}')">Detail</button>
          ${b.state==='PENDING_PAYMENT'?`<button class="btn btn-xs btn-accent" onclick="openPayModal('${b.id}')">Pay</button>`:''}
          ${['REQUESTED','CONFIRMED'].includes(b.state)?`<button class="btn btn-xs btn-danger" onclick="cancelBooking('${b.id}')">Cancel</button>`:''}
        </td>
      </tr>`),
      'No bookings'
    )}
  </div>`;
  el.querySelector('table')?.setAttribute('id','bk-tbl');
}

async function renderPayments(el) {
  const payments = await apiSafe(`/payments/client/${session.clientId}`)||[];
  el.innerHTML=`<div class="card">
    <div class="card-header"><div class="card-title">Transaction History</div></div>
    ${tableOf(['Txn ID','Booking','Amount','Method','State','Date','Actions'],
      payments.map(p=>`<tr>
        <td class="td-mono">${p.transactionId}</td>
        <td class="td-mono">#${p.bookingId}</td>
        <td class="td-mono font-bold">${fmtMoney(p.amount)}</td>
        <td>${p.method.replace('_',' ')}</td>
        <td>${badge(p.state)}</td>
        <td class="td-mono text-sm">${fmtDate(p.createdAt)}</td>
        <td>${p.state==='SUCCESSFUL'?`<button class="btn btn-xs btn-ghost" onclick="refundPayment('${p.transactionId}')">Refund</button>`:''}</td>
      </tr>`),
      'No transactions yet'
    )}
  </div>`;
}

async function renderPayMethods(el) {
  const methods = await apiSafe(`/payments/methods/${session.clientId}`)||[];
  el.innerHTML=`
  <div class="section-header">
    <div><div class="section-title">Saved Payment Methods</div></div>
    <button class="btn btn-accent" onclick="openAddMethodModal()">＋ Add Method</button>
  </div>
  <div class="card">
    ${tableOf(['Type','Label','Details','Actions'],
      methods.map(m=>`<tr>
        <td>${m.type.replace('_',' ')}</td>
        <td>${m.label}</td>
        <td class="td-mono text-sm">${m.masked}</td>
        <td><button class="btn btn-xs btn-danger" onclick="removeMethod('${m.id}')">Remove</button></td>
      </tr>`),
      'No payment methods saved'
    )}
  </div>`;
}

function openAddMethodModal() {
  openModal('Add Payment Method',`
    <div class="form-group mb-4">
      <label>Type</label>
      <select id="pm-type" onchange="renderMethodFields()">
        <option value="CREDIT_CARD">Credit Card</option>
        <option value="DEBIT_CARD">Debit Card</option>
        <option value="PAYPAL">PayPal</option>
        <option value="BANK_ACCOUNT">Bank Account</option>
      </select>
    </div>
    <div class="form-group mb-4">
      <label>Label (e.g. "My Visa")</label>
      <input id="pm-label" placeholder="Label"/>
    </div>
    <div id="pm-fields"></div>`,
    `<button class="btn btn-ghost" onclick="closeModal()">Cancel</button>
     <button class="btn btn-accent" onclick="saveMethod()">Save Method</button>`
  );
  renderMethodFields();
}

function renderMethodFields() {
  const type = document.getElementById('pm-type')?.value;
  const el   = document.getElementById('pm-fields');
  if (!el) return;
  const card=`
    <div class="form-grid">
      <div class="form-group full"><label>Card Number (16 digits)</label><input id="pm-cardNum" placeholder="4111111111111111"/></div>
      <div class="form-group"><label>Expiry (MM/yy)</label><input id="pm-expiry" placeholder="12/27"/></div>
      <div class="form-group"><label>CVV</label><input id="pm-cvv" placeholder="123"/></div>
      <div class="form-group full"><label>Cardholder Name</label><input id="pm-name" placeholder="Full Name"/></div>
    </div>`;
  const paypal=`<div class="form-group"><label>PayPal Email</label><input id="pm-ppEmail" placeholder="you@paypal.com"/></div>`;
  const bank=`
    <div class="form-grid">
      <div class="form-group full"><label>Account Number (8-17 digits)</label><input id="pm-acct" placeholder="12345678"/></div>
      <div class="form-group"><label>Routing Number (9 digits)</label><input id="pm-routing" placeholder="021000021"/></div>
      <div class="form-group"><label>Bank Name</label><input id="pm-bank" placeholder="TD Bank"/></div>
    </div>`;
  el.innerHTML = type==='PAYPAL' ? paypal : type==='BANK_ACCOUNT' ? bank : card;
}

async function saveMethod() {
  const type  = document.getElementById('pm-type').value;
  const label = document.getElementById('pm-label').value;
  if (!label) { toast('Label required','error'); return; }
  const body = {clientId:session.clientId, type, label};
  if (type==='PAYPAL')        { body.paypalEmail=document.getElementById('pm-ppEmail')?.value; }
  else if (type==='BANK_ACCOUNT') {
    body.accountNumber=document.getElementById('pm-acct')?.value;
    body.routingNumber=document.getElementById('pm-routing')?.value;
    body.bankName=document.getElementById('pm-bank')?.value;
  } else {
    body.cardNumber=document.getElementById('pm-cardNum')?.value;
    body.expiryDate=document.getElementById('pm-expiry')?.value;
    body.cvv=document.getElementById('pm-cvv')?.value;
    body.cardHolderName=document.getElementById('pm-name')?.value;
  }
  setLoading(true);
  const r = await apiSafe('/payments/methods','POST',body);
  setLoading(false);
  if (r) { closeModal(); toast('Payment method saved','success'); navigate('pay-methods'); }
}

async function removeMethod(id) {
  setLoading(true);
  const r = await apiSafe(`/payments/methods/${session.clientId}/${id}`,'DELETE');
  setLoading(false);
  if (r) { toast('Method removed','info'); navigate('pay-methods'); }
}

async function openPayModal(bookingId) {
  const methods = await apiSafe(`/payments/methods/${session.clientId}`)||[];
  if (!methods.length) {
    openModal('No Payment Methods',
      '<p>You have no saved payment methods. Please add one first.</p>',
      `<button class="btn btn-ghost" onclick="closeModal()">Close</button>
       <button class="btn btn-accent" onclick="closeModal();navigate('pay-methods')">Add Method</button>`);
    return;
  }
  const bk = await apiSafe(`/bookings/${bookingId}`);
  openModal('Process Payment',`
    <div style="text-align:center;margin-bottom:18px">
      <div style="font-size:28px;font-weight:bold;color:#c0392b">${fmtMoney(bk?.amount||0)}</div>
      <div class="text-dim text-sm">Booking #${bookingId}</div>
    </div>
    <div class="form-group">
      <label>Payment Method</label>
      <select id="pay-method-sel">
        ${methods.map(m=>`<option value="${m.id}">${m.label} — ${m.masked}</option>`).join('')}
      </select>
    </div>`,
    `<button class="btn btn-ghost" onclick="closeModal()">Cancel</button>
     <button class="btn btn-accent" onclick="confirmPayment('${bookingId}')">💳 Pay ${fmtMoney(bk?.amount||0)}</button>`
  );
}

async function confirmPayment(bookingId) {
  const pmId = document.getElementById('pay-method-sel').value;
  setLoading(true);
  const r = await apiSafe('/payments/process','POST',{
    bookingId, paymentMethodId:pmId, clientId:session.clientId });
  setLoading(false);
  if (r) { closeModal(); toast('Payment successful! ✓','success'); navigate('my-bookings'); }
}

async function refundPayment(txnId) {
  const methods = await apiSafe(`/payments/methods/${session.clientId}`)||[];
  if (!methods.length) { toast('No payment method to refund to','error'); return; }
  setLoading(true);
  const r = await apiSafe('/payments/refund','POST',{
    transactionId:txnId, paymentMethodId:methods[0].id, clientId:session.clientId });
  setLoading(false);
  if (r) { toast('Refund processed','info'); navigate('payments'); }
}

async function renderConsultants(el) {
  const consultants = await apiSafe('/consultants/approved')||[];
  el.innerHTML=`
  <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(260px,1fr));gap:14px">
    ${consultants.map(c=>`<div class="card" style="padding:20px">
      <div class="flex items-center gap-3" style="margin-bottom:14px">
        <div class="user-avatar" style="width:38px;height:38px;font-size:14px">${c.name.split(' ').map(n=>n[0]).slice(0,2).join('')}</div>
        <div>
          <div style="font-weight:bold;font-size:14px">${c.name}</div>
          <div class="text-dim text-sm td-mono">${c.email}</div>
        </div>
      </div>
      <div class="text-dim text-sm" style="margin-bottom:10px">${c.availableSlots} available slot(s)</div>
      <button class="btn btn-sm btn-accent" style="width:100%" onclick="navigate('book')">Book Session</button>
    </div>`).join('')}
  </div>`;
}

// CHATBOT
let chatHistory = [];
let isSendingChat = false;  

async function renderChatbot(el) {
  chatHistory = [];
  isSendingChat = false;
  el.innerHTML=`
  <div style="max-width:680px">
    <div class="card">
      <div class="card-header">
        <div>
          <div class="card-title">🤖 AI Customer Assistant</div>
          <div class="text-dim text-sm">Powered by Groq AI · Ask me anything about ConsultHub</div>
        </div>
      </div>
      <div class="chat-container">
        <div class="chat-messages" id="chat-msgs">
          <div class="chat-msg bot">👋 Hi! I'm the ConsultHub AI assistant. I can help you with booking sessions, payment options, cancellation policies, and more. What would you like to know?</div>
        </div>
        <div class="chat-input-row">
          <input type="text" id="chat-input" placeholder="Ask a question…"/>
          <button class="btn btn-accent" id="chat-send-btn">Send</button>
        </div>
      </div>
      <div class="card-body" style="padding-top:12px">
        <div class="text-dim text-sm" style="margin-bottom:8px">Suggested questions:</div>
        <div style="display:flex;flex-wrap:wrap;gap:8px">
          ${['How do I book a session?','What payment methods are accepted?','Can I cancel my booking?','What services are available?'].map(q=>`
            <button class="btn btn-xs btn-ghost" data-q="${q}">${q}</button>`).join('')}
        </div>
      </div>
    </div>
  </div>`;

  // Attach events once after render — no inline handlers that can fire multiple times
  document.getElementById('chat-send-btn').addEventListener('click', sendChat);
  document.getElementById('chat-input').addEventListener('keydown', function(e) {
    if (e.key === 'Enter') { e.preventDefault(); sendChat(); }
  });
  el.querySelectorAll('[data-q]').forEach(btn => {
    btn.addEventListener('click', function() { askSuggested(this.dataset.q); });
  });
}

function appendChatMsg(cls, text) {
  const msgs = document.getElementById('chat-msgs');
  if (!msgs) return;
  const div = document.createElement('div');
  div.className = 'chat-msg ' + cls;
  div.textContent = text;
  msgs.appendChild(div);
  msgs.scrollTop = msgs.scrollHeight;
}

async function sendChat() {
  if (isSendingChat) return; 

  const input = document.getElementById('chat-input');
  const msg   = input?.value?.trim();
  if (!msg) return;

  isSendingChat = true;
  input.value = '';
  const sendBtn = document.getElementById('chat-send-btn');
  if (sendBtn) sendBtn.disabled = true;

  appendChatMsg('user', msg);

  const msgs = document.getElementById('chat-msgs');
  const typing = document.createElement('div');
  typing.className = 'chat-msg bot typing';
  typing.textContent = '…';
  if (msgs) { msgs.appendChild(typing); msgs.scrollTop = msgs.scrollHeight; }

  const r = await apiSafe('/chat','POST',{message:msg});
  typing.remove();
  appendChatMsg('bot', r?.reply || 'Sorry, I could not process that.');

  if (sendBtn) sendBtn.disabled = false;
  isSendingChat = false;
}

function askSuggested(q) {
  const input = document.getElementById('chat-input');
  if (input) input.value = q;
  sendChat();
}


// CONSULTANT PAGES
async function renderMySchedule(el) {
  const bookings = await apiSafe(`/bookings/consultant/${session.consultantId}`)||[];
  el.innerHTML=`<div class="card">
    ${tableOf(['ID','Client','Service','Date','Amount','State','Actions'],
      bookings.filter(b=>!['CANCELLED','REJECTED'].includes(b.state)).map(b=>`<tr>
        <td class="td-mono">#${b.id}</td><td>${b.clientName}</td><td>${b.serviceName}</td>
        <td class="td-mono text-sm">${fmtDate(b.slotStart)}</td>
        <td class="td-mono">${fmtMoney(b.amount)}</td>
        <td>${badge(b.state)}</td>
        <td class="flex gap-2">
          ${b.state==='REQUESTED'?`<button class="btn btn-xs btn-success" onclick="acceptBooking('${b.id}')">Accept</button><button class="btn btn-xs btn-danger" onclick="rejectBooking('${b.id}')">Reject</button>`:''}
          ${b.state==='PAID'?`<button class="btn btn-xs btn-success" onclick="completeBooking('${b.id}')">Complete</button>`:''}
          <button class="btn btn-xs btn-ghost" onclick="viewBooking('${b.id}')">Detail</button>
        </td>
      </tr>`),
      'No bookings'
    )}
  </div>`;
}

async function renderIncoming(el) {
  const bookings = await apiSafe(`/bookings/consultant/${session.consultantId}`)||[];
  const req = bookings.filter(b=>b.state==='REQUESTED');
  el.innerHTML=`<div class="card">
    <div class="card-header"><div class="card-title">Pending Requests (${req.length})</div></div>
    ${tableOf(['Client','Service','Date','Amount','Actions'],
      req.map(b=>`<tr>
        <td>${b.clientName}</td><td>${b.serviceName}</td>
        <td class="td-mono text-sm">${fmtDate(b.slotStart)}</td>
        <td class="td-mono font-bold">${fmtMoney(b.amount)}</td>
        <td class="flex gap-2">
          <button class="btn btn-xs btn-success" onclick="acceptBooking('${b.id}')">✓ Accept</button>
          <button class="btn btn-xs btn-danger" onclick="rejectBooking('${b.id}')">✕ Reject</button>
          <button class="btn btn-xs btn-ghost" onclick="viewBooking('${b.id}')">Detail</button>
        </td>
      </tr>`),
      'No pending requests — all caught up!'
    )}
  </div>`;
}

async function renderManageBookings(el) {
  const bookings = await apiSafe(`/bookings/consultant/${session.consultantId}`)||[];
  el.innerHTML=`<div class="card">
    ${tableOf(['ID','Client','Service','Date','Amount','State','Actions'],
      bookings.map(b=>`<tr>
        <td class="td-mono">#${b.id}</td><td>${b.clientName}</td><td>${b.serviceName}</td>
        <td class="td-mono text-sm">${fmtDate(b.slotStart)}</td>
        <td class="td-mono">${fmtMoney(b.amount)}</td>
        <td>${badge(b.state)}</td>
        <td class="flex gap-2">
          ${b.state==='REQUESTED'?`<button class="btn btn-xs btn-success" onclick="acceptBooking('${b.id}')">Accept</button><button class="btn btn-xs btn-danger" onclick="rejectBooking('${b.id}')">Reject</button>`:''}
          ${b.state==='PAID'?`<button class="btn btn-xs btn-success" onclick="completeBooking('${b.id}')">Done</button>`:''}
          <button class="btn btn-xs btn-ghost" onclick="viewBooking('${b.id}')">Detail</button>
        </td>
      </tr>`),
      'No bookings'
    )}
  </div>`;
}

async function renderAvailability(el) {
  const con   = await apiSafe(`/consultants/${session.consultantId}`);
  const slots = con?.slots||[];
  el.innerHTML=`
  <div class="card">
    <div class="card-header"><div class="card-title">My Availability Slots</div>
      <button class="btn btn-sm btn-accent" onclick="openAddSlotModal()">＋ Add Slot</button></div>
    ${tableOf(['Slot ID','Start','End','Status','Actions'],
      slots.map(s=>`<tr>
        <td class="td-mono">${s.id}</td>
        <td class="td-mono text-sm">${fmtDate(s.start)}</td>
        <td class="td-mono text-sm">${fmtDate(s.end)}</td>
        <td>${badge(s.available?'AVAILABLE':'BOOKED')}</td>
        <td>${s.available?`<button class="btn btn-xs btn-danger" onclick="removeSlot('${s.id}')">Remove</button>`:''}</td>
      </tr>`),
      'No slots added yet'
    )}
  </div>`;
}

function openAddSlotModal() {
  openModal('Add Availability Slot',`
    <div class="form-grid">
      <div class="form-group"><label>Start</label><input type="datetime-local" id="slot-start"/></div>
      <div class="form-group"><label>End</label><input type="datetime-local" id="slot-end"/></div>
    </div>`,
    `<button class="btn btn-ghost" onclick="closeModal()">Cancel</button>
     <button class="btn btn-accent" onclick="addSlot()">Add Slot</button>`
  );
}

async function addSlot() {
  const start = document.getElementById('slot-start').value;
  const end   = document.getElementById('slot-end').value;
  if (!start||!end) { toast('Fill both fields','error'); return; }
  setLoading(true);
  const r = await apiSafe(`/consultants/${session.consultantId}/slots`,'POST',{start,end});
  setLoading(false);
  if (r) { closeModal(); toast('Slot added','success'); navigate('availability'); }
}

async function removeSlot(slotId) {
  setLoading(true);
  const r = await apiSafe(`/consultants/${session.consultantId}/slots/${slotId}`,'DELETE');
  setLoading(false);
  if (r) { toast('Slot removed','info'); navigate('availability'); }
}



// ADMIN PAGES

async function renderPolicies(el) {
  const p = await apiSafe('/admin/policies')||{};
  el.innerHTML=`
  <div class="section-header">
    <div><div class="section-title">System Policies</div>
    <div class="section-sub">Command Pattern — configure platform-wide rules</div></div>
  </div>
  <div class="policy-grid">
    <div class="policy-card">
      <div class="policy-edit"><button class="btn btn-sm btn-ghost" onclick="editCancelPolicy(${p.cancellationHours})">Edit</button></div>
      <div class="policy-icon">⏱</div><div class="policy-name">Cancellation Policy</div>
      <div class="policy-value">${p.cancellationHours}h</div>
      <div class="policy-desc">Min hours before session start to cancel</div>
    </div>
    <div class="policy-card">
      <div class="policy-edit"><button class="btn btn-sm btn-ghost" onclick="editPricePolicy(${p.priceMin},${p.priceMax})">Edit</button></div>
      <div class="policy-icon">💵</div><div class="policy-name">Pricing Policy</div>
      <div class="policy-value">${fmtMoney(p.priceMin||0)} – ${fmtMoney(p.priceMax||1000)}</div>
      <div class="policy-desc">Allowed service price range</div>
    </div>
    <div class="policy-card">
      <div class="policy-edit"><button class="btn btn-sm btn-ghost" onclick="toggleNotif(${!p.notificationsEnabled})">${p.notificationsEnabled?'Disable':'Enable'}</button></div>
      <div class="policy-icon">🔔</div><div class="policy-name">Notifications</div>
      <div class="policy-value" style="font-size:18px;color:${p.notificationsEnabled?'#27ae60':'#c0392b'}">${p.notificationsEnabled?'✓ Enabled':'✕ Disabled'}</div>
      <div class="policy-desc">System-wide notification toggle</div>
    </div>
    <div class="policy-card">
      <div class="policy-icon">↩</div><div class="policy-name">Refund Policy</div>
      <div class="policy-value" style="font-size:18px">${p.refundPolicy}</div>
      <div class="policy-desc">Payment state eligible for refund</div>
    </div>
  </div>`;
}

function editCancelPolicy(cur) {
  openModal('Cancellation Policy',`<div class="form-group"><label>Minimum hours before session</label>
    <input type="number" id="cp-hrs" value="${cur}" min="0"/></div>`,
    `<button class="btn btn-ghost" onclick="closeModal()">Cancel</button>
     <button class="btn btn-accent" onclick="saveCancelPolicy()">Save</button>`);
}
async function saveCancelPolicy() {
  const h = parseInt(document.getElementById('cp-hrs').value);
  if (isNaN(h)||h<0) { toast('Invalid','error'); return; }
  setLoading(true);
  const r = await apiSafe('/admin/policies/cancellation','PUT',{hours:h});
  setLoading(false);
  if (r) { closeModal(); toast('Cancellation policy updated','success'); navigate('policies'); }
}

function editPricePolicy(mn, mx) {
  openModal('Pricing Policy',`<div class="form-grid">
    <div class="form-group"><label>Min Price ($)</label><input type="number" id="pp-min" value="${mn}"/></div>
    <div class="form-group"><label>Max Price ($)</label><input type="number" id="pp-max" value="${mx}"/></div>
  </div>`,
    `<button class="btn btn-ghost" onclick="closeModal()">Cancel</button>
     <button class="btn btn-accent" onclick="savePricePolicy()">Save</button>`);
}
async function savePricePolicy() {
  const mn = parseFloat(document.getElementById('pp-min').value);
  const mx = parseFloat(document.getElementById('pp-max').value);
  if (isNaN(mn)||isNaN(mx)||mx<mn) { toast('Invalid range','error'); return; }
  setLoading(true);
  const r = await apiSafe('/admin/policies/pricing','PUT',{min:mn,max:mx});
  setLoading(false);
  if (r) { closeModal(); toast('Pricing policy updated','success'); navigate('policies'); }
}

async function toggleNotif(val) {
  setLoading(true);
  await apiSafe('/admin/policies/notifications','PUT',{enabled:val});
  setLoading(false);
  toast(`Notifications ${val?'enabled':'disabled'}`,'info');
  navigate('policies');
}

async function renderAllConsultants(el) {
  const cons = await apiSafe('/consultants')||[];
  el.innerHTML=`<div class="card">
    ${tableOf(['Name','Email','Status','Slots','Actions'],
      cons.map(c=>`<tr>
        <td>${c.name}</td><td class="td-mono text-sm">${c.email}</td>
        <td>${badge(c.status)}</td><td class="td-mono">${c.slots?.length||0}</td>
        <td class="flex gap-2">
          ${c.status==='PENDING'?`<button class="btn btn-xs btn-success" onclick="approveConsultant('${c.id}')">Approve</button>
            <button class="btn btn-xs btn-danger" onclick="rejectConsultant('${c.id}')">Reject</button>`:''}
          ${c.status==='APPROVED'?`<span class="text-dim text-sm">Active</span>`:''}
          ${c.status==='REJECTED'?`<span class="text-dim text-sm">Rejected</span>`:''}
        </td>
      </tr>`)
    )}
  </div>`;
}

async function renderAllBookings(el) {
  const bookings = await apiSafe('/bookings')||[];
  el.innerHTML=`<div class="card">
    ${tableOf(['ID','Client','Consultant','Service','Date','Amount','State'],
      bookings.map(b=>`<tr>
        <td class="td-mono">#${b.id}</td>
        <td>${b.clientName}</td><td>${b.consultantName}</td><td>${b.serviceName}</td>
        <td class="td-mono text-sm">${fmtDate(b.slotStart)}</td>
        <td class="td-mono font-bold">${fmtMoney(b.amount)}</td>
        <td>${badge(b.state)}</td>
      </tr>`),
      'No bookings'
    )}
  </div>`;
}

async function renderAllPayments(el) {
  const [clients] = await Promise.all([apiSafe('/clients')||[]]);
  let allPayments = [];
  for (const c of clients) {
    const p = await apiSafe(`/payments/client/${c.id}`)||[];
    allPayments = allPayments.concat(p);
  }
  const total = allPayments.filter(p=>p.state==='SUCCESSFUL').reduce((a,p)=>a+p.amount,0);
  el.innerHTML=`
  <div class="stats-grid" style="margin-bottom:16px">
    <div class="stat-card"><div class="stat-label">Total Revenue</div><div class="stat-value">${fmtMoney(total)}</div></div>
    <div class="stat-card"><div class="stat-label">Successful</div><div class="stat-value">${allPayments.filter(p=>p.state==='SUCCESSFUL').length}</div></div>
    <div class="stat-card"><div class="stat-label">Refunded</div><div class="stat-value">${allPayments.filter(p=>p.state==='REFUNDED').length}</div></div>
    <div class="stat-card"><div class="stat-label">Total Txns</div><div class="stat-value">${allPayments.length}</div></div>
  </div>
  <div class="card">
    ${tableOf(['Txn ID','Booking','Client','Amount','Method','State','Date'],
      allPayments.map(p=>`<tr>
        <td class="td-mono">${p.transactionId}</td>
        <td class="td-mono">#${p.bookingId}</td>
        <td>${p.clientId}</td>
        <td class="td-mono font-bold">${fmtMoney(p.amount)}</td>
        <td>${p.method.replace('_',' ')}</td>
        <td>${badge(p.state)}</td>
        <td class="td-mono text-sm">${fmtDate(p.createdAt)}</td>
      </tr>`),
      'No payments yet'
    )}
  </div>`;
}

// SHARED ACTIONS
async function viewBooking(id) {
  const b = await apiSafe(`/bookings/${id}`);
  if (!b) return;
  openModal(`Booking #${b.id}`,`
    ${bookingTimeline(b.state)}
    <div class="divider"></div>
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;font-size:13px">
      <div><span class="text-dim">Client:</span><br><strong>${b.clientName}</strong></div>
      <div><span class="text-dim">Consultant:</span><br><strong>${b.consultantName}</strong></div>
      <div><span class="text-dim">Service:</span><br><strong>${b.serviceName}</strong></div>
      <div><span class="text-dim">Amount:</span><br><strong style="font-size:20px;color:#c0392b">${fmtMoney(b.amount)}</strong></div>
      <div><span class="text-dim">Session:</span><br><strong class="td-mono">${fmtDate(b.slotStart)}</strong></div>
      <div><span class="text-dim">Booked:</span><br><strong class="td-mono">${fmtDate(b.createdAt)}</strong></div>
    </div>`,
    `<button class="btn btn-ghost" onclick="closeModal()">Close</button>
     ${b.state==='PENDING_PAYMENT'?`<button class="btn btn-accent" onclick="closeModal();openPayModal('${b.id}')">Pay Now</button>`:''}
     ${['REQUESTED','CONFIRMED'].includes(b.state)?`<button class="btn btn-danger" onclick="closeModal();cancelBooking('${b.id}')">Cancel</button>`:''}`
  );
}

async function cancelBooking(id) {
  setLoading(true);
  const r = await apiSafe(`/bookings/${id}/cancel`,'PUT');
  setLoading(false);
  if (r) { toast('Booking cancelled','info'); navigate(session.page); }
}

async function acceptBooking(id) {
  setLoading(true);
  const r = await apiSafe(`/bookings/${id}/accept`,'PUT');
  setLoading(false);
  if (r) { toast('Booking accepted — awaiting payment','success'); navigate(session.page); }
}

async function rejectBooking(id) {
  setLoading(true);
  const r = await apiSafe(`/bookings/${id}/reject`,'PUT');
  setLoading(false);
  if (r) { toast('Booking rejected','info'); navigate(session.page); }
}

async function completeBooking(id) {
  setLoading(true);
  const r = await apiSafe(`/bookings/${id}/complete`,'PUT');
  setLoading(false);
  if (r) { toast('Booking completed!','success'); navigate(session.page); }
}

async function approveConsultant(id) {
  setLoading(true);
  const r = await apiSafe(`/consultants/${id}/approve`,'PUT');
  setLoading(false);
  if (r) { toast('Consultant approved','success'); navigate(session.page); }
}

async function rejectConsultant(id) {
  setLoading(true);
  const r = await apiSafe(`/consultants/${id}/reject`,'PUT');
  setLoading(false);
  if (r) { toast('Consultant rejected','info'); navigate(session.page); }
}

// TABLE FILTERS
function filterTbl(input, tblId) {
  const q = input.value.toLowerCase();
  const t = document.getElementById(tblId)||input.closest('.card')?.querySelector('table');
  if (!t) return;
  t.querySelectorAll('tbody tr').forEach(tr=>{
    tr.style.display = tr.textContent.toLowerCase().includes(q) ? '' : 'none';
  });
}

function filterState(sel, tblId) {
  const v = sel.value;
  const t = document.getElementById(tblId)||sel.closest('.filter-bar')?.nextElementSibling?.querySelector('table');
  if (!t) return;
  t.querySelectorAll('tbody tr').forEach(tr=>{
    tr.style.display = (!v || tr.dataset.state===v) ? '' : 'none';
  });
}

// INIT
renderNav();
navigate('dashboard');

