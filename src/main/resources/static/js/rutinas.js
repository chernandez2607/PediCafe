document.addEventListener('DOMContentLoaded', function () {
    const confirmModal = document.getElementById('confirmModal');
    if (!confirmModal) {
        return;
    }
    confirmModal.addEventListener('show.bs.modal', function (event) {
        const button = event.relatedTarget;
        if (!button) {
            return;
        }
        document.getElementById('modalId').value = button.getAttribute('data-bs-id');
        document.getElementById('modalDescripcion').textContent = button.getAttribute('data-bs-descripcion');
    });
});
