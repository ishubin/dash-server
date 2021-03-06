function _stringSortNumber(a, b) {
    if (!a && b) {
        return -1;
    } else if (!b && a) {
        return 1;
    }
    if (a < b) {
        return -1;
    } else if (a > b) {
        return 1;
    } else {
        return 0;
    }
};


var API = {
    fetchDashboard: function (dashboardName, callback) {
        $.ajax({
            dataType: "json",
            url: "/api/dashboards/" + dashboardName,
            success: callback
        });
    },
    fetchDashboardWidgets: function (dashboardName, callback) {
        $.ajax({
            dataType: "json",
            url: "/api/dashboards/" + dashboardName + "/widgets",
            success: callback
        });
    }
};

function WidgetHandler(settings) {
    this.settings = settings;
}
WidgetHandler.prototype.init = function () {
    if (this.settings.init) {
        this.settings.init.call(this);
    }
};
WidgetHandler.prototype.render = function (widgetElement, data, template) {
    this.settings.render.call(this, widgetElement, data, template);
};


var Widgets = {
    widgets: {},
    registerWidget: function (widgetType, widgetHandler) {
        this.widgets[widgetType] = new WidgetHandler(widgetHandler);
    },

    findWidgetHandler: function(widgetType) {
        return this.widgets[widgetType];
    },

    initAllWidgets: function () {
        for (var key in this.widgets) {
            if (this.widgets.hasOwnProperty(key)) {
                this.widgets[key].init();
            }
        }
    }
};



function Matrix(columns, rows) {
    this.columns = columns;
    this.rows = rows;
    this.matrix = [];
}
Matrix.prototype.clear = function () {
    for (var i = 0; i < this.columns; i++) {
        this.matrix[i] = [];
        for (var j = 0; j < this.rows; j++) {
            this.matrix[i][j] = 0;
        }
    }
};
Matrix.prototype.reserveSpot = function (preferredPosition, width, height) {
    if (preferredPosition) {
        console.log(preferredPosition);
        if (this.hasFreeSpotAt(preferredPosition.left, preferredPosition.top, width, height)) {
            this.reserveSpotAt(preferredPosition.left, preferredPosition.top, width, height);
            return {x : preferredPosition.left, y: preferredPosition.top};
        }
    }
    
    
    for (var j = 0; j < this.rows - height; j++) {
        for (var i = 0; i < this.columns; i++) {
            if (this.hasFreeSpotAt(i, j, width, height)) {
                this.reserveSpotAt(i, j, width, height);
                return {x : i, y: j};
            }
        }
    }
    return null;
};
Matrix.prototype.hasFreeSpotAt = function (x, y, width, height) {
    for (var j = y; j < y + height; j++) {
        for (var i = x; i < x + width; i++) {
            if (this.matrix[i][j] !== 0) {
                return false;
            }
        }
    }
    return true;
};
Matrix.prototype.reserveSpotAt = function (x, y, width, height) {
    for (var j = y; j < y + height; j++) {
        for (var i = x; i < x + width; i++) {
            this.matrix[i][j] = 1;
        }
    }
    return true;
};


var DASHBOARD_LAYOUT_FIXED = "fixed";
var DASHBOARD_LAYOUT_FLEX = "flex";

function Dashboard(dashboardName, profile, domElement) {
    this.$dashboard = $(domElement);
    this.dashboardName = dashboardName;
    this.profile = profile;
}
Dashboard.prototype.monitor = function () {
    var that = this;
    that._monitorInterval = setInterval(function () {
        that.updateWidgets();
    }, 5000);
};
Dashboard.prototype.update = function () {
    var that = this;
    API.fetchDashboard(this.dashboardName, function (dashboard) {
        var dw = that.$dashboard.width(),
            dh = that.$dashboard.height();

        var dashboardSettings = dashboard.settings[that.profile];
        
        if (DASHBOARD_LAYOUT_FIXED === dashboardSettings.layout) {
            that.columns = dashboardSettings.columns;
            that.rows = dashboardSettings.rows;
        } else if (DASHBOARD_LAYOUT_FLEX === dashboardSettings.layout) {
            that.columns = Math.floor(dw / dashboardSettings.cellSize.width);
            that.rows = Math.floor(dh / dashboardSettings.cellSize.height);
        } else {
            throw new Error("Unknown dashboard layout: " + dashboardSettings.layout);
        }

        that.cellWidth = Math.floor(dw / that.columns);
        that.cellHeight = Math.floor(dh / that.rows);
        that.matrix = new Matrix(that.columns, that.rows);
        that.renderWidgets(dashboard.widgets);
    });

};
Dashboard.prototype.updateWidgets = function () {
    var that = this;
    API.fetchDashboardWidgets(this.dashboardName, function (widgets) {
        that.renderWidgets(widgets);
    });
};
Dashboard.prototype.renderWidgets = function (widgets) {
    this.$dashboard.empty();
    this.matrix.clear();
    var sortedWidgets = [];
    for (var key in widgets) {
        if (widgets.hasOwnProperty(key)) {
            sortedWidgets.push({name: key, widget: widgets[key]});
        }
    }

    sortedWidgets = sortedWidgets.sort(function (a, b) {
        if (a.widget.position && !b.widget.position) {
            return -1;
        } else if (!a.widget.position && b.widget.position) {
            return 1;
        }
        
        var sortNumber = _stringSortNumber(a.widget.sortOrder, b.widget.sortOrder);
        if (sortNumber != 0) {
            return sortNumber;
        } else {
            return _stringSortNumber(a.name, b.name);
        }
    });

    var thatDashboard = this;
    sortedWidgets.forEach(function (w) {
        if (w.widget.visible) {
            var preferredPosition = w.widget.position || null;
            
            var position = thatDashboard.matrix.reserveSpot(preferredPosition, w.widget.width, w.widget.height);
            if (position != null) {
                thatDashboard.renderWidget(position.x, position.y, w.widget);
            }
        }
    });
};
Dashboard.prototype.renderWidget = function (x, y, widget) {
    var id = "widget-cell-" + x + "-" + y;
    this.$dashboard.append(Handlebars.templates["widget-cell"]({
        widgetCellId: id,
        widgetType: widget.widgetType,
        top: y * this.cellHeight,
        left: x * this.cellWidth,
        width: widget.width * this.cellWidth,
        height: widget.height * this.cellHeight
    }));

    var widgetElement = document.getElementById(id);

    var tpl = Handlebars.templates["widget-" + widget.widgetType];
    var widgetHandler = Widgets.findWidgetHandler(widget.widgetType);
    
    var widgetData = {
        dashboardName: this.dashboardName,
        profile: this.profile,
        data: widget.data
    };
    
    if (widgetHandler) {
        widgetHandler.render(widgetElement, widgetData, tpl);
    } else {
        if (tpl) {
            $(widgetElement).html(tpl(widgetData));
        } else {
            console.error("Cannot find widget template for: " + widget.widgetType);
        }
    }
};

var _dashboard = null;
function initDashboard(dashboardName, profile, domElement) {
    Widgets.initAllWidgets();
    _dashboard = new Dashboard(dashboardName, profile, domElement);
    _dashboard.update();
    _dashboard.monitor();
}