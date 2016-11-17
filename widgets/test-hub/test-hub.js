Widgets.registerWidget("test-hub", {
    render: function (element, data, template) {
        if (data.failedTests && data.failedTests > 0) {
            data.hasFailedTests = true;
            data.status = "failed";
        }
        
        $(element).html(template(data));
    } 
});