var editor;

function createEditor (data) {
    editor = CodeMirror.fromTextArea(myTextarea, {
        mode: "text/x-apex",
        extraKeys: {"Ctrl-Q": "autocomplete"},
        hint: CodeMirror.hint.sql,
        hintOptions: {
            tables: data ? data : {}
        }
    })
}

(function createEditorWithRemoteData () {
    $.ajax({
        type:'POST',
        dataType:'json',
        url:'/apex/getMethodSuggestion',
        success:createEditor,
        error:function () {}
    })
})();