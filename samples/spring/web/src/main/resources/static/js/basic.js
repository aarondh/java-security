
(function ($) {
    $.fn.formToDataObject = function () {

        var o = {};
        var a = this.serializeArray();
        $.each(a, function () {
            if (o[this.name]) {
                if (!o[this.name].push) {
                    o[this.name] = [o[this.name]];
                }
                o[this.name].push(this.value || '');
            } else {
                o[this.name] = this.value || '';
            }
        });
        return o;
    };
})(jQuery);



(function ($) {
    $.fn.sendJsonForm = function (oncomplete, onerror) {
        var xhr = new XMLHttpRequest();
        xhr.responseType = 'json';
        xhr.open(form.method, form.action, true);
        xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
        xhr.onload = function () {
            var jsonResult;
            if (xhr.status == 200) {
                if (xhr.response) {
                    jsonResult = xhr.response;
                } else {
                    jsonResult = JSON.parse(xhr.responseText);
                }
                if (oncomplete) {
                    oncomplete(jsonResult)
                }
            } else if (onerror) {
                onerror(xhr.status, xhr.statusText, xhr);
            }
        }
        var jsonData = $(this).formToDataObject()
        xhr.send(JSON.stringify(jsonData));
    };
})(jQuery);

function goto(url) {
    window.location.href = url;
}

function submitJsonForm(formId, oncomplete, onerror) {
    $("#" + formId).sendJsonForm(oncomplete, onerror);

}

function getCookie(name) {
    var v = document.cookie.match('(^|;) ?' + name + '=([^;]*)(;|$)');
    return v ? v[2] : null;
}

function setCookie(name, value, path, domain, days) {
    var cookie = name;
    if (value) {
        cookie += "=" + value;
    }
    if (path) {
        cookie += ";path=" + path;
    }
    if (domain) {
        cookie += ";domain=" + domain;
    }
    if (days) {
        var d = new Date;
        d.setTime(d.getTime() + 24 * 60 * 60 * 1000 * days);
        cookie += ";expires=" + d.toGMTString();
    }
    document.cookie = cookie;
}

function deleteCookie(name) {
    setCookie(name, "", "/c2id/", "localhost", -1);
}

function deleteBearerToken() {
    deleteCookie("bearer-token");
    localStorage["bearer-token"] = null;
}

let baseXHROpen = window.XMLHttpRequest.prototype.open;
window.XMLHttpRequest.prototype.open = function (method, url, async, user, password) {

    this.addEventListener("readystatechange", function () {
        if (this.readyState == 1) {
            var token;
            if (!token) {
                token = localStorage["bearer-token"];
            }
            if (!token) {
                token = getCookie("bearer-token")
            }
            if (token) {
                this.setRequestHeader("Authentication", "Bearer " + token);
            }
        }
    });
    return baseXHROpen.apply(this, arguments);
}
/**
 * Convert a byte array to a hex encoded string
 * @param {byte array} buffer 
 */
function hexString(buffer) {
    const byteArray = new Uint8Array(buffer);

    const hexCodes = [...byteArray].map(value => {
        const hexCode = value.toString(16);
        const paddedHexCode = hexCode.padStart(2, '0');
        return paddedHexCode;
    });

    return hexCodes.join('');
}
/**
 * generate a random string for a nonce
 */
function generateNonce() {
    return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
}
/**
 * Do a one way SHA-256 hash of a message
 * @param {string} message 
 */
async function digestMessage(message) {
    return new Promise(function (resolve, reject) {
        const encoder = new TextEncoder();
        const data = encoder.encode(message);
        const buf = window.crypto.subtle.digest('SHA-256', data).then(digestValue => {
            resolve(hexString(digestValue));
        });
    });
}
$.urlParam = function getUrlParam(sParam) {
    var sPageURL = window.location.search.substring(1),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : decodeURIComponent(sParameterName[1]);
        }
    }
};
function postJson(url, data, onsuccess, onfail, busy) {
    if (busy) {
        $(busy).show();
    }

    $.ajax({
        url: url,
        type: 'POST',
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function (data) {
            if (busy) {
                $(busy).hide();
            }
            if (onsuccess) {
                onsuccess(data);
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            if (busy) {
                $(busy).hide();
            }
            if (onfail) {
                onfail(jqXHR, textStatus, errorThrown);
            }
        }
    });
}
