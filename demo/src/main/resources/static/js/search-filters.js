document.addEventListener('DOMContentLoaded', () => {
    initMultiSelects();
    initFilterFormSubmit();
});

function syncMultipleInputs(form, fieldName, values) {
    form.querySelectorAll(`input[type="hidden"][name="${fieldName}"]`).forEach(input => input.remove());

    values.forEach(val => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = fieldName;
        input.value = val;
        form.appendChild(input);
    });
}

function initMultiSelects() {
    const form = document.getElementById('filterForm');
    if (!form) return;

    const setupDropdown = (dropdownId, fieldName) => {
        const dropdown = document.getElementById(dropdownId);
        if (!dropdown) return;

        dropdown.querySelectorAll('input[type="checkbox"]').forEach(checkbox => {
            checkbox.addEventListener('change', () => {
                const selectedValues = Array.from(
                    dropdown.querySelectorAll('input[type="checkbox"]:checked')
                ).map(cb => cb.value);

                syncMultipleInputs(form, fieldName, selectedValues);
            });
        });
    };

    setupDropdown('genresDropdown', 'genres');
    setupDropdown('directorsDropdown', 'directors');
}

function calculateRangeMinutes() {
    const hoursFrom = Number(document.getElementById('durationHoursFrom')?.value || 0);
    const minsFrom = Number(document.getElementById('durationMinsFrom')?.value || 0);
    const hoursTo = Number(document.getElementById('durationHoursTo')?.value || 0);
    const minsTo = Number(document.getElementById('durationMinsTo')?.value || 0);

    const minInput = document.getElementById('minDuration');
    const maxInput = document.getElementById('maxDuration');

    if (minInput) minInput.value = (hoursFrom * 60 + minsFrom) || '';
    if (maxInput) maxInput.value = (hoursTo * 60 + minsTo) || '';
}

function initFilterFormSubmit() {
    const filterForm = document.getElementById('filterForm');
    if (!filterForm) return;

    filterForm.addEventListener('submit', (event) => {
        calculateRangeMinutes();

        const from = Number(document.getElementById('minDuration')?.value);
        const to = Number(document.getElementById('maxDuration')?.value);

        if (from && to && from > to) {
            event.preventDefault();
            if (typeof showFlashMessage === 'function') {
                showFlashMessage('Минимальная длительность не может быть больше максимальной', 'error');
            } else {
                alert('Минимальная длительность не может быть больше максимальной');
            }
        }
    });
}