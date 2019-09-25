
function submitSuccessful() {
    $("#modelProfileSaved").modal('show');
}

function submitFail(errMsg) {
    $("#htmlError").html(errMsg.error);
    $("#htmlErrorDescription").html(errMsg.error_description);
    $("#alertSubmitFailed").show();
}
$(function () {
    $('#modelProfileSaved').on('hidden.bs.modal', function () {
        goto("/c2id/basic/profile")
    });
});
