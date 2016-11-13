
var _templates = {};
function compileTemplates() {
    var html = $("#tpl-widget-cell").html();
    _templates.widgetCell = Handlebars.compile(html);
}

function WidgetHandler(settings) {
    this.settings = settings;
}
WidgetHandler.prototype.render = function (widgetElement, data) {
    this.settings.render(widgetElement, data);
};


var Widgets = {
    widgets: {},
    registerWidget: function (widgetType, widgetHandler) {
        this.widgets[widgetType] = new WidgetHandler(widgetHandler);
    },

    findWidgetHandler: function(widgetType) {
        return this.widgets[widgetType];
    }
};



function Matrix(columns, rows) {
    this.columns = columns;
    this.rows = rows;
    this.matrix = [];
    for (var i = 0; i < this.columns; i++) {
        this.matrix[i] = [];
        for (var j = 0; j < this.rows; j++) {
            this.matrix[i][j] = 0;
        }
    }
}
Matrix.prototype.reserveSpot = function (width, height) {
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

function Dashboard(dashboardName, domElement, desiredCellWidth, desiredCellHeight) {
    compileTemplates();
    this.$dashboard = $(domElement);
    this.dashboardName = dashboardName;

    var dw = this.$dashboard.width(),
        dh = this.$dashboard.height();

    this.columns = Math.floor(dw / desiredCellWidth);
    this.rows = Math.floor(dh / desiredCellHeight);
    this.cellWidth = Math.floor(dw / this.columns),
    this.cellHeight = Math.floor(dh / this.rows);
    this.matrix = new Matrix(this.columns, this.rows);
}
Dashboard.prototype.update = function () {
    var that = this;
    API.fetchDashboard(this.dashboardName, function (dashboard) {
        that.renderWidgets(dashboard.widgets);
    });

};
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
Dashboard.prototype.renderWidgets = function (widgets) {
    this.$dashboard.empty();
    var sortedWidgets = [];
    for (var key in widgets) {
        if (widgets.hasOwnProperty(key)) {
            sortedWidgets.push({name: key, widget: widgets[key]});
        }
    }

    sortedWidgets = sortedWidgets.sort(function (a, b) {
        var sortNumber = _stringSortNumber(a.widget.sortOrder, b.widget.sortOrder);
        if (sortNumber != 0) {
            return sortNumber;
        } else {
            return _stringSortNumber(a.name, b.name);
        }
    });

    var thatDashboard = this;
    sortedWidgets.forEach(function (w) {
        var position = thatDashboard.matrix.reserveSpot(w.widget.width, w.widget.height);
        if (position != null) {
            thatDashboard.renderWidget(position.x, position.y, w.widget);
        }
    });
};
Dashboard.prototype.renderWidget = function (x, y, widget) {
    var id = "widget-cell-" + x + "-" + y;
    this.$dashboard.append(_templates.widgetCell({
        widgetCellId: id,
        widgetType: widget.widgetType,
        top: y * this.cellHeight,
        left: x * this.cellWidth,
        width: widget.width * this.cellWidth,
        height: widget.height * this.cellHeight
    }));

    var widgetElement = document.getElementById(id);

    Widgets.findWidgetHandler(widget.widgetType).render(widgetElement, widget.data);
};

var _dashboard = null;
function initDashboard(desiredCellWidth, desiredCellHeight, dashboardName, domElement) {
    _dashboard = new Dashboard(dashboardName, domElement, desiredCellWidth, desiredCellHeight);
    _dashboard.update();
}