var editBtn = document.getElementById('editBtn');
var saveBtn = document.getElementById('saveBtn');
var textAreaHtml = document.getElementsByTagName("textarea");

editBtn.addEventListener('click', function (e) {
    textAreaHtml[0].removeAttribute('readonly');
});

saveBtn.addEventListener('click', function (e) {
    textAreaHtml[0].setAttribute('readonly', true);
});