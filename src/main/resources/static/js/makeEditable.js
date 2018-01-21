var editBtn = document.getElementById('editBtn');
var saveBtn = document.getElementById('saveBtn');
var editables = document.querySelectorAll(".tab-content");

editBtn.addEventListener('click', function (e) {
    if (!editables[0].isContentEditable) {
        editables[0].contentEditable = 'true';
    }
});

saveBtn.addEventListener('click', function (e) {
    if (editables[0].isContentEditable) {
        editables[0].contentEditable = 'false';
    }
});