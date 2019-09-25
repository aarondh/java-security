/**
 * Add query parameters to a url
 * @param {*} url 
 * @param {*} params in the form of a name-value map
 */
function addQueryParams(url, params) {
    var actionWithParams = new String(url);
    if (url.indexOf("?")) {
        actionWithParams += "?";
    } else {
        actionWithParams += "&";
    }
    for (var paramName in params) {
        if (isNaN(paramName)) {
            var paramValue = params[paramName];
            var lastChar = actionWithParams.charAt(actionWithParams.length - 1)
            if (lastChar != "?" && lastChar != "&") {
                actionWithParams += "&";
            }
            actionWithParams += encodeURIComponent(paramName) + "=" + encodeURIComponent(paramValue);
        }
    }
    return actionWithParams;
}

/**
 * send and receive json
 * the request is of the form {method,url,params,data}
 * @param {The http method: GET, POST, PUT, DELETE, OPTIONS etc.} reques 
 * @param {The url of the request} url 
 * @param {query parameters of the request as a name:value map} params 
 * @param {The data object to be serialized as json} data 
 */
async function sendJsonRequest(request) {

    var action;
    if (request.params) {
        action = addQueryParams(request.url, request.params);
    } else {
        action = request.url;
    }
    return new Promise((resolve, reject) => {
        var xhr = new XMLHttpRequest();
        xhr.responseType = 'json';
        xhr.open(request.method, action, true);
        xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
        xhr.onload = () => {
            var jsonResult;
            if (xhr.status == 200) {
                if (xhr.response) {
                    jsonResult = xhr.response;
                } else {
                    jsonResult = JSON.parse(xhr.responseText);
                }
                resolve(jsonResult)
            } else {
                var errMsg;
                if (xhr.response) {
                    errMsg = xhr.response;
                } else {
                    errMsg = { error: xhr.status, error_description: xhr.statusText };
                }
                reject(errMsg);
            }
        };
        if(request.data){
            xhr.send(JSON.stringify(request.data));
        } else {
            xhr.send();
        }
    });
}
/**
 * Sends a request will a nonce and a hash
 * 
 * @param {*} action 
 * @param {*} method 
 * @param {*} data 
 * @param {*} nonce 
 */
async function sendJsonRequestWithNonce(request) {
    return new Promise((resolve, reject) => {
        var cnonce = generateNonce();
        var payload = JSON.stringify(request.data);
        if(!request.nonce){
            throw "request.nonce is undefined";
        }
        digestMessage(request.nonce + cnonce + payload).then((hash) => {
            if (request.params) {
                request.params.nonce = cnonce;
                request.params.hash = hash;
            } else {
                request.params = { nonce: cnonce, hash: hash }
            }
            sendJsonRequest(request).then(resolve);
        });
    });
}

async function sendJsonRequestNonce(request) {
    var nonceRequest = {
        method: request.method,
        action: request.action,
        data: JSON.stringify(request.data)
    };
    return sendJsonRequest({
        method: "POST",
        url: "/c2id/api/auth/nonce",
        data: nonceRequest
    }).then(data => {
        return data.nonce;
    });
}

$(function () {

    $(".ajax-form").on("submit", e => {
        e.preventDefault();
        var formToSubmit = $(e.target);
        var action = formToSubmit.attr("action");
        var method = formToSubmit.attr("data-method");
        var successfunc = formToSubmit.attr("data-success-func");
        var failfunc = formToSubmit.attr("data-fail-func");
        var isNonceRequired = formToSubmit.attr("data-nonce-required") == "true";

        var data = $(formToSubmit).formToDataObject();

        var request = {
            method: method,
            url: action,
            data: data
        };

        if (isNonceRequired) {
            sendJsonRequestNonce(request).then(nonce => {
                request.nonce = nonce;
                sendJsonRequestWithNonce(request).then(data => {
                    if(successfunc && window[successfunc]){
                        window[successfunc](data);
                    } else {
                        alert("submited successfully");
                    }
                });
            }).catch(errMsg => {
                if(failfunc && window[failfunc]){
                    window[failfunc](errMsg);
                } else if(errMsg.error){
                    alert(errMsg.error + ": " + errMsg.error_description);
                } else {
                    alert(errMsg);
                }
            });
        } else {
            sendJsonRequest(request).then(data => {
                if(successfunc && window[successfunc]){
                    window[successfunc](data);
                } else {
                    alert("submited successfully");
                }
        }).catch(errMsg =>{
            if(failfunc && window[failfunc]){
                window[failfunc](errMsg);
            } else if(errMsg.error){
                alert(errMsg.error + ": " + errMsg.error_description);
            } else {
                alert(errMsg);
            }
    });
        }
    });
});
