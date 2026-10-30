document.addEventListener('DOMContentLoaded', () => {
    const fileInput = document.getElementById('imagenFile');
    const urlInput = document.getElementById('imagenUrl');
    const preview = document.getElementById('imagenPreview');
    const previewWrap = document.getElementById('imagenPreviewWrap');
    const error = document.getElementById('imagenFileError');

    if (!fileInput || !urlInput || !preview || !previewWrap || !error) {
        return;
    }

    const maxBytes = 5 * 1024 * 1024;
    const allowedTypes = new Set(['image/jpeg', 'image/png', 'image/webp']);

    function showPreview(source) {
        if (source) {
            preview.src = source;
            previewWrap.classList.remove('d-none');
        } else {
            preview.removeAttribute('src');
            previewWrap.classList.add('d-none');
        }
    }

    function showUrlPreview() {
        if (fileInput.files.length > 0) {
            return;
        }
        const value = urlInput.value.trim();
        const isHttpUrl = /^https?:\/\//i.test(value);
        showPreview(isHttpUrl ? value : '');
    }

    fileInput.addEventListener('change', () => {
        error.textContent = '';
        const file = fileInput.files[0];
        if (!file) {
            showUrlPreview();
            return;
        }
        if (file.size > maxBytes) {
            error.textContent = fileInput.dataset.errorSize;
            fileInput.value = '';
            showUrlPreview();
            return;
        }
        if (file.type && !allowedTypes.has(file.type)) {
            error.textContent = fileInput.dataset.errorType;
            fileInput.value = '';
            showUrlPreview();
            return;
        }

        const reader = new FileReader();
        reader.onload = function (event) {
            showPreview(event.target.result);
        };
        reader.readAsDataURL(file);
    });

    urlInput.addEventListener('input', showUrlPreview);
});
