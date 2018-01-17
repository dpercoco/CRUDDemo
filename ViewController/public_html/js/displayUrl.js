(function () {
    var ref;
    
    function initialise() {
        //alert ("initialized page");
    }
    
    displayUrl = function() {
        var args = arguments;
        var str = "displayUrl, argCount:" + args.length + ", arguments:";
        var url = arguments[0];
        //alert (str + " " + url);
        //ref = cordova.InAppBrowser.open(url, '_blank'); //_system, _self
         ref = window.open(url, '_blank', 'location=no'); //set to yes to display Back, Forward button
         ref.addEventListener('loadstop', function(event) {
             //alert('background window loaded'); 
         });
         ref.show();
    }
    
   
    document.addEventListener("showpagecomplete", initialise, false);
    
    // These methods are the DetailRecipe popUp
    
     popupUtilsShowPopup = function() {
        alert ("popupUtilsShowPopup");
     }
     
    /** 
    popupUtilsShowPopup = function() {
        alert ("popupUtilsShowPopup");
        //the argument is required and cannot be missing
        if (arguments.length > 0) {
            var popupOpener = document.getElementById(arguments[0]);
            if (popupOpener != null && popupOpener != undefined) {
                adf.mf.api.amx.triggerBubbleEventListener(popupOpener,"tap");
            }
            else {
                adf.mf.log.Application.logp(adf.mf.log.level.WARNING,
                  "PopupUtils", "showPopup", "hidden button to launch
                   popup not found");
            }
        }
        else {
            adf.mf.log.Application.logp(adf.mf.log.level.WARNING,
              "PopupUtils", "showPopup", "Missing input argument");
        }
    }

    popupUtilsHidePopup = function() {
        alert ("popupUtilsHidePopup");
        //the argument is required and cannot be missing
        if (arguments.length > 0) {
            var popupCloser = document.getElementById(arguments[0]);

            if (popupCloser != null && popupCloser != undefined) {
                adf.mf.api.amx.triggerBubbleEventListener(popupCloser,"tap");
            }
            else {
                adf.mf.log.Application.logp(adf.mf.log.level.WARNING,
                "PopupUtils","showPopup", "hidden button to close popup
                 not found");
            }
        }
        else {
            adf.mf.log.Application.logp(adf.mf.log.level.WARNING,
             "PopupUtils", "hidePopup", "Missing input argument");
        }
    }
    **/
    
    // device APIs are available
    //
    function onDeviceReady() {
    
         ref = window.open("www.google.com", '_blank', 'hidden=yes');
         ref.addEventListener('loadstop', function(event) {
             //alert('background window loaded'); 
         });
         //var ref = window.open('http://google.com', '_blank', 'location=yes');
         // close InAppBrowser after 5 seconds
         //setTimeout(function() {
         //    ref.close();
         //}, 5000);
    }
    
    document.addEventListener("deviceready", onDeviceReady, false);

    function onSuccess(result) {
        adf.mf.api.setValue( { "name": "#{viewScope.BarcodeBean.barcodeError}", 
                               "value": ""}, 
                               function() {}, 
                               function() {});

        adf.mf.api.setValue( { "name": "#{viewScope.BarcodeBean.barcodeResult}", 
                               "value": result.text}, 
                               function() {}, 
                               function() {});

        adf.mf.api.setValue( { "name": "#{viewScope.BarcodeBean.barcodeFormat}", 
                               "value": result.format}, 
                               function() {}, 
                               function() {});

        adf.mf.api.setValue( { "name": "#{viewScope.BarcodeBean.barcodeCancelled}", 
                               "value": result.cancelled == 1 ? "Yes" : "No"}, 
                               function() {}, 
                               function() {});
    }
    
    function onError(error) {
        adf.mf.api.setValue( { "name": "#{viewScope.BarcodeBean.barcodeError}", 
                               "value": "ERROR: " + error.text}, 
                               function() {}, 
                               function() {});
    }
    
    doAlert = function () {
        var args = arguments;
        var str = "doAlert, argCount:" + args.length + ", arguments:";
        for (x = 0;x < args.length;x++) {
            if (x > 0) {
                str += ", ";
            }
            str += arguments[x];
        }
        alert(str);
    };

    // This method uses PhoneGap and calls the getPicture method to get a picture from the photo library
    fetchPic = function () {
        navigator.camera.getPicture(onSuccess, onFail,{quality : 50, destinationType : navigator.camera.DestinationType.FILE_URI, sourceType : navigator.camera.PictureSourceType.PHOTOLIBRARY});
    };

    // Once a valid picture returns, it calls back to java with the result
    function onSuccess(URI) {
        adf.mf.api.invokeMethod("mobile.MyClass", "FetchCallback", URI, onInvokeSuccess, onFail);
    };

    function onFail() {
        alert("It failed");
    };

    function onInvokeSuccess(param) {
    };

    // This method uses PhoneGap and calls the getPicture method to get a picture from the photo library
    fetchVideo = function () {
        navigator.device.capture.captureVideo(captureSuccess, captureFail, {limit : 1});
    };

    function captureSuccess(mediaFiles) {
        var i, len;
        for (i=0, len=mediaFiles.length; i<len; i++) {
            adf.mf.api.invokeMethod("mobile.MyClass", "FetchCallback", mediaFiles[i].fullPath, onInvokeSuccess, onFail);
        }
    };

    function captureFail() {
        alert("It failed.  Note: This is not supported on the simulator");
    };
    
    
}) ();