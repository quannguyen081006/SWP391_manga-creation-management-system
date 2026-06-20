(function () {
    'use strict';

    document.addEventListener('click', function (event) {
        var target = event.target;
        var button = null;
        while (target && target !== document) {
            if (target.getAttribute && target.getAttribute('data-salary-toggle') !== null) {
                button = target;
                break;
            }
            target = target.parentNode;
        }
        if (!button) {
            return;
        }
        event.preventDefault();

        var targetId = button.getAttribute('data-salary-target');
        if (!targetId) {
            targetId = 'task-detail-' + button.getAttribute('data-salary-toggle');
        }
        var row = targetId ? document.getElementById(targetId) : null;
        if (!row) {
            return;
        }
        var isOpen = row.classList.toggle('is-open');
        var indicator = button.querySelector('.salary-task-chevron');
        if (indicator) {
            indicator.textContent = isOpen ? '-' : '+';
        } else {
            button.textContent = isOpen ? '-' : '+';
        }
        button.setAttribute('aria-expanded', String(isOpen));
    });
})();
