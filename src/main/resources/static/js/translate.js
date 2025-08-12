document.getElementById('translateForm').addEventListener('submit', async function (e) {
    e.preventDefault();
    const fileInput = document.getElementById('file');
    const targetLang = document.getElementById('targetLang').value;
    if (!fileInput.files.length) return;
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    formData.append('targetLang', targetLang);
    document.getElementById('loading').style.display = 'block';
    const res = await fetch('/translate', {
        method: 'POST',
        body: formData
    });
    document.getElementById('loading').style.display = 'none';
    if (res.ok) {
        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const link = document.getElementById('downloadLink');
        link.href = url;
        link.download = 'translated_' + fileInput.files[0].name;
        document.getElementById('result').style.display = 'block';
    } else {
        alert('Translation failed');
    }
});
