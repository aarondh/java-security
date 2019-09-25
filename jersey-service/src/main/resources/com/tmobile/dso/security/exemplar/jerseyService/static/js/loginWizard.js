function getRedirectUri() {
    var redirectUri = $.urlParam("see");
    if(!redirectUri){
        redirectUri = "/c2id/basic/index";
    }
    return redirectUri;
}

/**
 * Do name and password login
 */
function login() {
    startProgress();
    var loginRequest = {
        email: $("#txtEmail").val(),
        password: $("#txtPassword").val(),
        issuer: $("#txtIssuer").val(),
        redirectUri: getRedirectUri()
    };
    postJson("/c2id/api/auth/login",
        loginRequest,
        (data) => {
            stopProgress();
            localStorage["bearer-token"] = data.token;
            localStorage["given-name"] = data.givenName;
            localStorage["family-name"] = data.familyName;
            localStorage["preferred-username"] = data.preferredUsername;
            localStorage["user-id"] = data.userId;
            setCookie(data.cookie);
            var redirectPath = data.redirectUri;
            if (!data.redirectUri) {
                data.redirectUri = getRedirectUri();
            }
            window.location.href = data.redirectUri;
        }, () => {
            stopProgress();
            $("#txtFailedPassword").val(loginRequest.password)
            showLoginFailed();
        }, "#busyLogin"
    );
}
function sendAccountCreation() {
    startProgress();

    var createAccountRequest = {
        givenName: $("#txtNewGivenName").val(),
        familyName: $("#txtNewFamilyName").val(),
        preferredUserName: $("#txtNewPreferredUsername").val(),
        email: $("#txtNewEmail").val(),
        password: $("#txtNewPassword").val(),
    };
    postJson("/c2id/api/auth/createAccount",
        createAccountRequest,
        (data) => {
            stopProgress();
            var redirectPath = data.redirectUri;
            if (!data.redirectUri) {
                data.redirectUri = getRedirectUri();
            }
            localStorage["bearer-token"] = data.token;
            localStorage["given-name"] = data.givenName;
            localStorage["family-name"] = data.familyName;
            localStorage["preferred-username"] = data.preferredUsername;
            localStorage["user-id"] = data.userId;
            setCookie(data.cookie);
            window.location.href = data.redirectUri;
        }, () => {
            stopProgress();
            showAccountCreationFailed();
        }, "#busyLogin"
    );
}
function sendPasswordReset() {
    startProgress();

    var passwordRequestRequest = {
        email: $("#txtEmail").val()
    };
    postJson("/c2id/api/auth/passwordReset",
    passwordRequestRequest,
        (data) => {
            stopProgress();
        }, () => {
            stopProgress();
        });
}
/**
 * Do openid based login
 */
function openidLogin() {
    startProgress();

    $("txtIssuerLabel").val($("#txtIssuer").val());
    $("#txtOauthEmail").val($("#txtEmail").val());
    $("#txtOauthRedirectUri").val(getRedirectUri());
    $("#formAuthLogin").submit();

}
function checkEmail() {
    startProgress();
    var checkEmailRequest = {
        email: $("#txtEmail").val(),
    };
    postJson("/c2id/api/auth/checkemail",
        checkEmailRequest,
        (data) => {
            stopProgress();
            var issuer = data.issuer;
            if (issuer) {
                if (issuer == "localhost") {
                    $("#txtIssuer").val(issuer)
                    showLoginStep(2);
                } else {
                    $("#txtOpenIdIssuerLabel").html(issuer);
                    $("#txtOpenIdIssuer").val(issuer)
                    $("#txtOpenIdEmail").val(checkEmailRequest.email);
                    $("#txtOpenIdRedirectUri").val(getRedirectUri());
                    showLoginStep(3);
                }
            }
            else {
                $("#txtFailedEmail").val(checkEmailRequest.email)
                showEmailNotRecognized();
            }
        }, () => {
            stopProgress();
            $("#txtFailedEmail").val(checkEmailRequest.email)
            showEmailNotRecognized();
        }, "#busyCheckEmail"
    );
}
var progressTimer;
function startProgress() {
    var loginBar = $("#busyLoginBar");
    progressTimer = setInterval(() => {
        var current = parseInt(loginBar.prop("data-current"));
        if (isNaN(current)) {
            current = 0;
        }
        current += 10;
        if (current > 100) {
            current = 0;
        }
        loginBar.prop("data-current", current);
        loginBar.css("width", current + "%");
    }, 500);
}
function stopProgress() {
    clearInterval(progressTimer);
    $("#busyLoginBar").prop("data-current", 0).css("width", "0%");
}
/** Check to see next button can be enabled
 */
var testTimer;
function testEnableNextButton(e) {
    if (testTimer) {
        clearTimeout(testTimer);
    }
    if (!isInsideLoginNext) {
        if (e && e.keyCode == 13) {
            if (!$("#btnLoginNext").prop("disabled")) {
                $("#btnLoginNext").click();
            }
        }
        testTimer = setTimeout(() => {
            if (currentLoginStep == 1) {
                if ($("#txtEmail").val() == "" || $("#txtEmail").val() == $("#txtFailedEmail").val()) {
                    $("#btnLoginNext").prop('disabled', true);
                } else {
                    $("#alertEmailNotRecognized").hide();
                    $("#btnLoginNext").prop('disabled', false);
                }
            }
            else if (currentLoginStep == 2) {
                if ($("#txtPassword").val() == "" || $("#txtPassword").val() == $("#txtFailedPassword").val()) {
                    $("#btnLoginNext").prop('disabled', true);
                } else {
                    $("#alertLoginFailed").hide();
                    $("#btnLoginNext").prop('disabled', false);
                }
            }
            else if(currentLoginStep == 5){
                var missingRequired = false;
                $(".create-account-group").find("[data-required=true]").each((i,elem)=>{
                    if($(elem).val() == ""){
                        missingRequired = true;
                    }
                });
                $("#btnLoginNext").prop('disabled', missingRequired);
            }
            else if (currentLoginStep == 6) {
                if ($("#txtForgotEmail").val() == "" || $("#txtForgotEmail").val() == $("#txtFailedEmail").val()) {
                    $("#btnLoginNext").prop('disabled', true);
                } else {
                    $("#alertEmailNotRecognized").hide();
                    $("#btnLoginNext").prop('disabled', false);
                }
            }
        }, 100);
    }
}
/**
 * Clear the name and password login fields and disable the login button
 */
function clearLogin() {
    $("#txtEmail").val('')
    $("#txtPassword").val('')
    $("#txtFailedEmail").val('')
    $("#txtFailedPassword").val('')
    $("#txtIssuer").val('')
    $("#alertEmailNotRecognized").hide();
    $("#alertLoginFailed").hide();
    $("#alertAccountCreationFailed").hide();
    $('#btnLoginNext').prop('disabled', true);
}

function startOver() {
    clearLogin();
    showLoginStep(1);
}
/**
 * Show the secLoginSelection section and
 * hide the secNameAndPasswordLogin section
 */
function hideAllLoginSteps() {
    $("#secLoginStep1").hide();
    $("#secLoginStep2").hide();
    $("#secLoginStep3").hide();
    $("#secLoginStep4").hide();
    $("#secLoginStep5").hide();
    $("#secLoginStep6").hide();
}
var currentLoginStep = 0;
var autoLoginNextTimer;
function showLoginStep(step) {
    if (autoLoginNextTimer) {
        clearTimeout(autoLoginNextTimer);
        autoLoginNextTimer = undefined;
    }
    hideAllLoginSteps();
    $("#btnLoginNext").show();
    switch (step) {
        case 1:
            $("#btnLoginPrevious").hide();
            $("#btnLoginNext").html("Next");
            $("#secLoginStep1").show();
            setTimeout(() => {
                $("#txtEmail").focus();
            }, 500)
            break;
        case 2:
            $("#btnLoginNext").html("Login");
            $("#btnLoginPrevious").html("Start over");
            $("#btnLoginPrevious").show();
            $("#secLoginStep2").show();
            $("#txtPassword").focus();
            break;
        case 3:
            var countDown = 5;
            $("#btnLoginNext").prop("disabled", false);
            $("#btnLoginNext").html("Login in " + countDown);
            $("#btnLoginPrevious").show();
            $("#secLoginStep3").show();
            autoLoginNextTimer = setInterval(() => {
                $("#btnLoginNext").html("Login in " + countDown);
                if (countDown-- <= 0) {
                    loginNext();
                }
            }, 1000);
            break;
        case 4:
            $("#btnLoginNext").hide();
            $("#btnLoginPrevious").html("Start over");
            $("#btnLoginPrevious").show();
            $("#secLoginStep4").show();
            break;
        case 5:
            $("#btnLoginNext").html("Create Account");
            $("#btnLoginPrevious").html("Start over");
            $("#btnLoginPrevious").show();
            $("#secLoginStep5").show();
            break;
        case 6:
            $("#btnLoginNext").html("Send Password Reset");
            $("#btnLoginPrevious").html("Start over");
            $("#btnLoginPrevious").show();
            $("#secLoginStep6").show();
            break;
    }
    currentLoginStep = step;
    isInsideLoginNext = false;
    testEnableNextButton();
    $("#modalLogin").modal('show');
}

function showLogin() {
    clearLogin();
    showLoginStep(1);
    $("#modalLogin").modal('show');
}

function showLoginFailed() {
    showLoginStep(2);
    $("#alertLoginFailed").show();
    $("#modalLogin").modal('show');
}

function showEmailNotRecognized() {
    showLoginStep(1);
    $("#alertEmailNotRecognized").show();
    $("#modalLogin").modal('show');
}
function showAccountCreationFailed(reason) {
    showLoginStep(5);
    $("#txtAccountCreationFailedReason").html(" " + reason + ".");
    $("#alertAccountCreationFailed").show();
    $("#modalLogin").modal('show');
}

function hideLogin() {
    $("#modalLogin").modal('hide');
}

function returnToLogin() {
    showLogin();
}

function loginPrevious() {
    switch (currentLoginStep) {
        case 1:
            break;
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
            startOver();
            break;
    }
}
var isInsideLoginNext = false;
function loginNext() {
    if (!isInsideLoginNext) {
        isInsideLoginNext = true;
        $("#btnLoginNext").prop("disabled", true);
        switch (currentLoginStep) {
            case 1:
                checkEmail();
                break;
            case 2:
                login();
                break;
            case 3:
                openidLogin();
                break;
            case 5:
                sendAccountCreation();
                break;
            case 6:
                sendPasswordReset();
                break;
        }
    }
}
function createAccount() {
    $("#txtNewEmail").val($("#txtEmail").val());
    showLoginStep(5);
}
function showForgotAccount() {
    $("#txtForgotEmail").val($("#txtEmail").val());
    showLoginStep(6);
}
function loginUsingIssuer(e) {
    var issuer = $(e.target).attr("data-issuer");
    $("#txtOpenIdIssuer").val(issuer)
    $("#txtOpenIdEmail").val($("#txtEmail").val());
    $("#txtOpenIdRedirectUri").val(getRedirectUri());
    $("#formAuthLogin").submit();
}
/**
 * Wire up the jquery logic on document ready
 */
$(function () {
    $("#btnShowLogin").click(showLogin);
    $("#btnHomeShowLogin").click(showLogin);
    $("#btnLogin").click(login);
    $("#btnLoginNext").click(loginNext);
    $("#btnLoginPrevious").click(loginPrevious);
    $("#btnCreateAccount1").click(createAccount);
    $("#btnSigninOptions1").click(() => { showLoginStep(4) });
    $("#btnForgotAccount1").click(showForgotAccount);
    $("#btnCreateAccount2").click(createAccount);
    $("#btnSigninOptions2").click(() => { showLoginStep(4) });
    $("#btnForgotAccount2").click(showForgotAccount);
    $("#btnForgotAccount3").click(showForgotAccount);
    $("#btnCreateAccount3").click(createAccount);
    $(".btnLoginUsingIssuer").click(loginUsingIssuer);
    $(".create-account-group").find("[data-required=true]").bind('paste', testEnableNextButton).keypress(testEnableNextButton);
    $("#txtEmail").keydown(testEnableNextButton).bind('paste', testEnableNextButton).keypress(testEnableNextButton);
    $("#txtForgotEmail").keydown(testEnableNextButton).bind('paste', testEnableNextButton).keypress(testEnableNextButton);
    $("#txtPassword").keydown(testEnableNextButton).bind('paste', testEnableNextButton).keypress(testEnableNextButton);
    if($.urlParam("expired") == "true") {
        deleteBearerToken();
        showLogin();
    }
});