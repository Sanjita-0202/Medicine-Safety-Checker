(() => {
  'use strict';
  const scenarios = {
    safe: { name: 'Amoxicillin', dose: '500', times: '2', allergies: '', current: [], label: 'Safe dose' },
    allergy: { name: 'Amoxicillin', dose: '500', times: '2', allergies: 'penicillin', current: [], label: 'Allergy conflict' },
    overdose: { name: 'Amoxicillin', dose: '2000', times: '2', allergies: '', current: [], label: 'Overdose risk' },
    interaction: { name: 'Levofloxacin', dose: '500', times: '1', allergies: '', current: ['Aspirin'], label: 'Drug interaction' }
  };
  const patients = {
    allergy: { name: 'Maya Shah', age: '32', gender: 'Female', weight: '60', allergies: 'penicillin' },
    general: { name: 'Arjun Rao', age: '40', gender: 'Male', weight: '72', allergies: '' },
    nsaid: { name: 'Neha Mehta', age: '28', gender: 'Female', weight: '55', allergies: 'NSAID' }
  };
  const input = (name) => document.querySelector(`[name="${name}"]`);
  const set = (name, value) => { const field = input(name); if (field) { field.value = value; field.dispatchEvent(new Event('input', { bubbles: true })); } };
  const announce = (message) => { const target = document.querySelector('[data-sample-status]'); if (target) target.textContent = message; };

  const preview = document.querySelector('[data-dose-summary]');
  const updateDose = () => {
    if (!preview) return;
    const dose = Number(input('dose')?.value), times = Number(input('timesPerDay')?.value);
    const total = dose * times;
    preview.textContent = Number.isFinite(total) && dose > 0 && Number.isInteger(times) && times > 0
      ? `Daily total: ${new Intl.NumberFormat(undefined, { maximumFractionDigits: 3 }).format(total)} mg across ${times} intake${times === 1 ? '' : 's'}. Run the check for a safety result.`
      : 'Enter a positive dose and whole-number frequency to preview the daily total.';
  };
  input('dose')?.addEventListener('input', updateDose);
  input('timesPerDay')?.addEventListener('input', updateDose);
  updateDose();

  const checks = [...document.querySelectorAll('[name="currentMedicineNames"]')];
  const updateCount = () => { const count = document.querySelector('[data-selected-count]'); if (count) count.textContent = `${checks.filter(field => field.checked).length} medicines selected`; };
  checks.forEach(field => field.addEventListener('change', updateCount));
  updateCount();
  document.querySelector('[data-medicine-filter]')?.addEventListener('input', (event) => {
    const query = event.target.value.trim().toLocaleLowerCase();
    checks.forEach(field => { field.closest('label').hidden = !field.value.toLocaleLowerCase().includes(query); });
  });

  function fillScenario(key) {
    const scenario = scenarios[key];
    if (!scenario || !input('newMedicineName')) return;
    set('newMedicineName', scenario.name); set('dose', scenario.dose); set('timesPerDay', scenario.times); set('allergies', scenario.allergies);
    checks.forEach(field => { field.checked = scenario.current.includes(field.value); field.closest('label').hidden = false; });
    const filter = document.querySelector('[data-medicine-filter]'); if (filter) filter.value = '';
    updateCount();
    announce(`${scenario.label} example filled. Review the form and press Check to run it.`);
  }
  function fillPatient(key) {
    const patient = patients[key];
    if (!patient || !document.querySelector('[data-patient-sample]')) return;
    Object.entries(patient).forEach(([name, value]) => set(name, value));
    announce(`${patient.name}'s details are filled. Press Add patient when you are ready to save.`);
  }
  document.querySelectorAll('[data-scenario]').forEach(button => button.addEventListener('click', () => fillScenario(button.dataset.scenario)));
  document.querySelectorAll('[data-patient-sample]').forEach(button => button.addEventListener('click', () => fillPatient(button.dataset.patientSample)));
  const params = new URLSearchParams(location.search);
  fillScenario(params.get('scenario')); fillPatient(params.get('sample'));

  const search = document.querySelector('[data-patient-search]');
  if (search) {
    const rows = [...document.querySelectorAll('[data-patient-row]')];
    search.addEventListener('input', () => {
      const query = search.value.trim().toLocaleLowerCase();
      let visible = 0;
      rows.forEach(row => {
        const name = row.querySelector('td')?.textContent || '';
        row.hidden = !(name + ' ' + row.dataset.patientId).toLocaleLowerCase().includes(query);
        if (!row.hidden) visible++;
      });
      const count = document.querySelector('[data-results-count]');
      if (count) count.textContent = `${visible} of ${rows.length} patients shown`;
      const empty = document.querySelector('[data-search-empty]');
      if (empty) empty.hidden = visible > 0 || rows.length === 0;
    });
  }
  document.querySelector('[data-toggle-password]')?.addEventListener('click', (event) => {
    const password = document.getElementById('password');
    const reveal = password.type === 'password';
    password.type = reveal ? 'text' : 'password';
    event.currentTarget.textContent = reveal ? 'Hide' : 'Show';
    event.currentTarget.setAttribute('aria-pressed', String(reveal));
  });
  document.querySelectorAll('[data-dismiss-toast]').forEach(button => button.addEventListener('click', () => button.closest('.toast').remove()));

  const dialog = document.getElementById('stop-dialog');
  let pendingStop = null;
  if (dialog && typeof dialog.showModal === 'function') {
    document.querySelectorAll('form[data-confirm-stop]').forEach(form => form.addEventListener('submit', event => {
      if (form.dataset.confirmed === 'true') return;
      event.preventDefault();
      pendingStop = form;
      dialog.querySelector('[data-stop-message]').textContent = `${form.querySelector('[name="medicineName"]').value} will be removed from active medicines. A STOPPED event will be saved in history.`;
      dialog.showModal();
      dialog.querySelector('[data-stop-cancel]').focus();
    }));
    dialog.querySelector('[data-stop-cancel]').addEventListener('click', () => { pendingStop = null; dialog.close(); });
    dialog.addEventListener('cancel', () => { pendingStop = null; });
    dialog.querySelector('[data-stop-confirm]').addEventListener('click', () => {
      const form = pendingStop; pendingStop = null; dialog.close();
      if (form) { form.dataset.confirmed = 'true'; form.requestSubmit(); }
    });
  }
  document.querySelectorAll('form[method="post"]').forEach(form => form.addEventListener('submit', event => {
    if (event.defaultPrevented || !form.checkValidity()) return;
    const submit = form.querySelector('button[type="submit"]');
    if (submit) { submit.disabled = true; submit.textContent = 'Please wait…'; }
  }));
  window.addEventListener('pageshow', event => {
    if (event.persisted) location.reload();
  });
})();
