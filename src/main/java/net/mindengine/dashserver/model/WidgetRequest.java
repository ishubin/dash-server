/*******************************************************************************
 * Copyright 2016 Ivan Shubin https://github.com/ishubin/dash-server
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.mindengine.dashserver.model;

public class WidgetRequest extends Widget {

    public Widget asWidget() {
        Widget widget = new Widget();
        widget.setData(getData());
        widget.setWidth(getWidth());
        widget.setHeight(getHeight());
        widget.setSortOrder(getSortOrder());
        widget.setWidgetType(getWidgetType());
        widget.setVisible(isVisible());
        widget.setPosition(getPosition());
        return widget;
    }
}
