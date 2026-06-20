(function () {
    'use strict';

    document.addEventListener('click', function (event) {
        var button = event.target.closest('[data-salary-toggle]');
        if (!button) {
            return;
        }
        var assistantId = button.getAttribute('data-salary-toggle');
        var row = document.getElementById('task-detail-' + assistantId);
        if (!row) {
            return;
        }
        var isOpen = row.classList.toggle('is-open');
        button.textContent = isOpen ? 'v' : '>';
        button.setAttribute('aria-expanded', String(isOpen));
    });
})();
