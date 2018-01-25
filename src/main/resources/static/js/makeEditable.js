var editBtn = document.getElementById('editBtn');
var saveBtn = document.getElementById('saveBtn');
var textAreaHtml = document.getElementsByTagName("textarea");
var globalEditor;

editBtn.addEventListener('click', function (e) {
    globalEditor = $('.CodeMirror')[0].CodeMirror;
    globalEditor.readOnly = false;
    textAreaHtml[0].removeAttribute('readonly');
});

saveBtn.addEventListener('click', function (e) {
    globalEditor.readOnly = true;
    textAreaHtml[0].setAttribute('readonly', true);
});