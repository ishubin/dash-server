Widgets.registerWidget("test-hub", {
    render: function (element, data, template) {
        if (data.data.failedTests && data.failedTests > 0) {
            data.data.hasFailedTests = true;
            data.data.status = "failed";
        }
        
        $(element).html(template(data.data));
    } 
});