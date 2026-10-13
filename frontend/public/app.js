const API = window.API_URL || 'http://localhost:8080/api';
let token = localStorage.getItem('ems_token');
let current = 'dashboard';
const $ = (s) => document.querySelector(s);
const money = (n) => '₹' + Number(n || 0).toLocaleString('en-IN');
const esc = (s) => String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));

async function api(path, method = 'GET', body) {
  const res = await fetch(API + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: 'Bearer ' + token } : {}) },
    body: body ? JSON.stringify(body) : undefined
  });
  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (res.status === 401 && path !== '/auth/login') { logout(); throw new Error('Session expired, please login'); }
  if (!res.ok) throw new Error((data && data.message) || 'Request failed');
  return data;
}

function toast(msg, err) {
  const t = $('#toast'); t.textContent = msg; t.className = 'toast' + (err ? ' err' : ''); t.hidden = false;
  setTimeout(() => t.hidden = true, 3000);
}
async function run(fn) { try { await fn(); } catch (e) { toast(e.message, true); } }

// ---------- Generic modal form ----------
function openForm(title, fields, onSubmit, submitLabel = 'Save') {
  const f = $('#modal-form');
  f.innerHTML = `<h3>${esc(title)}</h3>` + fields.map(x => {
    const input = x.options
      ? `<select name="${x.name}">${x.options.map(o => `<option value="${esc(o.value)}" ${String(o.value) === String(x.value ?? '') ? 'selected' : ''}>${esc(o.label)}</option>`).join('')}</select>`
      : `<input name="${x.name}" type="${x.type || 'text'}" value="${esc(x.value ?? '')}" ${x.required ? 'required' : ''} ${x.step ? `step="${x.step}"` : ''}>`;
    return `<label>${esc(x.label)}${input}</label>`;
  }).join('') + `<div class="row"><button type="button" class="btn" id="m-cancel">Cancel</button><button class="btn primary" type="submit">${submitLabel}</button></div>`;
  f.onsubmit = (e) => {
    e.preventDefault();
    const v = Object.fromEntries(new FormData(f).entries());
    $('#modal').close();
    run(() => onSubmit(v));
  };
  $('#m-cancel').onclick = () => $('#modal').close();
  $('#modal').showModal();
}

// ---------- Auth ----------
$('#login-form').onsubmit = async (e) => {
  e.preventDefault();
  $('#login-err').textContent = '';
  try {
    const r = await api('/auth/login', 'POST', { username: $('#l-user').value.trim(), password: $('#l-pass').value });
    token = r.token; localStorage.setItem('ems_token', token);
    showApp();
  } catch (err) { $('#login-err').textContent = err.message; }
};
function logout() {
  if (token) fetch(API + '/auth/logout', { method: 'POST', headers: { Authorization: 'Bearer ' + token } }).catch(() => {});
  token = null; localStorage.removeItem('ems_token');
  $('#app-view').hidden = true; $('#login-view').hidden = false;
}
$('#logout').onclick = logout;

function showApp() {
  $('#login-view').hidden = true; $('#app-view').hidden = false;
  navigate(current);
}
document.querySelectorAll('nav a').forEach(a => a.onclick = () => navigate(a.dataset.view));
function navigate(view) {
  current = view;
  document.querySelectorAll('nav a').forEach(a => a.classList.toggle('active', a.dataset.view === view));
  run(() => ({ dashboard, employees, departments, salaries })[view]());
}

// ---------- Dashboard ----------
async function dashboard() {
  const d = await api('/dashboard');
  $('#view').innerHTML = `
    <div class="head"><h1>Dashboard</h1><span class="muted">${esc(d.month)}</span></div>
    <div class="stats">
      <div class="card stat"><span class="muted">Employees</span><b>${d.totalEmployees}</b></div>
      <div class="card stat"><span class="muted">Departments</span><b>${d.totalDepartments}</b></div>
      <div class="card stat"><span class="muted">Monthly payroll</span><b>${money(d.monthlyPayroll)}</b></div>
      <div class="card stat"><span class="muted">Paid this month</span><b>${money(d.paidThisMonth)}</b></div>
    </div>`;
}

// ---------- Employees ----------
let deptCache = [], empCache = [];
async function employees() {
  [empCache, deptCache] = await Promise.all([api('/employees'), api('/departments')]);
  $('#view').innerHTML = `
    <div class="head"><h1>Employees</h1><button class="btn primary" id="add-emp">+ Add employee</button></div>
    <div class="card table-wrap"><table>
      <tr><th>Name</th><th>Email</th><th>Phone</th><th>Designation</th><th>Department</th><th>Joined</th><th>Salary</th><th>Actions</th></tr>
      ${empCache.map(e => `<tr>
        <td>${esc(e.name)}</td><td>${esc(e.email)}</td><td>${esc(e.phone)}</td><td>${esc(e.designation)}</td>
        <td>${esc(e.department?.name || '—')}</td><td>${esc(e.joiningDate)}</td><td>${money(e.baseSalary)}</td>
        <td class="actions">
          <button class="btn sm" data-a="edit" data-id="${e.id}">Edit</button>
          <button class="btn sm" data-a="assign" data-id="${e.id}">Assign</button>
          <button class="btn sm" data-a="salary" data-id="${e.id}">Set salary</button>
          <button class="btn sm primary" data-a="pay" data-id="${e.id}">Pay</button>
          <button class="btn sm danger" data-a="remove" data-id="${e.id}">Remove</button>
        </td></tr>`).join('') || '<tr><td colspan="8" class="muted">No employees yet</td></tr>'}
    </table></div>`;
  $('#add-emp').onclick = () => empForm();
  document.querySelectorAll('[data-a]').forEach(b => b.onclick = () => {
    const e = empCache.find(x => x.id == b.dataset.id);
    ({ edit: empForm, assign: assignForm, salary: salaryForm, pay: payForm, remove: removeEmp })[b.dataset.a](e);
  });
}
const deptOptions = () => [{ value: '', label: '— None —' }, ...deptCache.map(d => ({ value: d.id, label: d.name }))];

function empForm(e) {
  const f = [
    { name: 'name', label: 'Full name', value: e?.name, required: true },
    { name: 'email', label: 'Email', type: 'email', value: e?.email, required: true },
    { name: 'phone', label: 'Phone', value: e?.phone },
    { name: 'designation', label: 'Designation', value: e?.designation },
    { name: 'joiningDate', label: 'Joining date', type: 'date', value: e?.joiningDate }
  ];
  if (!e) {
    f.push({ name: 'departmentId', label: 'Department', options: deptOptions() });
    f.push({ name: 'baseSalary', label: 'Base salary (monthly)', type: 'number', step: '0.01', value: 0 });
  }
  openForm(e ? 'Edit employee' : 'Add employee', f, async (v) => {
    const body = { name: v.name, email: v.email, phone: v.phone, designation: v.designation, joiningDate: v.joiningDate || null };
    if (e) await api('/employees/' + e.id, 'PUT', body);
    else await api('/employees', 'POST', { ...body, baseSalary: v.baseSalary || 0, department: v.departmentId ? { id: Number(v.departmentId) } : null });
    toast(e ? 'Employee updated' : 'Employee added'); employees();
  });
}
function assignForm(e) {
  openForm('Assign — ' + e.name, [
    { name: 'departmentId', label: 'Department', options: deptOptions(), value: e.department?.id },
    { name: 'designation', label: 'Designation', value: e.designation }
  ], async (v) => {
    await api(`/employees/${e.id}/assign`, 'PUT', { departmentId: v.departmentId || null, designation: v.designation });
    toast('Assignment updated'); employees();
  });
}
function salaryForm(e) {
  openForm('Set salary — ' + e.name, [
    { name: 'baseSalary', label: 'Base salary (monthly)', type: 'number', step: '0.01', value: e.baseSalary, required: true }
  ], async (v) => {
    await api(`/employees/${e.id}/salary`, 'PUT', { baseSalary: v.baseSalary });
    toast('Salary updated'); employees();
  });
}
function payForm(e) {
  const month = new Date().toISOString().slice(0, 7);
  openForm(`Pay salary — ${e.name} (base ${money(e.baseSalary)})`, [
    { name: 'month', label: 'Month', type: 'month', value: month, required: true },
    { name: 'bonus', label: 'Bonus', type: 'number', step: '0.01', value: 0 },
    { name: 'deductions', label: 'Deductions', type: 'number', step: '0.01', value: 0 }
  ], async (v) => {
    const p = await api('/salaries/pay', 'POST', { employeeId: e.id, ...v });
    toast('Paid ' + money(p.netAmount) + ' to ' + e.name); employees();
  }, 'Pay now');
}
async function removeEmp(e) {
  if (!confirm(`Remove ${e.name}? Their salary history will also be deleted.`)) return;
  await api('/employees/' + e.id, 'DELETE');
  toast('Employee removed'); employees();
}

// ---------- Departments ----------
async function departments() {
  const list = await api('/departments');
  $('#view').innerHTML = `
    <div class="head"><h1>Departments</h1><button class="btn primary" id="add-dept">+ Add department</button></div>
    <div class="card table-wrap"><table>
      <tr><th>ID</th><th>Name</th><th>Actions</th></tr>
      ${list.map(d => `<tr><td>${d.id}</td><td>${esc(d.name)}</td>
        <td><button class="btn sm danger" data-del="${d.id}">Delete</button></td></tr>`).join('')}
    </table></div>`;
  $('#add-dept').onclick = () => openForm('Add department', [{ name: 'name', label: 'Name', required: true }], async (v) => {
    await api('/departments', 'POST', { name: v.name }); toast('Department added'); departments();
  });
  document.querySelectorAll('[data-del]').forEach(b => b.onclick = () => run(async () => {
    if (!confirm('Delete this department?')) return;
    await api('/departments/' + b.dataset.del, 'DELETE'); toast('Department deleted'); departments();
  }));
}

// ---------- Salary payments ----------
async function salaries() {
  const list = await api('/salaries');
  $('#view').innerHTML = `
    <div class="head"><h1>Salary Payments</h1></div>
    <div class="card table-wrap"><table>
      <tr><th>Month</th><th>Employee</th><th>Base</th><th>Bonus</th><th>Deductions</th><th>Net paid</th><th>Paid on</th></tr>
      ${list.map(p => `<tr><td>${esc(p.month)}</td><td>${esc(p.employee?.name)}</td><td>${money(p.baseSalary)}</td>
        <td>${money(p.bonus)}</td><td>${money(p.deductions)}</td><td><b>${money(p.netAmount)}</b></td><td>${esc(p.paidDate)}</td></tr>`).join('')
        || '<tr><td colspan="7" class="muted">No payments yet</td></tr>'}
    </table></div>`;
}

if (token) showApp();
